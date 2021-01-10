(ns snake.rendering
  (:require [snake.consts :as consts]))

(def canvas (.getElementById js/document "canvas"))
(def ctx (.getContext canvas "2d"))

(def borders
  (set
    (concat
      (for [x (range consts/rects-max) y [0]] [x y])
      (for [x (range consts/rects-max) y [(- consts/rects-max 1)]] [x y])
      (for [x [0] y (range consts/rects-max)] [x y])
      (for [x [(- consts/rects-max 1)] y (range consts/rects-max)] [x y]))))

(def mesh
  (for [x (filter odd? (range consts/rects-max))
        y (filter odd? (range consts/rects-max))]
    [x y]))


(defn side-length! []
  (.-innerHeight js/window))

(defn ratio! []
  (/ (side-length!) consts/rects-max))

(defn resize-canvas! []
  (set! (.-width canvas) (side-length!))
  (set! (.-height canvas) (side-length!)))

(defn request-animation-frame! [callback]
  (.requestAnimationFrame js/window callback canvas))

(defn clear-area! []
  (set! (.-fillStyle ctx) consts/light-blue)
  (.fillRect ctx 0 0 (side-length!) (side-length!)))

(defn draw-rects! [rects color border-width]
  (loop [[rect & rects] rects]
    (let [x (+ (* (rect 0) (ratio!)) border-width)
          y (+ (* (rect 1) (ratio!)) border-width)
          width (- (ratio!) (* border-width 2))
          height (- (ratio!) (* border-width 2))]
      (set! (.-fillStyle ctx) color)
      (.fillRect ctx x y width height)
      (if (seq rects) (recur rects)))))

(defn draw-field! [store]
  (clear-area!)
  (draw-rects! mesh consts/dark-blue 0)
  (draw-rects! borders consts/light-grey 0)
  (draw-rects! (@store :body) consts/dark-grey 0)
  (draw-rects! (@store :body) consts/olive 1)
  (draw-rects! [(@store :food)] consts/red 0))
