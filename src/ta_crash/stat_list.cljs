(ns ta-crash.stat-list
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ta-crash.sql-queries :as queries]
            [ta-crash.props :as ta-props]
            [ta-crash.stat-list-item :as stat-list-item]))

(defui StatList
  static om/IQueryParams
  (params [this]
      {:carto-query false})
  static om/IQuery
  (query [_]
    '[(:stat-list/items {:query ?carto-query})])
  Object
  (set-query
    ([this] (om/set-query! this {:params {:carto-query (queries/get-query :factors (om/props this))}}))
    ([this props] (om/set-query! this {:params {:carto-query (queries/get-query :factors props)}})))
  (componentWillMount [this]
    (.set-query this))
  (componentWillReceiveProps [this next-props]
    (when (not (ta-props/same-props? (om/props this) next-props))
        (.set-query this next-props)))
  (render [this]
    (let [{:keys [stat-list/items end-date start-date query]} (om/props this)]
      (dom/div #js {:className "factor-box"}
        (dom/div #js {:className "stat-list-title"} "Contributing Factors")
        (apply dom/ul #js {:className "stat-list"} (map stat-list-item/stat-list-item (take 10 items)))))))

(def stat-list (om/factory StatList))
