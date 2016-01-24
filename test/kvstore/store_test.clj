(ns kvstore.store-test
  (:require [clojure.test :refer :all]
            [kvstore.core :refer :all]
            [kvstore.store :as store]
            [clojure.java.io :refer [delete-file file]]
            [kvstore.config :refer [conf]])
  (:import [java.nio ByteBuffer]
           [java.io RandomAccessFile File FileOutputStream]))

(defn delete-db []
  (if (.exists (file (:db_file conf)))
    (.setLength (RandomAccessFile. (:db_file conf) "rw") 0)))

(defn clear
  "Delete file"
  [test]
   (delete-db)
  (try
    (test)
    (finally
      (delete-db))))

(use-fixtures :each clear)

(deftest test-set-is-stored
  (testing "testing key/value are stored"
    (store/put! "key" "value")
    (is (= "value" (store/get-key "key")))))

(deftest test-read-from-file
  (testing "testing key/value are stored"
    (store/put! "key" "value")
    (is (= ["key" "value"] (store/read-from-file 0)))))
