(ns ta-crash.carto-query
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async :refer [<! >! put! chan]]
            [clojure.string :as string]))

(enable-console-print!)

(defn carto-query
  [c-sql query done-cb error-cb]
  ;(println "executing: " query)
  (-> (.execute c-sql query)
    (.done done-cb)
    (.error error-cb)))

(defn execute-query
  [query done-cb error-cb]
  (let [c-sql (js/cartodb.SQL. (clj->js {:user "akilism"}))]
    (carto-query c-sql query done-cb error-cb)))
