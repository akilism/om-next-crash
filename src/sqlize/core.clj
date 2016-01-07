(ns sqlize.core)
; This is taken from the yesql idea
; https://github.com/krisajenkins/yesql

; (def sample-query "-- name: stats-date
; -- Counts all death / injury stats for a given date range
; SELECT
;   count(c.cartodb_id) as total_crashes,
;   (select count(cartodb_id) from table_20k_crashes where (number_of_cyclist_injured > 0) or (number_of_motorist_injured > 0) or (number_of_pedestrians_injured > 0) or (number_of_persons_injured > 0)) as total_crashes_with_injury,
;   (select count(cartodb_id) from table_20k_crashes where (number_of_cyclist_killed > 0) or (number_of_motorist_killed > 0) or (number_of_pedestrians_killed > 0) or (number_of_persons_killed > 0)) as total_crashes_with_death,
;   sum(c.number_of_cyclist_injured) as cyclist_injured,
;   sum(c.number_of_cyclist_killed) as cyclist_killed,
;   sum(c.number_of_motorist_injured) as motorist_injured,
;   sum(c.number_of_motorist_killed) as motorist_killed,
;   sum(c.number_of_pedestrians_injured) as pedestrians_injured,
;   sum(c.number_of_pedestrians_killed) as pedestrians_killed,
;   sum(c.number_of_persons_injured) as persons_injured,
;   sum(c.number_of_persons_killed) as persons_killed
; FROM
;   table_20k_crashes c
; WHERE
;   (date <= date ':end-date')
; AND
;   (date >= date ':start-date')")

(defn get-fn-name
  [str-name]
  (-> str-name
    (clojure.string/replace #"name: " " ")
    (clojure.string/replace #"--" "")
    clojure.string/trim
    (clojure.string/split #" ")
    first
    clojure.string/trim))

(defn get-doc-string
  [str-doc]
  (-> str-doc
    (clojure.string/replace #"--" "")
    clojure.string/trim))

(def keyword-regex #":[\w-!\?\*]+")

(def keyword-matcher
  (partial re-seq keyword-regex))

(defn get-inputs
  "extracts keywords from query."
  ([query-str]
   (map #(symbol (clojure.string/replace % #":" "")) (keyword-matcher query-str)))
  ([query-str distinct?]
   (if distinct? (distinct (map #(symbol (clojure.string/replace % #":" "")) (keyword-matcher query-str)))
       (get-inputs query-str))))

(defn insert-symbols
  "replaces keywords in query with corresponding symbol."
  [query-str]
  (let [symbols (get-inputs query-str)
        splits (clojure.string/split query-str keyword-regex)]
    (cond
      (> (count symbols) (count splits)) (conj (into [] (interleave (map #(clojure.string/trim %) splits) symbols)) (last symbols))
      (> (count splits) (count symbols)) (conj (into [] (interleave (map #(clojure.string/trim %) splits) symbols)) (last splits))
      :else (into [] (interleave splits symbols)))))

(defn reducer
  [acc v]
  (if (re-find keyword-regex v)
    (assoc acc :seq-query (into [] (concat (:seq-query acc) (insert-symbols v))) :input-params (concat (:input-params acc) (get-inputs v true)))
    (assoc acc :seq-query (conj (:seq-query acc) (clojure.string/trim v)))))

(defn format-inputs
  [inputs]
  (if inputs
    [{:keys inputs}]
    []))

(defmacro def-sql-query [sql-query]
  (let [[str-name# str-doc# & query-lines#] (clojure.string/split-lines (clojure.string/trim sql-query))
      fn-name# (get-fn-name str-name#)
      doc-string# (get-doc-string str-doc#)
      query# (reduce reducer {:input-params [] :seq-query []} query-lines#)
      inputs# (format-inputs (:input-params query#))]
    `(defn ~(symbol fn-name#)
       {:doc ~doc-string#}
       ~inputs#
       (clojure.string/join " " ~(:seq-query query#)))))
