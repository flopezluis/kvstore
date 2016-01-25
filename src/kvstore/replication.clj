(ns kvstore.replication
  (:require [kvstore.config :refer [conf]]
            [clojure.tools.logging :as log]
            [kvstore.helper :as helper]
            [kvstore.store :as store]
            [clojure.string :as str]
            [aleph.tcp :as tcp]
            [manifold.stream :as s]
            [kvstore.protocol :as protocol :refer [parse-cmd]]
            [overtone.at-at :as at])
  (:import [java.io File]))

(def my-pool (at/mk-pool))

(defn replica-close [client error]
  (log/info "Replica::Error:: " error " Closing....")
  (.close @client))

(defn master-close [client error]
  (log/info "master::Error:: " error " Closing....")
  (.close client))

(defn end [s]
  (log/debug "REPLICA::END")
  (s/close! s))

(defn myset [k v s]
  (log/debug "Replica::SET:: received " k ":" v)
  (store/put! k v))

(defn generate-set-cmd [k v]
  (str "RSET " k " " v "\r\n"))

(defn offset [offset s]
  "It sends to the replica all the k/v since offset. If replica has closed
     the connection it stops."
  (log/debug "Master::offset:: " offset)
  (store/reading-kv-from-file (Integer. offset)
                              (fn [k v offset]
                                (do
                                  (log/debug "Master::sending " k ":" v)
                                  (s/put! s (generate-set-cmd k v))
                                  (not (s/closed? s)))))
  (s/put! s "ENDR\r\n"))

(defmethod parse-cmd "OFFSET"
  [cmd & args]
  (apply offset args))

(defmethod parse-cmd "ENDR"
  [cmd & args]
  (apply end args))

(defmethod parse-cmd "RSET"
  [cmd & args]
  (apply myset args))

(defn process-replica [stream]
  (protocol/consume-cmds stream replica-close))

(defn process-master [stream info]
  (protocol/consume-cmds stream master-close))

(defn start-server []
  (log/info "Listening to replicas... ")
  (tcp/start-server process-master
   {:port (:replication-port conf)}))

(defn master []
  (start-server))

(defn get-offset []
  (.length (File. (:db_file conf))))

(defn send-offset [client]
  (let [offset (get-offset)]
    (log/debug "Replica::OFFSET:: " offset)
    (s/put! client (str "OFFSET " offset "\r\n"))))

(defn replica []
  (let [client (tcp/client {:port (:replication-port conf)
                            :host (:master-host conf)})
        client-closed (promise)]
    (send-offset @client)
    (process-replica @client)
    (s/on-closed @client (fn [] (deliver client-closed true)))
    (deref client-closed)))

(defn start-replica []
    (at/every (:replication-delay conf) replica my-pool :fixed-delay true))

(defn is-master []
  (:master conf))

(defn start-replication []
  (if (is-master)
    (master)
    (start-replica)))
