(ns snake.levels
  (:require [snake.consts :as consts]))

; TODO add second and third levels
(def borders-first
  (set
    (concat
      (for [x (range consts/rects-max-count) y [0]] [x y])
      (for [x (range consts/rects-max-count) y [(- consts/rects-max-count 1)]] [x y])
      (for [x [0] y (range consts/rects-max-count)] [x y])
      (for [x [(- consts/rects-max-count 1)] y (range consts/rects-max-count)] [x y]))))

(def borders-second
  (conj borders-first [20 20]))

(def borders-third
  (conj borders-second [20 21]))


(defn level-name [level]
  (case level
    0 "First"
    1 "Second"
    2 "Third"))

(defn choose-borders [level]
  (case level
    0 borders-first
    1 borders-second
    2 borders-third))

(defn choose-body [level]
  (case level
    0 [13 15]
    1 [13 15]
    2 [13 15]))

(defn choose-food [level]
  (case level
   0 [12 15]
   1 [12 15]
   2 [12 15]))
