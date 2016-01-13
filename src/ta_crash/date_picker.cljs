(ns ta-crash.date-picker
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(defn days-in-month
  ([date] (let [days-in-month (.daysInMonth date)]
    (range 1 (+ 1 days-in-month))))
  ([date formatter] (let [days-in-month (.daysInMonth date)]
    (map #(formatter date %) (range 1 (+ 1 days-in-month))))))

(defn start-of-month
  ([date] (.startOf date "month"))
  ([date formatter] (formatter (.startOf date "month"))))

(defn end-of-month
  ([date] (.endOf date "month"))
  ([date formatter] (formatter (.endOf date "month"))))

(defn zero [_] 0)

(defn prev-month-days
  [start-of-month]
  (map zero (range (.day start-of-month))))

(defn next-month-days
  [end-of-month]
  (map zero (range (- 7 (.day end-of-month)))))

(defn to-moment
  [date day]
  (.date (.clone date) day))

(defn get-month
  [date]
  (let [days (days-in-month date to-moment)
        start-of-month (start-of-month (.clone date))
        end-of-month (end-of-month (.clone date))]
    (partition 7 7 (repeatedly zero) (concat (prev-month-days start-of-month) days))))

(defn get-cell
  [day-change-handler day]
  (if (= 0 day)
    (dom/td #js {:className "calendar-day inactive"} "X")
    (dom/td #js {:className "calendar-day active" :onClick #(day-change-handler {:key :picker :date day})} (.format day "DD"))))

(defn get-row
  [day-change-handler days]
  (apply dom/tr #js {:className "calendar-week"}
    (map #(get-cell day-change-handler %) days)))

(defn get-month-cells
  [day-change-handler all-days]
  [(dom/tr #js {:className "calendar-week-header"}
    (dom/td #js {:className "calendar-day-of-week"} "S")
    (dom/td #js {:className "calendar-day-of-week"} "M")
    (dom/td #js {:className "calendar-day-of-week"} "T")
    (dom/td #js {:className "calendar-day-of-week"} "W")
    (dom/td #js {:className "calendar-day-of-week"} "T")
    (dom/td #js {:className "calendar-day-of-week"} "F")
    (dom/td #js {:className "calendar-day-of-week"} "S"))
  (map #(get-row day-change-handler %) all-days)])

(defui DatePicker
  Object
  (get-prev
    [this date-a date-b month-change-handler]
    (if-not (.isBefore date-a date-b "month")
      (dom/a #js {:className "month-prev" :onClick #(month-change-handler date-a) :dataMonth (.format date-a "YYYY-MM-DD")} "<")
      ""))
  (get-next
    [this date-a date-b month-change-handler]
    (if (or (.isBefore date-a date-b "month") (.isSame date-a date-b "month"))
      (dom/a #js {:className "month-next" :onClick #(month-change-handler date-a)} ">")
      ""))
  (draw-cal
    [this date date-max date-min month-change-handler day-change-handler]
    (let [calendar (get-month-cells day-change-handler (get-month date))]
      (dom/table #js {:className "calendar"}
        (dom/thead nil
          (dom/tr #js {:className "calendar-header-row"}
            (dom/td nil (.get-prev this (.subtract (.clone date) 1 "month") date-min month-change-handler))
            (dom/td #js {:className "calendar-header-month" :colSpan 5} (.format date "MMM"))
            (dom/td nil (.get-next this (.add (.clone date) 1 "month") date-max month-change-handler))))
        (dom/tbody nil calendar))))
  (render [this]
    (let [{:keys [date-max date-min selected-date month-change-handler day-change-handler]} (om/props this)]
      (dom/div nil
        (when selected-date
          (.draw-cal this selected-date date-max date-min month-change-handler day-change-handler))))))

(def date-picker (om/factory DatePicker))
