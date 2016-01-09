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

;
;// create a layer with 1 sublayer
;cartodb.createLayer(map, {
;  user_name: 'mycartodbuser',
;  type: 'cartodb',
;  sublayers: [{
;    sql: "SELECT * FROM table_name",
;    cartocss: '#table_name {marker-fill: #F0F0F0;}'
;  }]
;})
;.addTo(map) // add the layer to our map which already contains 1 sublayer
;.done(function(layer) {
;
;  // create and add a new sublayer
;  layer.createSubLayer({
;    sql: "SELECT * FROM table_name limit 200",
;    cartocss: '#table_name {marker-fill: #F0F0F0;}'
;  });
;
;  // change the query for the first layer
;  layer.getSubLayer(0).setSQL("SELECT * FROM table_name limit 10");
;});

;(-> (.execute c-sql query)
;    (.done done-cb)
;    (.error error-cb))

;static om/IQueryParams
;  (params [this]
;      {:params {:end-date "" :start-date "" :geo-table "" :identifier ""} :query false})
;  static om/IQuery
;  (query [_]
;    '[(:stat-list/items {:params ?params :query ?query})])
;  Object
;  (get-query [this props]
;    (let [{:keys [query active-area query-area]} props]
;      (if (empty? active-area)
;        query
;        query-area)))
;  (get-query-params [this props]
;    (let [{:keys [end-date start-date active-area]} props
;          {:keys [area-type identifier]} active-area]
;      (if (empty? active-area)
;        {:end-date end-date :start-date start-date}
;        {:end-date end-date :start-date start-date :geo-table (get-geo-table area-type) :identifier (if (string? identifier) (str "'" identifier "'") identifier)})))
;  (set-query
;    ([this] (let [{:keys [end-date start-date]} (om/props this)]
;      (om/set-query! this {:params {:params (.get-query-params this (om/props this)) :query (.get-query this (om/props this))}})))
;    ([this props] (om/set-query! this {:params {:params (.get-query-params this props)
;                                                :query (.get-query this props)}})))
;  (componentWillMount [this]
;    (.set-query this))
;  (componentWillReceiveProps [this next-props]
;    (.set-query this next-props))


