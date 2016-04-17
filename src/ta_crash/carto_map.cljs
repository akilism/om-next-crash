(ns ta-crash.carto-map
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.pprint :as pprint]
            [cljs.core.async :as async :refer [<! >! put! chan]]
            [ta-crash.sql-queries :as queries]
            [ta-crash.props :as props]
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
          sub-layer (.getSubLayer data-layer 0)]
     (set-map (queries/get-query :crashes props) sub-layer (.getNativeMap vis))))
  (set-area-layer
    [this vis query]
    (let [data-layer (nth (.getLayers vis) 1)
          sub-layer-count (.getSubLayerCount data-layer)
          ]
      (println sub-layer-count)
      (println (query))
      (condp = sub-layer-count
        1 (.createSubLayer data-layer #js {:sql (query) :cartocss "#layer { polygon-fill: #FF6600; polygon-opacity: 0.7; line-color: #FFF; line-width: 0.5; line-opacity: 1; }"})
        2 (let [sub-layer (.getSubLayer data-layer 1)]
            (.setSQL sub-layer (query))))))
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
    (let [{:keys [text type]} (om/props this)]
      (dom/div #js {:className "carto-map" :ref "cartoMap" :id "cartoMap"}))))

(def carto-map (om/factory CartoMap))
