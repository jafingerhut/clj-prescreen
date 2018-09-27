(ns clj-prescreen.test.presc
  (:import (java.io StringReader))
  (:use [clojure.test])
  (:require [clojure.java.io :as io]
            [clj-prescreen.presc :as ps]))


;; All of the following sample output of the 'ant' command while
;; compiling Clojure core code has some lines deleted for brevity.


;; No warnings or errors, and no test failures.

(def ant-output-good
  "Buildfile: /Users/jafinger/clj/clojure/build.xml

clean:
   [delete] Deleting directory /Users/jafinger/clj/clojure/target
   [delete] Deleting /Users/jafinger/clj/clojure/clojure-1.4.0-master-SNAPSHOT.jar
   [delete] Deleting /Users/jafinger/clj/clojure/clojure.jar

init:
    [mkdir] Created dir: /Users/jafinger/clj/clojure/target/classes
    [mkdir] Created dir: /Users/jafinger/clj/clojure/target/classes/clojure

compile-java:
    [javac] Compiling 147 source files to /Users/jafinger/clj/clojure/target/classes
    [javac] Note: Some input files use unchecked or unsafe operations.
    [javac] Note: Recompile with -Xlint:unchecked for details.

compile-clojure:
     [java] Compiling clojure.core to /Users/jafinger/clj/clojure/target/classes
     [java] Compiling clojure.core.protocols to /Users/jafinger/clj/clojure/target/classes
     [java] Compiling clojure.main to /Users/jafinger/clj/clojure/target/classes
     [java] Compiling clojure.set to /Users/jafinger/clj/clojure/target/classes
     [java] Compiling clojure.xml to /Users/jafinger/clj/clojure/target/classes
     [java] Compiling clojure.zip to /Users/jafinger/clj/clojure/target/classes
     [java] Compiling clojure.inspector to /Users/jafinger/clj/clojure/target/classes
     [java] Compiling clojure.walk to /Users/jafinger/clj/clojure/target/classes

build:

compile-tests:
    [mkdir] Created dir: /Users/jafinger/clj/clojure/target/test-classes
     [java] Compiling clojure.test-clojure.protocols.examples to /Users/jafinger/clj/clojure/target/test-classes
     [java] Compiling clojure.test-clojure.genclass.examples to /Users/jafinger/clj/clojure/target/test-classes

test:
     [java] 
     [java] Testing clojure.test-clojure.agents
     [java] 
     [java] Testing clojure.test-clojure.annotations
     [java] 
     [java] Testing clojure.test-clojure.atoms
     [java] 
     [java] Testing clojure.test-clojure.clojure-set
     [java] 
     [java] Testing clojure.test-clojure.clojure-walk
     [java] 
     [java] Testing clojure.test-clojure.clojure-xml
     [java] 
     [java] Testing clojure.test-clojure.clojure-zip
     [java] 
     [java] Ran 427 tests containing 12472 assertions.
     [java] 0 failures, 0 errors.

jar:
      [jar] Building jar: /Users/jafinger/clj/clojure/clojure-1.4.0-master-SNAPSHOT.jar
     [copy] Copying 1 file to /Users/jafinger/clj/clojure

all:

BUILD SUCCESSFUL
Total time: 38 seconds
")


;; A reflection warning occurs in core.clj.  'ant' returns a 0 exit
;; status in this case, and all unit tests pass, so the only way to
;; catch this one is to recognize it in the output.

(def ant-output-warning-but-no-errors
  "Buildfile: /Users/jafinger/clj/clojure/build.xml

clean:
   [delete] Deleting directory /Users/jafinger/clj/clojure/target
   [delete] Deleting /Users/jafinger/clj/clojure/clojure-1.4.0-master-SNAPSHOT.jar
   [delete] Deleting /Users/jafinger/clj/clojure/clojure.jar

init:
    [mkdir] Created dir: /Users/jafinger/clj/clojure/target/classes
    [mkdir] Created dir: /Users/jafinger/clj/clojure/target/classes/clojure

compile-java:
    [javac] Compiling 147 source files to /Users/jafinger/clj/clojure/target/classes
    [javac] Note: Some input files use unchecked or unsafe operations.
    [javac] Note: Recompile with -Xlint:unchecked for details.

compile-clojure:
     [java] Reflection warning, clojure/core.clj:6205 - call to getProperty can't be resolved.
     [java] Reflection warning, clojure/core.clj:6215 - call to contains can't be resolved.
     [java] Compiling clojure.core to /Users/jafinger/clj/clojure/target/classes
     [java] Reflection warning, clojure/core.clj:6205 - call to getProperty can't be resolved.
     [java] Reflection warning, clojure/core.clj:6215 - call to contains can't be resolved.
     [java] Compiling clojure.core.protocols to /Users/jafinger/clj/clojure/target/classes
     [java] Compiling clojure.main to /Users/jafinger/clj/clojure/target/classes
     [java] Compiling clojure.set to /Users/jafinger/clj/clojure/target/classes
     [java] Compiling clojure.xml to /Users/jafinger/clj/clojure/target/classes
     [java] Compiling clojure.zip to /Users/jafinger/clj/clojure/target/classes
     [java] Compiling clojure.inspector to /Users/jafinger/clj/clojure/target/classes
     [java] Compiling clojure.data to /Users/jafinger/clj/clojure/target/classes
     [java] Compiling clojure.reflect to /Users/jafinger/clj/clojure/target/classes

build:

compile-tests:
    [mkdir] Created dir: /Users/jafinger/clj/clojure/target/test-classes
     [java] Compiling clojure.test-clojure.protocols.examples to /Users/jafinger/clj/clojure/target/test-classes
     [java] Compiling clojure.test-clojure.genclass.examples to /Users/jafinger/clj/clojure/target/test-classes

test:
     [java] 
     [java] Testing clojure.test-clojure.agents
     [java] 
     [java] Testing clojure.test-clojure.annotations
     [java] 
     [java] Testing clojure.test-clojure.atoms
     [java] 
     [java] Testing clojure.test-clojure.clojure-set
     [java] 
     [java] Testing clojure.test-clojure.clojure-walk
     [java] 
     [java] Testing clojure.test-clojure.clojure-xml
     [java] 
     [java] Ran 427 tests containing 12472 assertions.
     [java] 0 failures, 0 errors.

jar:
      [jar] Building jar: /Users/jafinger/clj/clojure/clojure-1.4.0-master-SNAPSHOT.jar
     [copy] Copying 1 file to /Users/jafinger/clj/clojure

all:

BUILD SUCCESSFUL
Total time: 36 seconds
")



;; Test failure.  ant returns exit status of 1, so not crucial to
;; catch this with string matching, but for the summary it would be
;; nice to extract out the summary line "1 failures, 0 errors"

(def ant-output-test-failure
  "Buildfile: /Users/jafinger/clj/clojure/build.xml

clean:
   [delete] Deleting directory /Users/jafinger/clj/clojure/target
   [delete] Deleting /Users/jafinger/clj/clojure/clojure-1.4.0-master-SNAPSHOT.jar
   [delete] Deleting /Users/jafinger/clj/clojure/clojure.jar

init:
    [mkdir] Created dir: /Users/jafinger/clj/clojure/target/classes
    [mkdir] Created dir: /Users/jafinger/clj/clojure/target/classes/clojure

compile-java:
    [javac] Compiling 147 source files to /Users/jafinger/clj/clojure/target/classes
    [javac] Note: Some input files use unchecked or unsafe operations.
    [javac] Note: Recompile with -Xlint:unchecked for details.

compile-clojure:
     [java] Compiling clojure.core to /Users/jafinger/clj/clojure/target/classes
     [java] Compiling clojure.core.protocols to /Users/jafinger/clj/clojure/target/classes
     [java] Compiling clojure.main to /Users/jafinger/clj/clojure/target/classes
     [java] Compiling clojure.set to /Users/jafinger/clj/clojure/target/classes

build:

compile-tests:
    [mkdir] Created dir: /Users/jafinger/clj/clojure/target/test-classes
     [java] Compiling clojure.test-clojure.protocols.examples to /Users/jafinger/clj/clojure/target/test-classes
     [java] Compiling clojure.test-clojure.genclass.examples to /Users/jafinger/clj/clojure/target/test-classes

test:
     [java] 
     [java] Testing clojure.test-clojure.agents
     [java] 
     [java] Testing clojure.test-clojure.annotations
     [java] 
     [java] Testing clojure.test-clojure.atoms
     [java] 
     [java] Testing clojure.test-clojure.parallel
     [java] 
     [java] Testing clojure.test-clojure.pprint
     [java] 
     [java] FAIL in (d-tests) (test_cl_format.clj:24)
     [java] expected: (clojure.core/= (cl-format nil \"~:D\" 2000000) (clojure.test-helper/platform-newlines \"2000,000\"))
     [java]   actual: (not (clojure.core/= \"2,000,000\" \"2000,000\"))
     [java] 
     [java] Testing clojure.test-clojure.predicates
     [java] 
     [java] Testing clojure.test-clojure.printer
     [java] 
     [java] Ran 427 tests containing 12472 assertions.
     [java] 1 failures, 0 errors.

BUILD FAILED
/Users/jafinger/clj/clojure/build.xml:100: Java returned: 1

Total time: 34 seconds")


;; A syntax error.  ant returns an exit status of 1, so not crucial to
;; catch any errors in the output, but would be cool if we could pick
;; out some useful summary that it was a compile error.

(def ant-output-compilation-error
  "Buildfile: /Users/jafinger/clj/clojure/build.xml

clean:
   [delete] Deleting directory /Users/jafinger/clj/clojure/target

init:
    [mkdir] Created dir: /Users/jafinger/clj/clojure/target/classes
    [mkdir] Created dir: /Users/jafinger/clj/clojure/target/classes/clojure

compile-java:
    [javac] Compiling 147 source files to /Users/jafinger/clj/clojure/target/classes
    [javac] /Users/jafinger/clj/clojure/src/jvm/clojure/lang/Ratio.java:30: operator && cannot be applied to <nulltype>,boolean
    [javac]            && arg0 instanceof Ratio
    [javac]                    ^
    [javac] /Users/jafinger/clj/clojure/src/jvm/clojure/lang/Ratio.java:29: incompatible types
    [javac] found   : java.lang.Object
    [javac] required: boolean
    [javac]     return arg0 = null
    [javac]                 ^
    [javac] Note: Some input files use unchecked or unsafe operations.
    [javac] Note: Recompile with -Xlint:unchecked for details.
    [javac] 2 errors

BUILD FAILED
/Users/jafinger/clj/clojure/build.xml:40: Compile failed; see the compiler error output for details.

Total time: 5 seconds
")


;; These should cover the variety of cases in the implementation of
;; check-ant-output.

(def interesting-props-to-test [
                                {"java.vendor" "Oracle Corporation",
                                 "java.version" "1.6.0"}
                                {"java.vendor" "Oracle Corporation",
                                 "java.version" "1.7.0"}
                                {"java.vendor" "Oracle Corporation",
                                 "java.version" "1.8.0"}
                                ])


(deftest test-check-ant-output
  (doseq [props interesting-props-to-test]
    (is (= [:ok "Success"]
           (let [{:keys [ant-status ant-msg]}
                 (ps/check-ant-output ant-output-good props)]
             [ant-status (and ant-msg (re-find #"Success" ant-msg))])))
;;    (is (let [{:keys [ant-status ant-msg]}
;;              (ps/check-ant-output ant-output-good props)]
;;          (and (= ant-status :ok)
;;               (re-find #"Success" ant-msg))))
    (is (= [:fail "Reflection warning"]
           (let [{:keys [ant-status ant-msg]}
                 (ps/check-ant-output ant-output-warning-but-no-errors props)]
             [ant-status (and ant-msg (re-find #"Reflection warning" ant-msg))])))
;;    (is (let [{:keys [ant-status ant-msg]}
;;              (ps/check-ant-output ant-output-warning-but-no-errors props)]
;;          (and (= ant-status :fail)
;;               (re-find #"Reflection warning" ant-msg))))
    (is (= [:fail "1 failures, 0 errors"]
           (let [{:keys [ant-status ant-msg]}
                 (ps/check-ant-output ant-output-test-failure props)]
             [ant-status (and ant-msg (re-find #"1 failures, 0 errors" ant-msg))])))
;;    (is (let [{:keys [ant-status ant-msg]}
;;              (ps/check-ant-output ant-output-test-failure props)]
;;          (and (= ant-status :fail)
;;               (re-find #"test: 1 failure, 0 errors" ant-msg))))
    (is (= [:fail "Compile failed"]
           (let [{:keys [ant-status ant-msg]}
                 (ps/check-ant-output ant-output-compilation-error props)]
             [ant-status (and ant-msg (re-find #"(?i)Compile failed" ant-msg))])))
;;    (is (let [{:keys [ant-status ant-msg]}
;;              (ps/check-ant-output ant-output-compilation-error props)]
;;          (and (= ant-status :fail)
;;               (re-find #"compile-java: 2 errors" ant-msg))))
    ))


(def test-patch-content-1 "From 7459a805b37c1e24cc024e4bd34410b4e62f714b Mon Sep 17 00:00:00 2001
From: Andy Fingerhut <andy_fingerhut@alum.wustl.edu>
Date: Fri, 24 Feb 2012 15:34:00 -0800
Subject: [PATCH 1/2] Tighten up existing tests for recur across try boundaries.

Some were syntactically incorrect (e.g. loop [x]) and were detecting
exceptions thrown that weren't from the compiler.

---
 test/clojure/test_clojure/compilation.clj |   36 ++++++++++++++++-------------
 1 files changed, 20 insertions(+), 16 deletions(-)

diff --git a/test/clojure/test_clojure/compilation.clj b/test/clojure/test_clojure/compilation.clj
index f8b27de..8fe5631 100644
--- a/test/clojure/test_clojure/compilation.clj
+++ b/test/clojure/test_clojure/compilation.clj
 
@@ -54,26 +55,29 @@
 
 (deftest test-no-recur-across-try
   (testing \"don't recur to function from inside try\"
-    (is (thrown? Exception (eval '(fn [x] (try (recur 1)))))))
+    (is (thrown? Compiler$CompilerException
+                 (eval '(fn [x] (try (recur 1)))))))
 
 ;; disabled until build box can call java from mvn
 #_(deftest test-numeric-dispatch
-- 
1.7.3.4


From e04a3121db983bb3890058b2470fc2efff483717 Mon Sep 17 00:00:00 2001
From: John Public <john.public@aol.com>
Date: Sun, 31 Oct 2010 19:32:25 +0200
Subject: [PATCH 2/2] Allow loop/recur nested in catch and finally

---
 src/jvm/clojure/lang/Compiler.java        |    8 +++-----
 test/clojure/test_clojure/compilation.clj |   26 ++++++++++++++++++++++++++
 2 files changed, 29 insertions(+), 5 deletions(-)

diff --git a/src/jvm/clojure/lang/Compiler.java b/src/jvm/clojure/lang/Compiler.java
index bfc8274..dea9310 100644
--- a/src/jvm/clojure/lang/Compiler.java
+++ b/src/jvm/clojure/lang/Compiler.java
@@ -2147,7 +2147,7 @@ public static class TryExpr implements Expr{
                             try 
                                 {
                                     Var.pushThreadBindings(RT.map(NO_RECUR, true));
-                                    bodyExpr = (new BodyExpr.Parser()).parse(context, RT.seq(body));
+				    bodyExpr = (new BodyExpr.Parser()).parse(C.EXPRESSION, RT.seq(body));
                                 } 
                             finally
                                 {
-- 
1.7.3.4")



;; TBD: Test a git format patch file that has one of the lines in the
;; commit comments beginning with "From: ".  How does git recognize
;; the boundaries of a patch in this case?  Perhaps by looking for the
;; line "---" by itself at the end of the comments?

(def test-people-data-contents-string
  "
[
{:display-name \"John Public\"
 :aliases #{ }
 :usernames #{ \"johnpublic\" }
 :emails #{ \"john.public@aol.com\" }
 :contributor true
 }
{:display-name \"John Jacob Jingleheimer Schmidt\"
 :aliases #{ }
 :usernames #{ \"johnjacob\" }
 :emails #{ \"john_jacob_jingleheimer_schmidt@gmail.com\" }
 :contributor false
 }
{:display-name \"Andy Fingerhut\"
 :aliases #{ \"John Andrew Fingerhut\" }
 :usernames #{ \"jafingerhut\" }
 :emails #{ \"andy_fingerhut@alum.wustl.edu\" \"andy.fingerhut@gmail.com\" \"jafinger@cisco.com\" }
 :contributor true
 }
]
")

(deftest test-author-checks
  (let [people (read (java.io.PushbackReader.
                      (StringReader. test-people-data-contents-string)))
        authors1 (ps/git-patch-authors test-patch-content-1)
        names-and-emails1 (set (map ps/extract-name-and-email authors1))
        andy {:display-name "Andy Fingerhut",
              :aliases #{"John Andrew Fingerhut"},
              :usernames #{"jafingerhut"},
              :emails
              #{"andy.fingerhut@gmail.com" "andy_fingerhut@alum.wustl.edu"
                "jafinger@cisco.com"},
              :contributor true}]
    (is (= (set authors1) #{"Andy Fingerhut <andy_fingerhut@alum.wustl.edu>"
                            "John Public <john.public@aol.com>"}))
    (is (= names-and-emails1
           #{{:name "Andy Fingerhut", :email "andy_fingerhut@alum.wustl.edu"}
             {:name "John Public", :email "john.public@aol.com"}}))
    (is (= (ps/find-by-name-and-email people
                                      {:name "Andy Fingerhut"
                                       :email "andy_fingerhut@alum.wustl.edu"})
           [:one-full-match andy]))
    (is (= (ps/find-by-name-and-email people
                                      {:name "John Andrew Fingerhut"
                                       :email "andy.fingerhut@gmail.com"})
           [:one-full-match andy]))
    (is (= (ps/find-by-name-and-email people
                                      {:name "J. Andy Fingerhut"
                                       :email "andy_fingerhut@alum.wustl.edu"})
           [:only-partial-matches (list andy)]))
    (is (= (ps/find-by-name-and-email people
                                      {:name "Andy Fingerhut"
                                       :email "jafingerhut@me.com"})
           [:only-partial-matches (list andy)]))
    (is (= (ps/find-by-name-and-email people
                                      {:name "J. Andy Fingerhut"
                                       :email "jafingerhut@me.com"})
           [:no-matches nil]))
    (is (= (set (ps/patch-authors-contributor-status authors1 people))
           #{{:contributor-status :contributor,
              :name "Andy Fingerhut",
              :display-name "Andy Fingerhut",
              :email "andy_fingerhut@alum.wustl.edu"}
             {:contributor-status :contributor,
              :name "John Public",
              :display-name "John Public",
              :email "john.public@aol.com"}}))
    (is (= (set (ps/patch-authors-contributor-status
                 ["John Jacob Jingleheimer Schmidt <john_jacob_jingleheimer_schmidt@gmail.com>"] people))
           #{{:contributor-status :not-contributor,
              :name "John Jacob Jingleheimer Schmidt",
              :display-name "John Jacob Jingleheimer Schmidt",
              :email "john_jacob_jingleheimer_schmidt@gmail.com"}}))
    (is (= (set (ps/patch-authors-contributor-status
                 [ "Andy Fingerhut <jafingerhut@me.com>" ] people))
           #{{:contributor-status :only-partial-matches,
              :name "Andy Fingerhut",
              :email "jafingerhut@me.com"}}))
    (is (= (set (ps/patch-authors-contributor-status
                 [ "Daffy Duck <daffy@disney.com>" ] people))
           #{{:contributor-status :no-matches,
              :name "Daffy Duck",
              :email "daffy@disney.com"}}))
    ))
