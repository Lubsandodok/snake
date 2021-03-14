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

(defn compute-menu [store input]
  (assoc @store
    :mode (@input :mode)
    :level-initial (@input :level-initial)
    :difficulty-initial (@input :difficulty-initial)))

; TODO
(defn start-game [store input]
  (assoc @store
    :body '([13 15])
    :direction [-1 0]
    :difficulty-current (@store :difficulty-initial)
    :food [12 15]
    :level-current (@store :level-initial)
    :level-playing? false
    :session-playing? true))

(defn prepare-new-level [store input]
  (assoc @store
    :body '(13 15)
    :direction [-1 0]
    :difficulty-current (@store :difficulty-initial)
    :food [12 15]
    :level-current (+ (@store :level-current) 1)))

(defn start-new-level [store input]
  (assoc @store
    :level-playing? true))

(defn compute [store input]
  (let [level-playing? (@store :level-playing?)
        session-playing? (@store :session-playing?)
        menu-session-playing? (@input :session-playing?)
        menu-level-playing? (@input :level-playing?)
        menu-changed? (@input :menu-changed?)
        time-to-move? (@input :time-to-move?)]
    (cond
      (and
        menu-changed?
        (not session-playing?)
        (not menu-session-playing?))
      (compute-menu store input)

      (and
        menu-changed?
        (not session-playing?)
        menu-session-playing?)
      (start-game store input)

      (and
        (not menu-changed?)
        session-playing?
        (not level-playing?)
        menu-level-playing?)
      (start-new-level store input)

      (and
        (not menu-changed?)
        session-playing?
        level-playing?
        time-to-move?)
      (move store input)

      (and
        (not menu-changed?)
        session-playing?
        (not level-playing?)
        (not menu-level-playing?))
      (prepare-new-level store input)

      :else @store)))
