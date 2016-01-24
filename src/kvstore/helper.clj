(ns kvstore.helper
  (:require [clojure.tools.logging :as log]
            [manifold.deferred :as d]
            [manifold.stream :as s]
            [clojure.string :as str])
  (:import (java.nio.charset StandardCharsets)))

(defn to-string [bytes]
  (String. bytes StandardCharsets/UTF_8))

(defn process-client [s info f forever]
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
               (do
                 (f s (str/join acc))
                 (if forever
                   (d/recur [])))
               (d/recur acc))
             )))
        (d/catch
            (fn [ex]
              (s/put! s (str "ERROR: " ex))
              (s/close! s))))))
