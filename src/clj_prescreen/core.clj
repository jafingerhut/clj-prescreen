(ns clj-prescreen.core
  (:import (java.io File ByteArrayInputStream))
  (:require [clojure.xml :as xml]
            [clojure.data :as data]
            [clojure.data.zip.xml :as dzx]
            ;;[clojure.edn :as edn]
            [clojure.tools.reader.edn :as edn]
            [clojure.zip :as zip]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.java.shell :as sh]
            [clojure.pprint :as p]
            [clj-http.client :as http]
            [fs.core :as fs]))

(set! *warn-on-reflection* true)


;; Flow of code

;; 1. Read XML file and extract info about tickets and attachments
;; from it, into a Clojure data structure.

;; Implemented in xml->attach-info.  Only side effects are reading the
;; File, InputStream, or string naming a URI provided as the argument.
;; The value returned is a sequence of maps, where each map contains
;; details about the attachment.


;; 2. Iterate through that structure, retrieving attachments from Jira
;; web site.

;; Implemented as download-attachments!  Creates a local directory
;; structure rooted at a directory named in argument 'attach-dir'.
;; Takes a sequence returned by xml->attach-info, and returns a
;; sequence where each map has been augmented with :local-filename and
;; :guessed-type keys.


;; 3. Write out the Clojure data structure, the enhanced one from step
;; 2, to a file.

;; A separate Clojure program will read the file produced in step 3
;; and determine which patches apply cleanly and which do not.  The
;; intent of putting it into a separate program is to allow a person
;; to hand-edit that file in case they want to customize which patches
;; are tried in that other program.

;; Another possibility is to merge that data structure with another
;; one that is hand-edited, so that a person can customize the first
;; set of ticket info they download, and then perhaps a week later,
;; download a new set of info, and for tickets that haven't changed,
;; at least significantly, the same hand-edited info can be merged
;; onto it to produce the patching/testing actions to be performed.


(def ^:dynamic *auto-flush* true)


(defn printf-to-writer [w fmt-str & args]
  (binding [*out* w]
    (apply clojure.core/printf fmt-str args)
    (when *auto-flush* (flush))))


(defn iprintf [fmt-str-or-writer & args]
  (if (instance? CharSequence fmt-str-or-writer)
    (apply printf-to-writer *out* fmt-str-or-writer args)
    (apply printf-to-writer fmt-str-or-writer args)))


(defn die [fmt-str & args]
  (apply iprintf *err* fmt-str args)
  (System/exit 1))


(defn read-safely [x & opts]
  (with-open [r (java.io.PushbackReader. (apply io/reader x opts))]
    (edn/read r)))


(defn spit-pretty [f data & options]
  (apply spit f (with-out-str (p/pprint data)) options))


(defn map-keys [f m]
  (into (empty m)
        (for [[k v] m] [(f k) v])))

(defn map-vals [f m]
  (into (empty m)
        (for [[k v] m] [k (f v)])))


(defn filter-vals [f m]
  (into (empty m)
        (filter (fn [[_ v]] (f v)) m)))


(defn extract-dec-num [s]
  (if-let [num-str (re-find #"\d+" s)]
    (Long/parseLong num-str)))


(defn ticket-fields [ticket]
  (let [fields [:key :title :type :attrs :status :resolution :reporter :labels
                :created :updated :votes :watches :fixVersion]
        t (into {} (map (fn [fld]
                          [fld (dzx/xml1-> ticket fld dzx/text)])
                        fields))]
    (-> t
        (assoc :ticket (t :key))
        (dissoc :key))))


(defn contents-of-tags [content tag]
  (->> content
       (filter #(= (:tag %) tag))
       first
       :content))


(defn ticket-custom-fields [ticket]
  (let [custs (map first (dzx/xml-> ticket :customfields :customfield))]
    (into {} (map (fn [fld]
                    (let [c (:content fld)]
                      [(first (contents-of-tags c :customfieldname))
                       (let [vals (contents-of-tags c :customfieldvalues)]
                         (first (contents-of-tags vals :customfieldvalue)))]))
                  custs))))


(defn attachments-from-ticket [ticket]
  (let [t (merge (ticket-fields ticket)
                 (ticket-custom-fields ticket))
        as (->> (dzx/xml-> ticket :attachments :attachment)
                (map (fn [att] (:attrs (first att)))))
        as (if (seq as) as [ {} ] )]
;;    (when (= as [ {} ])
;;      (println "Ticket has no attachments:" (:ticket t)))
    (map #(merge % t) as)))


(defn ticket-sort-key
  "Take a ticket name like \"CLJ-753\" as a string, and return a
vector of two sort keys [\"CLJ\" 753] consisting of the project name
as a string first, followed by the numerical ticket number.  These
vectors can be compared with the default 'compare' function to get a
sort order like this:

CLJ-5
CLJ-753
CLJ-1029
TBENCH-1
TBENCH-11"
  [ticket-name]
  (if-let [[_ project tick-num-str] (re-find #"(.*)-(\d+)" ticket-name)]
    [project (Long/parseLong tick-num-str)]
    ;; If doesn't match pattern, just use the whole string, then 0
    [ticket-name 0]))


(defn xml->attach-info [file]
  (let [z (zip/xml-zip (xml/parse file))
        tickets (dzx/xml-> z :channel :item)]
    (->> tickets
         (mapcat attachments-from-ticket)
         ;; add sort keys
         (map (fn [{:keys [ticket id] :as att}]
                (let [sort-key (ticket-sort-key ticket)]
                  [(conj sort-key id) att])))
         ;; sort by them
         (sort-by first)
         ;; remove sort keys
         (map second))))


(defn ticket-info-from-xml [^String xml-str]
  (let [z (-> (ByteArrayInputStream. (.getBytes xml-str "UTF-8"))
              xml/parse
              zip/xml-zip)
        tickets-zip (dzx/xml-> z :channel :item)
        tickets-info (map #(merge (ticket-fields %) (ticket-custom-fields %))
                          tickets-zip)]
    (map-vals first (group-by :ticket tickets-info))))


(defn tickets-from-xml [^String xml-str]
  (let [z (-> (ByteArrayInputStream. (.getBytes xml-str "UTF-8"))
              xml/parse
              zip/xml-zip)
        tickets-zip (dzx/xml-> z :channel :item)]
    (map #(dzx/xml1-> % :key dzx/text) tickets-zip)))


(defn url-all-open-tickets []
  "http://dev.clojure.org/jira/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?jqlQuery=Project%3DCLJ+and+status+not+in+%28Closed%2CResolved%29&tempMax=1000")


(defn url-for-tickets-voted-by-user [username]
  (let [url-part1 "http://dev.clojure.org/jira/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?jqlQuery=Project%3DCLJ+and+status+not+in+%28Closed%2CResolved%29+and+voter%3D%27"
        url-part2 "%27&tempMax=1000&field=title"]
    (str url-part1 username url-part2)))


(defn dl-open-tickets!
  "Download XML data about all open CLJ tickets and save it to a local
file."
  [file-name]
  (let [resp (http/get (url-all-open-tickets) {:throw-exceptions false})]
    (if (= 200 (:status resp))
      (spit file-name (:body resp))
      (do
        (binding [*out* *err*]
          (println
           (format "Got response status %d when trying to get URL:\n%s\n"
                   (:status resp)
                   (url-all-open-tickets))))))))


(defn dl-open-ticket-votes!
  "Download XML data about votes cast on all open CLJ tickets and
return it as a map from users to a list of tickets they voted on."
  [all-jira-users http-auth-info verbose]
  (when verbose
    (println "Getting votes for users:"))
  (into {}
        (for [user all-jira-users]
          (let [uname (first (:usernames user))
                _ (when verbose
                    (print (format "%s (%s) ...   " (:display-name user)
                                   uname))
                    (flush))
                resp (http/get (url-for-tickets-voted-by-user uname)
                               (merge http-auth-info
                                      {:throw-exceptions false}))
                tickets (if (= 200 (:status resp))
                          (tickets-from-xml (:body resp))
                          [])]
            (when verbose
              (println (if (= 200 (:status resp))
                         (if (zero? (count tickets))
                           ""
                           (format "%d" (count tickets)))
                         (format "HTTP error status %d"
                                 (:status resp)))))
            [user tickets]))))


(defn att-dir-name [ticket-name attach-dir]
  (str attach-dir "/" ticket-name "-attachments"))


(defn guess-attachment-type [content]
  (let [lines (str/split-lines content)]
;;    (iprintf "guess-attachment-type: # lines=%d\n" (count lines))
;;    (iprintf "      -------- first 5 lines --------\n")
;;    (iprintf "%s\n" (str/join "\n" (take 5 lines)))
;;    (iprintf "      -------------------------------\n")
    (cond
     (and (> (count lines) 4)
          (re-find #"^From " (nth lines 0))
          (re-find #"^From:" (nth lines 1))
          (re-find #"^Date:" (nth lines 2))
          (re-find #"^Subject:" (nth lines 3)))
     :git-diff

     (re-find #"^diff" content)
     :non-git-diff

     :else :other)))


(defn download-attachments! [atts attach-dir]
  (let [atts (filter :name atts)
        ticket-names (set (map :ticket atts))]
    (iprintf "Creating %d directories to store attachments:\n"
             (count ticket-names))
    (doseq [ticket-name ticket-names]
      (let [^File att-dir (io/file (att-dir-name ticket-name attach-dir))]
        (iprintf ".")
        (when-not (.exists att-dir)
          (when-not (.mkdirs att-dir)
            (die ".mkdirs %s failed.  Aborting.\n" att-dir)))))
    (iprintf "\n"))
  (let [num-atts (count atts)]
    (iprintf "Getting %d attachments:\n" num-atts)
    (doall
     (for [[idx att] (map-indexed list atts)]
       (if (nil? (:name att))
         att
         (let [{ticket-name :ticket id :id att-name :name} att
               local-filename (str (att-dir-name ticket-name attach-dir)
                                   "/" att-name)
               url (str "http://dev.clojure.org/jira/secure/attachment/"
                        id "/" att-name)
               _ (iprintf "    Attachment %d/%d %s ...\n"
                          (inc idx) num-atts att-name)
               att-contents (slurp url)
               guessed-type (guess-attachment-type att-contents)]
           (spit local-filename att-contents)
           (merge att {:local-filename local-filename
                       :guessed-type guessed-type})))))))


(defn git-dir? [dir]
  (sh/with-sh-dir dir
    (and (zero? (:exit (sh/sh "git" "version")))
         (zero? (:exit (sh/sh "git" "status"))))))


(defn clojure-git-dir?
  "A quick-and-dirty check to see if it looks like string 'dir' names
the root directory of a Clojure git repository."
  [dir]
  (and (git-dir? dir)
       (fs/file? (str dir "/doc/clojure/pprint/CommonLispFormat.markdown"))
       (fs/file? (str dir "/clojure.iml"))))


(def ^:dynamic *cmd-log* nil)


(defn normal-msg [{:keys [exit out err]}]
  (format "%s%s----- Sent to stderr ---------------------
%s------------------------------------------
"
          (if (zero? exit) "" (format "<Exit status %d>\n" exit)) out err))


(defn try-cmd [& args]
  (let [[throw-on-error args] (if (= :throw-on-error (first args))
                                [true (rest args)]
                                [false args])
        cmd-str (str/join " " args)]
    (when *cmd-log* (iprintf *cmd-log* "\n%% %s\n" cmd-str))
    (let [{:keys [exit out err] :as ret} (apply sh/sh args)]
      (if (and (not= exit 0) throw-on-error)
        (let [err-msg (format "Non-0 exit status %d from command: \"%s\"
%s"
                              exit cmd-str (normal-msg ret))]
          (when *cmd-log* (iprintf *cmd-log* "%s" err-msg))
          (throw (Exception. err-msg)))
        ;; else
        (do
          (when *cmd-log* (iprintf *cmd-log* "%s" (normal-msg ret)))
          ret)))))


(defn person-matches-name-and-email
  [person name email]
  (let [name-match (or (= name (:display-name person))
                       (get (:aliases person) name))
        email-match (get (:emails person) email)]
    (cond (and name-match email-match) :full-match
          name-match :match-name-but-not-email
          email-match :match-email-but-not-name
          :else nil)))


(defn find-by-name-and-email
  [people {:keys [name email]}]
  (let [matches (map (fn [p] {:person p
                              :match-type (person-matches-name-and-email
                                           p name email)})
                     people)
        grouped (group-by :match-type matches)
        full-matches (map :person (:full-match grouped))
        partial-matches (concat
                         (map :person (:match-name-but-not-email grouped))
                         (map :person (:match-email-but-not-name grouped)))]
    (cond
     (= (count full-matches) 1)
     ;; Good.  Assume it is that person, even if there were other
     ;; partial matches.
     [:one-full-match (first full-matches)]

     (> (count full-matches) 1)
     ;; weird.  People database looks like it needs correcting.
     [:bad-db-multiple-full-matches full-matches]

     (> (count partial-matches) 0)
     [:only-partial-matches partial-matches]

     :else [:no-matches nil])))


(defn extract-name-and-email
  [s]
  (if-let [[_ name email] (re-find #"^\s*(.*?)\s*<(.+@.+)>\s*$" s)]
    {:name name :email email}
    {:name s}))


(defn git-patch-authors
  "Find and extract a sequence of authors from a git format patch.
These are what comes after \"From: \" on every line beginning with
that string, and typically includes a name followed by an email
address <in angle brackets>.  Each unique string will be returned only
once, using a set."
  [s]
  (->> s
       (re-seq #"(?m)^From: (.*)$")
       (map second)
       (set)))


(defn one-author-contributor-status
  [people {:keys [name email] :as author}]
  (let [[match-kind x] (find-by-name-and-email people author)]
    (if (= match-kind :one-full-match)
      {:contributor-status (if (:contributor x)
                             :contributor
                             :not-contributor)
       :display-name (:display-name x)}
      {:contributor-status match-kind})))


(defn patch-authors-contributor-status
  [patch-authors people]
  (->> patch-authors
       (map extract-name-and-email)
       (map (fn [p]
              (merge p (one-author-contributor-status people p))))))


(defn patch-type
  "Use the patch's actual type if specified, otherwise the guessed
type."
  [p]
  (or (:actual-type p) (:guessed-type p)))


(defn apply-git-patch [p patch-filename idx num-patches]
  (let [{:keys [exit out err]} (try-cmd "git" "am" "--keep-cr"
                                        "--ignore-whitespace" "-s"
                                        patch-filename)]
    (if (zero? exit)
      (merge p {:patch-status :ok :patch-msg "Success."})
      ;; Otherwise check whether the output says it is because the
      ;; patch did not apply cleanly.
      (if (and (re-find #"Patch failed" out)
               (re-find #"git am --abort" out))
        (let [abort-ret (try-cmd "git" "am" "--abort")]
          (if (zero? (:exit abort-ret))
            ;; As long as the abort succeeded, return status about
            ;; the failed patch.
            (merge p {:patch-status :fail
                      :patch-msg "Failed."})
            (merge p {:patch-status :unrecoverable-fail
                      :patch-msg (format "Failed, and then attempt to 'git am --abort' failed with exit status %d.
Aborting eval-patch! to avoid corrupting the git repo." (:exit abort-ret))})))
        ;; else I don't know what went wrong here.  Better stop
        ;; everything, because proceeding could mess up the git
        ;; repo.
        (merge p {:patch-status :unrecoverable-fail
                  :patch-msg (format "Failed.  'git am' command had exit status %d, but its output did not have both
'Patch failed' and 'git am --abort' in it.  Aborting since it is not known how
to get git repo back into state to continue." exit)})))))


(defn apply-non-git-patch [p patch-filename idx num-patches]
  (let [patch-opts (or (:patch-opts p) [ "-p1" ])
        patch-cmd (concat [ "patch" ] patch-opts
                          [ "--batch" (str "--input=" patch-filename) ])
        {:keys [exit out err]} (apply try-cmd patch-cmd)]
    (if (zero? exit)
      ;; Check for unusual output, since patch --batch can
      ;; automatically choose to do some things you might not want to
      ;; do.
      (if (re-find #"Assuming -R" out)
        (merge p {:patch-status :fail
                  :patch-msg "Failed.  patch command reversed direction of patch.
Check it to see if it was created incorrectly."})
        (merge p {:patch-status :ok :patch-msg "Success."}))
      ;; otherwise this one is messed up for some reason, but we can
      ;; blow away the branch and keep going.
      (merge p {:patch-status :fail
                :patch-msg "Failed."}))))


;; "acceptable" here simply means that check-ant-output will treat the
;; build as successful if these warnings/errors appear in the ant
;; output.  We do this by first removing "acceptable" warnings or
;; errors from the ant output string, then after that checking for
;; other warnings or errors.
;;
;; Ideally this function shouldn't remove anything.  Strive to remove
;; as little as possible, and only for those OS/JDK combos where we
;; have problems we don't yet know how to work around otherwise.

(defn remove-acceptable-ant-output-problems [s]
  (let [p (System/getProperties)
        orig-s s
        s (if (and (= "Oracle Corporation" (get p "java.vendor"))
                   (.startsWith ^String (get p "java.version") "1.7.0"))
            (-> s
                (str/replace #"(?xms)
(^ compile-java: \s* $
 .*)
^ \s* \[javac\]\ warning:\ \[options\]\ bootstrap\ class\ path\ not\ set\ in\ conjunction\ with\ -source\ 1\.5 \s* $
(.*)
^ \s* \[javac\]\ 1\ warning \s* $
(.*
 ^ compile-clojure: \s* $)"
                             "$1$2$3")
                (str/replace #"(?xms)
(^ compile-tests: \s* $
 .*)
^ \s* \[javac\]\ warning:\ \[options\]\ bootstrap\ class\ path\ not\ set\ in\ conjunction\ with\ -source\ 1\.5 \s* $
(.*)
^ \s* \[javac\]\ 1\ warning \s* $
(.*
 ^ test: \s* $)"
                             "$1$2$3"))
            s)]
    (comment
      (printf "andy-debug: remove-acceptable-ant-output-problems ")
      (if (= orig-s s)
        (printf "left ant output UNCHANGED\n")
        (printf "CHANGED ant output\n"))
      (flush))
    s))


(defn check-ant-output [s]
  (condp re-find (remove-acceptable-ant-output-problems s)
    #"(?im)^.*compile failed.*$" :>> (fn [m] {:ant-status :fail, :ant-msg m})
    #"(?im)^.*[1-9]\d* failures, 0 errors.*$" :>> (fn [m] {:ant-status :fail, :ant-msg m})
    #"(?im)^.*0 failures, [1-9]\d* errors.*$" :>> (fn [m] {:ant-status :fail, :ant-msg m})
    #"(?im)^.*[1-9]\d* failures, [1-9]\d* errors.*$" :>> (fn [m] {:ant-status :fail, :ant-msg m})
    #"(?m)^.*FAIL in.*$" :>> (fn [m] {:ant-status :fail, :ant-msg m})
    #"(?im)^.*reflection warning.*$" :>> (fn [m] {:ant-status :fail, :ant-msg m})
    #"(?im)^.*warning.*$" :>> (fn [m] {:ant-status :fail, :ant-msg m})
    #"(?m)^.*BUILD FAILED.*$" :>> (fn [m] {:ant-status :fail, :ant-msg m})
    {:ant-status :ok, :ant-msg "Success."}))


(comment
  (if-let [[clean-out
            init-out
            compile-java-out
            compile-clojure-out
            build-out
            compile-tests-out
            test-out
            jar-out
            all-out]
           (re-find #"(?msx)
                      \A ^ Buildfile: .*clojure/build.xml \s* $
                      .*
                      ^ clean: \s* $
                      (.*)
                      ^ init: \s* $
                      (.*)
                      ^ compile-java: \s* $
                      (.*)
                      ^ compile-clojure: \s* $
                      (.*)
                      ^ build: \s* $
                      (.*)
                      ^ compile-tests: \s* $
                      (.*)
                      ^ test: \s* $
                      (.*)
                      ^ jar: \s* $
                      (.*)
                      ^ all: \s* $
                      (.*)
                      \Z
                      "
                    s)]
    ;; TBD: Do more detailed checks on each section of output here.
    {:ant-status :ok :ant-msg "Success."}
    {:ant-status :fail
     :ant-msg "ant output did not contain all major sections"})
)


(defn build-and-test-clojure [p]
  (try-cmd :throw-on-error "ant" "clean")
  (let [{:keys [exit out err]} (try-cmd "ant")
        p (merge p (check-ant-output out))]
    (cond
     ;; We've already found a problem just looking at the output, so
     ;; return it.  More specific error messages are good.
     (not= :ok (:ant-status p)) p
     ;; Everything is good
     (zero? exit) p
     ;; otherwise return a generic failure if the exit status was
     ;; non-0.
     :else (merge p {:ant-status :fail, :ant-msg "Unknown failure"}))))


(defn patch-file-name [p attach-dir]
  (str (att-dir-name (:ticket p) attach-dir)
       "/" (:name p)))


(defn add-author-info
  [p attach-dir people-info]
  (if (nil? (:name p))
    p
    (if (= :git-diff (patch-type p))
      (let [patch-filename (patch-file-name p attach-dir)
            patch-content (slurp patch-filename)
            author-info (patch-authors-contributor-status
                         (git-patch-authors patch-content) people-info)]
        (merge p {:patch-author-info author-info
                  :patch-author-summary
                  (if (every? #(= :contributor (:contributor-status %))
                              author-info)
                    :CA-ok
                    :not-CA-clean)}))
      ;; else not a git patch
      (merge p {:patch-author-info nil
                :patch-author-summary :not-git-patch}))))


(defn eval-patch! [p attach-dir idx num-patches
                   unmodified-clojure-dir temp-clojure-dir
                   try-to-build?]
  (if (nil? (:name p))
    ;; No patch to evaluate
    (do
      (iprintf "eval-patch! %d/%d %s no patch to evaluate for this ticket\n"
               (inc idx) num-patches (:ticket p))
      p)
    (let [logfile-name (str (att-dir-name (:ticket p) attach-dir) "/"
                            (:name p) "-log.txt")]
      (with-open [logf (io/writer logfile-name)]
        (binding [*cmd-log* logf]
          (iprintf "eval-patch! %d/%d %s %s\n"
                   (inc idx) num-patches (:ticket p) (:name p))
          ;; (1) Copy unmodified Clojure tree to a temporary working copy.
          (try-cmd :throw-on-error
                   "cp" "-pr" unmodified-clojure-dir temp-clojure-dir)
          ;; (2) Try to apply the patch
          (try
            (sh/with-sh-dir temp-clojure-dir
              (let [patch-filename (patch-file-name p attach-dir)
                    {:keys [patch-status patch-msg] :as p}
                    (case (patch-type p)
                      :git-diff
                      (apply-git-patch p patch-filename idx num-patches)
                      :non-git-diff
                      (apply-non-git-patch p patch-filename idx num-patches)
                      ;; default case:
                      (merge p {:patch-status :not-patch
                                :patch-msg "Attachment file not recognized as a patch."}))]
                (when *cmd-log*
                  (iprintf *cmd-log* "Patch status: %s (%s)\n" patch-msg
                           patch-status))
                (iprintf "    Patch status: %s (%s)\n" patch-msg patch-status)
                (case patch-status
                  :ok
                  (if try-to-build?
                    ;; (3) Try to build and test
                    (let [{:keys [ant-status ant-msg] :as p} (build-and-test-clojure p)]
                      (when *cmd-log*
                        (iprintf *cmd-log* "Build status: %s (%s)\n" ant-msg
                                 ant-status))
                      (iprintf "    Build status: %s (%s)\n" ant-msg ant-status)
                      p)
                    ;; otherwise don't try to build and test
                    p)
                  
                  :fail ;; patch attempt failed.
                  ;; Don't try to build, but try other patches if there are
                  ;; more.
                  p
                  
                  :not-patch  ;; file didn't look like a patch
                  p
                  
                  :unrecoverable-fail
                  ;; Throw exception to stop any attempt to apply later
                  ;; patches, if there are more.
                  (throw (Exception. ^String (:patch-msg p))))))
            (finally
             ;; (4) Clean up: remove temp Clojure tree
             (iprintf (str "rm -fr " temp-clojure-dir "\n"))
             (try-cmd :throw-on-error "rm" "-fr" temp-clojure-dir))))))))


(defn eval-patches! [patches attach-dir unmodified-clojure-dir
                     temp-clojure-dir try-to-build?]
  (if-not (clojure-git-dir? unmodified-clojure-dir)
    (iprintf *err* "eval-patches!: '%s' is not a Clojure git repo root directory.\n" unmodified-clojure-dir)
    (let [n (count patches)]
      (doall
       (map-indexed (fn [idx p] (eval-patch! p attach-dir idx n
                                             unmodified-clojure-dir
                                             temp-clojure-dir try-to-build?))
                    patches)))))


(defn name-or-default [p k deflt]
  (if-let [s (get p k)]
    (name s)
    deflt))


(defn trunc-str [s max-length]
  (if (> (count s) max-length)
    (subs s 0 max-length)
    s))


(defn one-patch-summary [p]
  (let [ai (:patch-author-info p)
        auth-name (if-not (nil? ai)
                    (or (:display-name (first ai))
                        (:name (first ai))))]
    ;; Properties of the ticket as a whole
    (iprintf "%-8s %1s %2s %-8s %-11s"
             (:ticket p)
             (subs (:type p) 0 1)
             (:votes p)
             (trunc-str (or (get p "Approval") "--") 8)
             (trunc-str (or (:fixVersion p) "--") 11))
    ;; Properties specific to each patch
    (iprintf " %-13s %-10s %-5s %-15s %s\n"
             (name-or-default p :patch-author-summary "--")
             (name-or-default p :patch-status "--")
             (name-or-default p :ant-status "--")
             (trunc-str (or auth-name "--") 15)
             (:name p))))


(defn eval-patches-summary [patches]
  (dorun (map (fn [p-group]
                (iprintf "\n")
                (dorun (map one-patch-summary p-group)))
              (partition-by :ticket patches))))


(defn dl-patches-check-ca!
  "Download all attachments for selected tickets.  Do this once on one
machine, not once for each OS/JDK combo I want to test."
  [cur-eval-dir patch-type-list ticket-dir clojure-tree]
  (doseq [patch-type patch-type-list]
    (let [as1 (xml->attach-info (str cur-eval-dir patch-type ".xml"))
          as2 (download-attachments! as1 ticket-dir)
          as3 (if clojure-tree
                (eval-patches! as2 ticket-dir clojure-tree "./temp-clojure"
                               false)
                as2)
          as4 (let [people-info (read-safely "data/people-data.clj")]
                (map #(add-author-info % ticket-dir people-info) as3))]
      (spit-pretty (str cur-eval-dir patch-type "-downloaded-only.txt") as4)
      (spit (str cur-eval-dir patch-type "-author-info.txt")
            (with-out-str (eval-patches-summary as4))))))


(defn do-eval-check-ca!
  "Assume dl-patches-check-ca! has already been run, and we have a
file <patch_type>-downloaded-only.txt containing a big Clojure map
describing them all, and separate files for each downloaded patch in
the directory structure created by dl-patches-check-ca!  For the
version of the Clojure repo in the local directory whose path name is
given by clojure-tree, for each patch to evaluate copy it, try to
apply the patch, and try to build with 'ant' in that copy."
  [cur-eval-dir ticket-dir clojure-tree patch-type-list]
  (doseq [patch-type patch-type-list]
    (let [fname2 (str cur-eval-dir patch-type "-downloaded-only.txt")
          as2 (read-safely fname2)
          as3 (eval-patches! as2 ticket-dir clojure-tree "./temp-clojure" true)
          as4 (let [people-info (read-safely "data/people-data.clj")]
                (map #(add-author-info % ticket-dir people-info) as3))]
      (spit-pretty (str cur-eval-dir patch-type "-evaled-authors.txt") as4)
      (spit (str cur-eval-dir patch-type "-patch-summary.txt")
            (with-out-str (eval-patches-summary as4))))))


;; Desired order in which to show the categories relative to each
;; other.

(def +patch-category-show-order+
  [ "Doc string fixes only"
    "Better error reporting"
    "Debug/tooling enhancement"
    "Clojure language/library bug fixes"
    "Language enhancement, reducers"
    "Allow more correct-looking Clojure code to work"
    "Language/library enhancement"
    "Disable failing tests"
    "Performance enhancement"
    "Code cleanup"
    ])

(def +map-patch-category-to-show-order+
  (into {} (map-indexed (fn [idx cat] [cat idx])
                        +patch-category-show-order+)))


(defn prescreened? [att]
  (and (:name att)
       (= (:patch-author-summary att) :CA-ok)
       (= (:patch-status att) :ok)
       (= (:ant-status att) :ok)))

(defn next-release? [att]
  (= (:fixVersion att) "Release 1.5"))

(defn approval-in? [att approval-set]
  (approval-set (get att "Approval")))

(defn prescreened-not-screened-not-next-release? [att]
  (and (prescreened? att)
       (not (approval-in? att #{"Incomplete" "Not Approved" "Screened" "OK"}))
       (not (next-release? att))))

(defn prescreened-not-screened-next-release? [att]
  (and (prescreened? att)
       (not (approval-in? att #{"Incomplete" "Not Approved" "Screened" "OK"}))
       (next-release? att)))

(defn prescreened-and-screened? [att]
  (and (prescreened? att)
       (approval-in? att #{"Screened" "OK"})))

(defn prescreened-and-needs-work? [att]
  (and (prescreened? att)
       (approval-in? att #{"Incomplete" "Not Approved"})))

(defn not-prescreened-and-needs-work? [att]
  (and (not (prescreened? att))
       (approval-in? att #{"Incomplete" "Not Approved" "Vetted"})))

(defn patch-name-exists? [atts-for-ticket att-name]
  (first (filter #(= att-name (:name %)) atts-for-ticket)))


(defn warning-log-text [atts ppats att-fname ppat-fname]
  (with-out-str
    (let [atts-by-ticket (group-by :ticket atts)
          ppats-by-ticket (group-by :ticket ppats)
          
          open-tickets (set (map :ticket atts))
          prescreened-tickets (set (map :ticket (filter prescreened? atts)))
          ppat-tickets (set (map :ticket ppats))
          
          ppat-but-not-open-tickets (set/difference ppat-tickets open-tickets)

          prescreened-but-no-ppat-tickets (set/difference prescreened-tickets
                                                          ppat-tickets)

          dup-ppat-tickets (->> ppats-by-ticket
                                (filter-vals #(> (count %) 1))
                                keys
                                set)
        
          bad-patch-names
          (into {} (filter (fn [[ticket [ppat]]]
                             (not (patch-name-exists? (get atts-by-ticket ticket)
                                                      (:name ppat))))
                           ppats-by-ticket))

          ppat-is-not-prescreened
          (into {} (filter (fn [[ticket [ppat]]]
                             (let [as (get atts-by-ticket ticket)
                                   a (patch-name-exists? as (:name ppat))]
                               (not (prescreened? a))))
                           ppats-by-ticket))
          ]

      (if (empty? ppat-but-not-open-tickets)
        (printf "All tickets with preferred patches are for open JIRA tickets.")
        (printf "List of tickets in the preferred patch file '%s'
that are not in open JIRA tickets from file '%s'.
Check whether the ticket was closed, and remove from preferred patch file if so:
    %s"
                ppat-fname att-fname
                (str/join "\n    "
                          (sort-by extract-dec-num
                                   ppat-but-not-open-tickets))))

      (print "\n\n")
      (if (empty? prescreened-but-no-ppat-tickets)
        (printf "All tickets with prescreened patches have a preferred patch.")
        (printf "List of tickets with prescreened patches from file '%s'
but have no record in the preferred patch file '%s'.
Consider adding a record to the preferred patch file for them:
    %s"
                att-fname ppat-fname
                (str/join "\n    "
                          (sort-by extract-dec-num
                                   prescreened-but-no-ppat-tickets))))

      (print "\n\n")
      (if (empty? dup-ppat-tickets)
        (printf "Every ticket has at most one preferred patch.")
        (printf "List of tickets with more than one record in preferred patch file '%s'.
Consider removing all but one, perhaps merging the existing records somehow:
    %s"
                ppat-fname
                (str/join "\n    "
                          (sort-by extract-dec-num dup-ppat-tickets))))

      (print "\n\n")
      (if (empty? bad-patch-names)
        (printf "Every preferred patch exists in JIRA.")
        (printf "List of preferred patches from file '%s'
that have patch names that are not attached to open JIRA tickets from '%s'.
Update the patch name to a current prescreened patch:
    %s"
                ppat-fname att-fname
                (with-out-str (p/pprint bad-patch-names))))

      (print "\n\n")
      (if (empty? ppat-but-not-open-tickets)
        (printf "Every preferred patch is also prescreened.")
        (printf "List of preferred patches from file '%s'
that have patch names that are not prescreened in '%s'.
Check whether preferred patch should be updated, but this could happen
because there are no prescreened patches for the ticket at this time:
    %s"
                ppat-fname att-fname
                (with-out-str (p/pprint ppat-but-not-open-tickets))))
      )))


(defn preferred-patch-report-text [atts ppats filter-pred]
  (with-out-str
    (let [pref-pats (map-vals first (group-by (juxt :ticket :name) ppats))
          sel-atts (group-by :ticket (filter filter-pred atts))
          some-pref-pats
          (into {}
                (for [[ticket atts-of-ticket] sel-atts]
                  [ticket
                   (first (keep #(if-let [y (get pref-pats [ticket (:name %)])]
                                   (merge % y))
                                atts-of-ticket))
                   ]))
          tickets-with-pref-pats (filter-vals identity some-pref-pats)
          twpp-by-cat (group-by :patch-category (vals tickets-with-pref-pats))]
      
      (if (empty? twpp-by-cat)
        (printf "\n<none>\n")
        (doseq [cat (sort-by +map-patch-category-to-show-order+
                             (keys twpp-by-cat))]
          (printf "\n      %s:\n\n" cat)
          (doseq [twpp (sort-by #(extract-dec-num (:ticket %))
                                (twpp-by-cat cat))]
            (printf "%1s  %-8s %s\n"
                    (or (first (get twpp "Approval")) " ")
                    (:ticket twpp)
                    (:name twpp))
            (when (:patch-extra-note twpp)
              (printf "      %s\n" (:patch-extra-note twpp)))))))))


(defn not-prescreened-patch-report-text [atts filter-pred]
  (with-out-str
    (let [sel-atts (group-by :ticket (filter filter-pred atts))
          ;; Keep only those tickets that have no prescreened patches
          sel-tickets
          (filter-vals identity
                       (map-vals #(if-not (first (filter prescreened? %)) (first %))
                                 sel-atts))]

      ;; TBD: Consider adding code where I can add categories and/or
      ;; notes for tickets that have no prescreened patches, and those
      ;; categories and/or notes will appear in the report.

      ;; That would probably add more cases to my code for creating
      ;; the warning logs.

      (if (empty? sel-tickets)
        (printf "\n<none>\n")
        (doseq [ticket (sort-by (fn [att]
                                  [(- (extract-dec-num (or (:votes att) 0)))
                                   (extract-dec-num (:ticket att))])
                                (vals sel-tickets))]
          (printf "%1s  %2s %s\n"
                  (or (first (get ticket "Approval")) " ")
                  (or (:votes ticket) "0")
                  (:title ticket)))))))


(defn prescreened-report-text [atts ppats]
  (with-out-str
    (doseq [[filter-pred heading-str]
            [ [ prescreened-not-screened-not-next-release?
"----------------------------------------------------------------------
Prescreened patches *not* marked with Fix Version/s = \"Release 1.5\"
----------------------------------------------------------------------"
               ]
              [ prescreened-not-screened-next-release?
"----------------------------------------------------------------------
Prescreened patches that are marked with Fix Version/s = \"Release
1.5\", but not screened
----------------------------------------------------------------------"
               ]
              [ prescreened-and-screened?
"----------------------------------------------------------------------
Prescreened, and screened or accepted
----------------------------------------------------------------------"
               ]
              ]]
      (println)
      (print heading-str)
      (print (preferred-patch-report-text atts ppats filter-pred)))))


(defn prescreened-need-work-report-text [atts ppats]
  (with-out-str
    (doseq [[filter-pred heading-str]
            [ [ prescreened-and-needs-work?
"----------------------------------------------------------------------
Tickets with prescreened patches, but they may need work, since the
ticket is marked Incomplete (I) or Not Approved (N).
----------------------------------------------------------------------"
               ]
              ]]
      (println)
      (print heading-str)
      (print (preferred-patch-report-text atts ppats filter-pred)))))


(defn not-prescreened-need-work-report-text [atts]
  (with-out-str
    (doseq [[filter-pred heading-str]
            [
             [ next-release?
"----------------------------------------------------------------------
Tickets marked for Clojure release 1.5 that have no prescreened
patches (see also Note 3 at the bottom):
----------------------------------------------------------------------"
              ]

             [ #(approval-in? % #{"Vetted" "Incomplete" "Not Approved"})
"----------------------------------------------------------------------
Tickets needing work that have no prescreened patches.  These are all
Vetted (marked V), Incomplete (I), or Not Approved (N).  The number
after the letter is the number of votes, and tickets have been sorted
from most to fewest votes.
----------------------------------------------------------------------"
               ]
              ]]
      (println)
      (print heading-str)
      (println)
      (print (not-prescreened-patch-report-text atts filter-pred)))))


(defn ticket-plus-vote-info [ticket-xml-fname votes-fname]
  (let [;; votes-by-user is expected to be a map where:
        ;;
        ;; + The keys are themselves maps describing JIRA users.
        ;; + The values are lists of strings, where each string is a
        ;;   JIRA ticket name, e.g. "CLJ-863".
        ;;
        ;; The maps describing users are of this form:
        ;; {:display-name "Brian Siebert",
        ;;  :usernames #{"bsiebert"},
        ;;  :emails #{"bsiebert@fgm.com"}}
        votes-by-user (read-safely votes-fname)

        ;; From votes-by-user, calculate votes-by-ticket, which is a
        ;; map where:
        ;; + The keys are strings with JIRA ticket names, e.g. "CLJ-863"
        ;; + The values are lists of maps describing users who voted
        ;;   for the ticket.  The user maps are as explained above,
        ;;   plus one additional key :user-num-votes whose value is an
        ;;   integer, the number of tickets the user has voted for
        ;;   total.
        votes-by-ticket (reduce (fn [vs [ticket user]]
                                  (update-in vs [ticket] conj user))
                                {}
                                (mapcat
                                 (fn [[user ticks]]
                                   (let [u (assoc user
                                             :user-num-votes (count ticks))]
                                     (map (fn [t] [t u]) ticks)))
                                 votes-by-user))

        ;; From votes-by-ticket, calculate vote-info-by-ticket.  It is
        ;; the same as votes-by-ticket, except now each value is a map
        ;; with these keys:

        ;; :num-votes has a value equal to the number of users who
        ;; voted for the ticket.

        ;; :weighted-vote is the weighted vote for the ticket, equal
        ;; to the sum of 1/N for each user who voted on it, where N is
        ;; the number of votes that user cast.

        ;; :vote-list is a sorted list of the user maps described
        ;; above, of all users who voted for the ticket.
        vote-info-by-ticket
        (map-vals (fn [users]
                    {:num-votes (count users)
                     :weighted-vote (apply + (map #(/ 1 (:user-num-votes %))
                                                  users))
                     :vote-list (sort-by (fn [u]
                                           [(:user-num-votes u)
                                            (:display-name u)
                                            u])
                                         users)})
                  votes-by-ticket)

        tickets-info (ticket-info-from-xml (slurp ticket-xml-fname))]

    ;; Merge vote info for tickets with everything else we know about
    ;; them from JIRA.
    (reduce (fn [oti [ticket vote-info]]
              (update-in oti [ticket]
                         merge vote-info))
            tickets-info
            vote-info-by-ticket)))


;; Double-check calculated vote info with vote count on ticket.  If
;; they mismatch, then either votes have been cast while I downloaded
;; the info, or I am missing a user that cast a vote, and should get
;; an updated list of users.
(defn vote-diffs [ticket-info]
  (let [num-votes-from-tickets (filter-vals
                                #(and % (> % 0))
                                (into {}
                                      (map (fn [[ticket info]]
                                             [ticket
                                              (if (:votes info)
                                                (Long/parseLong (:votes info)))])
                                           ticket-info)))
        num-votes-from-vote-info (filter-vals identity
                                              (map-vals :num-votes ticket-info))
        ]
    (take 2 (data/diff num-votes-from-tickets num-votes-from-vote-info))))


(defn sort-key-weighted-vote-then-num-votes [[ticket vote-info]]
  [(- (or (:weighted-vote vote-info) 0))
   (- (or (:num-votes vote-info) 0))
   (extract-dec-num ticket)])


(defn sort-key-num-votes-then-weighted-vote [[ticket vote-info]]
  [(- (or (:num-votes vote-info) 0))
   (- (or (:weighted-vote vote-info) 0))
   (extract-dec-num ticket)])


(defn print-tickets [sorted-ticket-info col-order]
  (doseq [[ticket info] sorted-ticket-info]
    ;(pprint info)
    (doseq [col col-order]
      (case col
        :num-votes (printf " %3d" (:num-votes info))
        :weighted-vote (printf " %7.2f" (double (:weighted-vote info)))
        :ticket (printf " %-8s" ticket)
        :title (printf " %s" (:title info))
        :type (printf " %s" (subs (:type info) 0 1))
        :approval (printf " %-8s" (trunc-str (or (get info "Approval") "--") 8))
        :voter-details
        (printf "\n             %s"
                (str/join "\n             "
                          (map #(format "%s (%s)"
                                        (:display-name %)
                                        (let [nv (:user-num-votes %)]
                                          (if (and (number? nv) (> nv 1))
                                            (str "1/" nv)
                                            nv)))
                               (:vote-list info))))))
    (printf "\n")))


(defn print-top-tickets-by-vote-weight! [out-fname ticket-info]
  (let [tickets-with-votes (filter-vals #(and (:weighted-vote %)
                                              (> (:weighted-vote %) 0))
                                        ticket-info)]
    (spit out-fname
          (with-out-str
            (print-tickets (sort-by sort-key-weighted-vote-then-num-votes
                                    tickets-with-votes)
                           [:weighted-vote :num-votes :type :approval
                            :title :voter-details])))))



(comment

;; =================================================================
;; Older manual way to get XML info about open CLJ tickets (newer way
;; is shortly after the def's below)
;; =================================================================
;; Go to the Clojure Jira page, then to the filters, and look at the
;; tickets that match a particular filter.  There is a popup menu that
;; says "Views" in the upper right of the page.

;; Pick the XML view from that menu.  An XML form of the ticket list
;; will be shown.  Save the page as a file.  That is how I created the
;; file notclosed.xml, which I saved in the directory named by the
;; string cur-eval-dir below.
;; =================================================================

;; You also need to pull a clone of the Clojure repo if you haven't
;; done so already.
;;
;; % git clone git://github.com/clojure/clojure.git
;;
;; Put it in some directory and then change the symbol clojure-tree
;; below to name it.  The code for evaluating whether patches that
;; apply cleanly also build and test cleanly only runs "ant", not
;; "./antsetup.sh", to save time, so you must run "./antsetup.sh" in
;; that directory after creating it, or else all of the "ant" runs
;; will fail their tests.

(use 'clj-prescreen.core 'clojure.pprint)
(require '[clojure.java.io :as io] '[fs.core :as fs])
(def cur-eval-dir (str @fs/cwd "/eval-results/2013-02-21/"))
(def clojure-tree "./eval-results/2013-02-13-clojure-to-prescreen/clojure")
(def ticket-dir (str cur-eval-dir "ticket-info"))
(def patch-type-list [ "open" ])
;; Note: Don't check any password into git
(def auth-info {:basic-auth ["jafingerhut" "tbd-password-here"]})
;;(def patch-type-list [ "screened" "incomplete" "np" "rfs"])
;;(def patch-type-list [ "notclosed" ])

;; Download info about all open tickets and save in file open.xml.
;; Download votes cast by each CLJ JIRA user and save in file
;; votes-on-tickets.clj.
(dl-open-tickets! (str cur-eval-dir "open.xml"))
(let [fname (str cur-eval-dir "votes-on-tickets.clj")
      all-users (read-safely "data/all-clojure-jira-users.clj")
      votes-by-user (dl-open-ticket-votes! all-users auth-info true)]
  (spit-pretty fname votes-by-user))


;; Read JIRA ticket info from open.xml and vote info from
;; votes-on-tickets.clj, and combine them into one data structure
;; open-tickets-info.
(def open-tickets-info (ticket-plus-vote-info
                        (str cur-eval-dir "open.xml")
                        (str cur-eval-dir "votes-on-tickets.clj")))

(vote-diffs open-tickets-info)
;; If vote-differences is anything other than '(nil nil), there is a
;; mismatch.  Either votes have been cast while I downloaded the info,
;; or I am missing a user, and should get an updated user list.  See
;; Note 2.

;; Print a report of top tickets sorted from highest weighted vote to
;; lowest.
(print-top-tickets-by-vote-weight!
 (str cur-eval-dir "top-tickets-by-weighted-vote.txt") open-tickets-info)

;; TBD: Consider adding code to dl-patches-check-ca! that reads the
;; votes file, and combines the list of users who have voted for each
;; ticket, with each of their vote weights, and a single field giving
;; the total weighted vote for the ticket.

;; TBD: Include the weighted vote count and the normal vote count in
;; the prescreened and needs work reports.

;; Download all attachments for selected tickets.  Do this once on one
;; machine, not once for each OS/JDK combo I want to test.  Also, for
;; all git format patches, check people data to see if they have
;; signed a Clojure CA.
(dl-patches-check-ca! cur-eval-dir patch-type-list ticket-dir clojure-tree)

;; Evaluate downloaded attachments.  Do this once for each OS/JDK
;; combo.
(do-eval-check-ca! cur-eval-dir ticket-dir clojure-tree patch-type-list)

;; After doing the dl-patches-check-ca! above, if you edit
;; data/people-data.clj and want to redo the author evaluations only,
;; do this:
(doseq [patch-type patch-type-list]
  (let [fname1 (str cur-eval-dir patch-type "-downloaded-only.txt")
        fname-sum (str cur-eval-dir patch-type "-author-info.txt")
        as1 (read-safely fname1)
        as1 (let [people-info (read-safely "data/people-data.clj")]
              (map #(add-author-info % ticket-dir people-info) as1))]
    (spit-pretty fname1 as1)
    (spit fname-sum (with-out-str (eval-patches-summary as1)))))

;; After doing the do-eval-check-ca! above, and hand-edited a file
;; containing the "preferred patches" to show in the prescreened patch
;; list, do the below to generate part of the prescreened patch
;; and tickets needing work reports.
(doseq [patch-type patch-type-list]
  (let [fname1 (str cur-eval-dir patch-type "-evaled-authors.txt")
        atts (read-safely fname1)
        ppat-fname "./data/preferred-patches.clj"
        ppats (read-safely ppat-fname)
        fname-warnings (str cur-eval-dir patch-type "-warnings.txt")
        fname-prescreened (str cur-eval-dir patch-type "-prescreened-report.txt")
        fname-need-work (str cur-eval-dir patch-type "-needs-work.txt")]
    (spit fname-warnings (warning-log-text atts ppats fname1 ppat-fname))
    (spit fname-prescreened (prescreened-report-text atts ppats))
    (spit fname-need-work
          (str (prescreened-need-work-report-text atts ppats)
               (not-prescreened-need-work-report-text atts)))
    ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Note 2:
;;
;; Instructions to update the list of all Clojure JIRA users:
;;
;; Note: You must have admin access on the Clojure JIRA web site to
;; get a list of all users like this.
;;
;; Go to this link: http://dev.clojure.org/jira/browse/CLJ
;;
;; Click "Administration" link next to your name near the upper right
;; corner of the page.
;;
;; In the "Users" section, click the "Users" link.  Re-enter your
;; password if it prompts you for it.
;;
;; In the "Filter Users" section, change "Users Per Page" to "All",
;; then click "Filter" button beneath that to update the page.
;;
;; Wait a while for the complete list of all users to be generated and
;; included on the page.
;;
;; Tested only with Firefox 18 on Mac OS X so far: File -> Save Page
;; As...  Choose format "Web Page, complete".  Save it as file
;; data/all-clojure-jira-users.html
;;
;; Run this hacked-up Perl program to extract the user data and create
;; all-clojure-jira-users.clj:
;;
;; ./extract-user-info.pl all-users.html >| all-clojure-jira-users.clj 
;;
;; It is hacked up because it does line-by-line regex parsing of the
;; HTML file, which is fragile in the sense that it is based upon how
;; the HTML is broken up into lines.  I don't know off hand of a good
;; way to do something more robust than that.  It seems to work for
;; now.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;; Older steps for creating some other reports about votes on tickets
;; in JIRA.

(def open-tickets-1-vote-or-more (filter-vals
                                  #(and (:weighted-vote %)
                                        (> (:weighted-vote %) 0))
                                  open-tickets-info))

;; Print sequence of tickets in descending order of number of votes,
;; then by descending order of weighted vote, then by ticket number as
;; a tie-breaker.
(print-tickets (sort-by sort-key-num-votes-then-weighted-vote
                        open-tickets-1-vote-or-more)
               [:num-votes :weighted-vote :type :approval :title :voter-details])
  
;; Print users in descending order of number of votes cast on open
;; tickets, with a list of CLJ-<n> ticket numbers they have voted on.
(require '[clojure.string :as str])
(def vote-count-by-user (map-vals count votes-by-user))
(doseq [[user votes] (sort-by (fn [[k v]] [(- v) (:display-name k)])
                              (filter-vals #(not (zero? %)) vote-count-by-user))]
  (printf "%3d %s (%s)\n" votes (:display-name user)
          (str/join " " (sort (map extract-dec-num (votes-by-user user))))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Beginning of older copy-and-paste one-step-at-a-time method
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;(def patch-type "screened")
;;(def patch-type "incomplete")
;;(def patch-type "rfs")
;;(def patch-type "np")

(def as1 (xml->attach-info (str cur-eval-dir patch-type ".xml")))
(pprint (take 10 as1))
(def as2 (download-attachments! as1 ticket-dir))
(spit-pretty (str cur-eval-dir patch-type "-info.txt") as2)

;; See Note 1 below about editing.

(def as2 (read-safely (str cur-eval-dir patch-type "-info.txt")))
;; Evaluate all patches:
(def as3 (eval-patches! as2 ticket-dir "./clojure" true))
;; Evaluate one patch:
;; TBD

(spit-pretty (str cur-eval-dir patch-type "-evaled.txt") as3)

(def as3 (read-safely (str cur-eval-dir patch-type "-evaled.txt")))
;; Update author info, perhaps after editing "data/people-data.clj"
(def as4 (let [people-info (read-safely "data/people-data.clj")]
           (map #(add-author-info % ticket-dir people-info) as3)))

(spit-pretty (str cur-eval-dir patch-type "-evaled-authors.txt") as4)
(eval-patches-summary as4)


;; Testing with 1 patch at a time.
(use 'clj-prescreen.core 'clojure.pprint)
(require '[clojure.java.io :as io])
(def as2 (read-safely "att-1-non-git-wrong-opts.txt"))
(def as3 (eval-patches! as2 ticket-dir "../clojure"))

(def as2 (read-safely "att-2-non-git-hand-corrected-opts.txt"))


;; Older expressions I used for learning how clojure.data.zip.xml
;; works while writing xml->attach-info:

(use 'clojure.data.zip.xml 'clojure.pprint)
(require '[clojure.xml :as xml] '[clojure.zip :as zip] '[clojure.inspector :as i])
(def z (zip/xml-zip (xml/parse "rfs.xml")))
(def tickets (dzx/xml-> z :channel :item))
(count tickets) ; => 53

(def t1 (first tickets))
(def k (dzx/xml1-> t1 :key dzx/text))
(def title (dzx/xml1-> t1 :title dzx/text))
(def atts (dzx/xml-> t1 :attachments :attachment))

(->> (dzx/xml-> t1 :attachments :attachment)
     (map (fn [att] (:attrs (first att))))
     (map #(merge % {:key k :title title}))
     )

)


;; ======================================================================
;; Note 1
;; ======================================================================
;; A developer potentially edits the file att-info-printed.txt at this
;; point.

;; Useful things to do:

;; Optionally add a key :actual-type with one of the following values:

;; + :git-diff      (i.e. use 'git am --keep-cr --ignore-whitespace -s < attach-file' to apply)
;; + :non-git-diff  (i.e. use 'patch -p1 < attach-file' to apply)
;;   If -p1 is wrong, add another key like so:   :patch-opts [ "-p0" ]

;; or any other value you want to describe what is in the attachment.

;; If :actual-type is one of the above, it will be considered a patch
;; in the appropriate format, and the next step will try to apply the
;; patch, then compile and run tests for Clojure.

;; If :actual-type does not exist for an attachment, the next step
;; will use :guessed-type instead, so there is no need to add an
;; :actual-type key/val pair if :guessed-type is correct.


;; ======================================================================
;; Information clj-prescreen should add to each attachment:

;; (0) TBD: When was the evaluation done?  Against what version of
;; Clojure source code?  What OS and JVM were used?

;; (a) Did it apply cleanly?  Whether it did or not, store the patch
;; command output for future reference.

;; Note: also good to include output of 'git status .' to show if any
;; new files were created as a result of patch failing to apply.

;; (b) Did the build and all tests succeed?  Whether it did or not,
;; store the build output for future reference.

;; (c) Who are the people who write and/or submitted the patch, and
;; have they signed CAs?  If not, who hasn't?

;; TBD: Make mappings between :author field values like "jszakmeister"
;; and names on the official contributor list, and email addresses
;; that they use in submitted patches.  Use these to help quickly
;; detect whether a patch is "CA clean", and if not, warn about it.
;; Such a warning might mean the patch is still "CA clean" -- the
;; mapping just needs to be updated.  Consider writing code for
;; downloading info mapping Jira account names <-> email addresses.
;; Some of it will have to be done manually, but preferably only once,
;; and won't change rapidly after that.

;; TBD: Make it quick and easy to use this code to evaluate just one
;; patch, perhaps specified merely by the ticket name and the
;; attachment file name.
