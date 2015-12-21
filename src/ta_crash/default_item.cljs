(ns ta-crash.default-item
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ta-crash.label :as label]
            [ta-crash.value :as value]))

(defui DefaultItem
  static om/IQuery
  (query [this]
    [:type :id :display :count])
  Object
  (render [this]
    (let [{:keys [type id display count]} (om/props this)]
      (dom/div #js {:className (str (name id) "-default-item default-item item")}
        (value/value {:value count :type :default-item})
        (label/label {:text display :type :default-item})))))

(def default-item (om/factory DefaultItem))
