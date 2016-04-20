(ns ta-crash.conversion
  (:require [clojure.string :as string]))

(defn normalize
  [text]
  (-> text
    string/lower-case
    (string/replace #" " "-")
    (string/replace #"\." "")))

(defn denormalize
  [text]
  (-> text
    string/capitalize
    (string/replace #"-" " ")))

(defn cb-identifier
  [cb modifier]
  (let [parts (.split cb "cb")
        cb-num (.parseInt js/window (nth parts 1))]
    (+ modifier cb-num)))


(defmulti convert-type (fn [_ type] type))

(defmethod convert-type :borough
  [id _]
  (condp = id
    1 "Manhattan"
    2 "Bronx"
    3 "Brooklyn"
    4 "Queens"
    5 "Staten Island"))

(defmethod convert-type :borough-rev
  [borough _]
  (condp = (normalize borough)
    "manhattan" 1
    "bronx" 2
    "brooklyn" 3
    "queens" 4
    "staten-island" 5))


(defmethod convert-type :city-council
  [id _]
  (str "City Council Dist. " id))

(defmethod convert-type :city-council-rev
  [district _]
  (string/replace (normalize district) #"city-council-dist-" ""))


(defmethod convert-type :community-board
  [id _]
  (cond
    (< id 200) (str "Manhattan CB" (- id 100))
    (< id 300) (str "Bronx CB" (- id 200))
    (< id 400) (str "Brooklyn CB" (- id 300))
    (< id 500) (str "Queens CB" (- id 400))
    (< id 600) (str "Staten Island CB" (- id 500))))

(defmethod convert-type :community-board-rev
  [cb _]
  (let [norm-cb (normalize cb)]
    (cond
    (>= (.indexOf norm-cb "manhattan") 0) (cb-identifier norm-cb 100)
    (>= (.indexOf norm-cb "bronx") 0) (cb-identifier norm-cb 200)
    (>= (.indexOf norm-cb "brooklyn") 0) (cb-identifier norm-cb 300)
    (>= (.indexOf norm-cb "queens") 0) (cb-identifier norm-cb 400)
    (>= (.indexOf norm-cb "staten") 0) (cb-identifier norm-cb 500))))


(defmethod convert-type :precinct
  [id _]
  (str id " Police Precinct"))

(defmethod convert-type :precinct-rev
  [precinct _]
  (string/replace (normalize precinct) #"-police-precinct" ""))

(defn is-hyphenated?
  [neighborhood]
  (condp = neighborhood
    "bedford-stuyvesant" "Bedford-Stuyvesant"
    "co-op-city" "Co-op City"
    "prospect-lefferts-gardens" "Prospect-Lefferts Gardens"
    "green-wood-cemetery" "Green-Wood Cemetery"
    false))


(defmethod convert-type :neighborhood-rev
  [neighborhood _]
  (if-let [converted-neighborhood (is-hyphenated? neighborhood)]
    converted-neighborhood
    (string/join " " (map denormalize (string/split neighborhood #"-")))))


(defmethod convert-type :default [id _] id)
