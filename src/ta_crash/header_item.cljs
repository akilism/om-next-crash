(ns ta-crash.header-item
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ta-crash.label :as label]
            [ta-crash.value :as value]))


(defui HeaderItem
  static om/IQuery
  (query [this]
    [:type :id :display :count])
  Object
  (render [this]
    (let [{:keys [type id display count]} (om/props this)]
      (dom/h2 #js {:className (str (name id) "-header-item header-item item")}
        (value/value {:value count :type :header-item}) (label/label {:text display :type :header-item})))))

(def header-item (om/factory HeaderItem))
