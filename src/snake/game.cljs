(ns snake.game
  (:require-macros [cljs.core.async.macros :as m :refer [go]])
  (:require [cljs.core.async :as a :refer [>! <! chan]]
            [snake.api :as api]
            [snake.rendering :as rendering]
            [snake.state :as state]))

(def then (atom (api/now!)))
(def direction (atom [-1 0]))

(def command-chan (chan))

(defn init-store []
  {:mode "campaign"
   :body '([13 15])
   :direction [-1 0]
   :difficulty-initial 0
   :difficulty-current 0
   :food [12 15]
   :level-initial 0
   :level-current 0
   :level-playing? false
   :session-playing? false
   })

(defn game-loop! []
  (go (loop [store-before (init-store)]
        (rendering/draw! store-before)
        (let [command (<! command-chan)
              store (state/compute store-before command)]
          (recur store)))))


(defn set-direction! [event]
  (let [direction-value (case (api/keycode! event)
                          38 [0 -1]
                          40 [0 1]
                          37 [-1 0]
                          39 [1 0]
                          nil)]
    (when (some? direction-value)
      (reset! direction direction-value))))

(defn create-listener [command-name extract-value]
  (fn [event]
;    (go
;      (>! command-chan (list command-name (extract-value event)))
      ))

(defn init-listeners! []
  (api/add-event-listener! js/window "resize" rendering/resize-canvas!)
  (api/add-event-listener! js/document "keydown" set-direction!)
  (api/add-event-listener!
    rendering/new-game-button
    "click"
    (fn [event] (go
                  (>! command-chan (list :start-new-game true)))))
  (run!
    (fn [x] (.log js/console x)
            (api/add-event-listener!
              x
              "click"
              (fn [event]
                (.log js/console event)
                (go
                  (>!
                    command-chan
                    (list
                      :set-mode
                      (case (api/dataset-order! (api/current-target! event))
                        "0" "campaign"
                        "1" "free")))))))
    (api/query-selector-all! rendering/mode-menu ".pure-menu-item"))
;  (run!
;    (fn [x] (api/add-event-listener!
;              x
;              "click"
;              (create-listener
;                :level-initial
;                (fn [event] (api/dataset-order! (api/current-target! event))))))
;    (api/query-selector-all! rendering/level-initial-menu ".pure-menu-item"))
;  (run!
;    (fn [x] (api/add-event-listener!
;              x
;              "click"
;              (create-listener
;                :difficulty-initial
;                (fn [event] (api/dataset-order! (api/current-target! event))))))
;    (api/query-selector-all! rendering/difficulty-initial-menu ".pure-menu-item")))
  )


(rendering/resize-canvas!)
(init-listeners!)
(game-loop!)
