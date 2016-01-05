(ns ta-crash.date-range
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ta-crash.date-picker :as date-picker]))

(defui DateRange
  Object
  (show-cal
    [this key evt]
    (condp = key
      :show-max (om/update-state! this merge {key (not (key (om/get-state this)))
                                              :show-min false
                                              :pos {:x (+ 10 (.-clientX evt)) :y (+ 10 (.-clientY evt))}})
      :show-min (om/update-state! this merge {key (not (key (om/get-state this)))
                                              :show-max false
                                              :pos {:x (+ 10 (.-clientX evt)) :y (+ 10 (.-clientY evt))}})))
  (componentWillMount [this]
    (om/set-state! this {:show-max false :show-min false :pos {:x 0 :y 0}}))
  (month-change
    [this key date]
    (let [{:keys [month-change]} (om/get-computed this)]
      (month-change {:key key :date date})))
  (date-change
    [this key date]
    (let [{:keys [date-change]} (om/get-computed this)]
      (date-change {:key key :date (:date date)})
      (om/update-state! this merge {:show-min false :show-max false})))
  (render [this]
    (let [{:keys [date-max date-min cal-date-max cal-date-min selected-date-max selected-date-min]} (om/props this)]
      (dom/div #js {:className "date-range-block"}
        (dom/div #js {:className "date-range-spacer"} "Crashes from ")
        (dom/span #js {:className "min-date date-range" :onClick #(.show-cal this :show-min %)} (.format selected-date-min "Do MMM YYYY"))
        (dom/div #js {:className "date-range-spacer"} " to ")
        (dom/span #js {:className "date-range" :onClick #(.show-cal this :show-max %)} (.format selected-date-max "Do MMM YYYY"))
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
