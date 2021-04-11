(ns snake.state
  (:require [snake.consts :as consts]
            [snake.levels :as levels]))

(defn compute-difficulty-timeout [difficulty]
  (condp = difficulty
    0 150
    1 125
    2 100
    150))

(defn add-vecs [[x1 y1] [x2 y2]]
  [(+ x1 x2) (+ y1 y2)])

(defn compute-direction [dir-new dir-old]
  (let [product-x (* (first dir-new) (first dir-old))
        product-y (* (last dir-new) (last dir-old))]
    (if (= 0 (+ product-x product-y))
      dir-new
      dir-old)))

(defn check-borders-or-body? [rect body level]
  (let [borders-crossed? (contains? (levels/choose-borders level) rect)
        body-crossed? (some #{rect} (drop-last body))]
    (if (or borders-crossed? body-crossed?)
      true
      false)))

(defn generate-food [store]
  (rand-nth
    (filter
      (fn [rect]
        (not (or
          (contains? (levels/choose-borders (store :level-current)) rect)
          (contains? (set (store :body)) rect))))
      (seq (for [x (range consts/rects-max-count) y (range consts/rects-max-count)] [x y])))))

(defn compute-difficulty [store]
  (let [length (count (store :body))
        difficulty (store :difficulty-current)]
    (if (= (store :mode) "campaign")
      (cond
        (and (> length 0) (<= length 5)) 0
        (and (> length 5) (<= length 10)) 1
        (and (> length 10) (<= length 15)) 2
        :default difficulty)
      difficulty)))

(defn move [store direction-input]
  (let [direction (compute-direction direction-input (store :direction))
        body (store :body)
        score (store :score)
        head (add-vecs direction (first body))
        alive? (not (check-borders-or-body? head body (store :level-current)))
        food-eaten? (= head (store :food))]
    (assoc store
      :body (cond
              (and alive? food-eaten?) (conj body head)
              (and alive? (not food-eaten?)) (drop-last (conj body head))
              (not alive?) body)
      :direction direction
      :difficulty-current (compute-difficulty store)
      :food (if food-eaten? (generate-food store) (store :food))
      :score (if food-eaten? (inc score) score)
      :message (if alive? (store :message) "Game Over")
      :level-playing? alive?
      :session-playing? alive?)))

(defn move-to-next-level [store]
  (let [level (store :level-current)]
    (if (>= level 2)
      (assoc store
        :level-playing? false
        :session-playing? false
        :message "You won! Campaign is over!")
      (assoc store
        :body (list (levels/choose-body level))
        :direction [-1 0]
        :difficulty-current (store :difficulty-initial)
        :level-current (inc level)
        :food (levels/choose-food level)
        :message "You passed the level. Prepare to next one"
        :level-playing? false))))

(defn try-move [store direction-input]
  (cond
    (and
      (store :session-playing?)
      (store :level-playing?)
      (= (store :mode) "campaign")
      (> (count (store :body)) 16))
    (move-to-next-level store)

    (and
      (store :session-playing?)
      (store :level-playing?)
      (= (store :mode) "campaign")
      (not (> (count (store :body)) 16)))
    (move store direction-input)

    (and
      (store :session-playing?)
      (store :level-playing?)
      (= (store :mode) "free"))
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
  (if (not (store :session-playing?))
    (assoc store :mode mode)
    store))

(defn set-level-initial [store level-initial]
  (if (not (store :session-playing?))
    (assoc store :level-initial level-initial)
    store))

(defn set-difficulty-initial [store difficulty-initial]
  (if (not (store :session-playing?))
    (assoc store :difficulty-initial difficulty-initial)
    store))

(defn start-new-game [store]
  (let [level (store :level-initial)]
    (assoc store
      :body (list (levels/choose-body level))
      :direction [-1 0]
      :food (levels/choose-food level)
      :message "Press Space to start"
      :difficulty-current (store :difficulty-initial)
      :level-current level
      :score 0
      :level-playing? false
      :session-playing? true)))

(defn compute [store command]
  (condp = (first command)
    :set-mode (set-mode store (last command))
    :set-level-initial (set-level-initial store (last command))
    :set-difficulty-initial (set-difficulty-initial store (last command))
    :start-new-game (start-new-game store)
    :start-playing (start-playing store)
    :move (try-move store (last command))
    store))
