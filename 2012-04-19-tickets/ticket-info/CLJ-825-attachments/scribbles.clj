(ns scribbles)

(defprotocol BugTest
  (hello [_] [_ _]))

(defrecord Zomg []
  BugTest
  (hello [_] (println "Zomg arity 1"))
  (hello [_ _] (println "Zomg arity 2")))

(hello (Zomg.))
(hello (Zomg.) :foo)

(extend-type Object
  BugTest
  (hello [_] (println "Object arity 1"))
  (hello [_ _] (println "Object arity 2")))

(try
  (hello "string")
  (catch clojure.lang.ArityException _
    (println "extend-type multi arity failed")))

;; Overwrites the first function
(hello "string" :foo)

(extend-type String
  BugTest
  (hello
    ([_] (println "String arity 1"))
    ([_ _] (println "String arity 2"))))

(hello "string")
(hello "string" :foo)

;; But now defrecord doesn't support that syntax

(defrecord Zomg2 []
  BugTest
  (hello
    ([_] (println "Zomg2 arity 1"))
    ([_ _] (println "Zomg2 arity 2"))))
