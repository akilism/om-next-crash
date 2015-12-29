(ns ta-crash.stat-list
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ta-crash.sql-queries :as queries]
            [ta-crash.stat-list-item :as stat-list-item]))

(defui StatList
  static om/IQueryParams
  (params [this]
    ;(let [{:keys [end-date start-date query]} (om/props this)]
      ;{:params {:end-date end-date :start-date start-date} :query query})
      {:params {:end-date "" :start-date ""} :query false})
  static om/IQuery
  (query [_]
    '[(:stat-list/items {:params ?params :query ?query})])
  Object
  (componentWillMount [this]
    (let [{:keys [end-date start-date query]} (om/props this)]
  ;    (println "props" end-date)
  ;    (println "params" (:end-date (om/get-params this)))
      (om/set-query! this {:params {:params {:end-date end-date :start-date start-date} :query query}})))
  (render [this]
    (let [{:keys [stat-list/items end-date start-date query]} (om/props this)]
      (print (om/props this))
      (dom/div #js {:className "stat-box"}
        ";*("))))

(def stat-list (om/factory StatList))

;(apply dom/ul #js {:className "stat-list"} (map stat-list-item/stat-list-item items))
