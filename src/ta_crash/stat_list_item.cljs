(ns ta-crash.stat-list-item
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ta-crash.label :as label]
            [ta-crash.value :as value]))


(defui StatListItem
  static om/IQuery
  (query [this]
    [:type :id :display :count])
  Object
  (render [this]
    (let [{:keys [type id display count]} (om/props this)]
      (dom/h2 #js {:className (str (name id) "-stat-list-item stat-list-item item")}
        (value/value {:value count :type :stat-list-item}) (label/label {:text display :type :stat-list-item})))))

(def stat-list-item (om/factory StatListItem))
