(ns snake.state)

(def store
  (atom {:mode "campaign"
         :body '([13 15])
         :direction [-1 0]
         :difficulty-interval (/ 1000 10)
         :difficulty-initial 0
         :difficulty-current 0
         :score 0
         :food [12 15]
         :level-initial 1
         :level-current 0
         :levelplaying? false
         :sessionplaying? false
         }))

(def input
  (atom {:changed? false
         :game-started? false
         :direction [-1 0]}))


(defn add-vecs [[x1 y1] [x2 y2]]
  [(+ x1 x2) (+ y1 y2)])

(defn compute-direction [dir-new dir-old]
  (let [product-x (* (first dir-new) (first dir-old))
        product-y (* (last dir-new) (last dir-old))]
    (if (= 0 (+ product-x product-y))
      dir-new
      dir-old)))

(defn check-borders-or-body? [rect body]
  (let [borders-crossed? (contains? rendering/borders rect)
        body-crossed? (some #{rect} (drop-last body))]
    (if (or borders-crossed? body-crossed?)
      true
      false)))

(defn generate-food [store]
  (rand-nth
    (filter
      (fn [rect]
        (not (or
          (contains? rendering/borders rect)
          (contains? (set (@store :body)) rect))))
      (seq (for [x (range consts/rects-max) y (range consts/rects-max)] [x y])))))

(defn move [store input]
  ())

; TODO
(defn compute [store input]
  (let [
        ]))
