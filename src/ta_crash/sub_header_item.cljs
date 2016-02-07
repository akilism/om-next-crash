(ns ta-crash.sub-header-item
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ta-crash.item :as item]))

(defui SubHeaderItem
  static om/IQuery
  (query [this]
    [:type :id :display :count])
  Object
  (render [this]
    (let [{:keys [type id display count]} (om/props this)
          {:keys [item-click-handler]} (om/get-computed this)]
      (if (nil? item-click-handler)
          (item/static-item count display id type dom/div)
          (item/clickable-item count display id type dom/div item-click-handler)))))

(def sub-header-item (om/factory SubHeaderItem))
