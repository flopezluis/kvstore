(ns kvstore.core
  (:import (java.net Socket)
           (java.io PrintWriter InputStreamReader BufferedReader BufferedOutputStream)))

(declare conn-handler)

(defn connect [server]
  (let [socket (Socket. (:name server) (:port server))
        in (BufferedReader. (InputStreamReader. (.getInputStream socket)))
        out (BufferedOutputStream. (.getOutputStream socket))
        conn (ref {:in in :out out :socket socket})]
    (doto (Thread. #(conn-handler conn)) (.start))
    conn))

(defn write [conn msg]
  (doto (:out @conn)
    (.write (.getBytes (str msg "\r\n")))
    (.flush)))

(def client (connect {:name "localhost" :port 10009}))

(defn read-value [client key]
  (write client (str "GET " key))
  (.readLine (:in @client)))

(defn performance[]
  (run-clients)
  (let [client (connect {:name "localhost" :port 10009})]
    (loop[]
      (let [value (read-value client "z_c_49_99")]
        (if (= "Not Found" value)
          (recur))))))

(defn verify-data []
  (dotimes [n 50]
    (let [client (connect {:name "localhost" :port 10009})]
      (dotimes [nn 100]
        (doseq [k (seq "abcdefghijklmnopqrstuvwyxz")]
          (let [letter (str k)
                key (str  letter "_c_" n "_" nn " ")]
            (if (not (= (read-value client key) letter))
              (println key))))))))

(defn run-clients []
  "Run 100 clients in parallel, each of them inserts 26 keys"
  (dotimes [n 50]
    (let [client (connect {:name "localhost" :port 10009})]
      (dotimes [nn 100]
               (doseq [k (seq "abcdefghijklmnopqrstuvwyxz")]
                 (let [key (str k)
                       data (str  "FSET " key "_c_" n "_" nn " " key)]
                   (write client data)))))))
