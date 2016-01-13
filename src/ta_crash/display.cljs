(ns ta-crash.display)

(defmulti get-area-display (fn [_ type] type))

(defmethod get-area-display :borough
  [id _]
  (condp = id
    1 "Manhattan"
    2 "Bronx"
    3 "Brooklyn"
    4 "Queens"
    5 "Staten Island"))

(defmethod get-area-display :city-council
  [id _]
  (str "City Council Dist. " id))

(defmethod get-area-display :community-board
  [id _]
  (str "Community Board Dist. " id))

(defmethod get-area-display :precinct
  [id _]
  (str id " Police Precinct"))

(defmethod get-area-display :default [id _] id)
