(ns ta-crash.props)

(defn same-props?
  [curr-props next-props]
  (let [curr-area (:active-area curr-props)
        curr-stat (:active-stat curr-props)
        curr-start-date (:start-date curr-props)
        curr-end-date (:end-date curr-props)
        curr-shape (:custom-shape curr-props)
        next-area (:active-area next-props)
        next-stat (:active-stat next-props)
        next-start-date (:start-date next-props)
        next-end-date (:end-date next-props)
        next-shape (:custom-shape next-props)]
    (and
      (= (:area-type curr-area) (:area-type next-area))
      (= (:identifier curr-area) (:identifier next-area))
      (= curr-stat next-stat)
      (= curr-start-date next-start-date)
      (= curr-end-date next-end-date)
      (= curr-shape next-shape)
      (not (nil? next-start-date))
      (not (nil? next-end-date)))))
