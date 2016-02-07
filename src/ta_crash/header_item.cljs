(ns ta-crash.header-item
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ta-crash.item :as item]))

(defui HeaderItem
  static om/IQuery
  (query [this]
    [:type :id :display :count])
  Object
  (render [this]
    (let [{:keys [type id display count]} (om/props this)
          {:keys [item-click-handler]} (om/get-computed this)]
      (if (nil? item-click-handler)
        (item/static-item count display id type dom/h2)
        (item/clickable-item count display id type dom/h2 item-click-handler)))))

(def header-item (om/factory HeaderItem))
