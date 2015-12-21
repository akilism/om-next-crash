(ns ta-crash.carto-map
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(defui CartoMap
  Object
  (componentDidMount [this]
    (let [{:keys [text type]} (om/props this)]
      (.createVis js/cartodb (om/react-ref this "cartoMap") "https://akilism.cartodb.com/api/v2/viz/a918eea8-a119-11e5-a2a5-0ecfd53eb7d3/viz.json")))
  (render [this]
    (let [{:keys [text type]} (om/props this)]
      (dom/div #js {:className "carto-map" :ref "cartoMap" :id "cartoMap"}))))

(def carto-map (om/factory CartoMap))
