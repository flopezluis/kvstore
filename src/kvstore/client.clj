(ns kvstore.core
  (:import (java.net Socket)
           (java.io PrintWriter InputStreamReader BufferedReader)))

(declare conn-handler)

(defn connect [server]
  (let [socket (Socket. (:name server) (:port server))
        in (BufferedReader. (InputStreamReader. (.getInputStream socket)))
        out (PrintWriter. (.getOutputStream socket))
        conn (ref {:in in :out out})]
    (doto (Thread. #(conn-handler conn)) (.start))
    conn))

(defn write [conn msg]
  (doto (:out @conn)
    (println msg)
    (.println (str msg "\r"))
    (.flush)))

(def client (connect {:name "localhost" :port 10009}))

(defn run-clients []
  "Run 100 clients in parallel, each of them inserts 26 keys"
  (dotimes [n 100]
    (doseq [k (seq "abcdefghijklmnopqrstuvwyxz")]
      (let [key (str k)
            data (str  "SET " key n " " key)]
        (write client data)
        (Thread/sleep 4)))
    (println (str "client " n))))













(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
