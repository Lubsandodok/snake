(ns snake.api)

(defn get-element-by-id! [id]
  (.getElementById js/document id))

(defn get-context! [canvas]
  (.getContext canvas "2d"))

(defn side-length! []
  (.-innerHeight js/window))

(defn set-width! [element value]
  (set! (.-width element) value))

(defn set-height! [element value]
  (set! (.-height element) value))

(defn request-animation-frame! [callback canvas]
  (.requestAnimationFrame js/window callback canvas))

(defn set-color! [ctx color]
  (set! (.-fillStyle ctx) color))

(defn draw-rect! [ctx x y width height]
  (.fillRect ctx x y width height))

(defn query-selector-all! [element selector]
  (.querySelectorAll element selector))

(defn item-from-node-list! [node-list item-number]
  (.item node-list item-number))

; TODO
(defn remove-classes! [element & classes]
  (.remove (.-classList element)) (first classes) (last classes))

(defn add-classes! [element & classes]
  (.add (.-classList element) (first classes)))
