(defproject kvstore "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [aleph "0.4.1-beta2"]
                 [org.clojure/tools.logging "0.3.1"]
                 [log4j "1.2.15" :exclusions  [javax.mail/mail
                                              javax.jms/jms
                                              com.sun.jdmk/jmxtools
                                              com.sun.jmx/jmxri]]
                 [org.slf4j/slf4j-log4j12 "1.6.6"]
                 [gloss "0.2.5"]]
  :main ^:skip-aot kvstore.core
  :target-path "target/%s"
  :profiles {
             :dev {:resource-paths ["resources/dev"]}
             :prod {:resource-paths ["resources/prod"]}
             :uberjar  {:aot :all}
             })
