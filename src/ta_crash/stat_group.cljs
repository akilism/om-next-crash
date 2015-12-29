(ns ta-crash.stat-group
  (:require [cljs.pprint :as pprint]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
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
    ;(let [{:keys [end-date start-date query]} (om/props this)])
     {:params {:end-date "" :start-date ""} :query false})
  static om/IQuery
  (query [_]
    '[(:group/items {:params ?params :query ?query})])
  Object
  (render [this]
    (let [{:keys [group/items]} (om/props this)]
      (dom/div #js {:className "stat-box"}
        (apply dom/div #js {:className "stat-group"}
               (map group-item/group-item
                    (mapcat (fn [id] (filter #(= id (:id %)) items)) group-order)))))))

(def stat-group (om/factory StatGroup))
