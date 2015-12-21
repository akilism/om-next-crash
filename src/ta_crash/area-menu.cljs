(ns ta-crash.area-menu
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))


;(defui MenuItem
;  Object
;  (render [this]
;    (let [{:keys [name type table cols]} (om/props this)]
;      (dom/li #js {:className "area-menu-item" :onClick #(println table cols)} name))))

;(def menu-item (om/factory MenuItem))

(declare menu-item)

(defmulti get-area-display (fn [_ type] type))

(defmethod get-area-display :borough
  [id _]
  (condp = id
    1 "Manhattan"
    2 "Bronx"
    3 "Brooklyn"
    4 "Queens"
    5 "Staten Island"))

(defn build-sub-item
  [item sub-item]
  (assoc sub-item :table (:table item)
    :display-name (get-area-display (:identifier sub-item) (:area-type item))))

(defui AreaMenu
  static om/IQueryParams
  (params [_]
    {:area-type "" :table "" :cols []})
  static om/IQuery
  (query [_]
    '[:menu/items (:area/items {:area-type ?area-type :table ?table :cols ?cols})])
  Object
  (render [this]
    (let [{items :menu/items sub-items :area/items} (om/props this)
          sub-parent (:parent (first sub-items))]
      (if (:item-type (first items))
        (let [item-type (name (:item-type (first items)))]
          (apply dom/ul
            #js {:className (str item-type "-area-menu area-menu")}
            (map (fn [i]
                    (if (= sub-parent (:area-type i))
                      (menu-item (assoc i :sub-menu (map (fn [si] (build-sub-item i si)) sub-items)) this)
                      (menu-item i this)))
                    items)))
        (dom/span nil "Error")))))

(def area-menu (om/factory AreaMenu))

(defn menu-item [item am]
  (let [{:keys [area-type cols item-type display-name sub-menu table]} item]
    (dom/li #js {:className (str (name item-type) "-area-menu-item area-menu-item")
                 :onClick #(om/set-query! am {:params {:area-type area-type :table table :cols cols}})}
      display-name
      (when-not (empty? sub-menu)
        (area-menu {:menu/items sub-menu})))))
