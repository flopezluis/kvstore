(ns kvstore.store)

(def ^{:private true} storage (atom {}))

(defn put! [key value]
  (swap! storage assoc key value))

(defn get-key [key]
  (clojure.core/get @storage key))
