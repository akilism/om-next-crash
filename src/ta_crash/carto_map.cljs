(ns ta-crash.carto-map
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.pprint :as pprint]
            [cljs.core.async :as async :refer [<! >! put! chan]]
            [ta-crash.sql-queries :as queries]
            [ta-crash.props :as props]
            [ta-crash.conversion :as conversion]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(defn error-handler
  [error]
  (pprint/pprint error))

(defn set-map
  [query layer map-el]
  (let [c-sql (js/cartodb.SQL. (clj->js {:user "akilism"}))]
    (.setSQL layer query)
    (-> (.getBounds c-sql query)
        (.done #(.fitBounds map-el %)))))


(defn tooltip
  [hoverData]
  (if hoverData
    (dom/div #js {:className "area-tooltip" :style #js {:top (+ 5 (:y hoverData)) :left (+ 5 (:x hoverData))}} (:name hoverData))
    ""))

(defui CartoMap
  static om/IQuery
  (query [_]
    [:start-date :end-date :active-area :active-stat :area-overlay])
  Object
  (create-handler
    [this vis layers]
    (om/set-state! this {:vis vis}))
  (map-new-parameters
    [this vis props]
    (let [data-layer (nth (.getLayers vis) 1)
          sub-layer (.getSubLayer data-layer 0)
          sub-layer-count (.getSubLayerCount data-layer)]
      (when (> sub-layer-count 1)
        (let [overlay-layer (.getSubLayer sub-layer 1)]
          (.hide overlay-layer)
          (.show sub-layer)
          (.setInteraction overlay-layer false)
          (.setInteraction sub-layer true)))
      (set-map (queries/get-query :crashes props) sub-layer (.getNativeMap vis))))
  (set-area-layer
    ; TODO DRY ALL OF THIS UP.
    ; fix loading in new area shapes when there is already a set on the map.
    [this vis query]
    (let [area-layer (nth (.getLayers vis) 1)
          sub-layer-count (.getSubLayerCount area-layer)
          map-el (.getNativeMap vis)]
      (condp = sub-layer-count
        1 (let [sub-layer (.createSubLayer area-layer #js {:sql (query)
                                                           :cartocss "#layer { polygon-fill: #2167ab; polygon-opacity: 0.7; line-color: #fff; line-width: 1; line-opacity: 1; }"
                                                           :interactivity "cartodb_id, the_geom_webmercator, identifier"})
                crash-layer (.getSubLayer area-layer 0)]
            ;(.hide crash-layer)
            (.setInteraction crash-layer false)
            (.setInteraction sub-layer true)
            (.setZoom map-el 11)
            (.on sub-layer "featureOver" (fn [e latlng pos data layer]
              (let [area-type (get-in (om/props this) [:area-overlay :area-type])
                    identifier (:identifier (js->clj data :keywordize-keys true))
                    feature-name (conversion/convert-type identifier area-type)
                    x (.-clientX e)
                    y (.-clientY e)]
                (println [feature-name x y])
                (om/set-state! this {:hover {:name feature-name :x x :y y}})
                ; e.target.classList.add
                )))
            (.on sub-layer "featureClick" (fn [e latlng pos data layer]
              (let [area-type (get-in (om/props this) [:area-overlay :area-type])
                    area-change (:area-change (om/get-computed this))
                    identifier (:identifier (js->clj data :keywordize-keys true))]
                ; nav to selected area.
                (.scrollTo js/window 0 0)
                (area-change {:type area-type :identifier identifier})
                ))))
        2 (let [sub-layer (.getSubLayer area-layer 1)
                crash-layer (.getSubLayer area-layer 0)]
            (.setInteraction crash-layer false)
            (.setInteraction sub-layer true)
            ;(.setSQL sub-layer (query))
            (set-map (query) sub-layer map-el)
            (.setZoom map-el 11)))))
  (componentDidMount [this]
    (let [{:keys [text type]} (om/props this)
          viz "https://akilism.cartodb.com/api/v2/viz/fcdfe28c-b6de-11e5-849b-0e787de82d45/viz.json"]
      (-> (.createVis js/cartodb (om/react-ref this "cartoMap") viz)
          (.done (fn [vis layers] (.create-handler this vis layers)))
          (.error error-handler))))
  (componentWillMount [this]
    (om/set-state! this {:vis nil}))
  (componentWillReceiveProps
    [this next-props]
    (let [vis (:vis (om/get-state this))
          curr-overlay (:area-overlay (om/props this))
          area-overlay (:area-overlay next-props)]
      (when (and vis (not (props/same-props? (om/props this) next-props)))
        (.map-new-parameters this vis next-props))
      (when (and vis (not (= (:area-type curr-overlay) (:area-type area-overlay))))
        (println (str "carto-map:" (:area-type area-overlay)))
        (.set-area-layer this vis (:query area-overlay)))))
  (render [this]
    (let [{:keys [text type]} (om/props this)
          hover (tooltip (:hover (om/get-state this)))]
      (dom/div #js {:className "map-box"}
        hover
        (dom/div #js {:className "carto-map" :ref "cartoMap" :id "cartoMap"})))))

(def carto-map (om/factory CartoMap))
