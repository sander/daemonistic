(ns examples.chat
  (:require [clojure.core.async :refer [go-loop <! close!]])
  (:require [daemonistic.oocsi :as oocsi]))

(def room "#room")

(defn print-incoming [{:keys [sender data]}]
  (println (newline) (str sender "> " (data "message")))
  (println "> ")
  (flush))

(defn send-message [c s]
  (oocsi/send! c room {"message" s})
  (print-incoming {:sender "(you)" :data {"message" s}}))

(defn start [name]
  (let [c (oocsi/connected-client! name)
        s (oocsi/subscribe! c room)]
    (go-loop [] (print-incoming (<! s)) (recur))
    (go-loop []
      (print "> ")
      (flush)
      (let [s (read-line)]
        (case s
          "/quit" (do
                    (oocsi/unsubscribe! c room)
                    (oocsi/disconnect! c)
                    (close! s))
          (do
            (send-message c s)
            (recur)))))))

(defn -main [name & args]
  (println (str "Starting chat as " name "…"))
  (start name))
