(ns ta-crash.carto-map
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.pprint :as pprint]
            [cljs.core.async :as async :refer [<! >! put! chan]]
            [ta-crash.sql-queries :as queries]
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
    :zipcode "nyc_zip_codes"))

(defn get-geo-identifier [identifier]
  (if (string? identifier)
    (str "'" identifier "'")
    identifier))

(defn set-map-query
  [query layer params]
  (let [query-str (query params)]
    (.setSQL layer (query params))))

(defui CartoMap
  static om/IQuery
  (query [_]
    [:start-date :end-date :active-area])
  Object
  (create-handler
    [this vis layers]
    (om/set-state! this {:vis vis}))
  (componentDidMount [this]
    (let [{:keys [text type]} (om/props this)
          viz "https://akilism.cartodb.com/api/v2/viz/fcdfe28c-b6de-11e5-849b-0e787de82d45/viz.json"]
      (-> (.createVis js/cartodb (om/react-ref this "cartoMap") viz)
          (.done (fn [vis layers] (.create-handler this vis layers)))
          (.error error-handler))))
  (componentWillMount [this]
    (om/set-state! this {:vis nil}))
  (componentWillReceiveProps [this next-props]
    (when-let [vis (:vis (om/get-state this))]
      (let [vis (:vis (om/get-state this))
         data-layer (nth (.getLayers vis) 1)
         sub-layer (.getSubLayer data-layer 0)
         {:keys [start-date end-date active-area]} (om/props this)
         {:keys [area-type identifier]} active-area
         params {:end-date end-date :start-date start-date}]
     (if (or (empty? active-area) (= "citywide" (:identifier active-area)))
        (set-map-query queries/crashes-by-date sub-layer params)
        (set-map-query queries/crashes-by-date-area sub-layer (assoc params :geo-table (get-geo-table area-type) :identifier (get-geo-identifier identifier)))))))
  (render [this]
    (let [{:keys [text type]} (om/props this)]
      (dom/div #js {:className "carto-map" :ref "cartoMap" :id "cartoMap"}))))

(def carto-map (om/factory CartoMap))
