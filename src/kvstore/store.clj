(ns kvstore.store
  (:require [kvstore.config :refer [conf]]
            [gloss.core :as gloss :refer [defcodec]]
            [gloss.io :refer [decode encode contiguous]]
            [clojure.java.io :refer [file]]
            [clojure.tools.logging :as log])
  (:use [clojure.java.io])
  (:import [java.nio ByteBuffer]
           [java.io RandomAccessFile File FileOutputStream]))

(def ^{:private true} storage (atom {}))

(def stoken (gloss/string :utf-8 :delimiters "«"))
(defcodec record-codec [stoken stoken])

(defn write-to-file [key value]
  (let [f (File. (:db_file conf))
        fo (FileOutputStream. f true)
        ch  (.getChannel fo)
        offset (.length f)]
    (.write ch (contiguous (encode record-codec [key value])))
    (.close ch)
    offset))

(defn read-from-file [offset]
  (let [raf (RandomAccessFile. (:db_file conf) "r")
        buf (byte-array 1024)
        _ (.seek raf offset)
        n (.read raf buf)]
    (.close raf)
    (decode record-codec (java.nio.ByteBuffer/wrap buf) false)))

(defn get-key [key]
  (-> (get @storage key)
      read-from-file
      last))

(defn put! [key value]
  (let [offset (write-to-file key value)]
    (swap! storage assoc key offset)
    :ok))

(defn recreate-storage []
  (log/debug "Reading db file...")
  (loop [offset 0]
    (when (< offset (.length (File. (:db_file conf))))
      (let [[k v] (read-from-file offset)
            next-key (.remaining (contiguous (encode record-codec [k v])))]
        (swap! storage assoc k offset)
        (recur (+ offset next-key)))))
  (log/debug "Database ready"))
