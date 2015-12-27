(ns ta-crash.value
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(defui Value
  Object
  (render [this]
    (let [{:keys [value type]} (om/props this)]
      (dom/span #js {:className (str (name type) "-value value")} (str value " ")))))

(def value (om/factory Value))
