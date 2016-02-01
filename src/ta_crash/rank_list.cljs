(ns ta-crash.rank-list
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.pprint :as pprint]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ta-crash.props :as props]))

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

(defui RankList
  static om/IQueryParams
  (params [this]
      {:params {:end-date "" :start-date "" :geo-table "" :identifier "" :order-col "total_crashes" :order-dir "DESC"} :query false})
  static om/IQuery
  (query [_]
    '[(:rank-list/items {:params ?params :query ?query})])
  Object
  (get-query [this props]
    (let [{:keys [query active-area query-area]} props]
      (if (or (empty? active-area) (= "citywide" (:identifier active-area)))
        false
        query)))
  (get-query-params [this props]
    (let [{:keys [end-date start-date active-area]} props
          {:keys [area-type identifier]} active-area]
      (if (or (empty? active-area) (= "citywide" (:identifier active-area)))
        {:end-date "" :start-date "" :geo-table "" :identifier "" :order-col "persons_killed" :order-dir "DESC"}
        {:end-date end-date :start-date start-date :geo-table (get-geo-table area-type) :identifier (get-geo-identifier identifier) :order-col "persons_killed" :order-dir "DESC"})))
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
    (let [{:keys [rank-list/items start-date end-date active-area]} (om/props this)]
      (println "ranklist:" (take 1 items))
      (dom/div #js {:className "rank-list"}
        (dom/div #js {:className "rank-title"} "Persons Killed")
        (apply dom/ul nil
          (map #(dom/li #js {:className "rank-list-item"} (str (:streets %) ": " (:persons_killed %))) (take 10 (filter #(not (= "," (:streets %))) items))))))))

(def rank-list (om/factory RankList))


;(and (< 0 (:persons_killed %)) (= "," (:streets %)))
