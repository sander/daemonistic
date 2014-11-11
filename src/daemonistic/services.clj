(ns daemonistic.services
  (:require [daemonistic.oocsi :as oocsi]
            [clojure.core.async :as async :refer [chan sliding-buffer put! alts! close! go-loop <! close! timeout go >!]]))

(defn- millis [] (System/currentTimeMillis))
(defn- interval [t]
  (let [ch (chan)]
    (go (while (>! ch true) (<! (timeout t))))
    ch))

(defn- defaults [fn client name]
  (let [out (chan (sliding-buffer 1))
        stop (chan)]
    (fn client name 1000 out stop)
    {:out out :stop stop}))
(defn listen!
  "Calls callback with every new output value from service."
  [service callback]
  (go
    (while (let [val (<! (:out service))]
             (if val
               (do
                 (callback val)
                 true)
               false)))))
(defn stop!
  "Stops a service and closes its channels."
  [service]
  (put! (:stop service) true)
  (close! (:stop service))
  (close! (:out service)))

(defn- tick-from [msg] (get-in  msg [:data "tick"] -1))
(defn- unsubscribe! [client channel-id channel-in]
  (oocsi/unsubscribe! client channel-id)
  (close! channel-in))

(defn simple-sync!
  "Synchronizes client on channel-id with a time interval.
  Counts on the out channel until a stop signal."
  ([client channel-id interval out stop]
   (let [in (oocsi/subscribe! client channel-id)
         tick! (fn [k]
                 (oocsi/send! client channel-id {"tick" k})
                 (put! out k)
                 k)]
     (go-loop [n 0, trigger (timeout 0)]
       (let [[msg ch] (alts! [in trigger stop])]
         (condp = ch
           in (let [other-n (tick-from msg)]
                (if (> other-n n)
                  (recur (tick! other-n) (timeout interval))
                  (recur n trigger)))
           trigger (recur (tick! (inc n)) (timeout interval))
           stop (unsubscribe! client channel-id in))))))
  ([client] (defaults simple-sync! client "sync")))

(defn- purge [t timeout times]
  (let [limit (- t timeout)
        timed-out (for [[id t] times :when (< t limit)] id)]
    (apply dissoc times timed-out)))
(defn- shout! [client channel-id]
  (oocsi/send! client channel-id {"id" (.getName client)}))
(defn- update-ids! [client out times]
  (>! out (apply sorted-set (.getName client) (keys times))))

(defn identify!
  "Identifies client on channel-id every ival msecs.
  Sends an up-to-date sorted-set of participants to out, until a stop signal."
  ([client channel-id ival out stop]
   (let [in (oocsi/subscribe! client channel-id)
         check (interval ival)]
     (go-loop [times {}]
       (update-ids! client out times)
       (let [[msg c] (alts! [in check stop])]
         (condp = c
           in (let [other (get-in msg [:data "id"])]
                (recur (assoc times other (millis))))
           check (do
                   (shout! client channel-id)
                   (recur (purge (millis) ival times)))
           stop (do
                  (unsubscribe! client channel-id in)
                  (close! check)))))))
  ([client] (defaults identify! client "identify")))
