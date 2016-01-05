(ns ta-crash.formatter
  (:require [cljs.pprint :as pprint]
            [clojure.string :as str]))

;[{:cyclist_killed 0,
;  :total_crashes 9429,
;  :total_crashes_with_injury 3574,
;  :cyclist_injured 147,
;  :motorist_killed 4,
;  :persons_killed 10,
;  :persons_injured 2168,
;  :total_crashes_with_death 20,
;  :pedestrians_injured 533,
;  :motorist_injured 1488,
;  :pedestrians_killed 6}]

(defn get-stat
  [type count id display]
  {:type type :count count :id id :display display})

(defn set-stat-group
  [[k v]]
  (condp = k
    :cyclist_killed (get-stat :group/default-item v :total-cyclist-killed "Cyclist")
    :cyclist_injured (get-stat :group/default-item v :total-cyclist-injured "Cyclist")
    :motorist_killed (get-stat :group/default-item v :total-motorist-killed "Motorist")
    :motorist_injured (get-stat :group/default-item v :total-motorist-injured "Motorist")
    :pedestrians_killed (get-stat :group/default-item v :total-pedestrian-killed "Pedestrian")
    :pedestrians_injured (get-stat :group/default-item v :total-pedestrian-injured "Pedestrian")
    :persons_killed (get-stat :group/header-item v :total-persons-killed "People killed")
    :persons_injured (get-stat :group/header-item v :total-persons-injured "People injured")
    :total_crashes (get-stat :group/header-item v :total-crashes "Total Crashes")
    :total_crashes_with_death (get-stat :group/sub-header-item v :total-killed "Crashes resulting in a death")
    :total_crashes_with_injury (get-stat :group/sub-header-item v :total-injured "Crashes resulting in an injury")))

(defn for-stat-group
  [raw-row cb & _]
  (let [data (into [] (map set-stat-group (first raw-row)))]
    (cb {:group/items data})))

(defn for-area-menu
  [raw-rows cb area-type]
  (let [items (into [] (map #(assoc % :parent area-type :item-type :sub) raw-rows))]
    (cb {:area/items items})))

(defn factor-id
  [val]
  (str/replace (str/replace val #"[()]" "") #"[ //]" "-"))

(defn get-id
  [type val]
  (condp = type
    :factor (factor-id val)))

(defn for-stat-list
  [raw-rows cb type]
  (let [items (into [] (map (fn [i] {:display (:factor i) :count (:count_factor i) :type :factor :id (get-id :factor (:factor i))}) raw-rows))]
    (cb {:stat-list/items items})))
