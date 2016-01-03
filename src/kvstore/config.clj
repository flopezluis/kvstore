(ns kvstore.config
  (:require [clojure.tools.logging :as log]))

(def conf
  (read-string (slurp (clojure.java.io/resource "config.edn"))))
