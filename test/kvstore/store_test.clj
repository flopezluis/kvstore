(ns kvstore.store-test
  (:require [clojure.test :refer :all]
            [kvstore.core :refer :all]
            [kvstore.store :as store]
            [clojure.java.io :refer [delete-file file]]
            [kvstore.config :refer [conf]]))

(defn delete-db []
  (if (.exists (file (:db_file conf)))
    (delete-file (:db_file conf))))

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
    (is (= :ok (store/put! "key" "value")))
    (is (= "value" (store/get-key "key")))))

(deftest test-read-from-file
  (testing "testing key/value are stored"
    (is (= :ok (store/put! "key" "value")))
    (is (= ["key" "value"] (store/read-from-file 0)))))

(deftest test-write-to-file
  (testing "testing key/value are stored"
    (is (= 0 (store/write-to-file "mykey" "myvalue")))
    (is (= ["mykey" "myvalue"] (store/read-from-file 0)))))
