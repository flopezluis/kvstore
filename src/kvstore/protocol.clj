(ns kvstore.protocol
  (:require [kvstore.config :refer [conf]]
            [clojure.tools.logging :as log]
            [kvstore.helper :as helper]
            [clojure.string :as str]
            [aleph.tcp :as tcp]
            [manifold.deferred :as d]
            [manifold.stream :as s])
  (:import [java.nio ByteBuffer]))

(defmulti parse-cmd (fn [cmd & other] cmd))

(defn run-cmd
  [command s]
  (let [args (str/split command #" ")]
    (apply parse-cmd (conj args s))))

(defn process-commands [stream batch-commands f]
  (let [cmds (str/split-lines batch-commands)]
    (dorun
     (for [cmd cmds]
       (f stream cmd)))))

(defn consume-cmds-from-socket [stream f f_close]
  "It processes any new connection.
   The code is based on this example
      http://ideolalia.com/aleph/literate.html#aleph.examples.tcp"
  (d/loop [cmd []]
    (-> (s/take! stream ::none)
        (d/chain
         (fn [b]
           (if (not (= b ::none))
             (do
               (let [text (helper/to-string b)
                     acc (conj cmd text)]
                 (if (= \newline (last text))
                   (do
                     (process-commands stream (str/join acc) f)
                     (d/recur []))
                   (d/recur acc)))))))
        (d/catch
            (fn [ex]
              (log/error  ex)
              (s/close! stream)
              (f_close stream ex))))))

(defn consume-cmds [stream end-callback]
  (consume-cmds-from-socket stream
                            (fn [s cmd]
                              (run-cmd cmd s))
                            end-callback))
