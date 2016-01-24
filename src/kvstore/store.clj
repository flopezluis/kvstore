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

(def ag-out-file (agent (.getChannel
                         (FileOutputStream. (File. (:db_file conf)) true))))

(defn err-handler-fn [ag ex]
  (println "Error occured: " ex " value in agent: " @ag))

(set-error-handler! ag-out-file err-handler-fn)

(defn update-key [ch key value]
  (let [offset (.position ch)]
    (.write ch (key-value-to-buffer key value))
    (swap! storage assoc key offset)
    ch)
  )

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
  (await (send ag-out-file update-key key value)))

(defn reading-kv-from-file [start-offset f]
  (log/info "Reading db file..." start-offset)
  (loop [offset start-offset]
    (when (< offset (.length (File. (:db_file conf))))
      (let [[k v] (read-from-file offset)
            next-key (.remaining (key-value-to-buffer k v))]
        (if (f k v offset)
          (recur (+ offset next-key)))))))

(defn recreate-storage []
  (reading-kv-from-file 0
                        (fn [k v offset]
                          (swap! storage assoc k offset)
                          true))
  (log/debug "Database ready"))
