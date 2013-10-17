(ns clj-prescreen.core
  (:import (java.io File ByteArrayInputStream))
  (:import (org.joda.time LocalDate DateTime Duration))
  (:require [clojure.xml :as xml]
            [clojure.data :as data]
            [clojure.data.zip.xml :as dzx]
            [clojure.tools.reader.edn :as edn]
            [clojure.zip :as zip]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.java.shell :as sh]
            [clojure.pprint :as p]
            [clj-http.client :as http]
            [me.raynes.fs :as fs]))

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


(defn filter-keys [f m]
  (into (empty m)
        (filter (fn [[k _]] (f k)) m)))

(defn filter-vals [f m]
  (into (empty m)
        (filter (fn [[_ v]] (f v)) m)))


(defn extract-dec-num [s]
  (if-let [num-str (re-find #"\d+" s)]
    (bigint num-str)))


(defn days-between-dates [^DateTime earlier-joda-date
                          ^DateTime later-joda-date]
  (. (Duration. earlier-joda-date later-joda-date) getStandardDays))


(defn ticket-fields [ticket]
  (let [fields [:key :title :type :attrs :status :resolution :reporter :labels
                :created :resolved :updated :votes :watches :fixVersion]
        tick-name (dzx/xml1-> ticket :key dzx/text)
        t (into {} (map (fn [fld]
                          [ fld
                            (let [vals (seq (dzx/xml-> ticket fld dzx/text))]
                              (cond
                               (= fld :fixVersion) vals
                               (<= (count vals) 1) (first vals)
                               :else (do
                                       (iprintf *err* "Warning: ticket-fields found multiple values for field %s of ticket '%s':\n%s\n"
                                                fld tick-name vals)
                                       vals)))
                           ])
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
    [project (bigint tick-num-str)]
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


(defn url-all-CLJ-tickets []
  "http://dev.clojure.org/jira/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?jqlQuery=Project%3DCLJ&tempMax=2000")


(defn url-all-open-CLJ-tickets []
  "http://dev.clojure.org/jira/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?jqlQuery=Project%3DCLJ+and+status+not+in+%28Closed%2CResolved%29&tempMax=1000")


(defn url-all-open-non-CLJ-tickets []
  "http://dev.clojure.org/jira/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?jqlQuery=Project%21%3DCLJ+and+status+not+in+%28Closed%2CResolved%29&tempMax=1000")


(defn url-for-tickets-voted-by-user [username]
  (let [url-part1 "http://dev.clojure.org/jira/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?jqlQuery=status+not+in+%28Closed%2CResolved%29+and+voter%3D%27"
        url-part2 "%27&tempMax=1000&field=title"]
    (str url-part1 username url-part2)))


(defn get-url-to-file!
  "Write body of response from HTTP GET request to specified url to a
file with name file-name"
  [file-name url]
  (let [resp (http/get url {:throw-exceptions false})]
    (if (= 200 (:status resp))
      (spit file-name (:body resp))
      (do
        (binding [*out* *err*]
          (println
           (format "Got response status %d when trying to get URL:\n%s\n"
                   (:status resp) url)))))))


(defn dl-all-CLJ-tickets!
  "Download XML data about all CLJ tickets and save it to a local
file."
  [file-name]
  (get-url-to-file! file-name (url-all-CLJ-tickets)))


(defn dl-open-tickets!
  "Download XML data about all open CLJ tickets and save it to a local
file."
  [file-name project]
  (get-url-to-file! file-name (if (= project :CLJ)
                                (url-all-open-CLJ-tickets)
                                (url-all-open-non-CLJ-tickets))))


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


(defn split-lines-preserve-CRs
  "Like clojure.string/split, but preserves \\r characters."
  [s]
  (str/split s #"\n"))


(defn git-patch-added-line-includes-CR [line-str]
  (re-find #"^\+.*\r" line-str))


(defn patch-adds-lines-with-CRs [patch-filename]
  (->> patch-filename
       slurp
       split-lines-preserve-CRs
       (some git-patch-added-line-includes-CR)))


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
      (cond (zero? (count err))
            (if (patch-adds-lines-with-CRs patch-filename)
              (merge p {:patch-status :warn :patch-msg "Warning: adds CRs"})
              (merge p {:patch-status :ok :patch-msg "Success."}))

            (re-find #"warn" err)
            (merge p {:patch-status :warn :patch-msg "Warning."})

            :else
            (merge p {:patch-status :stderr :patch-msg "Something on stderr."}))
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

(defn remove-acceptable-ant-output-problems [s system-props]
  (let [p system-props
        orig-s s
        s (cond (and (= "Oracle Corporation" (get p "java.vendor"))
                     (.startsWith ^String (get p "java.version") "1.7.0"))
                (-> s
                    (str/replace #"(?xms)
(^ compile-java: \s* $
 .*)
^ \s* \[javac\]\ warning:\ \[options\]\ bootstrap\ class\ path\ not\ set\ in\ conjunction\ with\ -source\ 1\.[56] \s* $
(.*)
^ \s* \[javac\]\ 1\ warning \s* $
(.*
 ^ compile-clojure: \s* $)"
                                 "$1$2$3")
                    (str/replace #"(?xms)
(^ compile-tests: \s* $
 .*)
^ \s* \[javac\]\ warning:\ \[options\]\ bootstrap\ class\ path\ not\ set\ in\ conjunction\ with\ -source\ 1\.[56] \s* $
(.*)
^ \s* \[javac\]\ 1\ warning \s* $
(.*
 ^ test: \s* $)"
                                 "$1$2$3"))

                (and (= "Oracle Corporation" (get p "java.vendor"))
                     (.startsWith ^String (get p "java.version") "1.8.0"))
                (-> s
                    (str/replace #"(?xms)
(^ compile-java: \s* $
 .*)
^ \s* \[javac\]\ warning:\ \[options\]\ bootstrap\ class\ path\ not\ set\ in\ conjunction\ with\ -source\ 1\.[56] \s* $
(.*
 ^ compile-clojure: \s* $)"
                                 "$1$2")
                    (str/replace #"(?xms)
(^ compile-tests: \s* $
 .*)
^ \s* \[javac\]\ warning:\ \[options\]\ bootstrap\ class\ path\ not\ set\ in\ conjunction\ with\ -source\ 1\.[56] \s* $
(.*
 ^ test: \s* $)"
                                 "$1$2"))
                :else s)]
    (comment
      (printf "andy-debug: remove-acceptable-ant-output-problems ")
      (if (= orig-s s)
        (printf "left ant output UNCHANGED\n")
        (printf "CHANGED ant output\n"))
      (flush))
    s))


(defn check-ant-output [s system-props]
  (condp re-find (remove-acceptable-ant-output-problems s system-props)
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
        system-props (System/getProperties)
        p (merge p (check-ant-output out system-props))]
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


(def next-release-short "1.6")

(def next-release (str "Release " next-release-short))

(defn next-rel? [fix-versions]
  (some #(= % next-release) fix-versions))

(defn backlog? [fix-versions]
  (some #(= % "Backlog") fix-versions))


(defn derived-ticket-state-seq [att]
  (let [status (:status att)
        open (contains? #{"Open" "In Progress" "Reopened"} status)
        approval (get att "Approval")
        fix-versions (:fixVersion att)
        next-rel (next-rel? fix-versions)
        backlog (backlog? fix-versions)
        patch (get att "Patch")
        bad-field-vals
        (filter identity
                [(when (not (contains? #{"Closed" "Resolved"
                                         "Open" "In Progress" "Reopened"}
                                       status))
                   "Bad: Unkn Status")
                 (when (not (or (nil? approval)
                                (contains? #{"Triaged" "Vetted" "Incomplete"
                                             "Screened" "Ok"}
                                           approval)))
                   "Bad: Unkn Approval")
                 (when (not (or (empty? fix-versions) next-rel backlog))
                   "Bad: Unkn Fix Version")
                 (when (not (or (nil? patch)
                                (contains? #{"Code" "Code and Test"}
                                           patch)))
                   "Bad: Unkn Patch")
                 ]) ]
    (cond
     (not (empty? bad-field-vals)) bad-field-vals
     (not open) ["Closed"]
     :else (filter identity
                   [(when (nil? approval) "Open")
                    (when (= approval "Triaged") "Triaged")
                    (when (and (= approval "Vetted")
                               (empty? fix-versions))
                      "Vetted")
                    (when backlog "Backlog")
                    (when (and next-rel
                               (= approval "Vetted")
                               (nil? patch))
                      "Needs Patch")
                    (when (and next-rel
                               (= approval "Vetted")
                               (contains? #{"Code" "Code and Test"} patch))
                      "Screenable")
                    (when (and next-rel (= approval "Incomplete")) "Incomplete")
                    (when (and next-rel (= approval "Screened")) "Screened")
                    (when (and next-rel (= approval "Ok")) "Ok")
                    ]))))


(defn derived-ticket-state [att]
  (let [s (derived-ticket-state-seq att)
        n (count s)]
    (cond (= 1 n) (first s)
          (zero? n) "Bad: Match no state"
          :else (str "Bad: Match >1 state: "
                     (str/join ", " s)))))
  

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


(defn add-preferred-patch-info
  [p pref-pats]
  (if (nil? (:name p))
    p
    (if-let [pref-pat (get pref-pats (:ticket p))]  ;; (:name p)])]
      (if (= (:name pref-pat) (:name p))
        (merge p {:preferred-patch :yes})
        ;; :no means a preferred patch is specified for this ticket,
        ;; but this patch is not the one.
        (merge p {:preferred-patch :no}))
      ;; Then there is no preferred patch specified for this ticket.
      ;; Do not use :preferred-patch :no, but distinguish this case by
      ;; having no :preferred-patch key for the patch at all.
      p)))


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
                  (:ok :warn)
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
                  
                  (:stderr :fail) ;; patch attempt failed.
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
  (if-not (.exists ^File (io/file unmodified-clojure-dir))
    (iprintf *err* "eval-patches!: No directory '%s' exists\n" unmodified-clojure-dir)
    (if-not (clojure-git-dir? unmodified-clojure-dir)
      (iprintf *err* "eval-patches!: '%s' is not a Clojure git repo root directory.\n" unmodified-clojure-dir)
      (let [n (count patches)]
        (doall
         (map-indexed (fn [idx p] (eval-patch! p attach-dir idx n
                                               unmodified-clojure-dir
                                               temp-clojure-dir try-to-build?))
                      patches))))))


(defn name-or-default [p k deflt]
  (if-let [s (get p k)]
    (name s)
    deflt))


(defn trunc-str [s max-length]
  (if (> (count s) max-length)
    (subs s 0 max-length)
    s))


(defn fixVersion-to-string [fv]
  (if fv
    (str/join ", " fv)
    "--"))


(defn preferred-patch-summary-status [p]
  (if-let [preferred-patch (:preferred-patch p)]
    (let [derived-state (derived-ticket-state p)]
      (cond (= preferred-patch :no) "notme"
            ;; otherwise :preferred-patch must be :yes
            (= (:patch-status p) :ok) "pp-ok"
            ;; otherwise something is not perfect with the patch
            (#{"Screenable" "Incomplete" "Screened" "Ok"} derived-state)
            "fixnow"
            (#{"Vetted"} derived-state) "fixsoon"
            :else "fixlater"))
    ;; In this case there is no preferred patch for the ticket
    ;; specified at all.  Make this visually distinct from
    ;; :preferred-patch :no in the report.
    "--"))


(defn one-patch-summary [p]
  (let [ai (:patch-author-info p)
        auth-name (if-not (nil? ai)
                    (or (:display-name (first ai))
                        (:name (first ai))))]
    ;; Properties of the ticket as a whole
    (iprintf "%-8s %1s %2s %-8s %-11s %-16s"
             (:ticket p)
             (subs (:type p) 0 1)
             (:votes p)
             (trunc-str (or (get p "Approval") "--") 8)
             (trunc-str (fixVersion-to-string (:fixVersion p)) 11)
             (trunc-str (derived-ticket-state p) 16))
    ;; Properties specific to each patch
    (iprintf " %-13s %-10s %-5s %-8s %-15s %s\n"
             (name-or-default p :patch-author-summary "--")
             (name-or-default p :patch-status "--")
             (name-or-default p :ant-status "--")
             (trunc-str (preferred-patch-summary-status p) 8)
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
  [cur-eval-dir patch-type-list ticket-dir ppat-fname clojure-tree]
  (doseq [patch-type patch-type-list]
    (let [as1 (xml->attach-info (str cur-eval-dir patch-type ".xml"))
          as2 (download-attachments! as1 ticket-dir)
          as3 (if clojure-tree
                (eval-patches! as2 ticket-dir clojure-tree "./temp-clojure"
                               false)
                as2)
          pref-pats (->> (read-safely ppat-fname)
                         (group-by :ticket)
                         (map-vals first))
          as3b (map #(add-preferred-patch-info % pref-pats) as3)
          as4 (let [people-info (read-safely "data/people-data.clj")]
                (map #(add-author-info % ticket-dir people-info) as3b))]
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
  [cur-eval-dir ticket-dir clojure-tree patch-type-list ppat-fname]
  (doseq [patch-type patch-type-list]
    (let [fname2 (str cur-eval-dir patch-type "-downloaded-only.txt")
          as2 (read-safely fname2)
          as3 (eval-patches! as2 ticket-dir clojure-tree "./temp-clojure" true)
          pref-pats (->> (read-safely ppat-fname)
                         (group-by :ticket)
                         (map-vals first))
          as3b (map #(add-preferred-patch-info % pref-pats) as3)
          as4 (let [people-info (read-safely "data/people-data.clj")]
                (map #(add-author-info % ticket-dir people-info) as3b))]
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
       (contains? #{:ok :warn} (:patch-status att))
       (= (:ant-status att) :ok)))

(defn all-but-prescreened? [att]
  (and (:name att)
       (= (:patch-author-summary att) :CA-ok)
       (contains? #{:ok :warn} (:patch-status att))
       (not= (:ant-status att) :ok)))

(defn next-release? [att]
  (some #(= % next-release) (:fixVersion att)))

(defn approval-in? [att approval-set]
  (approval-set (get att "Approval")))

(defn prescreened-not-screened-not-next-release? [att]
  (and (prescreened? att)
       (not (approval-in? att #{"Incomplete" "Not Approved" "Screened" "Ok"}))
       (not (next-release? att))))

(defn prescreened-not-screened-next-release? [att]
  (and (prescreened? att)
       (not (approval-in? att #{"Incomplete" "Not Approved" "Screened" "Ok"}))
       (next-release? att)))

(defn prescreened-and-screened? [att]
  (and (prescreened? att)
       (approval-in? att #{"Screened" "Ok"})))

(defn prescreened-and-needs-work? [att]
  (and (prescreened? att)
       (approval-in? att #{"Incomplete"})))

(defn not-prescreened-and-needs-work? [att]
  (and (not (prescreened? att))
       (approval-in? att #{"Incomplete" "Triaged" "Vetted"})))

(defn patch-name-exists? [atts-for-ticket att-name]
  (first (filter #(= att-name (:name %)) atts-for-ticket)))


(defn warning-log-text [atts ppats att-fname ppat-fname]
  (with-out-str
    (let [atts-by-ticket (group-by :ticket atts)
          ppats-by-ticket (group-by :ticket ppats)
          
          open-tickets (set (map :ticket atts))
          prescreened-atts (filter prescreened? atts)
          prescreened-tickets (set (map :ticket prescreened-atts))
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

          prescreened-but-not-marked-with-patch
          (->> prescreened-atts
               (filter #(not (#{"Code" "Code and Test"} (get % "Patch"))))
               (map :ticket)
               set)
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

      (print "\n\n")
      (if (empty? prescreened-but-not-marked-with-patch)
        (printf "Every prescreened patch's ticket has 'Patch' attribute of 'Code' or 'Code and Test'.")
        (printf "List of prescreened patches from file '%s'
whose tickets have 'Patch' attribute that is neither 'Code' nor 'Code and Test':
    %s"
                att-fname
                (with-out-str (p/pprint prescreened-but-not-marked-with-patch))))
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
            (printf "%1s  %-8s %-12s %s\n"
                    (or (first (get twpp "Approval")) " ")
                    (:ticket twpp)
                    (derived-ticket-state twpp)
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
               (str
"----------------------------------------------------------------------
Prescreened patches *not* marked with Fix Version/s = \"" next-release "\"
----------------------------------------------------------------------")
               ]
              [ prescreened-not-screened-next-release?
               (str
"----------------------------------------------------------------------
Prescreened patches that are marked with Fix Version/s =
\"" next-release "\", but not screened
----------------------------------------------------------------------")
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


(defn prescreened-needs-work-report-text [atts ppats]
  (with-out-str
    (doseq [[filter-pred heading-str]
            [ [ prescreened-and-needs-work?
"----------------------------------------------------------------------
Tickets with prescreened patches, but they may need work, since the
ticket is marked Incomplete (I).
----------------------------------------------------------------------"
               ]
              ]]
      (println)
      (print heading-str)
      (print (preferred-patch-report-text atts ppats filter-pred)))))


(defn not-prescreened-needs-work-report-text [atts ppats]
  (with-out-str
    (doseq [[filter-pred heading-str]
            [
             [ next-release?
              (str
"----------------------------------------------------------------------
Tickets marked for Clojure " next-release " that have no prescreened
patches (see also Note 3 at the bottom):
----------------------------------------------------------------------")
              ]

             [ #(and (not (next-release? %))
                     (approval-in? % #{"Triaged" "Vetted" "Incomplete"}))
"----------------------------------------------------------------------
Tickets needing work that have no prescreened patches.  These are all
Triaged (T), Vetted (V), or Incomplete (I).  The number after the
letter is the number of votes, and tickets have been sorted from most
to fewest votes.
----------------------------------------------------------------------"
               ]
              ]]
      (println)
      (print heading-str)
      (println)
      (print (not-prescreened-patch-report-text atts filter-pred)))

    (doseq [[filter-pred heading-str]
            [
             [ #(and (all-but-prescreened? %)
                     (not (next-release? %))
                     (not (approval-in? % #{"Triaged" "Vetted" "Incomplete"})))
"----------------------------------------------------------------------
Tickets not fitting in previous categories, but they have a git format
patch that applies cleanly to latest Clojure master, written by a
contributor, and it does not build and pass tests.
----------------------------------------------------------------------"
               ]
              ]]
      (println)
      (print heading-str)
      (println)
      (print (preferred-patch-report-text atts ppats filter-pred)))
    ))


(defn vote-project [ticket-str]
  (second (re-find #"^(.*)-\d+$" ticket-str)))


(defn all-vote-projects [votes-fname]
  (let [votes-by-user (read-safely votes-fname)
        vote-tickets (apply concat (vals votes-by-user))]
    (set (map vote-project vote-tickets))))


(defn ticket-plus-vote-info [ticket-xml-fname votes-fname project]
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
        votes-by-user (map-vals (fn [ticket-strs]
                                  (filter #(= (vote-project %) project)
                                          ticket-strs))
                                votes-by-user)

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

;;        tickets-info (ticket-info-from-xml (slurp ticket-xml-fname))
;;        tickets-info (filter-keys #(= (vote-project %) project) tickets-info)
        tickets-info (->> (slurp ticket-xml-fname)
                          (ticket-info-from-xml)
                          (filter-keys #(= (vote-project %) project)))]

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
                                                (bigint (:votes info)))])
                                           ticket-info)))
        num-votes-from-vote-info (filter-vals identity
                                              (map-vals :num-votes ticket-info))
        ]
    (take 2 (data/diff num-votes-from-tickets num-votes-from-vote-info))))


(defn sort-key-weighted-vote-then-num-votes [[ticket vote-info]]
  [(- (:weighted-vote vote-info 0))
   (- (:num-votes vote-info 0))
   (derived-ticket-state vote-info)
   (extract-dec-num ticket)])


(defn sort-key-num-votes-then-weighted-vote [[ticket vote-info]]
  [(- (:num-votes vote-info 0))
   (- (:weighted-vote vote-info 0))
   (extract-dec-num ticket)])


(defn print-tickets [sorted-ticket-info col-order]
  (doseq [[ticket info] sorted-ticket-info]
    ;(pprint info)
    (doseq [col col-order]
      (case col
        :num-votes (printf " %3d" (:num-votes info 0))
        :weighted-vote (printf " %7.2f" (double (:weighted-vote info 0)))
        :ticket (printf " %-8s" ticket)
        :title (printf " %s" (:title info))
        :type (printf " %s" (subs (:type info) 0 1))
        :approval (printf " %-8s" (trunc-str (or (get info "Approval") "--") 8))
        :fixVersion (printf " %-11s" (trunc-str (fixVersion-to-string (:fixVersion info)) 11))
        :derivedState (printf " %-12s" (trunc-str (derived-ticket-state info) 12))
        "Patch" (printf " %-11s" (trunc-str (or (get info "Patch") "--") 11))
        :voter-details
        (if (seq (:vote-list info))
          (printf "\n             %s"
                  (str/join "\n             "
                            (map #(format "%s (%s)"
                                          (:display-name %)
                                          (let [nv (:user-num-votes %)]
                                            (if (and (number? nv) (> nv 1))
                                              (str "1/" nv)
                                              nv)))
                                 (:vote-list info)))))))
    (printf "\n")))


(defn html-escape-text [s]
  (str/escape s {\& "&amp;" \< "&lt;" \> "&gt;"}))


(defn url-for-clj-ticket [ticket-abbrev]
  (str "http://dev.clojure.org/jira/browse/" ticket-abbrev))


(defn print-tickets-html-table [sorted-ticket-info col-order]
  ;; HTML table tag
  (printf "<table style=\"text-align: left; width: 950px;\" border=\"1\" cellpadding=\"2\" cellspacing=\"2\">\n")
  (printf "  <tbody>\n")

  ;; Headings
  (printf "    <tr>\n")
  (doseq [col col-order]
    (printf "        <td style=\"vertical-align: bottom;%s\">%s\n"
            (case col
              :voter-details " width: 200px;"
              "")
            (case col
              :weighted-vote "Weighted vote"
              :num-votes "# of Votes"
              :type "Type"
              :approval "Approval"
              :fixVersion "Fix Version(s)"
              :derivedState "State"
              "Patch" "Patch"
              :ticket-with-link "Ticket"
              :title "Summary"
              :voter-details "Voters"))
    (printf "        </td>\n"))
  (printf "    </tr>\n")

  ;; Ticket info
  (doseq [[ticket info] sorted-ticket-info]
    (let [orig-title (:title info)
          [_ ticket-abbrev summary] (re-find #"^\s*\[(\S+)\]\s*(.*)\s*$"
                                             orig-title)]
      (printf "    <tr>\n")
      (doseq [col col-order]
        (printf "        <td style=\"vertical-align: top;\">")
        (case col
          :weighted-vote (printf "%.2f" (double (:weighted-vote info 0)))
          :num-votes (printf "%d" (:num-votes info 0))
          :type (printf "%s" (subs (:type info) 0 1))
          :approval (printf "%s" (or (get info "Approval") "--"))
          :fixVersion (printf "%s" (fixVersion-to-string (:fixVersion info)))
          :derivedState (printf "%s" (derived-ticket-state info))
          "Patch" (printf "%s" (or (get info "Patch") "--"))
          :ticket-with-link (printf "<a href=\"%s\">%s</a>"
                                    (url-for-clj-ticket ticket-abbrev)
                                    ticket-abbrev)
          :title (printf "%s" (html-escape-text summary))
          :voter-details
          (printf "%s" (str/join "<br>\n"
                                 (map #(html-escape-text
                                        (format "%s (%s)"
                                                (:display-name %)
                                                (let [nv (:user-num-votes %)]
                                                  (if (and (number? nv) (> nv 1))
                                                    (str "1/" nv)
                                                    nv))))
                                      (:vote-list info)))))
        (printf "\n        </td>\n"))
      (printf "    </tr>\n")))

  (printf "  </tbody>\n")
  (printf "</table>\n"))


(defn local-date-str []
  (.toString (LocalDate/now) "MMMM d, yyyy"))


(defn print-top-ticket-header!
  [format project date-str]
  (case format
    :text
    (printf "Top %s tickets by weighted vote

Date: %s
 
Open %s tickets with at least one vote, sorted in descending order of
their \"weighted vote\".  At the end of each list are tickets with no
votes, but they have been at least Triaged.  For the CLJ project,
Triaged means that at least one Clojure screener thinks the ticket
describes a real issue.

Suppose someone has currently voted on N open tickets.  Then their
vote counts as 1/N for each of those tickets.  Thus voting on all
tickets has the same relative effect on their ranking as voting on no
tickets.  You must be selective to change the rankings.

Each person gets 1 weighted vote to divide up as they wish for each
project, e.g. 1 for CLJ, 1 for CLJS, 1 for MATCH, etc.

Each ticket is listed with:

<weighted vote>  <vote count>  <State>   [<project>-<n>] <summary line>
             voter #1 (weight that voter #1 contributes)
             voter #2 (weight that voter #2 contributes)
             ...

where State is one of the states in the JIRA flow diagram at

    http://dev.clojure.org/display/community/JIRA+workflow
"
            project date-str project)
    :html
    (printf "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">
<html>
<head>
<meta content=\"text/html; charset=UTF-8\" http-equiv=\"content-type\">
<title>Top %s tickets by weighted vote</title>
</head>
<body>

<h1>Top %s tickets by weighted vote</h1>

Date: %s<br>
<br>

Open %s tickets with at least one vote, sorted in descending order of
their <span style=\"font-style: italic;\">weighted vote</span>.&nbsp;
At the end of each list are tickets with no votes, but they have been
at least Triaged.  For the CLJ project, Triaged means that at least
one Clojure screener thinks the ticket describes a real issue.

<p>
Suppose someone has currently voted on <span style=\"font-style:
italic;\">N</span> open tickets.&nbsp; Then their vote counts as <span
style=\"font-style: italic;\">1/N</span> for each of those
tickets.&nbsp; Thus voting on all tickets has the same relative effect
on their ranking as voting on no tickets.&nbsp; You must be selective
to change the rankings.

<p>
Each person gets 1 weighted vote to divide up as they wish for each
project, i.e. 1 for CLJ, 1 for CLJS, 1 for MATCH, etc.

<p>
State is one of the states in the JIRA flow diagram <a
href=\"http://dev.clojure.org/display/community/JIRA+workflow\">here</a>.
"
            project project date-str project)))


(defn print-top-ticket-short-project-header!
  [format project]
  (case format
    :text
    (printf "

Project %s tickets"
            project)
    :html
    (printf "
<h2>
Project %s tickets
</h2>
"
            project)
    ))


(defn print-top-ticket-body!
  [ticket-info format]
  (let [tickets-with-votes (->> ticket-info
                                (filter-vals #(or (> (:weighted-vote % 0) 0)
                                                  (not= "Open" (derived-ticket-state %))))
                                (sort-by sort-key-weighted-vote-then-num-votes))
        tickets-by-type (group-by (fn [[ticket info]] (:type info))
                                  tickets-with-votes)]
    (doseq [ticket-type (sort (keys tickets-by-type))]
      (case format
        :text
        (do
          (printf "\n========================================\n%s\n\n"
                  ticket-type)
          (print-tickets (get tickets-by-type ticket-type)
                         [:weighted-vote :num-votes :derivedState
                          ;; "Patch"
                          :title :voter-details]))
        :html
        (do
          (printf "<h2>%s</h2>\n\n" ticket-type)
          (print-tickets-html-table (get tickets-by-type ticket-type)
                                    [:weighted-vote :num-votes :derivedState
                                     ;; "Patch"
                                     :ticket-with-link
                                     :title :voter-details]))))))


(defn print-top-tickets-by-vote-weight!
  [out-fname ticket-info format project]
  ;; TBD: Might be better to extract the date from the input somehow.
  (let [date-str (local-date-str)]
    (spit out-fname
          (with-out-str
            (print-top-ticket-header! format project date-str)
            (print-top-ticket-body! ticket-info format)))))


;; Print a report of top tickets sorted from highest weighted vote to
;; lowest.
(defn gen-top-ticket-reports! [cur-eval-dir]
  (doseq [project ["CLJ" "CLJS"]]
    (let [open-tickets-info (ticket-plus-vote-info
                             (str cur-eval-dir
                                  (if (= project "CLJ")
                                    "open.xml"
                                    "non-CLJ-open.xml"))
                             (str cur-eval-dir "votes-on-tickets.clj")
                             project)]
      (printf "Project %s vote-diffs:\n" project)
      (println (vote-diffs open-tickets-info))
      
      (print-top-tickets-by-vote-weight!
       (str cur-eval-dir project "-top-tickets-by-weighted-vote.txt")
       open-tickets-info :text project)
      (print-top-tickets-by-vote-weight!
       (str cur-eval-dir project "-top-tickets-by-weighted-vote.html")
       open-tickets-info :html project)))

  ;; Now do the same for all other Clojure projects with votes on open
  ;; tickets, except put them all in one file together, since they
  ;; tend to have far fewer tickets and votes than the CLJ or CLJS
  ;; projects above.
  (doseq [format [:text :html]]
    (let [out-fname (str cur-eval-dir "OTHERS-top-tickets-by-weighted-vote."
                         (case format
                           :text "txt"
                           :html "html"))
          date-str (local-date-str)]
      (spit out-fname
            (with-out-str
              (print-top-ticket-header! format "OTHERS" date-str)
              (doseq [project (sort (disj (all-vote-projects
                                           (str cur-eval-dir "votes-on-tickets.clj"))
                                          "CLJ" "CLJS"))]
                (let [open-tickets-info (ticket-plus-vote-info
                                         (str cur-eval-dir "non-CLJ-open.xml")
                                         (str cur-eval-dir "votes-on-tickets.clj")
                                         project)]
                  (when (= format :text)
                    (binding [*out* *err*]
                      (printf "Project %s vote-diffs:\n" project)
                      (println (vote-diffs open-tickets-info))))
                  (print-top-ticket-short-project-header! format project)
                  (print-top-ticket-body! open-tickets-info format))))))))


(def month-abbrev-to-num
  {"Jan" 1, "Feb" 2, "Mar" 3, "Apr" 4, "May" 5, "Jun" 6,
   "Jul" 7, "Aug" 8, "Sep" 9, "Oct" 10, "Nov" 11, "Dec" 12})


(defn my-jodatime [yr mon date]
  (DateTime. (int yr) (int mon) (int date) (int 0) (int 0)))


(defn convert-date-fmt [date-str]
  (if-let [[_ date mon yr] (and (string? date-str)
                                (re-matches #"(?:Sun|Mon|Tue|Wed|Thu|Fri|Sat), (\d+) (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) (\d+) \d+:\d+:\d+ [+-]\d+"
                                            date-str))]
    (let [yr (long (extract-dec-num yr))
          mon (month-abbrev-to-num mon)
          date (long (extract-dec-num date))]
      {:err nil
       :s (format "%04d-%02d-%02d" yr mon date)
       :jodatime (my-jodatime yr mon date)})
    {:err (format "convert-date-fmt could not match date string '%s'" date-str)}))


(defn today-date-fmt []
  (let [today-date (LocalDate/now)
        yr (.getYear today-date)
        mon (.getMonthOfYear today-date)
        date (.getDayOfMonth today-date)]
    {:err nil
     :s (format "%04d-%02d-%02d" yr mon date)
     :jodatime (my-jodatime yr mon date)}))


(defn rgb-map [r g b]
  {:redness r :greenness g :blueness b})

(def red      (rgb-map 192   0   0))
(def bright-red
              (rgb-map 255   0   0))
(def green    (rgb-map   0 192   0))
(def dk-green (rgb-map   0 100   0))
(def black    (rgb-map   0   0   0))
(def blue     (rgb-map   0   0 192))
(def lt-blue  (rgb-map   0   0 255))


(def ticket-state-to-color
  {"Closed"      green
   "Resolved"    green
   "Open"        lt-blue
   "In Progress" lt-blue
   "Reopened"    lt-blue
   })


(defn color-by-state-only [ti]
  (get ticket-state-to-color (:status ti)))


(defn normalized-status [status]
  (cond (= status "Resolved") "Closed"
        (contains? #{"In Progress" "Reopened"} status) "Open"
        :else status))


(defn color-by-status-and-resolution [ti]
  (let [{:keys [status resolution]} ti
        status (normalized-status status)]
    (if (= status "Open")
      lt-blue
      ;; else "Closed"
      (cond (= resolution "Completed") green
            (= resolution "Duplicate") dk-green
            :else bright-red))))  ;; Denied


(defn print-gnuplot-data! [file-name all-ts
                           sort-all-tickets-cmp-fn
                           ticket-grouping-key-fn
                           grouping-key-cmp-fn
                           color-for-ticket-fn]
  (let [today (today-date-fmt)
        all-ts
        (into
         {}
         (map (fn [[ticket-id-str ticket-info]]
                (let [cre (convert-date-fmt (:created ticket-info))
                      res (if-let [r (:resolved ticket-info)]
                            (convert-date-fmt r))]
                  [ticket-id-str
                   (assoc ticket-info
                     :ticket-num (long (extract-dec-num ticket-id-str))
                     :created-gnuplot-fmt cre
                     :resolved-gnuplot-fmt res
                     :open-duration-days (days-between-dates
                                          (:jodatime cre)
                                          (:jodatime (or res today)))
                     )]))
              all-ts))
        sorted-ts (sort sort-all-tickets-cmp-fn (vals all-ts))
        grouped-ts (group-by ticket-grouping-key-fn sorted-ts)]
    (with-open [datf (io/writer file-name)]
      (binding [*out* datf]
        (with-local-vars [idx 0]
          (doseq [x (sort grouping-key-cmp-fn (keys grouped-ts))
                  ticket-info (get grouped-ts x)]
            (let [{:keys [status created-gnuplot-fmt resolved-gnuplot-fmt]} ticket-info
                  res (or resolved-gnuplot-fmt today)
                  color (color-for-ticket-fn ticket-info)]
              ;; TBD: Have an option to set idx equal to (:ticket-num ticket-info)
              (var-set idx (inc @idx))
              ;;(var-set idx (:ticket-num ticket-info))
              (printf "\n")
              (cond (:err created-gnuplot-fmt)
                    (printf "# ERROR in created date: %s\n"
                            (:s created-gnuplot-fmt))
                    
                    (:err res)
                    (printf "# ERROR in resolved date: %s\n"
                            (:s res))
                    
                    :else
                    (do
                      (printf "%s %d %d %d %d %d\n" (:s created-gnuplot-fmt) @idx
                              (:redness color) (:greenness color) (:blueness color)
                              (:ticket-num ticket-info))
                      (printf "%s %d %d %d %d %d\n" (:s res) @idx
                              (:redness color) (:greenness color) (:blueness color)
                              (:ticket-num ticket-info)))))))))))



(defn show-usage [prog-name]
  (iprintf *err* "usage:
    %s [ help | -h | --help ]
    %s top-tickets <jira-account-name> <jira-password>
" prog-name prog-name))


(def prog-name "lein run")


(defn -main [& args]
  (when (or (= 0 (count args))
            (#{"-h" "--help" "-help" "help"} (first args)))
    (show-usage prog-name)
    (System/exit 0))
  (let [[action & args] args]
    (case action
      
      "top-tickets"
      (if (= 2 (count args))
        (let [[jira-account jira-pw] args
              eval-root (str fs/*cwd* "/eval-results/")
              yyyy-mm-dd (str (LocalDate/now))
              cur-eval-dir (str eval-root yyyy-mm-dd "/")
              auth-info {:basic-auth [jira-account jira-pw]}]
          (when-not (fs/exists? eval-root)
            (die "Directory %s must exist, but does not.  Aborting." eval-root))
          (when (fs/exists? cur-eval-dir)
            (die "Eval directory %s
where all results will go already exists.
Aborting to avoid overwriting any files there.  Delete it and rerun if you wish.
"
                 cur-eval-dir))
          (when-not (fs/mkdirs cur-eval-dir)
            (die "mkdirs %s failed.  Aborting.\n" cur-eval-dir))

          (dl-open-tickets! (str cur-eval-dir "open.xml") :CLJ)
          (dl-open-tickets! (str cur-eval-dir "non-CLJ-open.xml") :non-CLJ)
          (let [fname (str cur-eval-dir "votes-on-tickets.clj")
                all-users (read-safely "data/all-clojure-jira-users.clj")
                votes-by-user (dl-open-ticket-votes! all-users auth-info true)]
            (spit-pretty fname votes-by-user))
          (gen-top-ticket-reports! cur-eval-dir))
        (do (iprintf *err* "Wrong number of args %d for '%s' action\n"
                     (count args) action)
            (show-usage prog-name)
            (System/exit 1)))

      ;; default case
      (do (iprintf *err* "Urecognized first arg '%s'\n" action)
          (show-usage prog-name)
          (System/exit 1)))))



(comment

;; Step 1: Create a directory to put patch evaluation results into.  I
;; put these into directories with names that are the date that I
;; downloaded the attachments, e.g. eval-results/2013-04-25 for Apr 25
;; 2013.  Update the value of cur-eval-dir below to this directory.

;; Step 2: Download the Clojure source from Github, and change
;; clojure-tree below to the root directory of this tree.

;; Step 3: If you want to get info about votes on tickets, change the
;; user name and password in auth-info below to your Clojure JIRA user
;; name and password.  It might require admin privileges to be able to
;; download this vote info -- I haven't tried it using an account
;; without admin privileges.

;; Step 4: Evaluate these expressions in a REPL.

(in-ns 'user)
(use 'clj-prescreen.core 'clojure.repl 'clojure.pprint)
(require '[clojure.java.io :as io] '[me.raynes.fs :as fs])
(def cur-eval-dir (str fs/*cwd* "/eval-results/2013-10-08/"))
(def clojure-tree "./eval-results/2013-10-08-clojure-to-prescreen/clojure")
(def ticket-dir (str cur-eval-dir "ticket-info"))
(def ppat-fname "./data/preferred-patches.clj")
(def patch-type-list [ "open" ])
;; Note: Don't check any password into git
(def auth-info {:basic-auth ["jafingerhut" "tbd-password-here"]})
;;(def patch-type-list [ "screened" "incomplete" "np" "rfs"])
;;(def patch-type-list [ "notclosed" ])


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; http://dev.clojure.org -----> Download via manual steps (Note 2)
;;         |           |           |
;;         |           |           v
;; dl-open-tickets!    |         data/all-clojure-jira-users.clj
;; Code at Note 4      +-----------+  |
;;    |         |                  v  v
;;    |         |                Code at Note 5
;;    v         v                  |
;; open.xml  non-CLJ-open.xml      v
;;    |  |                 |     <cur-eval-dir>/votes-on-tickets.clj
;;    |  |                 +---------+ |
;;    |  +-------------------------+ | |
;;    |                            v v v
;;    |                          Code at Note 6
;;    |                            |  
;;    |                            v  
;;    |                          Top tickets by weighted vote reports
;;    |                          for CLJ, CLJS, and all others, each
;;    | http://dev.clojure.org   in html and plain text formats.
;;    |  |
;;    |  |  +-----------------Clojure source code tree
;;    |  |  |  +---------------|-data/{people-data.clj, preferred-patches.clj}
;;    v  v  v  v               |  |
;; dl-patches-check-ca!        |  |
;; Code at Note 7              |  |
;;    |        |               |  |
;;    |        +---------------|--|------+
;;    v                        |  |      v
;; <cur-eval-dir>/             |  |  <cur-eval-dir>/open-author-info.txt
;;   ticket-info/ ...          |  |
;;   open-downloaded-only.txt  |  |
;;    |     +------------------+  |
;;    |     |  +------------------+
;;    v     v  v
;; do-eval-check-ca! (Code at Note 8)
;;    |        |
;;    |        +-------------------------+
;;    v                                  v
;; <cur-eval-dir>/                   <cur-eval-dir>/open-patch-summary.txt
;;   open-evaled-authors.txt
;;    |
;;    |  data/preferred-patches.clj
;;    |   |
;;    v   v
;; Code at Note 9
;;    |
;;    v
;; <cur-eval-dir>/
;;     open-warnings.txt
;;     open-prescreened-report.txt
;;     open-needs-work.txt
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


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

;;;; Note 4 ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Download info about all open tickets and save in file open.xml.
;; Download votes cast by each CLJ JIRA user and save in file
;; votes-on-tickets.clj.
(dl-open-tickets! (str cur-eval-dir "open.xml") :CLJ)
(dl-open-tickets! (str cur-eval-dir "non-CLJ-open.xml") :non-CLJ)
;;;; Note 5 ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Download all votes on open tickets.
(let [fname (str cur-eval-dir "votes-on-tickets.clj")
      all-users (read-safely "data/all-clojure-jira-users.clj")
      votes-by-user (dl-open-ticket-votes! all-users auth-info true)]
  (spit-pretty fname votes-by-user))


;;;; Note 6 ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Read JIRA ticket info from open.xml and non-CLJ-open.xml, and vote
;; info from votes-on-tickets.clj.  Produce reports of top tickets by
;; weighted vote, with a separate report for CLJ and CLJS tickets, but
;; one combined report for all other Clojure projects (since they
;; currently have so few tickets by comparison to CLJ and CLJS
;; projects).

;; If vote-differences is anything other than '(nil nil), there is a
;; mismatch.  Either votes have been cast while I downloaded the info,
;; or I am missing a user, and should get an updated user list.  See
;; Note 2.

(gen-top-ticket-reports! cur-eval-dir)
;;;; End of Note 6 ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;; TBD: Consider including the weighted vote count and the normal vote
;; count in the prescreened and needs work reports.

;;;; Note 7 ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Download all attachments for selected tickets.  Do this once on one
;; machine, not once for each OS/JDK combo I want to test.  Also, for
;; all git format patches, check people data to see if they have
;; signed a Clojure CA.

;; Writes these files:
;; In <cur-eval-dir>/ticket-info/
;;     One new dir per ticket containing attachments on that ticket
;;     from dev.clojure.org
;; <cur-eval-dir>/open-downloaded-only.txt
;;     Clojure list of maps with lots of details about each
;;     attachment, including whether it applies cleanly or not to the
;;     Clojure source tree specified by clojure-tree.
;; <cur-eval-dir>/open-author-info.txt
;;     A text file with only a fraction of the data in
;;     open-downloaded-only.txt.  Useful for looking at, and for
;;     diff'ing against a previous set of downloaded attachments.
(dl-patches-check-ca! cur-eval-dir patch-type-list ticket-dir ppat-fname clojure-tree)

;; After doing the dl-patches-check-ca! above, if you edit
;; data/people-data.clj and want to redo the author evaluations only,
;; do this:
(doseq [patch-type patch-type-list]
  (let [fname1 (str cur-eval-dir patch-type "-downloaded-only.txt")
        as1 (read-safely fname1)
        as1 (let [people-info (read-safely "data/people-data.clj")]
              (map #(add-author-info % ticket-dir people-info) as1))]
    (spit-pretty fname1 as1)
    (spit (str cur-eval-dir patch-type "-author-info.txt")
          (with-out-str (eval-patches-summary as1)))))

;;;; End of Note 7 ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;; Note 8 ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Evaluate downloaded attachments.  Do this once for each OS/JDK
;; combo.
(do-eval-check-ca! cur-eval-dir ticket-dir clojure-tree patch-type-list ppat-fname)

;; After doing the do-eval-check-ca! above, if you edit
;; data/people-data.clj and want to redo the author evaluations only,
;; do this:
(doseq [patch-type patch-type-list]
  (let [fname1 (str cur-eval-dir patch-type "-evaled-authors.txt")
        as1 (read-safely fname1)
        as1 (let [people-info (read-safely "data/people-data.clj")]
              (map #(add-author-info % ticket-dir people-info) as1))]
    (spit-pretty fname1 as1)
    (spit (str cur-eval-dir patch-type "-patch-summary.txt")
          (with-out-str (eval-patches-summary as1)))))

;;;; Note 9 ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; After doing the do-eval-check-ca! above, and hand-edited a file
;; data/preferred-patches.clj containing the "preferred patches" to
;; show in the prescreened patch list, do the below to generate part
;; of the prescreened patch and tickets needing work reports.
(doseq [patch-type patch-type-list]
  (let [fname1 (str cur-eval-dir patch-type "-evaled-authors.txt")
        atts (read-safely fname1)
        ppats (read-safely ppat-fname)
        fname-warnings (str cur-eval-dir patch-type "-warnings.txt")
        fname-prescreened (str cur-eval-dir patch-type "-prescreened-report.txt")
        fname-needs-work (str cur-eval-dir patch-type "-needs-work.txt")]
    (spit fname-warnings (warning-log-text atts ppats fname1 ppat-fname))
    (spit fname-prescreened (prescreened-report-text atts ppats))
    (spit fname-needs-work
          (str (prescreened-needs-work-report-text atts ppats)
               (not-prescreened-needs-work-report-text atts ppats)))
    ))
;;;; End of Note 9 ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;; Note 10 ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Scratch pad of some code that can be used to generate some charts
;; showing history of dates when tickets were created vs. when they
;; were closed/resolved.  Some of this should be cleaned up a bit and
;; added above.

(dl-all-CLJ-tickets! (str cur-eval-dir "all-CLJ.xml"))

(in-ns 'user)
(use 'clj-prescreen.core 'clojure.repl 'clojure.pprint)
(require '[clojure.java.io :as io] '[fs.core :as fs] '[clojure.tools.trace :as t])
(def cur-eval-dir (str fs/*cwd* "/eval-results/2013-08-18/"))

(def all-ts (ticket-info-from-xml (slurp (str cur-eval-dir "all-CLJ.xml"))))
(pprint (frequencies (map (fn [ti] [(normalized-status (:status ti))
                                    (:resolution ti)])
                          (vals all-ts))))

;; Print tickets sorted by :ticket-num, then grouped by :ticket-num,
;; then compared by :ticket-num.
(print-gnuplot-data! "./data/test-date-ranges2.dat" all-ts
                     (fn [a b] (compare (:ticket-num a) (:ticket-num b)))
                     :ticket-num
                     compare
                     color-by-status-and-resolution)
                     ;color-by-state-only)

;; Print tickets sorted by :open-duration-days, then grouped by
;; :open-duration-days, then compared by :open-duration-days.
(print-gnuplot-data! "./data/test-date-ranges2.dat" all-ts
                     (fn [a b] (compare (:open-duration-days a) (:open-duration-days b)))
                     :open-duration-days
                     compare
                     color-by-status-and-resolution)
                     ;color-by-state-only)

(def status-compare-key
  {"Closed"      1
   "Resolved"    1
   "Open"        2
   "In Progress" 2
   "Reopened"    2
   })

(defn status-then-open-duration-key [ticket-info]
  [(status-compare-key (:status ticket-info))
   (:open-duration-days ticket-info)])

(defn norm-status-resolution-open-duration [ti]
  [(normalized-status (:status ti))
   (:resolution ti)
   (:open-duration-days ti)])

(defn resolved-created [ti]
  [(:s (:resolved-gnuplot-fmt ti))
   (:s (:created-gnuplot-fmt ti))])

(def f1 norm-status-resolution-open-duration)
(def f1 resolved-created)

(print-gnuplot-data! "./data/test-date-ranges2.dat" all-ts
                     (fn [a b]
                       (compare (f1 a)
                                (f1 b)))
                     f1
                     compare
                     color-by-status-and-resolution)

;;;; End of Note 10 ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;;;; Note 2 ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
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
;;;; End of Note 2 ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;; Older steps for creating some other reports about votes on tickets
;; in JIRA.

(def open-tickets-1-vote-or-more (filter-vals
                                  #(> (:weighted-vote % 0) 0)
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


;; =================================================================
;; Note 3: Older manual way to get XML info about open CLJ tickets
;; (newer way is at Note 4)
;; =================================================================
;; Go to the Clojure Jira page, then to the filters, and look at the
;; tickets that match a particular filter.  There is a popup menu that
;; says "Views" in the upper right of the page.

;; Pick the XML view from that menu.  An XML form of the ticket list
;; will be shown.  Save the page as a file.  That is how I created the
;; file notclosed.xml, which I saved in the directory named by the
;; string cur-eval-dir.
;; =================================================================
