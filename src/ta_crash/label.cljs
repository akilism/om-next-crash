(ns ta-crash.label
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(defui Label
  Object
  (render [this]
    (let [{:keys [text type]} (om/props this)]
      (dom/span #js {:className (str (name type) "-label label")} (str " " text)))))

(def label (om/factory Label))
