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
          (contains? (set (store :body)) rect))))
      (seq (for [x (range consts/rects-max-count) y (range consts/rects-max-count)] [x y])))))

(defn move [store direction-input]
  (let [direction (compute-direction direction-input (store :direction))
        body (store :body)
        difficulty (store :difficulty-current)
        score (store :score)
        head (add-vecs direction (first body))
        alive? (not (check-borders-or-body? head body))
        food-eaten? (= head (store :food))]
    (assoc store
      :body (cond
              (and alive? food-eaten?) (conj body head)
              (and alive? (not food-eaten?)) (drop-last (conj body head))
              (not alive?) body)
      :direction direction
      :difficulty-current (if (> (count body) 9) (inc difficulty) difficulty)
      :food (if food-eaten? (generate-food store) (store :food))
      :score (if food-eaten? (inc score) score)
      :message (if alive? (store :message) "Game Over")
      :level-playing? alive?
;      :session-playing? alive?)))
    )))

(defn move-to-next-level [store]
  (assoc store
    :body '([13 15])
    :direction [-1 0]
    :difficulty-current (store :difficulty-initial)
    :level-current (inc (store :level-current))
    :food [12 15]
    :message "You passed the level. Prepare to next one"
    :level-playing? false
    ))

(defn try-move [store direction-input]
  (cond
    (and
      (store :session-playing?)
      (store :level-playing?)
      (> (count (store :body)) 4))
    (move-to-next-level store)

    (and
      (store :session-playing?)
      (store :level-playing?)
      (not (> (count (store :body)) 4)))
    (move store direction-input)

    :else store
  ))

(defn start-playing [store]
  (if (and
          (store :session-playing?)
          (not (store :level-playing?)))
    (assoc store
      :level-playing? true
      :message "Play Game")
    store))

(defn set-mode [store mode]
  (assoc store
    :mode mode))

(defn start-new-game [store]
  (assoc store
    :body '([13 15])
    :direction [-1 0]
    :food [12 15]
    :message "Press Space to start"
    :difficulty-current (store :difficulty-initial)
    :level-current (store :level-initial)
    :score 0
    :level-playing? false
    :session-playing? true))

(defn compute [store command]
  (condp = (first command)
    :set-mode (set-mode store (last command))
    :start-new-game (start-new-game store)
    :start-playing (start-playing store)
    :move (try-move store (last command))
    store))
