(ns ta-crash.stat-list
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ta-crash.sql-queries :as queries]
            [ta-crash.stat-list-item :as stat-list-item]))

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

(defui StatList
  static om/IQueryParams
  (params [this]
      {:params {:end-date "" :start-date "" :geo-table "" :identifier ""} :query false})
  static om/IQuery
  (query [_]
    '[(:stat-list/items {:params ?params :query ?query})])
  Object
  (get-query [this props]
    (let [{:keys [query active-area query-area]} props]
      (if (or (empty? active-area) (= "citywide" (:identifier active-area)))
        query
        query-area)))
  (get-query-params [this props]
    (let [{:keys [end-date start-date active-area]} props
          {:keys [area-type identifier]} active-area]
      (if (or (empty? active-area) (= "citywide" identifier))
        {:end-date end-date :start-date start-date}
        {:end-date end-date :start-date start-date :geo-table (get-geo-table area-type) :identifier (get-geo-identifier identifier)})))
  (set-query
    ([this] (let [{:keys [end-date start-date]} (om/props this)]
      (om/set-query! this {:params {:params (.get-query-params this (om/props this)) :query (.get-query this (om/props this))}})))
    ([this props] (om/set-query! this {:params {:params (.get-query-params this props)
                                                :query (.get-query this props)}})))
  (componentWillMount [this]
    (.set-query this))
  (componentWillReceiveProps [this next-props]
    (.set-query this next-props))
  (render [this]
    (let [{:keys [stat-list/items end-date start-date query]} (om/props this)]
      (dom/div #js {:className "stat-box"}
        (apply dom/ul #js {:className "stat-list"} (map stat-list-item/stat-list-item items))))))

(def stat-list (om/factory StatList))
