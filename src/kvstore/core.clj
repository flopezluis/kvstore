(ns kvstore.core
  (:require [manifold.stream :as s]
            [aleph.tcp :as tcp]
            [clojure.string :as str]
            [kvstore.store :as store]
            [kvstore.config :refer [conf]]
            [manifold.deferred :as d]
            [clojure.tools.logging :as log])
  (:import (java.nio.charset StandardCharsets))
  (:gen-class))

(defn to-string [bytes]
  (String. bytes StandardCharsets/UTF_8))

(defn write-operation [data]
  (apply store/put! data)
  "OK\r\n")

(defn fwrite-operation [data]
  "Writes with no response"
  (apply store/put! data)
  ::none)

(defn process-cmd [command]
  "It parses a cmd and return the response"
  (log/debug "Processing cmd " command)
  (let [data (str/split command #" ")]
    (case (first data)
      "GET" (str (store/get-key (last data)) "\r\n")
      "SET" (write-operation (rest data))
      "FSET" (fwrite-operation (rest data))
      "CLOSE" ::close
      "Unrecognized command")))

(defn process-protocol [bytes]
  (log/debug "Processing protocol " (to-string bytes))
  (let [data (str/split-lines (to-string bytes))]
    (doall (map process-cmd data))))

(defn process-response [s response]
  (log/debug "Processing response " response)
  (case response
    ::close (do  (s/put! s (str "Closing..."))
                 (s/close! s))
    ::none nil
    (s/put! s response)))

(defn process-commands [s batch-commands]
  (let [cmds (str/split-lines batch-commands)]
    (doall (map #(process-response s (process-cmd %1)) cmds))))


(defn process-client [s info]
  "It processes any new connection.
   The code is based on this example
      http://ideolalia.com/aleph/literate.html#aleph.examples.tcp"
  (d/loop [cmd []]
    (-> (s/take! s ::none)
        (d/chain
         (fn [b]
           (let [text (to-string b)]
             {:complete (= \newline (last text))
              :text text}))
         (fn [result]
           (let [acc (conj cmd (:text result))]
             (if (:complete result)
               (process-commands s (str/join acc)))
             (d/recur acc))))
        (d/catch
            (fn [ex]
              (s/put! s (str "ERROR: " ex))
              (s/close! s))))))

(defn start-server [port]
  (store/recreate-storage)
  (tcp/start-server process-client
   {:port port} )
  (log/info "Listening.."))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (log/info "Starting server..")
  (start-server (:port conf)))
