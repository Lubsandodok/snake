(ns snake.core
  (:require [snake.consts :as consts]
            [snake.rendering :as rendering]))

(def then (atom (.now js/Date)))

(def store
  (atom {:body '([13 15])
         :direction-new [-1 0]
         :direction-old [-1 0]
         :fps-interval (/ 1000 10)
         :food [12 15]
         :playing? true}))


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
     :fps-interval (/ 1000 10)
     :food (if food-eaten? (generate-food store) (@store :food))
     :playing? playing?}))

(defn move! [store]
  (reset! store (compute store))
  (rendering/draw-field! store))

(defn game-loop! []
  (when (@store :playing?)
    ((rendering/request-animation-frame! game-loop!)
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
(.addEventListener js/window "resize" rendering/resize-canvas!)
(rendering/resize-canvas!)
(game-loop!)
