(ns ta-crash.sub-header-item
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ta-crash.label :as label]
            [ta-crash.value :as value]))

(defn static-item
  [count display id]
  (println "static" count display)
  (dom/div #js {:className (str (name id) "-sub-header-item sub-header-item item")}
      (value/value {:value count :type :sub-header-item}) (label/label {:text display :type :sub-header-item})))

(defn clickable-item
  [count display id type item-click-handler]
  (println "clickable" id )
  (dom/div #js {:className (str (name id) "-sub-header-item sub-header-item item")
              :onClick #(item-click-handler {:key (keyword type) :id (name id)})}
    (value/value {:value count :type :sub-header-item}) (label/label {:text display :type :sub-header-item})))

(defui SubHeaderItem
  static om/IQuery
  (query [this]
    [:type :id :display :count])
  Object
  (render [this]
    (let [{:keys [type id display count]} (om/props this)
          {:keys [item-click-handler]} (om/get-computed this)]
      (if (nil? item-click-handler)
          (static-item count display id)
          (clickable-item count display id type item-click-handler)))))

(def sub-header-item (om/factory SubHeaderItem))
