(ns ta-crash.item
  (:require [ta-crash.label :as label]
            [ta-crash.value :as value]))

(defn static-item
  [count display id type elem]
  (let [type-keyword (keyword type)
        type-name (name type)
        id-name (name id)
        classes (str id-name "-" type-name " " type-name " item")]
        (elem #js {:className classes}
        (value/value {:value count :type type-keyword}) (label/label {:text display :type type-keyword}))))

(defn clickable-item
  [count display id type elem item-click-handler]
  (let [type-keyword (keyword type)
        type-name (name type)
        id-name (name id)
        classes (str id-name "-" type-name " " type-name " item")]
    (elem #js {:className classes
               :onClick #(item-click-handler {:key type-keyword :id id})}
      (value/value {:value count :type type-keyword}) (label/label {:text display :type type-keyword}))))
