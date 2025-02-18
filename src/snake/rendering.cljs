(ns snake.rendering
  (:require [snake.api :as api]
            [snake.consts :as consts]
            [snake.levels :as levels]
            [snake.state :as state]))

(def canvas (api/get-element-by-id! "canvas"))
(def ctx (api/get-context! canvas))
(def mode-menu (api/get-element-by-id! "mode"))
(def level-initial-menu (api/get-element-by-id! "level-initial"))
(def difficulty-initial-menu (api/get-element-by-id! "difficulty-initial"))
(def message-menu (api/get-element-by-id! "message"))
(def score-menu (api/get-element-by-id! "score"))
(def level-menu (api/get-element-by-id! "level"))
(def new-game-button (api/get-element-by-id! "new-game"))
(def save-game-button (api/get-element-by-id! "save-game"))
(def load-game-button (api/get-element-by-id! "load-game"))


(def mesh
  (for [x (filter odd? (range consts/rects-max-count))
        y (filter odd? (range consts/rects-max-count))]
    [x y]))

(defn ratio! []
  (/ (api/side-length!) consts/rects-max-count))

(defn resize-canvas! []
  (api/set-width! canvas (api/side-length!))
  (api/set-height! canvas (api/side-length!)))

(defn clear-area! []
  (api/set-color! ctx consts/light-blue)
  (api/draw-rect! ctx 0 0 (api/side-length!) (api/side-length!)))

(defn draw-rects! [rects color border-width]
  (loop [[rect & rects] rects]
    (let [x (+ (* (rect 0) (ratio!)) border-width)
          y (+ (* (rect 1) (ratio!)) border-width)
          width (- (ratio!) (* border-width 2))
          height (- (ratio!) (* border-width 2))]
      (api/set-color! ctx color)
      (api/draw-rect! ctx x y width height)
      (if (seq rects) (recur rects)))))

(defn draw-field! [store]
  (clear-area!)
  (draw-rects! mesh consts/dark-blue 0)
  (draw-rects! (levels/choose-borders (store :level-current)) consts/light-grey 0)
  (draw-rects! (store :body) consts/dark-grey 0)
  (draw-rects! (store :body) consts/olive 1)
  (draw-rects! [(store :food)] consts/red 0))

(defn draw-menu! [store menu selected]
  (let [node-list (api/query-selector-all! menu ".pure-menu-item")
        selected-element (api/item-from-node-list!
                            node-list
                            selected)]
    (run! (fn [x] (do
                    (api/remove-class! x "pure-menu-selected")
                    (api/remove-class! x "pure-menu-disabled")))
          node-list)
    (when (store :session-playing?)
      (run! (fn [x] (api/add-class! x "pure-menu-disabled")) node-list))
    (api/add-class! selected-element "pure-menu-selected")))

(defn draw-menus! [store]
  (let [mode-menu-selected (case (store :mode)
                             "campaign" 0
                             "free" 1)]
    (draw-menu! store mode-menu mode-menu-selected)
    (draw-menu! store difficulty-initial-menu (store :difficulty-initial))
    (draw-menu! store level-initial-menu (store :level-initial))))

(defn draw-controls! [store]
  (api/remove-class! new-game-button "pure-button-disabled")
  (api/remove-class! save-game-button "pure-button-disabled")
  (api/remove-class! load-game-button "pure-button-disabled")
  (api/set-inner-text! message-menu (store :message))
  (api/set-inner-text! score-menu (store :score))
  (api/set-inner-text! level-menu (levels/level-name (store :level-current)))
  (when (store :session-playing?)
    (api/add-class! new-game-button "pure-button-disabled")
    (api/add-class! load-game-button "pure-button-disabled"))
  (when (or (not (store :session-playing?)) (store :level-playing?))
    (api/add-class! save-game-button "pure-button-disabled")))

(defn draw! [store]
  (draw-field! store)
  (draw-menus! store)
  (draw-controls! store))
