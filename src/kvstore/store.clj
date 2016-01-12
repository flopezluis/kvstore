(ns kvstore.store
  (:require [kvstore.config :refer [conf]]
            [clojure.java.io :refer [file]]
            [clojure.tools.logging :as log]
            [clojure.string :as str])
  (:use [clojure.java.io])
  (:import [java.nio ByteBuffer]
           [java.io RandomAccessFile File FileOutputStream]))

(def ^{:private true} storage (atom {}))

(defn key-value-to-buffer [k v]
  (ByteBuffer/wrap (.getBytes (str k  #"«" v "\0"))))

(defn write-to-file [key value]
  (let [f (File. (:db_file conf))
        fo (FileOutputStream. f true)
        ch  (.getChannel fo)
        offset (.length f)]
    (.write ch (key-value-to-buffer key value))
    (.close ch)
    offset))

(defn char-seq
  [^java.io.RandomAccessFile rdr]
  (let [chr (.read rdr)]
    (if (> chr 0)
      (cons chr (lazy-seq (char-seq rdr))))))

(defn parse-key-value [bytes-buffer]
  (str/split (String. bytes-buffer) #"«"))

(defn read-from-file [offset]
  (let [raf (RandomAccessFile. (:db_file conf) "r")
        _ (.seek raf offset)
        buf (char-seq raf)
        data (parse-key-value (byte-array buf))]
    (.close raf)
    data))

(defn get-key [key]
  (if-let [offset (get @storage key)]
    (-> offset
        read-from-file
        last)
    "Not Found"))

(defn put! [key value]
  (let [offset (write-to-file key value)]
    (swap! storage assoc key offset)
    :ok))

(defn recreate-storage []
  (log/debug "Reading db file...")
  (loop [offset 0]
    (when (< offset (.length (File. (:db_file conf))))
      (let [[k v] (read-from-file offset)
            next-key (.remaining (key-value-to-buffer k v))]
        (swap! storage assoc k offset)
        (recur (+ offset next-key)))))
  (log/debug "Database ready"))
