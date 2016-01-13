(ns ta-crash.header
(:require [cljs.pprint :as pprint]
          [om.next :as om :refer-macros [defui]]
          [om.dom :as dom]
          [ta-crash.display :as display]
          [ta-crash.date-range :as date-range]))

(defui Header
  static om/IQuery
  (query [_]
    '[:active-area :selected-date-max :selected-date-min :cal-date-max :cal-date-min :date-max :date-min])
  Object
  (render [this]
    (let [{:keys [selected-date-max selected-date-min cal-date-max cal-date-min date-max date-min active-area]} (om/props this)
          {:keys [area-type identifier]} active-area]
      (dom/div #js {:className "header"}
      (dom/h1 #js {:className "area-header"} (display/get-area-display identifier area-type))
      (when (and selected-date-max selected-date-min)
          (date-range/date-range (om/computed {:date-max date-max
                                  :date-min date-min
                                  :cal-date-max cal-date-max
                                  :cal-date-min cal-date-min
                                  :selected-date-max selected-date-max
                                  :selected-date-min selected-date-min }
                                  {:date-change (:date-change (om/get-computed this))
                                   :month-change (:month-change (om/get-computed this))})))))))

(def header (om/factory Header))
