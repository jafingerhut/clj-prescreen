(defproject clj-prescreen "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [
                 [org.clojure/clojure "1.5.0-RC15"]
                 ;; tools.reader 0.6.5 has a bug that prevents it from
                 ;; reading from a java.io.PushbackReader.  Wait for
                 ;; next release, which should fix that.
                 ;;[org.clojure/tools.reader "0.6.5"]
                 [org.clojure/data.zip "0.1.0"]
                 [fs "1.1.2"]
                 [clj-http "0.6.3"]
                 [joda-time "2.1"]
                 ])
