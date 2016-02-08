(ns ta-crash.carto-map
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.pprint :as pprint]
            [cljs.core.async :as async :refer [<! >! put! chan]]
            [ta-crash.sql-queries :as queries]
            [ta-crash.props :as props]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(defn error-handler
  [error]
  (pprint/pprint error))

(defn get-geo-table [type]
  (condp = type
    :borough "nyc_borough"
    :city-council "nyc_city_council"
    :community-board "nyc_community_board"
    :neighborhood "nyc_neighborhood"
    :precinct "nyc_nypd_precinct"
    :zip-code "nyc_zip_codes"))

(defn get-geo-identifier [identifier]
  (if (string? identifier)
    (str "'" identifier "'")
    identifier))

(defn set-map-bounds
  [query map-el params]
  ;(println "bounds: " params)
  (let [c-sql (js/cartodb.SQL. (clj->js {:user "akilism"}))
        query-str (query params)]
    (-> (.getBounds c-sql query-str)
        (.done #(.fitBounds map-el %)))))

(defn set-map-query
  [query layer params]
  (println "query: " params)
  (let [query-str (query params)]
    (.setSQL layer (query params))))

(defn set-map
  [query layer map-el]
  (let [c-sql (js/cartodb.SQL. (clj->js {:user "akilism"}))]
    (.setSQL layer query)
    (-> (.getBounds c-sql query)
        (.done #(.fitBounds map-el %)))))

(defui CartoMap
  static om/IQuery
  (query [_]
    [:start-date :end-date :active-area :active-stat])
  Object
  (create-handler
    [this vis layers]
    (om/set-state! this {:vis vis}))
  (map-new-parameters
    [this vis props]
    (let [data-layer (nth (.getLayers vis) 1)
         sub-layer (.getSubLayer data-layer 0)
         ;{:keys [start-date end-date active-area active-stat]} props
         ;{:keys [area-type identifier]} active-area
         ;params {:end-date end-date :start-date start-date}
         ]
     (set-map (queries/get-query :crashes props) sub-layer (.getNativeMap vis))
     ;(cond
     ;  (and (or (= "citywide" (:identifier active-area)) (empty? active-area)) (nil? active-stat))
     ;   (set-map (queries/get-query :crashes-date [end-date start-date]) sub-layer (.getNativeMap vis))
     ;  (and (or (= "citywide" (:identifier active-area)) (empty? active-area)) (complement nil? active-stat))
     ;   (set-map (queries/get-query :crashes-date-filtered [end-date start-date active-stat]) sub-layer (.getNativeMap vis))
     ;  (and (not (= "citywide" (:identifier active-area))) (nil? active-stat))
     ;   (set-map (queries/get-query :crashes-date-area [end-date start-date area-type identifier]) sub-layer (.getNativeMap vis))
     ;  (and (not (= "citywide" (:identifier active-area))) (complement nil? active-stat))
     ;   (set-map (queries/get-query :crashes-date-area-filtered [end-date start-date area-type identifier active-stat]) sub-layer (.getNativeMap vis)))
     ;(if (or (empty? active-area) (= "citywide" (:identifier active-area)))
     ;   (do (set-map-query queries/crashes-by-date sub-layer params)
     ;       (set-map-bounds queries/crashes-by-date (.getNativeMap vis) params))
     ;   (let [final-params (assoc params :geo-table (get-geo-table area-type) :identifier (get-geo-identifier identifier))]
     ;     (set-map-query queries/crashes-by-date-area sub-layer final-params)
     ;     (set-map-bounds queries/crashes-by-date-area (.getNativeMap vis) final-params)))
     ))
  (componentDidMount [this]
    (let [{:keys [text type]} (om/props this)
          viz "https://akilism.cartodb.com/api/v2/viz/fcdfe28c-b6de-11e5-849b-0e787de82d45/viz.json"]
      (-> (.createVis js/cartodb (om/react-ref this "cartoMap") viz)
          (.done (fn [vis layers] (.create-handler this vis layers)))
          (.error error-handler))))
  (componentWillMount [this]
    (om/set-state! this {:vis nil}))
  (componentWillReceiveProps
    [this next-props]
    (let [vis (:vis (om/get-state this))]
      (when (and vis (not (props/same-props? (om/props this) next-props)))
        (.map-new-parameters this vis next-props))))
  (render [this]
    (let [{:keys [text type]} (om/props this)]
      (dom/div #js {:className "carto-map" :ref "cartoMap" :id "cartoMap"}))))

(def carto-map (om/factory CartoMap))
