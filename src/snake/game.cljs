(ns snake.game
  (:require [snake.api :as api]
            [snake.rendering :as rendering]
            [snake.state :as state]))

(def store
  (atom {:mode "campaign"
         :body '([13 15])
         :direction [-1 0]
         :difficulty-interval (/ 1000 10)
         :difficulty-initial 0
         :difficulty-current 0
         :score 0
         :food [12 15]
         :level-initial 0
         :level-current 0
         :level-playing? false
         :session-playing? false
         :win-message? false
         }))

(def input
  (atom {:mode "campaign"
         :menu-changed? false
         :difficulty-initial 0
         :level-initial 0
         :level-playing? false
         :session-playing? false
         :direction [-1 0]
         :time-to-move? false}))

(def then (atom (api/now!)))


(defn game-loop! []
  (api/request-animation-frame! game-loop! rendering/canvas)
  (let [now (api/now!)
        elapsed (- now @then)
        fps-interval (/ 1000 10)]
    (reset! store (state/compute store input))
    (rendering/draw! store)
    (swap! input assoc-in [:level-playing?] false)
    (when (and (@store :level-playing?) (> elapsed fps-interval))
      (reset! then (- now (rem elapsed fps-interval)))
      (swap! input assoc-in [:time-to-move?] true)
      (reset! store (state/compute store input))
      (rendering/draw! store)
      (swap! input assoc-in [:time-to-move?] false))
    (when (@input :menu-changed?)
      (swap! input assoc-in [:menu-changed?] false))))


(defn set-direction! [event]
  (let [direction (case (api/keycode! event)
                    38 [0 -1]
                    40 [0 1]
                    37 [-1 0]
                    39 [1 0]
                    nil)]
    (when (some? direction)
      (swap! input assoc-in [:direction] direction)
      (swap! input assoc-in [:level-playing?] true))))

(defn create-listener [key-name extract-value]
  (fn [event]
    (reset!
      input
      (assoc @input
        :menu-changed? true
        key-name (extract-value event)))))

(defn init-listeners! []
  (api/add-event-listener! js/window "resize" rendering/resize-canvas!)
  (api/add-event-listener! js/document "keydown" set-direction!)
  (api/add-event-listener!
    rendering/new-game-button
    "click"
    (create-listener :session-playing? (fn [event] true)))
  (run!
    (fn [x] (api/add-event-listener!
              x
              "click"
              (create-listener
                :mode
                (fn [event] (case (api/dataset-order! (api/current-target! event))
                              "0" "campaign"
                              "1" "free")))))
    (api/query-selector-all! rendering/mode-menu ".pure-menu-item"))
  (run!
    (fn [x] (api/add-event-listener!
              x
              "click"
              (create-listener
                :level-initial
                (fn [event] (api/dataset-order! (api/current-target! event))))))
    (api/query-selector-all! rendering/level-initial-menu ".pure-menu-item"))
  (run!
    (fn [x] (api/add-event-listener!
              x
              "click"
              (create-listener
                :difficulty-initial
                (fn [event] (api/dataset-order! (api/current-target! event))))))
    (api/query-selector-all! rendering/difficulty-initial-menu ".pure-menu-item")))


(rendering/resize-canvas!)
(rendering/draw! store)
(init-listeners!)
(game-loop!)
