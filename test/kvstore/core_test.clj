(ns kvstore.core-test
  (:require [clojure.test :refer :all]
            [kvstore.core :refer :all]
            [kvstore.store :as store]))

(deftest test-process-set-cmd
  (testing "testing a command is well parsed."
    (is (= "OK") (process-cmd "SET b 1"))
    (is (= "1") (get #'store/storage "b"))))

(deftest test-process-get-cmd
  (testing "testing a command is well parsed."
    (is (= "OK") (process-cmd "SET b 1"))
    (is (= "1") (store/get-key "b"))))

(deftest test-process-close-cmd
  (testing "testing a command is well parsed."
    (is (= ::close) (process-cmd "CLOSE"))))
