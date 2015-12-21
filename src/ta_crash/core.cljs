(ns ta-crash.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.pprint :as pprint]
            [cljs.core.async :as async :refer [<! >! put! chan]]
            [clojure.string :as string]
            [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ta-crash.area-menu :as area-menu]
            [ta-crash.carto-query :as carto-query]
            [ta-crash.stat-group :as stat-group]))

(enable-console-print!)

(def init-data
  {:geo-area {:type :geo-area/precinct
              :id "83"
              :display "83rd Precinct"
              :geoJson {}}
   :group/items
    [{:type :group/header-item
      :id :total-crashes
      :display "Total crashes"
      :count 13839}
     {:type :group/sub-header-item
      :id :total-injuries
      :display "Crashes resulting in an injury"
      :count 3839}
     {:type :group/sub-header-item
      :id :total-deaths
      :display "Crashes resulting in a death"
      :count 839}

     {:type :group/header-item
      :id :total-injured
      :display "People injured"
      :count 1839}
     {:type :group/default-item
      :id :total-cyclist-injured
      :display "Bicyclist"
      :count 39}
     {:type :group/default-item
      :id :total-pedestrian-injured
      :display "Pedestrian"
      :count 239}
     {:type :group/default-item
      :id :total-motorist-injured
      :display "Motorist"
      :count 339}

     {:type :group/header-item
      :id :total-deaths
      :display "People killed"
      :count 1839}
     {:type :group/default-item
      :id :total-cyclist-deaths
      :display "Bicyclist"
      :count 39}
     {:type :group/default-item
      :id :total-pedestrian-deaths
      :display "Pedestrian"
      :count 239}
     {:type :group/default-item
      :id :total-motorist-deaths
      :display "Motorist"
      :count 339}]})

(def menu-data
  {:menu/items
   [{:display-name "Borough"
     :item-type :group
     :area-type :borough
     :table "nyc_borough"
     :cols [:borough :identifier]}
    {:display-name "Community Board District"
     :item-type :group
     :area-type :community_board
     :table "nyc_community_board"
     :cols [:identifier]}
    {:display-name "City Counctil District"
     :item-type :group
     :area-type :city_council
     :table "nyc_city_council"
     :cols [:identifier]}
    {:display-name "Neighborhood"
     :item-type :group
     :area-type :neighborhood
     :table "nyc_neighborhood"
     :cols [:borough :identifier]}
    {:display-name "NYPD Precinct"
     :item-type :group
     :area-type :precinct
     :table "nyc_nypd_precinct"
     :cols [:borough :identifier]}
    {:display-name "Zip Code"
     :item-type :group
     :area-type :zip_code
     :table "nyc_zip_codes"
     :cols [:borough :identifier]}]})

;(map (fn [item]
;       (let [table (:table item)
;             cols (:cols item)]
;         (carto-query/select-distinct-cols cols table
;            #(println (:rows (js->clj % :keywordize-keys true)))
;            #(println "ERROR: " %)))) (:menu/items menu-data))

;query distinct cols specified in menu-data for the first item in menu-data
;(let [item (first (:menu/items menu-data))
;      table (:table item)
;      cols (:cols item)]
;  (carto-query/select-distinct-cols cols table))


(defn query-carto
  ([cols table] (query-carto (chan) cols table))
  ([c cols table]
   (carto-query/select-distinct-cols cols table
      #(put! c (:rows (js->clj % :keywordize-keys true)))
      #(println "ERROR: " %))
   c))

(defmulti read om/dispatch)

(defmethod read :group/items
  [{:keys [state] :as env} k _]
  (let [st @state]
    {:value (into [] (map #(get-in st %)) (get st k))}))

(defmethod read :menu/items
  [{:keys [state] :as env} k _]
  (let [st @state]
    {:value (get st k)}))

(defmethod read :area/items
  [{:keys [state ast] :as env} k {:keys [area-type cols table]}]
  (merge
    {:value (get @state k [])}
    (when-not (string/blank? table)
    {:search ast})))

(defn carto-loop [c]
  (go
    (loop [[area-type cols table cb] (<! c)]
      (let [result (<! (query-carto cols table))]
        (cb {:area/items (map #(assoc % :parent area-type :item-type :area) result)}))
      (recur (<! c)))))

(defn send-to-chan [c]
  (fn [{:keys [search]} cb]
    (when search
      (let [{[search] :children} (om/query->ast search)
            area-type (get-in search [:params :area-type])
            cols (get-in search [:params :cols])
            table (get-in search [:params :table])]
        (put! c [area-type cols table cb])))))

(def send-chan (chan))

(def reconciler
  (om/reconciler
    {:parser (om/parser {:read read})
     :remotes [:remote :search]
     :send (send-to-chan send-chan)
     :state menu-data}))

(carto-loop send-chan)

(om/add-root! reconciler
  area-menu/AreaMenu (gdom/getElement "app"))

;(def reconciler
;  (om/reconciler
;    {:state init-data
;     :parser (om/parser {:read read})}))

;(om/add-root! reconciler
;  stat-group/StatGroup (gdom/getElement "app"))
