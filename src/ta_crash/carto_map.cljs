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
    (println query)
    (.setSQL layer query)
    (-> (.getBounds c-sql query)
        (.done #(.fitBounds map-el %)))))

(defn tooltip
  [hoverData]
  (if hoverData
    (dom/div #js {:className "area-tooltip" :style #js {:top (+ 5 (:y hoverData)) :left (+ 5 (:x hoverData))}} (:name hoverData))
    ""))

(defn draw-click-handler
  [this evt]
  (om/update-state! this update :points (fn [_] (conj (:points (om/get-state this)) (.-latlng evt)))))

(defui CartoMap
  static om/IQuery
  (query [_]
    [:start-date :end-date :active-area :active-stat :area-overlay :edit-mode])
  Object
  (create-handler
    [this vis layers]
    (om/update-state! this update :vis (fn [_] vis)))
  (map-new-parameters
    [this vis props]
    (let [data-layer (nth (.getLayers vis) 1)
          sub-layer (.getSubLayer data-layer 0)
          sub-layer-count (.getSubLayerCount data-layer)]
      (when (> sub-layer-count 1)
        (let [overlay-layer (.getSubLayer data-layer 1)]
          (.hide overlay-layer)
          (.show sub-layer)
          (.setInteraction overlay-layer false)
          (.setInteraction sub-layer true)))
      (set-map (queries/get-query :crashes props) sub-layer (.getNativeMap vis))))
  (set-area-layer
    ; TODO DRY ALL OF THIS UP.
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
                                                y (.-clientY e)
                                                curr-state (om/get-state this)]
                                            (om/set-state! this (assoc curr-state :hover {:name feature-name :x x :y y})))))
                ; e.target.classList.add

            (.on sub-layer "featureClick" (fn [e latlng pos data layer]
                                           (let [area-type (get-in (om/props this) [:area-overlay :area-type])
                                                 area-change (:area-change (om/get-computed this))
                                                 identifier (:identifier (js->clj data :keywordize-keys true))]
                ; nav to selected area.
                                             (.scrollTo js/window 0 0)
                                             (area-change {:type area-type :identifier identifier})))))

        2 (let [sub-layer (.getSubLayer area-layer 1)
                crash-layer (.getSubLayer area-layer 0)]
            (.setInteraction crash-layer false)
            (.setInteraction sub-layer true)
            ;(.setSQL sub-layer (query))
            (set-map (query) sub-layer map-el)
            (.setZoom map-el 11)))))
  (draw-off
    [this vis]
    (let [l-map (.getNativeMap vis)]
     (.off l-map "click" (:draw-click-handler (om/get-state this)))
     (println "turn drawing off on the map")))
  (draw-on
    [this vis]
    (let [l-map (.getNativeMap vis)]
     (.on l-map "click" (:draw-click-handler (om/get-state this)))
     (println "turn drawing on on the map")))
  (toggle-draw-mode [this]
    (let [{:keys [draw-mode vis]} (om/get-state this)
          new-draw-mode (not draw-mode)]
     (if draw-mode
       (do
        (om/update-state! this update :draw-mode (fn [_] new-draw-mode))
        (.draw-off this vis))
       (do
        (om/update-state! this update :draw-mode (fn [_] new-draw-mode))
        (.draw-on this vis)))))
  (delete-shape [this]
    (om/update-state! this update :points (fn [_] []))
    (println "remove shape from localstorage and map."))
  (add-custom-shape [this]
    (println "add-custom-shape"))
  (componentDidMount [this]
    (let [{:keys [text type]} (om/props this)
          viz "https://akilism.cartodb.com/api/v2/viz/fcdfe28c-b6de-11e5-849b-0e787de82d45/viz.json"]
      (-> (.createVis js/cartodb (om/react-ref this "cartoMap") viz)
          (.done (fn [vis layers] (.create-handler this vis layers)))
          (.error error-handler))))
  (componentWillMount [this]
    (om/set-state! this {:vis nil
                         :draw-click-handler (partial draw-click-handler this)
                         :draw-mode false
                         :points []
                         :custom-shape nil}))
  (componentWillReceiveProps
    [this next-props]
    (let [vis (:vis (om/get-state this))
          curr-overlay (:area-overlay (om/props this))
          area-overlay (:area-overlay next-props)]
      (if vis
        (cond
          (not (= (:area-type curr-overlay) (:area-type area-overlay))) (.set-area-layer this vis (:query area-overlay))
          (not (props/same-props? (om/props this) next-props)) (.map-new-parameters this vis next-props)
          :else nil))))
  (render [this]
    (let [{:keys [edit-mode text type]} (om/props this)
          hover (tooltip (:hover (om/get-state this)))
          points (:points (om/get-state this))]
      (when (< 0 (count points))
        (.add-custom-shape this))
      (dom/div #js {:className "map-box"}
        hover
        (when edit-mode
          (dom/div #js {:className "edit-mode-menu"}
           (dom/ul nil [(dom/ul #js {:onClick #(.toggle-draw-mode this)} "Draw Shape") (dom/ul #js {:onClick #(.delete-shape this)} "Delete Shape")])))
        (dom/div #js {:className "carto-map" :ref "cartoMap" :id "cartoMap"})))))

(def carto-map (om/factory CartoMap))
