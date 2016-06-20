(ns ta-crash.area-menu
  (:require [cljs.pprint :as pprint]
            [clojure.string :as string]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ta-crash.conversion :as conversion]))

(defn build-sub-item
  [item sub-item]
  (assoc sub-item :query (:query item)
    :display-name (conversion/convert-type (:identifier sub-item) (:area-type item))))

(defui AreaMenuItem
  static om/IQuery
  (query [_]
    '[:area-type :query :item-type :display-name :area-menu])
  Object
  (render [this]
    (let [{:keys [area-menu area-type query item-type display-name]} (om/props this)
          {:keys [item-click-handler]} (om/get-computed this)]
      (dom/li #js {:className (str (name area-type) " " (name item-type) "-area-menu-item area-menu-item")
                   :onClick #(item-click-handler area-type query %)}
       display-name))))

(def area-menu-item (om/factory AreaMenuItem))

(defui SubMenuItem
  static om/IQuery
  (query [_]
    '[:identifier :parent :item-type])
  Object
  (render [this]
    (let [{:keys [identifier parent item-type b-key]} (om/props this)
          area-change (:area-change (om/get-computed this))]
      (dom/div #js {:className (str (string/replace (name b-key) #" " "-") " area-sub-menu-item")
                    :onClick (fn [_]
                               (.scrollTo js/window 0 0)
                               (area-change {:type parent :identifier identifier}))}
               (conversion/convert-type identifier parent)))))

(def sub-menu-item (om/factory SubMenuItem))

(defui SubMenu
  static om/IQuery
  (query [_]
    '[{:area-items (om/get-query SubMenuItem)} :show-sub :pos])
  Object
  (render [this]
    (let [items (:area/items (om/props this))
          {:keys [show-sub pos]} (om/props this)
          area-change (:area-change (om/get-computed this))]
      (apply dom/div #js {:className (if show-sub (str "area-sub-menu full-opacity") (str "area-sub-menu zero-opacity"))
                          :style #js {:left (:x pos) :top (:y pos)}}
        (map #(sub-menu-item (om/computed % {:area-change area-change})) items)))))

(def sub-menu (om/factory SubMenu))

(defn get-sub-menu-pos
  [evt]
  (let [x (.-offsetLeft evt.target)
        y (.-offsetTop evt.target)]
    {:x 0 :y (+ 38 y)}))

(defui AreaMenu
  static om/IQueryParams
  (params [_]
    {:area-type "" :query false})
  static om/IQuery
  (query [_]
    '[(:area/items {:area-type ?area-type :query ?query})
      {:menu/items (om/get-query AreaMenuItem)}])
  Object
  (toggle-menu [this show pos]
    (om/set-state! this {:show-sub show :pos pos}))
;  (menu-item-click
;    [this area-type query evt]
;    (let [prev-area-type (:area-type (om/get-params this))
;          show-sub (:show-sub (om/get-state this))]
;      (if (and (= area-type prev-area-type) show-sub)
;        (do (om/set-query! this {:params {:area-type nil :query false}})
;          (.toggle-menu this false (get-sub-menu-pos evt)))
;        (do (om/set-query! this {:params {:area-type area-type :query query}})
;          (.toggle-menu this true (get-sub-menu-pos evt))))))
  (menu-item-click
    [this area-type query evt]
    (let [area-select (:area-select (om/get-computed this))]
      (area-select area-type query)))
  (componentWillMount [this]
    (om/set-state! this {:show-sub false :pos {:x 10 :y 10}}))
  (render [this]
    (let [area-items (:area/items (om/props this))
          menu-items (:menu/items (om/props this))
          area-change (:area-change (om/get-computed this))
          area-select (:area-select (om/get-computed this))
          toggle-edit (:toggle-edit (om/get-computed this))
          {:keys [show-sub pos]} (om/get-state this)]
      (if (:item-type (first menu-items))
        (let [item-type (name (:item-type (first menu-items)))]
          (dom/div #js {:className "area-menu"}
            (apply dom/ul
              #js {:className (str item-type "-area-menu")}
              (conj (into [] (map #(area-menu-item (om/computed % {:item-click-handler (fn [area-type query evt] (.menu-item-click this area-type query evt))})) menu-items))
               (dom/li #js {:className "custom group-area-menu-item area-menu-item"
                            :onClick toggle-edit} "Custom")))
            (sub-menu (om/computed {:area/items area-items
                                    :show-sub show-sub
                                    :pos pos}
                                  {:area-change (fn [selected-area]
                                                  (.toggle-menu this false {:x 0 :y 0})
                                                  (area-change selected-area))}))
            (when (< 0 (count area-items)))))
        (dom/span nil "Error")))))

(def area-menu (om/factory AreaMenu))
