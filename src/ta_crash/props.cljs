(ns ta-crash.props)

(defn same-props?
  [curr-props next-props]
  (let [curr-area (:active-area curr-props)
     curr-start-date (:start-date curr-props)
     curr-end-date (:end-date curr-props)
     next-area (:active-area next-props)
     next-start-date (:start-date next-props)
     next-end-date (:end-date curr-props)]
    (and
      (= (:area-type curr-area) (:area-type next-area))
      (= (:identifier curr-area) (:identifier next-area))
      (= curr-start-date next-start-date)
      (= curr-end-date next-end-date)
      (not (nil? next-start-date))
      (not (nil? next-end-date)))))
