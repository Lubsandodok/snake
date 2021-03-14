(ns snake.api)

(defn get-element-by-id! [id]
  (.getElementById js/document id))

(defn get-context! [canvas]
  (.getContext canvas "2d"))

(defn now! []
  (.now js/Date))

(defn add-event-listener! [element action-name action-func]
  (.addEventListener element action-name action-func))

(defn keycode! [event]
  (.-keyCode event))

(defn current-target! [event]
  (.-currentTarget event))

(defn dataset-order! [element]
  (.-order (.-dataset element)))

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

(defn remove-class! [element class-name]
  (.remove (.-classList element) class-name))

(defn add-class! [element class-name]
  (.add (.-classList element) class-name))
