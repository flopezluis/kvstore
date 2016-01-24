(ns kvstore.protocol-test
  (:require [clojure.test :refer :all]
            [kvstore.protocol :refer :all]
            [manifold.stream :as s]
            [kvstore.protocol :as protocol]
            [kvstore.store :as store]))

(deftest test-process-set-cmd
  (testing "testing a command is well parsed."
    (let [s (s/stream)]
      (is (= "OK") (protocol/run-cmd "SET b 1" s))
      (is (= "1") (get #'store/storage "b")))))

(deftest test-process-get-cmd
  (testing "testing a command is well parsed."
    (let [s (s/stream)]
      (is (= "OK") (protocol/run-cmd "SET b 1" s))
      (is (= "1") (store/get-key "b")))))

(deftest test-process-close-cmd
  (testing "testing a command is well parsed."
    (let [s (s/stream)]
      (is (= ::close) (protocol/run-cmd "CLOSE" s)))))
