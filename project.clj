(defproject clj-prescreen "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [
                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.reader "0.7.10"]
                 [org.clojure/data.zip "0.1.1"]
                 [org.clojure/tools.trace "0.7.6"]
                 [me.raynes/fs "1.4.3"]
                 [clj-http "0.7.7"]
                 [joda-time "2.1"]
                 ]
  :jvm-opts ^:replace ["-Xmx102m"]
  :main clj-prescreen.core)
