(ns ta-crash.stat-group
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ta-crash.sql-queries :as queries]
            [ta-crash.group-item :as group-item]
            [ta-crash.carto-map :as carto-map]))

(defui StatGroup
  static om/IQueryParams
  (params [_]
    {:params {:end-date "12-26-2015" :start-date "12-1-2015"} :query queries/stats-date})
  static om/IQuery
  (query [_]
    '[(:group/items {:params ?params :query ?query})])
;  static om/IQuery
;  (query [_]
;    [{:group/items (om/get-query group-item/GroupItem)}])
  Object
  (render [this]
    (let [{:keys [group/items]} (om/props this)]
      (dom/div nil
        (dom/div #js {:className "content-box"}
          (dom/div #js {:className "stat-box"}
            (apply dom/div #js {:className "stat-group"} (map group-item/group-item items)))
          (dom/div #js {:className "factor-box"} "Factors"))
        (carto-map/carto-map)))))
