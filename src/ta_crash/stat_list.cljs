(ns ta-crash.stat-list
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ta-crash.sql-queries :as queries]
            [ta-crash.stat-list-item :as stat-list-item]))

(defui StatList
  static om/IQueryParams
  (params [this]
      {:params {:end-date "" :start-date ""} :query false})
  static om/IQuery
  (query [_]
    '[(:stat-list/items {:params ?params :query ?query})])
  Object
  (setQuery
    ([this] (let [{:keys [end-date start-date query]} (om/props this)]
      (om/set-query! this {:params {:params {:end-date end-date :start-date start-date} :query query}})))
    ([this props] (om/set-query! this {:params {:params {:end-date (:end-date props) :start-date (:start-date props)}
                                                :query (:query props)}})))
  (componentWillMount [this]
    (.setQuery this))
  (componentWillReceiveProps [this next-props]
    (.setQuery this next-props))
  (render [this]
    (let [{:keys [stat-list/items end-date start-date query]} (om/props this)]
      (dom/div #js {:className "stat-box"}
        (apply dom/ul #js {:className "stat-list"} (map stat-list-item/stat-list-item items))))))

(def stat-list (om/factory StatList))
