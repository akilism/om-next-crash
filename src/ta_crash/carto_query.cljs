(ns ta-crash.carto-query
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async :refer [<! >! put! chan]]
            [clojure.string :as string]))

(enable-console-print!)

(defn carto-query
  [c-sql query done-cb error-cb]
  (println "executing: " query)
  (-> (.execute c-sql query)
    (.done done-cb)
    (.error error-cb)))

(defn execute-query
  [query done-cb error-cb]
  (let [c-sql (js/cartodb.SQL. (clj->js {:user "akilism"}))]
    (carto-query c-sql query done-cb error-cb)))

(defmulti query-builder (fn [type &_] type))

(defmethod query-builder :select
  [_ table cols]
  (str "SELECT " (string/join ", " (map name cols)) " FROM " table))

(defmethod query-builder :select-distinct
  [_ table cols]
  (str "SELECT DISTINCT " (string/join ", " (map name cols)) " FROM " table))

(defmethod query-builder :select-where
  [_ table cols where]
  (str "SELECT " (string/join ", " (map name cols)) " FROM " table " WHERE " where))

(defmethod query-builder :select-distinct-where
  [_ table cols where]
  (str "SELECT DISTINCT " (string/join ", " (map name cols)) " FROM " table " WHERE " where))

(defn build-select
  [distinct? cols table done-cb error-cb]
  (let [c-sql (js/cartodb.SQL. (clj->js {:user "akilism"}))]
    (if distinct?
    (carto-query c-sql (query-builder :select-distinct table cols) done-cb error-cb)
    (carto-query c-sql (query-builder :select table cols) done-cb error-cb))))

(defn build-select-where
  [distinct? cols table where done-cb error-cb]
  (let [c-sql (js/cartodb.SQL. (clj->js {:user "akilism"}))]
    (if distinct?
    (carto-query c-sql (query-builder :select-distinct-where table cols where) done-cb error-cb)
    (carto-query c-sql (query-builder :select-where table cols where) done-cb error-cb))))

(def select-simple
  (partial build-select false ["*"]))

(def select-distinct-simple
  (partial build-select true ["*"]))

(def select-cols
  (partial build-select false))

(def select-distinct-cols
  (partial build-select true))

(def select-where
  (partial build-select-where false))

(def select-distinct-where
  (partial build-select-where true))
