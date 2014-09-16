(ns daemonistic.oocsi
  (:require [clojure.core.async :as async])
  (:import (nl.tue.id.oocsi.client OOCSIClient)
           (nl.tue.id.oocsi.client.protocol Handler)
           (nl.tue.id.oocsi.client.socket Base64Coder)))

(defn client [name] (OOCSIClient. name))

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
    (receive [sender data ts]
      (let [bais (java.io.ByteArrayInputStream. (Base64Coder/decode data))
            ois (java.io.ObjectInputStream. bais)
            output (.readObject ois)]
        (handler sender (into {} output) ts)))))
(defn subscribe! [client channel]
  (let [ch (async/chan)]
    (.subscribe
     client channel
     (-data-handler
      #(async/put! ch {:sender %1
                       :data %2
                       :timestamp %3})))
    ch))
(defn unsubscribe! [client channel] (.unsubscribe client channel))
