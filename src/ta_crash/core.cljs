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
            [ta-crash.date-range :as date-range]
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
  {:selected-date-max nil
   :selected-date-min nil
   :date-max nil
   :date-min nil
   :group/items []
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

(defmethod read :default
  [{:keys [state] :as env} k _]
  (let [st @state]
    {:value (get st k)}))

(defmulti mutate om/dispatch)

(defmethod mutate 'date/change
  [{:keys [state] :as env} key {:keys [date-key date] :as params}]
  (println "date/change:" date-key " -> " (.format date "YYYY-MM-DD"))
  (condp = key
    'date/change {:value {:keys [date-key]}
          :action #(swap! state update-in [date-key] (fn [_] date))}
    :else {:value :not-found}))

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
    (when
      area-menu (get-area-menu c cb area-menu))
    (when
      stat-group (get-stat-group c cb stat-group))
    (when
      stat-list (get-stat-list c cb stat-list))))

(def send-chan (chan))

(def reconciler
  (om/reconciler
    {:parser (om/parser {:read read :mutate mutate})
     :remotes [:remote :stat-group :area-menu :stat-list]
     :send (send-to-chan send-chan)
     :state init-data}))

(carto-loop send-chan)

(defui Root
  static om/IQuery
  (query [_]
    '[{:menu/items (om/get-query area-menu/AreaMenu)}
      {:group/items (om/get-query stat-group/StatGroup)}
      {:stat-list/items (om/get-query stat-list/StatList)}
      :cal-date-max :cal-date-min :date-max :date-min :selected-date-max :selected-date-min])
  Object
  (date-change
    [this {:keys [key date]}]
    (om/transact! this `[(date/change {:date-key ~key :date ~date}) :group/items :stat-list/items]))
  (month-change
    [this {:keys [key date]}]
    (om/merge! reconciler (merge (om/props this) {key date})))
  (componentWillMount [this]
    (go
      (let [result (<! (execute-query-carto queries/date-bounds []))
            date-max (js/moment (:max_date (first result)))
            date-min (js/moment (:min_date (first result)))]
        (om/merge! reconciler (merge (om/props this)
           {:date-max date-max
            :date-min date-min
            :cal-date-max date-max
            :cal-date-min date-min
            :selected-date-max date-max
            :selected-date-min date-min})))))
  (render [this]
    (let [{:keys [selected-date-max selected-date-min cal-date-max cal-date-min date-max date-min]} (om/props this)]
      ;(println "Root render: selected-date-max:" selected-date-max)
      (dom/div #js {:className "root"}
        (area-menu/area-menu {:menu/items (:menu/items (om/props this))})
        (when (and selected-date-max selected-date-min)
          (date-range/date-range {:date-max date-max
                                  :date-min date-min
                                  :cal-date-max cal-date-max
                                  :cal-date-min cal-date-min
                                  :selected-date-max selected-date-max
                                  :selected-date-min selected-date-min
                                  :date-change  #(.date-change this %)
                                  :month-change  #(.month-change this %)}))
        (stat-group/stat-group {:group/items (:group/items (om/props this))
                                :end-date (if selected-date-max
                                            (.format selected-date-max "YYYY-MM-DD")
                                            "2015-12-26")
                                :start-date (if selected-date-min
                                              (.format selected-date-min "YYYY-MM-DD")
                                              "2015-01-01")
                                :query queries/stats-date})
        (stat-list/stat-list {:stat-list/items (take 15 (:stat-list/items (om/props this)))
                              :end-date (if selected-date-max
                                          (.format selected-date-max "YYYY-MM-DD")
                                          "2015-12-26")
                              :start-date (if selected-date-min
                                            (.format selected-date-min "YYYY-MM-DD")
                                            "2015-01-01")
                              :query queries/all-factors-date})
        (carto-map/carto-map)))))

(om/add-root! reconciler
  Root (gdom/getElement "app"))
