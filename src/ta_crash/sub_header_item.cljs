(ns ta-crash.sub-header-item
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ta-crash.label :as label]
            [ta-crash.value :as value]))

(defui SubHeaderItem
  static om/IQuery
  (query [this]
    [:type :id :display :count])
  Object
  (render [this]
    (let [{:keys [type id display count]} (om/props this)]
      (dom/div #js {:className (str (name id) "-sub-header-item sub-header-item item")}
        (value/value {:value count :type :sub-header-item})
        (label/label {:text display :type :sub-header-item})))))

(def sub-header-item (om/factory SubHeaderItem))
