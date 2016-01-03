(ns ta-crash.date-range
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ta-crash.date-picker :as date-picker]))

(defui DateRange
  static om/IQuery
  (query [_]
    [:selected-date-max :selected-date-min :date-max :date-min :cal-date-max :cal-date-min :date-change :month-change])
  Object
  (show-cal
    [this key evt]
    (println (om/props this))
    (om/set-state! this {key (not (key (om/get-state this)))
                         :pos {:x (+ 10 (.-clientX evt)) :y (+ 10 (.-clientY evt))}}))
  (componentWillMount [this]
    (om/set-state! this {:show-max false :show-min false :pos {:x 0 :y 0}}))
  (month-change
    [this key date]
    (let [{:keys [month-change]} (om/props this)]
      ;(println key date)
      (month-change {:key key :date date})))
  (date-change
    [this key date]
    (let [{:keys [date-change]} (om/props this)]
      (date-change {:key key :date (:date date)})))
  (render [this]
    (let [{:keys [date-max date-min cal-date-max cal-date-min selected-date-max selected-date-min]} (om/props this)]
      ;(println "DateRange render: cal-date-max:" cal-date-max)
      (println (:date-change (om/props this)))
      (dom/div #js {:className "date-range-block"}
        (dom/span #js {:className "min-date date-range" :onClick #(.show-cal this :show-min %)} (.format selected-date-min "DD-MM-YYYY"))
        (dom/div #js {:className "date-range-spacer"} " to ")
        (dom/span #js {:className "date-range" :onClick #(.show-cal this :show-max %)} (.format selected-date-max "DD-MM-YYYY"))
        (dom/div #js {:className "min-date cal-holder" :style #js {:top (get-in (om/get-state this) [:pos :y]) :left (get-in (om/get-state this) [:pos :x]) :display (if (:show-min (om/get-state this)) "block" "none")}}
          (date-picker/date-picker
            {:day-change-handler #(.date-change this :selected-date-min %)
             :month-change-handler #(.month-change this :cal-date-min %)
             :date-max date-max
             :date-min date-min
             :selected-date cal-date-min}))
        (dom/div #js {:className "max-date cal-holder" :style #js {:top (get-in (om/get-state this) [:pos :y]) :left (get-in (om/get-state this) [:pos :x]) :display (if (:show-max (om/get-state this)) "block" "none")}}
          (date-picker/date-picker
            {:day-change-handler #(.date-change this :selected-date-max %)
             :month-change-handler #(.month-change this :cal-date-max %)
             :date-max date-max
             :date-min date-min
             :selected-date cal-date-max}))))))

(def date-range (om/factory DateRange))
