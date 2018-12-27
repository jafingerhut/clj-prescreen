(defproject clj-prescreen "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [
                 [org.clojure/clojure "1.10.0"]
                 [org.clojure/tools.reader "1.3.0"]
                 [org.clojure/data.zip "0.1.2"]
                 [me.raynes/fs "1.4.6"]
                 [clj-http "3.9.1"]
                 [joda-time "2.10"]
                 ]
  :profiles {:dev {:dependencies [[org.clojure/tools.trace "0.7.9"]]}
             :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}
             :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}
             :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}
             :1.9 {:dependencies [[org.clojure/clojure "1.9.0"]]}
             :1.10 {:dependencies [[org.clojure/clojure "1.10.0-alpha8"]]}}
  :aliases {"test-all" ["with-profile"
                        "dev,test,1.6:dev,test,1.7:dev,test,1.8:dev,test,1.9:dev,test,1.10"
                        "test"]}
  :jvm-opts ^:replace ["-Xmx1024m"]
  :main clj-prescreen.presc)
