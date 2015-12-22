(ns ta-crash.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.pprint :as pprint]
            [cljs.core.async :as async :refer [<! >! put! chan]]
            [clojure.string :as string]
            [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [yesql.core :refer [defqueries]]
            [ta-crash.area-menu :as area-menu]
            [ta-crash.carto-query :as carto-query]
            [ta-crash.stat-group :as stat-group]))

(enable-console-print!)
(defqueries "ta_crash/sql/crash_queries.sql")

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
     :query ()}
    {:display-name "Community Board District"
     :item-type :group
     :area-type :community-board
     :query ()}
    {:display-name "City Counctil District"
     :item-type :group
     :area-type :city-council
     :query ()}
    {:display-name "Neighborhood"
     :item-type :group
     :area-type :neighborhood
     :query ()}
    {:display-name "NYPD Precinct"
     :item-type :group
     :area-type :precinct
     :query ()}
    {:display-name "Zip Code"
     :item-type :group
     :area-type :zip-code
     :query ()}]})

(defmulti query-carto (fn [type &_] type))

(defmethod query-carto :select-distinct
  ([type cols table] (query-carto type (chan) cols table))
  ([c _ cols table]
   (carto-query/select-distinct-cols cols table
      #(put! c (:rows (js->clj % :keywordize-keys true)))
      #(println "ERROR: " %))
   c))

(defmethod query-carto :select
  ([type cols table] (query-carto type (chan) cols table))
  ([c _ cols table]
   (carto-query/select-cols cols table
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
      (let [result (<! (query-carto :select-distinct cols table))]
        (cb {:area/items (map #(assoc % :parent area-type :item-type :sub) result)}))
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
