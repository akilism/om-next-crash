(ns ta-crash.stat-group
  (:require [cljs.pprint :as pprint]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ta-crash.props :as props]
            [ta-crash.group-item :as group-item]))

(def group-order [:total-crashes
                  :total-killed
                  :total-injured
                  :total-persons-killed
                  :total-pedestrian-killed
                  :total-cyclist-killed
                  :total-motorist-killed
                  :total-persons-injured
                  :total-pedestrian-injured
                  :total-cyclist-injured
                  :total-motorist-injured])

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

(defui StatGroup
  static om/IQueryParams
  (params [this]
     {:params {:end-date "" :start-date "" :geo-table "" :identifier ""} :query false})
  static om/IQuery
  (query [_]
    '[(:group/items {:params ?params :query ?query})])
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
    (when (not (props/same-props? (om/props this) next-props))
        (.set-query this next-props)))
  (render [this]
    (let [{:keys [group/items]} (om/props this)
          {:keys [stat-change]} (om/get-computed this)]
      (println (mapcat (fn [id] (filter #(= id (:id %)) items)) group-order))
      (dom/div #js {:className "stat-box"}
        (apply dom/div #js {:className "stat-group"}
               (map #(group-item/group-item (om/computed % {:stat-change stat-change})) (mapcat (fn [id] (filter #(= id (:id %)) items)) group-order)))))))

(def stat-group (om/factory StatGroup))
