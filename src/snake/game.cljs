(ns snake.game
  (:require [snake.api :as api]
            [snake.rendering :as rendering]
            [snake.state :as state]))

(def then (atom (api/now!)))

(defn game-loop! []
  (rendering/request-animation-frame! game-loop!)
  (let [now (api/now!)
        elapsed (- now @then)
        fps-interval (/ 1000 10)]
    (cond
      (and (@state/store :levelplaying?) (> elapsed fps-interval))
      (do
        (reset! then (- now (rem elapsed fps-interval)))
        (reset! state/store (state/compute state/store state/input))
; TODO
        ()
        (rendering/draw! state/store))
      (@state/input :changed?)
      (do
        (reset! state/store (state/compute state/store state/input))
        (rendering/draw! state/store)))))


(rendering/resize-canvas!)
(rendering/draw! state/store)
(game-loop!)
