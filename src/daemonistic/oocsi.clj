(ns daemonistic.oocsi
  (:require [clojure.core.async :as async])
  (:import (nl.tue.id.oocsi.client OOCSIClient)
           (nl.tue.id.oocsi.client.protocol Handler)
           (nl.tue.id.oocsi.client.socket Base64Coder)))

(defn client [name] (OOCSIClient. name))

(defn client-name [client] (.getName client))

(defn connect!
  ([client] (connect! client "localhost" 4444))
  ([client host port] (.connect client host port)))
(defn disconnect! [client] (.disconnect client))
(defn connected? [client] (.isConnected client))

(defn connected-client! [name]
  (let [c (client name)]
    (if (connect! c) c)))

(defmulti send! #(class %3))
(defmethod send! String
  [client chan val]
  (.send client chan val))
(defmethod send! clojure.lang.PersistentArrayMap
  [client chan val]
  (.send client chan (java.util.HashMap. val)))

(defn channels [client] (.channels client))
(defn clients [client] (.clients client))

;; It was easier to proxy Handler than to gen-class DataHandler
(defn -data-handler [handler]
  (proxy [Handler] []
    (receive [sender data ts channel recipient]
      (handler sender (into {} data) ts channel recipient))))
(defn -chan-data-handler [ch]
  (-data-handler #(async/put! ch {:sender %1
                                  :data %2
                                  :timestamp %3
                                  :channel %4
                                  :recipient %5})))
(defn subscribe!
  ([client channel]
     (let [ch (async/chan)]
       (.subscribe client channel (-chan-data-handler ch))
       ch))
  ([client]
     (let [ch (async/chan)]
       (.subscribe client (-chan-data-handler ch))
       ch)))
(defn unsubscribe! [client channel] (.unsubscribe client channel))
