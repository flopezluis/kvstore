(ns kvstore.core
  (:require [manifold.stream :as s]
            [aleph.tcp :as tcp]
            [clojure.string :as str]
            [kvstore.store :as store]
            [kvstore.replication :as replication]
            [kvstore.config :refer [conf]]
            [kvstore.protocol :as protocol :refer [parse-cmd]]
            [manifold.deferred :as d]
            [clojure.tools.logging :as log])
  (:import (java.nio.charset StandardCharsets))
  (:gen-class))

(defn get-value [k s]
  (log/debug "MASTER::GET:: received " k)
  (let [value (str (store/get-key k) "\r\n")]
    (s/put! s value)))

(defn write-operation [k v s]
  (log/debug "MASTER::SET:: received " k v)
  (if (replication/is-master)
    (do (store/put! k v)
        (s/put! s "OK\r\n"))
    (s/put! s "Not Allowed\r\n")))

(defn fwrite-operation [k v s]
  "Writes with no response"
  (if (replication/is-master)
    (store/put! k v)
    (s/put! s "Not Allowed\r\n")))

(defn close [s]
  (s/close! s))

(defmethod parse-cmd "GET"
  [cmd & args]
  (apply get-value args))

(defmethod parse-cmd "SET"
  [cmd & args]
  (apply write-operation args))

(defmethod parse-cmd "FSET"
  [cmd & args]
  (apply fwrite-operation args))

(defmethod parse-cmd "CLOSE"
  [cmd & args]
  (apply close args))

(defn close-server []
  (println "closing"))

(defn process-cmds [stream info]
  (protocol/consume-cmds stream close-server))

(defn start-server [port]
  (store/recreate-storage)
  (tcp/start-server process-cmds
   {:port port} )
  (log/info "Listening.."))

(defn start-up []
  (replication/start-replication)
  (start-server (:port conf)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (log/info "Starting server..")
  (start-up))
