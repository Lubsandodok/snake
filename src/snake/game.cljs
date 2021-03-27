(ns snake.game
  (:require-macros [cljs.core.async.macros :as m :refer [go]])
  (:require [cljs.core.async :as a :refer [>! <! chan timeout]]
            [cljs.reader :as reader]
            [snake.api :as api]
            [snake.rendering :as rendering]
            [snake.state :as state]))

(def then (atom (api/now!)))
(def direction (atom [-1 0]))

(def command-chan (chan))

(defn init-store []
  {:mode "campaign"
   :body '([13 15])
   :direction [1 0]
   :difficulty-initial 0
   :difficulty-current 0
   :message "Start or Load Game"
   :food [12 15]
   :level-initial 0
   :level-current 0
   :score 0
   :level-playing? false
   :session-playing? false
   })

(defn save-game! [store]
  (api/local-storage-set-item! "user-data" (pr-str store)))

(defn load-game! []
  (reader/read-string (api/local-storage-get-item! "user-data")))

(defn move-loop! []
  (go (loop []
        (<! (timeout 100))
        (>! command-chan (list :move @direction))
        (recur))))

(defn game-loop! []
  (go (loop [store-before (init-store)]
        (rendering/draw! store-before)
        (let [command (<! command-chan)
              store (state/compute store-before command)]
          (when (= (first command) :save-game)
            (save-game! store))
          (when (= (first command) :load-game)
            ; TODO
            (reset! store (load-game!)))
          (recur store)))))


(defn set-direction! [event]
  (let [keycode (api/keycode! event)
        direction-value (case keycode
                          38 [0 -1]
                          40 [0 1]
                          37 [-1 0]
                          39 [1 0]
                          nil)]
    (when (some? direction-value)
      (reset! direction direction-value))
    (when (= keycode 32)
      (go
        (>! command-chan (list :start-playing true))))))

(defn create-button-listener [command-name]
  (fn [event]
    (when (not= (api/detail! event) 0)
      (go
        (>! command-chan (list command-name true))))))

(defn init-listeners! []
  (api/add-event-listener! js/window "resize" rendering/resize-canvas!)
  (api/add-event-listener! js/document "keydown" set-direction!)
  (api/add-event-listener! rendering/new-game-button "click" (create-button-listener :start-new-game))
  (api/add-event-listener! rendering/save-game-button "click" (create-button-listener :save-game))
  (api/add-event-listener! rendering/load-game-button "click" (create-button-listener :load-game))
  (run!
    (fn [x] (api/add-event-listener!
              x
              "click"
              (fn [event]
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
(move-loop!)
(game-loop!)
