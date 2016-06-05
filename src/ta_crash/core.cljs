(ns ta-crash.core
  (:import goog.History)
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.pprint :as pprint]
            [cljs.core.async :as async :refer [<! >! put! chan]]
            [clojure.string :as string]
            [goog.dom :as gdom]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [secretary.core :as secretary :refer-macros [defroute]]
            [ta-crash.carto-query :as carto-query]
            [ta-crash.formatter :as data-formatter]
            [ta-crash.sql-queries :as queries]
            [ta-crash.area-menu :as area-menu]
            [ta-crash.header :as header]
            [ta-crash.date-range :as date-range]
            [ta-crash.stat-group :as stat-group]
            [ta-crash.stat-list :as stat-list]
            [ta-crash.rank-list :as rank-list]
            [ta-crash.carto-map :as carto-map]
            [ta-crash.conversion :as conversion]))

(enable-console-print!)

(def init-data
  {:selected-date-max nil
   :selected-date-min nil
   :date-max nil
   :date-min nil
   :active-area {:area-type :citywide :identifier "citywide"}
   :active-stat nil
   :area-overlay nil
   :edit-mode false
   :custom-area nil
   :group/items []
   :stat-list/items []
   :rank-list/items []
   :area/items []
   :menu/items
   [{:display-name "Citywide"
     :item-type :group
     :area-type :citywide
     :query nil}
    {:display-name "Custom Area"
     :item-type :group
     :area-type :custom
     :query nil}
    {:display-name "Borough"
     :item-type :group
     :area-type :borough
     :query queries/distinct-borough}
    {:display-name "City Council District"
     :item-type :group
     :area-type :city-council
     :query queries/distinct-city-council}
    {:display-name "Community Board"
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
  ([query] (execute-query-carto (chan) query))
  ([c query]
   (carto-query/execute-query query
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
  ; (println "read:menu/items" k)
  (let [st @state]
    {:value (get st k)}))

(defmethod read :area/items
  [{:keys [state ast] :as env} k {:keys [area-type query]}]
  ; (println "read:area/items" k area-type)
  (merge
    {:value (get @state k [])}
    (when query
      {:area-menu ast})))

(defmethod read :stat-list/items
  [{:keys [state ast] :as env} k {:keys [query] :as params}]
  ;(println "read:stat-list/items" k)
  (merge
    {:value (get @state k [])}
    (when query
      {:stat-list ast})))

(defmethod read :rank-list/items
  [{:keys [state ast] :as env} k {:keys [query] :as params}]
  ; (println "read:rank-list/items" k)
  (merge
    {:value (get @state k [])}
    (when query
      {:rank-list ast})))

(defmethod read :default
  [{:keys [state] :as env} k _]
  (let [st @state]
    ; (println "default read:" k)
    {:value (get st k)}))

(defmulti mutate om/dispatch)

(defmethod mutate 'area/change
  [{:keys [state] :as env} key {:keys [area-type identifier] :as params}]
  ; (println "area/change:" area-type " -> " identifier)
  (condp = key
    'area/change {:value {:keys [:active-area]}
                  :action #(swap! state update :active-area (fn [_] {:area-type area-type :identifier identifier}))}
    :else {:value :not-found}))

(defmethod mutate 'date/change
  [{:keys [state] :as env} key {:keys [date-key date] :as params}]
  ; (println "date/change:" date-key " -> " (.format date "YYYY-MM-DD"))
  (condp = key
    'date/change {:value {:keys [date-key]}
                  :action #(swap! state update-in [date-key] (fn [_] date))}
    :else {:value :not-found}))

(defmethod mutate 'overlay/change
  [{:keys [state] :as env} key {:keys [area-type query] :as params}]
  ; (println "overlay/change:" area-type " -> " area-type)
  (condp = key
    'overlay/change {:value {:keys [:area-overlay]}
                     :action #(swap! state update-in [:area-overlay] (fn [_] {:area-type area-type :query query}))}
    :else {:value :not-found}))

(defmethod mutate 'stat/change
  [{:keys [state] :as env} key {:keys [id] :as params}]
  ; (println "stat/change:" key " -> " id)
  (condp = key
    'stat/change {:value {:keys [:active-stat]}
                  :action #(swap! state update :active-stat (fn [_] id))}
    :else {:value :not-found}))

(defn carto-loop [c]
  (go
    (loop [[area-type query formatter cb] (<! c)]
      (let [result (<! (execute-query-carto query))]
        ;(println "carto result" result)
        (formatter result cb area-type))
      (recur (<! c)))))

(defn get-area-menu
  [c cb area-menu]
  (let [{[area-menu] :children} (om/query->ast area-menu)
        area-type (get-in area-menu [:params :area-type])
        query (get-in area-menu [:params :query])
        params (get-in area-menu [:params :params])]
    (put! c [area-type (query params) data-formatter/for-area-menu cb])))

(defn get-stat-group
  [c cb stat-group]
  ; (println "get-stat-group")
  (let [{[stat-group] :children} (om/query->ast stat-group)
        area-type (get-in stat-group [:params :area-type])
        query (get-in stat-group [:params :query])
        params (get-in stat-group [:params :params])]
    (put! c [area-type query data-formatter/for-stat-group cb])))

(defn get-stat-list
  [c cb stat-list]
  (let [{[stat-list] :children} (om/query->ast stat-list)
        area-type (get-in stat-list [:params :area-type])
        query (get-in stat-list [:params :query])
        params (get-in stat-list [:params :params])]
    (put! c [area-type query data-formatter/for-stat-list cb])))

(defn get-rank-list
  [c cb rank-list]
  (let [{[rank-list] :children} (om/query->ast rank-list)
        area-type (get-in rank-list [:params :area-type])
        query (get-in rank-list [:params :query])
        params (get-in rank-list [:params :params])]
    (put! c [area-type query data-formatter/for-rank-list cb])))

(defn send-to-chan [c]
  (fn [{:keys [area-menu stat-group stat-list rank-list]} cb]
    ; (println "send-to-chan" stat-group)
    (when
      area-menu (get-area-menu c cb area-menu))
    (when
      stat-group (get-stat-group c cb stat-group))
    (when
      stat-list (get-stat-list c cb stat-list))
    (when
      rank-list (get-rank-list c cb rank-list))))

(def send-chan (chan))

(def reconciler
  (om/reconciler
    {:parser (om/parser {:read read :mutate mutate})
     :remotes [:remote :stat-group :area-menu :stat-list :rank-list]
     :send (send-to-chan send-chan)
     :state {}}))

(carto-loop send-chan)

(defn url
  [type identifier]
  (condp = type
    :custom (str "/custom/" (.btoa js/window identifier))
    (str "/" (name type) "/" (conversion/normalize (conversion/convert-type identifier type)))))


(defui Root
  static om/IQuery
  (query [_]
    ;; (println "ROOT QUERY FIX THIS")
    '[{:menu/items (om/get-query area-menu/AreaMenu)}
      {:area/items (om/get-query area-menu/SubMenu)}
      {:group/items (om/get-query stat-group/StatGroup)}
      {:stat-list/items (om/get-query stat-list/StatList)}
      {:rank-list/items (om/get-query rank-list/RankList)}
      :cal-date-max :cal-date-min :date-max :date-min :edit-mode :selected-date-max :selected-date-min :active-area :active-stat :area-overlay :custom-area])
  Object
  (area-change
    ;"Handler for selecting an exact area"
    [this {:keys [type identifier]}]
    (let [history (.-history js/window)
          next-page (url type identifier)]
      (.pushState history "" "" next-page)
      (secretary/dispatch! next-page)))
  (area-select
    ;"Handler for selecting an area type"
    [this area-type query]
    (if (nil? query)
     (do
      (.pushState (.-history js/window) "" "" "/")
      (secretary/dispatch! "/"))
     (om/transact! this `[(overlay/change {:area-type ~area-type :query ~query}) :area-overlay])))
  (date-change
    [this {:keys [key date]}]
    (om/transact! this `[(date/change {:date-key ~key :date ~date}) :group/items :stat-list/items :rank-list/items]))
  (month-change
    [this {:keys [key date]}]
    (om/merge! reconciler (merge (om/props this) {key date})))
  (toggle-edit-mode
    [this]
    (let [edit-mode (:edit-mode (om/props this))]
     (om/merge! reconciler (merge (om/props this) {:edit-mode (not edit-mode)}))))
  (stat-change
    [this {:keys [key id]}]
    (om/transact! this `[(stat/change {:key ~key :id ~id}) :group/items :stat-list/items :rank-list/items :active-area]))
  (componentWillMount [this]
    (go
      (let [result (<! (execute-query-carto (queries/date-bounds [])))
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
    (let [{:keys [selected-date-max selected-date-min cal-date-max cal-date-min date-max date-min edit-mode active-area active-stat area-overlay custom-area]} (om/props this)]
      ;;(println "Root render:" (:active-stat (om/props this)))
      (dom/div #js {:className "root"}
        (dom/div #js {:className "static-head"} "Transportation Alternatives: CrashStats")
        (dom/section #js {:className "header-outer"}
          (header/header (om/computed {:date-max date-max
                                       :date-min date-min
                                       :cal-date-max cal-date-max
                                       :cal-date-min cal-date-min
                                       :selected-date-max selected-date-max
                                       :selected-date-min selected-date-min
                                       :active-area active-area}
                                  {:date-change #(.date-change this %)
                                   :month-change #(.month-change this %)}))
          (area-menu/area-menu (om/computed {:menu/items (:menu/items (om/props this))
                                             :area/items (:area/items (om/props this))}
                                            {:area-change #(.area-change this %)
                                             :area-select #(.area-select this %1 %2)
                                             :toggle-edit #(.toggle-edit-mode this)})))
        (dom/div #js {:className "content-outer"}
         (stat-group/stat-group (om/computed {:group/items (:group/items (om/props this))
                                              :end-date (if selected-date-max
                                                          (.format selected-date-max "YYYY-MM-DD")
                                                          "2015-12-26")
                                              :start-date (if selected-date-min
                                                            (.format selected-date-min "YYYY-MM-DD")
                                                            "2015-01-01")
                                              :active-area active-area
                                              :custom-area custom-area}
                                           {:stat-change #(.stat-change this %)}))
         (carto-map/carto-map (om/computed {:end-date (if selected-date-max
                                                       (.format selected-date-max "YYYY-MM-DD")
                                                       "2015-12-26")
                                            :start-date (if selected-date-min
                                                          (.format selected-date-min "YYYY-MM-DD")
                                                          "2015-01-01")
                                            :edit-mode edit-mode
                                            :area-overlay area-overlay
                                            :active-area active-area
                                            :active-stat active-stat
                                            :custom-area custom-area}
                               {:area-change #(.area-change this %)}))
         (rank-list/rank-list {:rank-list/items (:rank-list/items (om/props this))
                               :end-date (if selected-date-max
                                           (.format selected-date-max "YYYY-MM-DD")
                                           "2015-12-26")
                               :start-date (if selected-date-min
                                             (.format selected-date-min "YYYY-MM-DD")
                                             "2015-01-01")
                               :active-area active-area
                               :active-stat active-stat
                               :custom-area custom-area})
         (stat-list/stat-list {:stat-list/items (:stat-list/items (om/props this))
                               :end-date (if selected-date-max
                                           (.format selected-date-max "YYYY-MM-DD")
                                           "2015-12-26")
                               :start-date (if selected-date-min
                                             (.format selected-date-min "YYYY-MM-DD")
                                             "2015-01-01")
                               :active-area active-area
                               :custom-area custom-area}))))))

(defn render-page
  [state]
  (om/merge! reconciler state)
  (om/add-root! reconciler
      Root (gdom/getElement "app")))

(defn get-client-route []
  (let [location (.-location js/window)
        path (.-pathname location)
        search (.-search location)]
    (str path search)))

(defroute home-route "/" []
  (render-page init-data))

(defroute custom-area-route "/custom/:encoded-shape" [encoded-shape]
  (let [decoded-shape (.atob js/window encoded-shape)]
    (render-page (assoc
                  init-data
                  :custom-area decoded-shape
                  :active-area {:area-type :custom :identifier "custom"}))))


(defroute area-route "/:area/:ident" [area ident query-params]
  (let [area-type (keyword area)
        rev-area-type (keyword (str area "-rev"))
        identifier (conversion/convert-type ident rev-area-type)]
    (render-page (assoc init-data :active-area {:area-type area-type :identifier identifier}))))

(let
  [route (get-client-route)]
  (aset js/window "onpopstate" #(secretary/dispatch! (get-client-route)))
  (secretary/dispatch! route))
