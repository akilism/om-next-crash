(ns ta-crash.stat-group
  (:require [cljs.pprint :as pprint]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ta-crash.props :as ta-props]
            [ta-crash.sql-queries :as queries]
            [ta-crash.group-item :as group-item]))

(def group-order [:total-crashes
                  :total-killed
                  :total-injured
                  :total-persons-killed
                  :total-pedestrian-killed
                  :total-cyclist-killed
                  :total-motorist-killed
                  :total-persons-injured
                  :total-pedestrian-injured
                  :total-cyclist-injured
                  :total-motorist-injured])

(defui StatGroup
  static om/IQueryParams
  (params [this]
     {:carto-query false})
  static om/IQuery
  (query [_]
    '[(:group/items {:query ?carto-query})])
  Object
  (set-query
    ([this] (om/set-query! this {:params {:carto-query (queries/get-query :stats (om/props this))}}))
    ([this props] (om/set-query! this {:params {:carto-query (queries/get-query :stats props)}})))
  (componentWillMount [this]
    (.set-query this))
  (componentWillReceiveProps [this next-props]
    (when (not (ta-props/same-props? (om/props this) next-props))
        (.set-query this next-props)))
  (render [this]
    (let [{:keys [group/items]} (om/props this)
          {:keys [stat-change]} (om/get-computed this)]
      (dom/div #js {:className "stat-box"}
        (apply dom/div #js {:className "stat-group"}
               (map #(group-item/group-item (om/computed % {:stat-change stat-change})) (mapcat (fn [id] (filter #(= id (:id %)) items)) group-order)))))))

(def stat-group (om/factory StatGroup))
