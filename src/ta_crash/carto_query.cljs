(ns ta-crash.carto-query
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async :refer [<! >! put! chan]]
            [clojure.string :as string]))

(defn carto-query
  [c-sql query done-cb error-cb]
  (println "executing: " query)
  (-> (.execute c-sql query)
    (.done done-cb)
    (.error error-cb)))

;(defn carto-query
;  [c-sql query]
;  (println "executing: " query)
;  (go (let [promise (.execute c-sql query)
;            response (<! (.done promise))
;            error (<! (.error promise))]
;        {:response response :error error})))


(defmulti query-builder (fn [type &_] type))

(defmethod query-builder :select
  [_ table cols]
  (str "SELECT " (string/join ", " (map name cols)) " FROM " table))

(defmethod query-builder :select-distinct
  [_ table cols]
  (str "SELECT DISTINCT " (string/join ", " (map name cols)) " FROM " table))

(defmethod query-builder :select-where
  [_ table cols where]
  (str "SELECT " (string/join ", " (map name cols)) " FROM " table))

(defmethod query-builder :select-distinct-where
  [_ table cols where]
  (str "SELECT DISTINCT " (string/join ", " (map name cols)) " FROM " table))

(defn select
  [distinct? cols table done-cb error-cb]
  (let [c-sql (js/cartodb.SQL. (clj->js {:user "akilism"}))]
    (if distinct?
    (carto-query c-sql (query-builder :select-distinct table cols) done-cb error-cb)
    (carto-query c-sql (query-builder :select table cols) done-cb error-cb))))

(defn select-where
  [distinct? cols table done-cb error-cb]
  (let [c-sql (js/cartodb.SQL. (clj->js {:user "akilism"}))]
    (if distinct?
    (carto-query c-sql (query-builder :select-distinct-where table cols) done-cb error-cb)
    (carto-query c-sql (query-builder :select-where table cols) done-cb error-cb))))

(def select-simple
  (partial select false ["*"]))

(def select-distinct-simple
  (partial select true ["*"]))

(def select-cols
  (partial select false))

(def select-distinct-cols
  (partial select true))

(def select-simple-where
  (partial select-where false))

(def select-distinct-simple-where
  (partial select-where true))
