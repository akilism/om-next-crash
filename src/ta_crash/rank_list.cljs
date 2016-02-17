(ns ta-crash.rank-list
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.pprint :as pprint]
            [clojure.string :as str]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ta-crash.sql-queries :as queries]
            [ta-crash.props :as ta-props]))


(defui RankList
  static om/IQueryParams
  (params [this]
      {:carto-query false})
  static om/IQuery
  (query [_]
    '[(:rank-list/items {:query ?carto-query})])
  Object
  (set-query
    ([this] (.set-query this (om/props this)))
    ([this props] (om/set-query! this {:params {:carto-query (queries/get-query :rank (assoc props :order-col "dval" :order-dir "DESC"))}})))
  (componentWillMount [this]
    (.set-query this))
  (componentWillReceiveProps [this next-props]
    (when (not (ta-props/same-props? (om/props this) next-props))
        (.set-query this next-props)))
  (render [this]
    (let [{:keys [rank-list/items start-date end-date active-area]} (om/props this)]
      (dom/div #js {:className "rank-list"}
        (dom/div #js {:className "rank-list-title"} "Most Dangerous Intersections")
        (apply dom/ul nil
          (map #(dom/li #js {:className "rank-list-item"} (str/lower-case (str/replace (str (:streets %)) #"," " & "))) (take 10 (filter #(not (= "," (:streets %))) items))))))))

(def rank-list (om/factory RankList))


;(and (< 0 (:persons_killed %)) (= "," (:streets %)))
