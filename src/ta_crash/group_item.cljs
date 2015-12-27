(ns ta-crash.group-item
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ta-crash.default-item :as default-item]
            [ta-crash.header-item :as header-item]
            [ta-crash.sub-header-item :as sub-header-item]))

(defui GroupItem
  static om/Ident
  (ident [_ {:keys [id type] :as props}]
    [type id])
  static om/IQuery
  (query [_]
    {:group/default-item (om/get-query default-item/DefaultItem)
     :group/header-item (om/get-query header-item/HeaderItem)
     :group/sub-header-item (om/get-query sub-header-item/SubHeaderItem)})
  Object
  (render [this]
    (let [{:keys [type id] :as props} (om/props this)]
      (({:group/default-item default-item/default-item
         :group/header-item header-item/header-item
         :group/sub-header-item sub-header-item/sub-header-item} type)
        (om/props this)))))

(def group-item (om/factory GroupItem))
