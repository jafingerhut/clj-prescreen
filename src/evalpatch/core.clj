(ns evalpatch.core
  (:import java.io.File)
  (:require [clojure.xml :as xml]
            [clojure.data.zip.xml :as dzx]
            [clojure.zip :as zip]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.java.shell :as sh]
            [clojure.pprint :as p]
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
    (binding [*read-eval* false]
      (read r))))


(defn spit-pretty [f data & options]
  (apply spit f (with-out-str (p/pprint data)) options))


(defn attachments-from-ticket [ticket]
  (let [k (dzx/xml1-> ticket :key dzx/text)
        title (dzx/xml1-> ticket :title dzx/text)]
    (->> (dzx/xml-> ticket :attachments :attachment)
         (map (fn [att] (:attrs (first att))))
         (map #(merge % {:ticket k :title title})))))


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
  (let [ticket-names (set (map :ticket atts))]
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
                     :guessed-type guessed-type}))))))


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
  (let [name-match (or (= name (:full-name person))
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
      (if (:contributor x)
        :contributor
        :not-contributor)
      match-kind)))


(defn patch-authors-contributor-status
  [patch-authors people]
  (->> patch-authors
       (map extract-name-and-email)
       (map (fn [p]
              (merge p {:contributor-status
                        (one-author-contributor-status people p)})))))


(defn patch-type
  "Use the patch's actual type if specified, otherwise the guessed
type."
  [p]
  (or (:actual-type p) (:guessed-type p)))


(defn apply-git-patch [p patch-filename idx num-patches]
  (let [{:keys [exit out err]} (try-cmd "git" "am" "--keep-cr" "-s"
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


(defn check-ant-output [s]
  (condp re-find s
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
              :patch-author-summary :not-git-patch})))


(defn eval-patch! [p attach-dir idx num-patches]
  (let [branch-name (str (:ticket p) "-" (:name p))
        logfile-name (str (att-dir-name (:ticket p) attach-dir) "/"
                          (:name p) "-log.txt")]
    (with-open [logf (io/writer logfile-name)]
      (binding [*cmd-log* logf]
        (iprintf "eval-patch! %d/%d %s %s\n"
                 (inc idx) num-patches (:ticket p) (:name p))
        ;; (1) Get repo back to latest master with no changes.
        (try-cmd :throw-on-error "git" "checkout" "-f" "master")
        ;; (2) Delete any branch that already exists with the desired
        ;; name.  Allow non-0 exit status, since that happens if the
        ;; branch does not exist.
        (try-cmd "git" "branch" "-D" branch-name)
        ;; (3) create a new branch for applying this patch
        (try-cmd :throw-on-error "git" "checkout" "-b" branch-name)
        ;; (4) Try to apply the patch
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
           :ok ;; (5) Try to build and test
           (let [{:keys [ant-status ant-msg] :as p} (build-and-test-clojure p)]
             (when *cmd-log*
               (iprintf *cmd-log* "Build status: %s (%s)\n" ant-msg
                        ant-status))
             (iprintf "    Build status: %s (%s)\n" ant-msg ant-status)
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
           (throw (Exception. ^String (:patch-msg p)))))))))


(defn eval-patches! [patches attach-dir people-info-filename clojure-dir]
  (if-not (clojure-git-dir? clojure-dir)
    (iprintf *err* "eval-patches!: '%s' is not a Clojure git repo root directory.\n" clojure-dir)
    (let [people-info (read-safely people-info-filename)]
      (sh/with-sh-dir clojure-dir
        (let [n (count patches)]
          (doall
           (map-indexed (fn [idx p] (eval-patch! p attach-dir idx n))
                        patches)))))))


(defn name-or-default [p k deflt]
  (if-let [s (get p k)]
    (name s)
    deflt))


(defn one-patch-summary [p]
  (iprintf "%-13s %-10s %-5s %s %s\n"
           (name-or-default p :patch-author-summary "--")
           (name-or-default p :patch-status "--")
           (name-or-default p :ant-status "--")
           (:ticket p) (:name p)))


(defn eval-patches-summary [patches]
  (dorun (map (fn [p-group]
                (iprintf "\n")
                (dorun (map one-patch-summary p-group)))
              (partition-by :ticket patches))))


(comment

;; Go to the Clojure Jira page, then to the filters, and look at the
;; tickets that match a particular filter.  There is a popup menu that
;; says "Views" in the upper right of the page.

;; Pick the XML view from that menu.  An XML form of the ticket list
;; will be shown.  Save the page as a file.  That is how I created the
;; file rfs.xml (abbreviation of "Ready For Screening").

(use 'evalpatch.core 'clojure.pprint)
(require '[clojure.java.io :as io] '[fs.core :as fs])
(def cur-eval-dir (str @fs/cwd "/2012-04-11-tickets/"))
(def ticket-dir (str cur-eval-dir "ticket-info"))

;; Also need to pull a clone of the Clojure repo if you haven't done
;; so already.  Don't make it your favorite one, as many branches will
;; be created and deleted in it, and it will erase any local changes
;; you have made in its current branch.
;; git clone git://github.com/clojure/clojure.git

;; Automate things a bit more

;; Download all attachments for selected tickets.  Do this once on one
;; machine, not once for each OS/JDK combo I want to test.
(doseq [cur-patch-type ["screened" "incomplete" "np" "rfs"]]
  (let [as1 (xml->attach-info (str cur-eval-dir cur-patch-type ".xml"))
        as2 (download-attachments! as1 ticket-dir)
        fname2 (str cur-eval-dir cur-patch-type "-downloaded-only.txt")]
    (spit-pretty fname2 as2)))

;; Evaluate downloaded attachments.  DO this once for each OS/JDK
;; combo.
(doseq [cur-patch-type ["screened" "incomplete" "np" "rfs"]]
  (let [fname2 (str cur-eval-dir cur-patch-type "-downloaded-only.txt")
        as2 (read-safely fname2)
        as3 (eval-patches! as2 ticket-dir "data/people-data.clj" "./clojure")
        as4 (let [people-info (read-safely "data/people-data.clj")]
              (map #(add-author-info % ticket-dir people-info) as3))
        fname4 (str cur-eval-dir cur-patch-type "-evaled-authors.txt")
        fname-sum (str cur-eval-dir cur-patch-type "-patch-summary.txt")]
    (spit-pretty fname4 as4)
    (spit fname-sum (with-out-str (eval-patches-summary as4)))))

;; After doing the above, if you edit data/people-data.clj and want to
;; redo the author evaluations only, do this:
(doseq [cur-patch-type ["screened" "incomplete" "np" "rfs"]]
  (let [fname4 (str cur-eval-dir cur-patch-type "-evaled-authors.txt")
        fname-sum (str cur-eval-dir cur-patch-type "-patch-summary.txt")
        as4 (read-safely fname4)
        as4 (let [people-info (read-safely "data/people-data.clj")]
              (map #(add-author-info % ticket-dir people-info) as4))]
    (spit-pretty fname4 as4)
    (spit fname-sum (with-out-str (eval-patches-summary as4)))))


;; Beginning of older copy-and-paste one-step-at-a-time method

;;(def cur-patch-type "screened")
;;(def cur-patch-type "incomplete")
;;(def cur-patch-type "rfs")
;;(def cur-patch-type "np")

(def as1 (xml->attach-info (str cur-eval-dir cur-patch-type ".xml")))
(pprint (take 10 as1))
(def as2 (download-attachments! as1 ticket-dir))
(spit-pretty (str cur-eval-dir cur-patch-type "-info.txt") as2)

;; See Note 1 below about editing.

(def as2 (read-safely (str cur-eval-dir cur-patch-type "-info.txt")))
;; Evaluate all patches:
(def as3 (eval-patches! as2 ticket-dir "data/people-data.clj" "./clojure"))
;; Evaluate one patch:
;; TBD

(spit-pretty (str cur-eval-dir cur-patch-type "-evaled.txt") as3)

(def as3 (read-safely (str cur-eval-dir cur-patch-type "-evaled.txt")))
;; Update author info, perhaps after editing "data/people-data.clj"
(def as4 (let [people-info (read-safely "data/people-data.clj")]
           (map #(add-author-info % ticket-dir people-info) as3)))

(spit-pretty (str cur-eval-dir cur-patch-type "-evaled-authors.txt") as4)
(eval-patches-summary as4)


;; Testing with 1 patch at a time.
(use 'evalpatch.core 'clojure.pprint)
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

;; + :git-diff      (i.e. use 'git am --keep-cr -s < attach-file' to apply)
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
;; Information evalpatch should add to each attachment:

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
