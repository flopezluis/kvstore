(ns kvstore.config
  (:require [clojure.tools.logging :as log]))

(def conf
  (read-string (slurp "resources/config.edn")))
