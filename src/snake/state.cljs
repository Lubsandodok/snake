(ns snake.state
  (:require [snake.consts :as consts]))


; TODO make levels
(def borders
  (set
    (concat
      (for [x (range consts/rects-max-count) y [0]] [x y])
      (for [x (range consts/rects-max-count) y [(- consts/rects-max-count 1)]] [x y])
      (for [x [0] y (range consts/rects-max-count)] [x y])
      (for [x [(- consts/rects-max-count 1)] y (range consts/rects-max-count)] [x y]))))

(defn add-vecs [[x1 y1] [x2 y2]]
  [(+ x1 x2) (+ y1 y2)])

(defn compute-direction [dir-new dir-old]
  (let [product-x (* (first dir-new) (first dir-old))
        product-y (* (last dir-new) (last dir-old))]
    (if (= 0 (+ product-x product-y))
      dir-new
      dir-old)))

(defn check-borders-or-body? [rect body]
  (let [borders-crossed? (contains? borders rect)
        body-crossed? (some #{rect} (drop-last body))]
    (if (or borders-crossed? body-crossed?)
      true
      false)))

(defn generate-food [store]
  (rand-nth
    (filter
      (fn [rect]
        (not (or
          (contains? borders rect)
          (contains? (set (@store :body)) rect))))
      (seq (for [x (range consts/rects-max-count) y (range consts/rects-max-count)] [x y])))))

(defn move [store input]
  (let [direction (compute-direction (@input :direction) (@store :direction))
        body (@store :body)
        head (add-vecs direction (first body))
        win-level? (> (count body) 16)
        level-playing? (not (or win-level? (check-borders-or-body? head body)))
        food-eaten? (= head (@store :food))]
    (assoc @store
      :body (cond
              (and level-playing? food-eaten?) (conj body head)
              (and level-playing? (not food-eaten?)) (drop-last (conj body head))
              (not level-playing?) body)
      :direction direction
      :difficulty-current 0
      :food (if food-eaten? (generate-food store) (@store :food))
      :level-playing? level-playing?
      :session-playing? (if level-playing? true win-level?))))

(defn set-mode [store mode]
  (assoc store
    :mode mode))

(defn compute [store command]
  (println "Command" command)
  (condp = (first command)
    :set-mode (set-mode store (last command))
    store))
