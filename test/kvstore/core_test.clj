(ns kvstore.core-test
  (:require [clojure.test :refer :all]
            [kvstore.core :as core]
            [kvstore.protocol :as protocol]
            [manifold.stream :as s]
            [kvstore.store :as store]))

(deftest test-get-value
  (testing "testing a command is well parsed."
    (let [s (s/stream)]
      (store/put! "key" "value")
      (core/get-value "key" s)
      (is (= "value") @(s/take! s)))))

(deftest test-write-value
  (testing "testing a command is well parsed."
    (let [s (s/stream)]
      (core/write-operation "key2" "value2" s)
      (is (= "OK\r\n") @(s/take! s))
      (core/get-value "key2" s)
      (is (= "value2") @(s/take! s)))))

(deftest test-fwrite-value
  (testing "testing a command is well parsed."
    (let [s (s/stream)]
      (core/fwrite-operation "key3" "value3" s)
      (core/get-value "key3" s)
      (is "value3" @(s/take! s)))))
