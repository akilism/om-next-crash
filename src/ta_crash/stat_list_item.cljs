(ns ta-crash.stat-list-item
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ta-crash.item :as item]))

(defui StatListItem
  static om/IQuery
  (query [this]
    [:type :id :display :count])
  Object
  (render [this]
    (let [{:keys [type id display count]} (om/props this)
          {:keys [stat-change]} (om/get-computed this)]
      (dom/h2 #js {:className (str (name id) "-stat-list-item stat-list-item item")}
        (if (nil? stat-change)
          (item/static-item count display id type dom/span)
          (item/clickable-item count display id type dom/a stat-change))))))

(def stat-list-item (om/factory StatListItem))
