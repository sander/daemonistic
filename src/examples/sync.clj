(ns examples.sync
  (:require [clojure.core.async :refer [go <! timeout]]
            [daemonistic.oocsi :refer [connected-client!]]
            [daemonistic.services :refer [simple-sync! listen!]]
            [quil.core :as q])
  (:gen-class))

(def user (str "client" (rand-int 255)))
(def client (connected-client! (str "client" (rand-int 255))))
(def color (atom 0))

(defn flash! [_]
  (go
    (reset! color 255)
    (<! (timeout 200))
    (reset! color 0)))

(listen! (simple-sync! client) flash!)

(defn -main [& args]
  (q/sketch :title user
            :draw #(q/background @color 0 0)
            :size [100 100]))
