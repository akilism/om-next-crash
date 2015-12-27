(ns ta-crash.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.pprint :as pprint]
            [cljs.core.async :as async :refer [<! >! put! chan]]
            [clojure.string :as string]
            [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ta-crash.carto-query :as carto-query]
            [ta-crash.sql-queries :as queries]
            [ta-crash.area-menu :as area-menu]
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
     :query queries/distinct-borough}
    {:display-name "City Counctil District"
     :item-type :group
     :area-type :city-council
     :query queries/distinct-city-council}
    {:display-name "Community Board District"
     :item-type :group
     :area-type :community-board
     :query queries/distinct-community-board}
    {:display-name "Neighborhood"
     :item-type :group
     :area-type :neighborhood
     :query queries/distinct-neighborhood}
    {:display-name "NYPD Precinct"
     :item-type :group
     :area-type :precinct
     :query queries/distinct-precinct}
    {:display-name "Zip Code"
     :item-type :group
     :area-type :zip-code
     :query queries/distinct-zip-code}]})


(defn execute-query-carto
  ([query params] (execute-query-carto (chan) query params))
  ([c query params]
   (println (query params))
   (carto-query/execute-query (query params)
      #(put! c (:rows (js->clj % :keywordize-keys true)))
      #(println "ERROR: " %))
   c))
;(defmulti query-carto (fn [type &_] type))

;(defmethod query-carto :select-distinct
;  ([type cols table] (query-carto type (chan) cols table))
;  ([c _ cols table]
;   (carto-query/select-distinct-cols cols table
;      #(put! c (:rows (js->clj % :keywordize-keys true)))
;      #(println "ERROR: " %))
;   c))

;(defmethod query-carto :select
;  ([type cols table] (query-carto type (chan) cols table))
;  ([c _ cols table]
;   (carto-query/select-cols cols table
;      #(put! c (:rows (js->clj % :keywordize-keys true)))
;      #(println "ERROR: " %))
;   c))

(defmulti read om/dispatch)

(defmethod read :group/items
  [{:keys [state ast] :as env} k {:keys [params query]}]
  (merge
    {:value (get @state k [])}
    (when query
    {:search ast})))

(defmethod read :menu/items
  [{:keys [state] :as env} k _]
  (let [st @state]
    {:value (get st k)}))

(defmethod read :area/items
  [{:keys [state ast] :as env} k {:keys [area-type query]}]
  (merge
    {:value (get @state k [])}
    (when query
    {:search ast})))

;(defn carto-loop [c]
;  (go
;    (loop [[area-type cols table cb] (<! c)]
;      (let [result (<! (query-carto :select-distinct cols table))]
;        (cb {:area/items (map #(assoc % :parent area-type :item-type :sub) result)}))
;      (recur (<! c)))))

(defn carto-loop [c]
  (go
    (loop [[area-type query params cb] (<! c)]
      (let [result (<! (execute-query-carto query params))]
        (println result)
        (cb {:area/items (map #(assoc % :parent area-type :item-type :sub) result)}))
      (recur (<! c)))))

(defn send-to-chan [c]
  (fn [{:keys [search]} cb]
    (when search
      (let [{[search] :children} (om/query->ast search)
            area-type (get-in search [:params :area-type])
            query (get-in search [:params :query])
            params (get-in search [:params :params])]
        (put! c [area-type query params cb])))))

(def send-chan (chan))

(def reconciler
  (om/reconciler
    {:parser (om/parser {:read read})
     :remotes [:remote :search]
     :send (send-to-chan send-chan)
     :state menu-data}))

(carto-loop send-chan)

;(om/add-root! reconciler
;  area-menu/AreaMenu (gdom/getElement "app"))

;(def reconciler
;  (om/reconciler
;    {:state init-data
;     :parser (om/parser {:read read})}))

(om/add-root! reconciler
  stat-group/StatGroup (gdom/getElement "app"))
