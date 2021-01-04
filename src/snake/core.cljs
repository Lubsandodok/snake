(ns snake.core)

(def canvas (.getElementById js/document "canvas"))
(def ctx (.getContext canvas "2d"))

(def height 30)
(def width 30)
(def ratio 20)
(def height-real (* height ratio))
(def width-real (* width ratio))

(def borders
  (set
    (concat
      (for [x (range width) y [0]] [x y])
      (for [x (range width) y [(- height 1)]] [x y])
      (for [x [0] y (range height)] [x y])
      (for [x [(- width 1)] y (range height)] [x y]))))

(def then (atom (.now js/Date)))

(def store
  (atom {:body '([13 15])
         :direction-new [-1 0]
         :direction-old [-1 0]
         :fps-interval (/ 1000 5)
         :food [12 15]
         :playing? true}))


(defn add-vecs [[x1 y1] [x2 y2]]
  [(+ x1 x2) (+ y1 y2)])

(defn draw-rects! [rects color border-width]
  (loop [[rect & rects] rects]
    (let [x (+ (* (rect 0) ratio) border-width)
          y (+ (* (rect 1) ratio) border-width)
          width (- ratio (* border-width 2))
          height (- ratio (* border-width 2))]
      (set! (.-fillStyle ctx) color)
      (.fillRect ctx x y width height)
      (if (seq rects) (recur rects)))))

(defn clear-area! []
  (set! (.-fillStyle ctx) "white")
  (.fillRect ctx 0 0 width-real height-real))

(defn compute-direction [dir-new dir-old]
  (let [product-x (* (first dir-new) (first dir-old))
        product-y (* (last dir-new) (last dir-old))]
    (if (= 0 (+ product-x product-y))
      dir-new
      dir-old)))

(defn check-borders-or-body? [rect body]
  (let [borders-crossed? (contains? borders rect)
        body-crossed? (some #{rect} body)]
    (if (or borders-crossed? body-crossed?)
      true
      false)))

(defn generate-food [store]
  (loop [food [(rand-int width) (rand-int height)]]
    (if (check-borders-or-body? food (@store :body))
      (recur [(rand-int width) (rand-int height)])
      food)))

(defn compute [store]
  (let [direction (compute-direction (@store :direction-new) (@store :direction-old))
        body (@store :body)
        head (add-vecs direction (first body))
        playing? (not (check-borders-or-body? head body))
        food-eaten? (= head (@store :food))]
    {:body (cond
             (and playing? food-eaten?) (conj body head)
             (and playing? (not food-eaten?)) (drop-last (conj body head))
             (not playing?) body)
     :direction-new direction
     :direction-old direction
     :fps-interval (/ 1000 5)
     :food (if food-eaten? (generate-food store) (@store :food))
     :playing? playing?}))

(defn move! [store]
  (clear-area!)
  (draw-rects! borders "black" 0)
  (reset! store (compute store))
  (draw-rects! (@store :body) "black" 0)
  (draw-rects! (@store :body) "red" 1)
  (draw-rects! [(@store :food)] "green" 0))

(defn game-loop! []
  (when (@store :playing?)
    ((.requestAnimationFrame js/window game-loop! canvas)
     (let [now (.now js/Date)
           elapsed (- now @then)
           fps-interval (@store :fps-interval)]
       (when (> elapsed fps-interval)
         (reset! then (- now (rem elapsed fps-interval)))
         (move! store))))))


(defn set-direction! [event]
  (swap! store assoc-in [:direction-new]
   (case (.-keyCode event)
    38 [0 -1]
    40 [0 1]
    37 [-1 0]
    39 [1 0])))


(.addEventListener js/document "keydown" set-direction!)
(game-loop!)
