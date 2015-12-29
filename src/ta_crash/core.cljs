(ns ta-crash.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.pprint :as pprint]
            [cljs.core.async :as async :refer [<! >! put! chan]]
            [clojure.string :as string]
            [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ta-crash.carto-query :as carto-query]
            [ta-crash.formatter :as data-formatter]
            [ta-crash.sql-queries :as queries]
            [ta-crash.area-menu :as area-menu]
            [ta-crash.stat-group :as stat-group]
            [ta-crash.stat-list :as stat-list]
            [ta-crash.carto-map :as carto-map]))

(enable-console-print!)

(def old-init-data
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
      :count 839}]})

(def init-data
  {:group/items []
   :stat-list/items []
   :menu/items
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
   ;(println (query params))
   (carto-query/execute-query (query params)
      #(put! c (:rows (js->clj % :keywordize-keys true)))
      #(println "ERROR: " %))
   c))

(defmulti read om/dispatch)

(defmethod read :group/items
  [{:keys [state ast] :as env} k {:keys [params query] :as params}]
  (merge
    {:value (get @state k [])}
    (when query
      {:stat-group ast})))

(defmethod read :menu/items
  [{:keys [state] :as env} k _]
  (let [st @state]
    {:value (get st k)}))

(defmethod read :area/items
  [{:keys [state ast] :as env} k {:keys [area-type query]}]
  (merge
    {:value (get @state k [])}
    (when query
      {:area-menu ast})))

(defmethod read :stat-list/items
  [{:keys [state ast] :as env} k {:keys [params query] :as params}]
  (merge
    {:value (get @state k [])}
    (when query
      {:stat-list ast})))

(defn carto-loop [c]
  (go
    (loop [[area-type query params formatter cb] (<! c)]
      (let [result (<! (execute-query-carto query params))]
        ;(println "carto result" result)
        (formatter result cb area-type))
      (recur (<! c)))))

(defn get-area-menu
  [c cb area-menu]
  (let [{[area-menu] :children} (om/query->ast area-menu)
        area-type (get-in area-menu [:params :area-type])
        query (get-in area-menu [:params :query])
        params (get-in area-menu [:params :params])]
    (put! c [area-type query params data-formatter/for-area-menu cb])))

(defn get-stat-group
  [c cb stat-group]
  (let [{[stat-group] :children} (om/query->ast stat-group)
        area-type (get-in stat-group [:params :area-type])
        query (get-in stat-group [:params :query])
        params (get-in stat-group [:params :params])]
    (put! c [area-type query params data-formatter/for-stat-group cb])))

(defn get-stat-list
  [c cb stat-list]
  (let [{[stat-list] :children} (om/query->ast stat-list)
        area-type (get-in stat-list [:params :area-type])
        query (get-in stat-list [:params :query])
        params (get-in stat-list [:params :params])]
    (put! c [area-type query params data-formatter/for-stat-list cb])))

(defn send-to-chan [c]
  (fn [{:keys [area-menu stat-group stat-list]} cb]
    (cond
      area-menu (get-area-menu c cb area-menu)
      stat-group (get-stat-group c cb stat-group)
      stat-list (get-stat-list c cb stat-list))))

(def send-chan (chan))

(def reconciler
  (om/reconciler
    {:parser (om/parser {:read read})
     :remotes [:remote :stat-group :area-menu :stat-list]
     :send (send-to-chan send-chan)
     :state init-data}))

(carto-loop send-chan)

(defui Root
  static om/IQuery
  (query [_]
    '[{:stat-list/items (om/get-query stat-list/StatList)}
      {:menu/items (om/get-query area-menu/AreaMenu)}
      {:group/items (om/get-query stat-group/StatGroup)}])
  Object
  (render [this]
    (let [{:keys [menu/items stat-list/items group/items]} (om/props this)])
    ;(pprint/pprint (:stat-list/items (om/props this)))
    ;(pprint/pprint (om/get-query area-menu/AreaMenu))
    ;(pprint/pprint (om/get-query stat-list/StatList))
    (dom/div nil
      (area-menu/area-menu {:menu/items (:menu/items (om/props this))})
      (stat-list/stat-list {:stat-list/items (:stat-list/items (om/props this))
                              :end-date "12-26-2015"
                              :start-date "1-1-2015"
                              :query queries/all-factors-date})
      (carto-map/carto-map))))

(om/add-root! reconciler
  Root (gdom/getElement "app"))
