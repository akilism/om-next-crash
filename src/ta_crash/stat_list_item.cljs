(ns ta-crash.stat-list-item
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ta-crash.label :as label]
            [ta-crash.value :as value]))


(defn static-item
  [count display]
  (println "static" count display)
  (dom/span nil
    (value/value {:value count :type :stat-list-item}) (label/label {:text display :type :stat-list-item})))

(defn clickable-item
  [count display id type click-handler]
  (println "clickable" id )
  (dom/a #js {:onClick #(click-handler id type %)}
    (value/value {:value count :type :stat-list-item}) (label/label {:text display :type :stat-list-item})))

(defn click-handler
  [id type evt]
  (println id))

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
          (static-item count display)
          (clickable-item count display id type stat-change))))))

(def stat-list-item (om/factory StatListItem))


;(fn [id type evt] (println type " : " id))
