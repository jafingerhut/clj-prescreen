(defproject clj-prescreen "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.reader "0.10.0"]
                 [org.clojure/data.zip "0.1.2"]
                 [org.clojure/tools.trace "0.7.9"]
                 [me.raynes/fs "1.4.6"]
                 [clj-http "3.1.0"]
                 [joda-time "2.9.4"]
                 ]
  :jvm-opts ^:replace ["-Xmx1024m"]
  :main clj-prescreen.core)
