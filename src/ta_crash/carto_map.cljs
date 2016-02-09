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
    [:start-date :end-date :active-area :active-stat])
  Object
  (create-handler
    [this vis layers]
    (om/set-state! this {:vis vis}))
  (map-new-parameters
    [this vis props]
    (let [data-layer (nth (.getLayers vis) 1)
          sub-layer (.getSubLayer data-layer 0)]
     (set-map (queries/get-query :crashes props) sub-layer (.getNativeMap vis))))
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
    (let [vis (:vis (om/get-state this))]
      (when (and vis (not (props/same-props? (om/props this) next-props)))
        (.map-new-parameters this vis next-props))))
  (render [this]
    (let [{:keys [text type]} (om/props this)]
      (dom/div #js {:className "carto-map" :ref "cartoMap" :id "cartoMap"}))))

(def carto-map (om/factory CartoMap))
