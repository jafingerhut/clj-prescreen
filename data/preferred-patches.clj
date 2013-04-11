(
 {:ticket "CLJ-99",
  :name "clj-99-min-key-max-key-performance-v1.txt",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-107",
  :name "clj-107-add-bit-count-v1.txt",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-124",
  :name nil,
  :patch-category "Language/library enhancement",
  :patch-extra-note
     "There are lots of different ideas discussed in the comments for
      CLJ-124, and not clear to me if there is an agreed-upon set of
      changes for the issue.  CLJ-959 could potentially be closed as a
      duplicate of CLJ-124.",
  }
 {:ticket "CLJ-129",
  :name nil,
  :patch-category "Doc string fixes only",
  :patch-extra-note
     "A thorough job of documenting this would require understanding
      how Clojure functions are made to implement the Comparable
      interface in AFunction.java of the Clojure source code, and the
      restrictions in Java of what makes a 'good' implementation of
      the Comparator interface.  Doing that clearly is probably best
      done in a form longer than is acceptable in a doc string for
      sorted-set-by or sorted-map-by, but perhaps such documentation
      could be put at a link that could be added to the doc string."
  }
 {:ticket "CLJ-196",
  :name "0002-Don-t-promise-the-value-of-file-in-the-REPL.patch",
  :patch-category "Doc string fixes only",
  }
 {:ticket "CLJ-200",
  :name "clj-200-cond-let-clauses-fixed-test-v2-patch.txt",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-248",
  :name "clj-248-SortedMap-SortedSet-interfaces-patch2.txt",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-308",
  :name "0001-Added-ClosableResource-protocol-for-with-open.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-322",
  :name "compile-interop-1.patch",
  :patch-category "Language/library enhancement",
  :patch-extra-note
     "Patch compile-interop-1.patch applies and builds cleanly.
      Comment discussion on various approaches to this ticket are
      extensive.",
  }
 {:ticket "CLJ-373",
  :name "0001-Support-empty-path-in-update-in.-CLJ-373.patch",
  :patch-category "Allow more correct-looking Clojure code to work",
  }
 {:ticket "CLJ-394",
  :name "clj-394-add-predicates-for-type-and-record.diff",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-415",
  :name "clj-415-assert-prints-locals-v1.txt",
  :patch-category "Debug/tooling enhancement",
  :patch-extra-note
     "The patch applies cleanly and passes tests, but gives a warning
      because macro local-bindings is same name (and perhaps the same
      author, since the body is almost the same) as a function
      local-bindings in clojure.test.generative.  If the one in
      clojure.test.generative were moved to clojure.core,
      test.generative could use that one.",
  }
 {:ticket "CLJ-457",
  :name "CLJ-457-2.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-666",
  :name "0001-Add-Big-support-to-Reflector-Updated.patch",
  :patch-category "Allow more correct-looking Clojure code to work",
  }
 {:ticket "CLJ-669",
  :name "0001-use-java.nio-in-do-copy-method-for-Files.patch",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-700",
  :name "clj-700-patch6.txt",
  :patch-category "Allow more correct-looking Clojure code to work",
  :patch-extra-note
     "Was Screened, then Incomplete in Aug 2012, now Approval is empty.",
  }
 {:ticket "CLJ-703",
  :name "improve-writeclassfile-perf.patch",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-706",
  :name "706-fix-deprecation-warnings-on-replicate.diff",
  :patch-category "Debug/tooling enhancement",
  :patch-extra-note
     "This patch is only to eliminate some use of deprecated symbols.
      706-deprecated-var-warning-patch-v2.txt is the patch that
      implements the warnings when using deprecated symbols, but fails
      tests when applied by itself.  It needs test fixes in other
      706-* patches and then they pass.",
  }
 {:ticket "CLJ-713",
  :name "asm-split.txt",
  :patch-category "Debug/tooling enhancement",
  :patch-extra-note
     "This patch applies cleanly, but fails to compile with
      './antsetup.sh ; ant' due to some extra package dependencies
      added by the patch.  It did compile and test successfully as of
      Mar 14, 2013 using the command 'mvn package'.",
  }
 {:ticket "CLJ-735",
  :name "protocolerr.diff",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-766",
  :name "byte-short-array-ctors.diff",
  :patch-category "Allow more correct-looking Clojure code to work",
  }
 {:ticket "CLJ-771",
  :name "clj-771-move-unchecked-casts-patch-v5.txt",
  :patch-category "Language/library enhancement",
  :patch-extra-note
     "TBD: The patch clj-771-move-unchecked-casts-patch-v5.txt applies
      cleanly to latest master and passes all tests. Rich marked this
      ticket as Incomplete on Dec 9 2011 with the comment \"still
      considering when to incorporate this\" above. Is it reasonable
      to change it back to Vetted or Screened so it can be considered
      again, perhaps after Release 1.5 is made?",
  }
 {:ticket "CLJ-783",
  :name "clj-783-patch.txt",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-803",
  :name "0001-atom-interface.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-827",
  :name "0001-add-unsigned-bit-shift-right.patch",
  :patch-category "Language/library enhancement",
  :patch-extra-note "or 0001-CLJ-827-Add-bit-shift-right-logical.patch",
  }
 {:ticket "CLJ-835",
  :name "0001-CLJ-835-Refine-doc-string-for-defmulti-hierarchy-opt.patch",
  :patch-category "Doc string fixes only",
  }
 {:ticket "CLJ-840",
  :name "clj840-2.diff",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-842",
  :name "clj-842-update-clojure.pprint-metadata-v2.txt",
  :patch-category "Code cleanup",
  }
 {:ticket "CLJ-849",
  :name "CLJ-849-line-number-pesudo-variable.diff",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-850",
  :name "CLJ-850-conform-to-invokePrim.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-858",
  :name "stm-rm-msecs-patch.diff",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-862",
  :name "pmap-chunking-862.diff",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-863",
  :name "clj-863-make-interleave-handle-odd-args-like-concat-patch-v1.txt",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-865",
  :name "updated.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-866",
  :name "clj-866-test-vars.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-873",
  :name "clj-873-namespace-divides-patch.txt",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-896",
  :name "clj-896-browse-url-uses-xdg-open-patch2.txt",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-908",
  :name "clj-908-Print-metadata-and-anonymous-classes-better-patch2.txt",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-935",
  :name "fix-trim-fns-different-whitespace-patch.txt",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-937",
  :name "clj-937-cl-format-coerces-ratios-patch2.txt",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-939",
  :name "clj-939-report-load-exceptions-with-file-and-line-patch-v2.txt",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-944",
  :name "0001-Fix-for-CLJ-944.patch",
  :patch-category "Clojure language/library bug fixes",
  :patch-extra-note
     "See comments for another patch 0002-Fix-for-CLJ-944.patch and
      why it fails some tests, which are fixable.",
  }
 {:ticket "CLJ-949",
  :name "0001-let-undeclared-exceptions-continue-unchecked.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-957",
  :name "clj-957-allow-typehinting-of-method-signatures-in-deftype-patch2.txt",
  :patch-category "Language/library enhancement",
  :patch-extra-note
     "TBD: Why marked Not Approved by Rich?  Should this be closed, or
      does he want something about the existing patches modified
      before considering it?",
  }
 {:ticket "CLJ-958",
  :name "0001-Make-APersistentVector.iterator-slightly-more-effici.patch",
  :patch-category "Language/library enhancement",
  :patch-extra-note
     "TBD: Why marked Not Approved by Rich?  Should this be closed, or
      does he want something about the existing patches modified
      before considering it?",
  }
 {:ticket "CLJ-970",
  :name "clj-970-extend-implement-parameterized-types-patch2.txt",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-976",
  :name "clj-976-queue-literal-eval-and-synquote-patch-v3.txt",
  :patch-category "Language/library enhancement",
  :stale-patch-last-time-applied-cleanly "Oct 2012",
  }
 {:ticket "CLJ-978",
  :name "clojure--bean-support-for-private-implementation-classes-v3.diff",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-980",
  :name "extended-type-doc-fix-v2.patch",
  :patch-category "Doc string fixes only",
  }
 {:ticket "CLJ-991",
  :name "reducer-partition-by4.diff",
  :patch-category "Language enhancement, reducers",
  }
 {:ticket "CLJ-994",
  :name "0001-repeat-for-clojure.core.reducers.patch",
  :patch-category "Language enhancement, reducers",
  :stale-patch-last-time-applied-cleanly "Jul 2012",
  }
 {:ticket "CLJ-1004",
  :name "arraychunk-seq-10004.diff",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1005",
  :name "0001-Use-transient-map-in-zipmap.2.patch",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1010",
  :name "0001-CLJ-1010-Add-a-left-to-right-version-of-comp-comp.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1018",
  :name "inconsistent_range_fix.diff",
  :patch-category "Allow more correct-looking Clojure code to work",
  }
 {:ticket "CLJ-1020",
  :name "clj-1020-inspect-table-skip-nil-rows-patch1.txt",
  :patch-category "Allow more correct-looking Clojure code to work",
  }
 {:ticket "CLJ-1021",
  :name "001-propagate-on-macro-meta.diff",
  :patch-category "Allow more correct-looking Clojure code to work",
  }
 {:ticket "CLJ-1026",
  :name "0001-Introduce-end-of-line-normalization.patch",
  :patch-category "Code cleanup",
  :patch-extra-note
     "Although this patch does not apply cleanly, it is quite
      mechanical to make it apply cleanly again, since the only
      changes are removing CR characters from all Clojure source
      files.",
  }
 {:ticket "CLJ-1029",
  :name "ns-patch.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1030",
  :name "improved-int-char-casting-error-messages.diff",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-1036",
  :name "clj-1036-hasheq-for-biginteger-patch-v2.txt",
  :patch-category "Clojure language/library bug fixes",
  :patch-extra-note
     "TBD: Andy F should change from Vetted back to Not Approved if
      Not Approved shouldn't be changed by anyone besides screeners,
      even if the reason it was marked Not Approved has been
      addressed.",
  }
 {:ticket "CLJ-1044",
  :name "001-enable-factory-ctor-inside-deftype.diff",
  :patch-category "Allow more correct-looking Clojure code to work",
  }
 {:ticket "CLJ-1045",
  :name "clj-1045-fold-by-halves-patch-v2.txt",
  :patch-category "Language enhancement, reducers",
  }
 {:ticket "CLJ-1046",
  :name "drop-while-reducer.patch",
  :patch-category "Language enhancement, reducers",
  :patch-extra-note
     "As of Oct 28 2012, some patches for CLJ-992 and CLJ-993 apply, build,
      and test cleanly if applied after the patches for CLJ-1045 and
      CLJ-1046.",
  }
 {:ticket "CLJ-1047",
  :name "001-simplify-fj-importing.patch",
  :patch-category "Language enhancement, reducers",
  }
 {:ticket "CLJ-1049",
  :name "0001-reduce-kv-transformations.diff",
  :patch-category "Language enhancement, reducers",
  }
 {:ticket "CLJ-1059",
  :name "001-clj-1059-make-persistentqueue-implement-list.diff",
  :patch-category "Clojure language/library bug fixes",
  :patch-extra-note "or 002-clj-1059-asequential-rebased-to-cached-hasheq.diff",
  }
 {:ticket "CLJ-1060",
  :name "list-star-fix.diff",
  :patch-category "Allow more correct-looking Clojure code to work",
  }
 {:ticket "CLJ-1063",
  :name "clj-1063-add-dissoc-in-patch-v2.txt",
  :patch-category "Language/library enhancement",
  :patch-extra-note "TBD: Open ticket for clojure.incubator for this.",
  }
 {:ticket "CLJ-1072",
  :name "0001-CLJ-1072-Replace-old-metadata-reader-macro-syntax.patch",
  :patch-category "Code cleanup",
  }
 {:ticket "CLJ-1073",
  :name "clj-1073-add-print-interruptibly-patch-v2.txt",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-1074",
  :name "0001-Read-Infinity-and-NaN.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1076",
  :name "clj-1076-fix-tests-on-windows-patch-v2.txt",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-1077",
  :name "thread-bound.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1078",
  :name "queue.patch",
  :patch-category "Language/library enhancement",
  :stale-patch-last-time-applied-cleanly "Dec 2012",
  :patch-extra-note
     "There are more substantive changes to make than simply making
      the patch apply to latest master, per comments from Rich.",
  }
 {:ticket "CLJ-1079",
  :name "CLJ-1079.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1080",
  :name "clj-1080-eliminate-many-reflection-warnings-patch-v3.txt",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1082",
  :name "clj-1082.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1083",
  :name "better-throw-arity-messages.diff",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-1086",
  :name "thread-last-arity-1.diff",
  :patch-category "Allow more correct-looking Clojure code to work",
  }
 {:ticket "CLJ-1087",
  :name "clj-1087-diff-perf-enhance-patch-v1.txt",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1088",
  :name "0001-Add-support-for-protocol-fns-to-repl-source.-CLJ-1088.patch",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-1090",
  :name "var-clear-locals.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1093",
  :name "clj-1093-fix-empty-record-literal-patch-v2.txt",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1094",
  :name "0001-Add-zero-arity-variants-for-every-pred-and-some-fn.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1095",
  :name "0001-map-indexed-accepts-multiple-collections.patch",
  :patch-category "Language/library enhancement",
  :patch-extra-note
     "0002-Add-test-for-multi-collection-map-indexed-fn.patch includes
      new tests",
  }
 {:ticket "CLJ-1096",
  :name "desctructure-keyword-lookup.diff",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1097",
  :name "node-seq.diff",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1099",
  :name "better-error-message-for-seq.patch",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-1101",
  :name "CLJ-1101-make-default-data-reader-fn-set-able-in-REPL.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1102",
  :name "clj-1102-improve-empty-stack-trace-handling-v1.txt",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1103",
  :name "clj-1103-make-conj-assoc-dissoc-handle-args-similarly-v1.txt",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1104",
  :name "clj-1104-doc-unsafety-of-concurrent-with-redefs-v1.txt",
  :patch-category "Doc string fixes only",
  }
 {:ticket "CLJ-1107",
  :name "0001-CLJ-1107-Throw-exception-for-get-called-on-unsupport.patch",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-1108",
  :name "clj-1108-enhance-future-call-patch-v2.txt",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1112",
  :name "0001-Allow-setting-loading-verbosely-by-system-property.patch",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-1113",
  :name "reductions-reducer.diff",
  :patch-category "Language enhancement, reducers",
  }
 {:ticket "CLJ-1115",
  :name "multi-arity-into.diff",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1120",
  :name "0001-CLJ-1120-ex-message-ex-cause.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1121",
  :name "0001-CLJ-1121-Reimplement-and-without-recursion.patch",
  :patch-category "Allow more correct-looking Clojure code to work",
  }
 {:ticket "CLJ-1122",
  :name "contributing.patch",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-1125",
  :name "threadlocal-removal-tcrawley-2012-12-11.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1128",
  :name "0002-Improve-merge-with.patch",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1134",
  :name "clj-1134-star-directive-in-cl-format.txt",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1137",
  :name "CLJ-1137-eval-metadata-once.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1143",
  :name "clj-1143-ns-doc-string-correction-v1.txt",
  :patch-category "Doc string fixes only",
  }
 {:ticket "CLJ-1148",
  :name "0001-new-defonce-hotness.patch",
  :patch-category "Allow more correct-looking Clojure code to work",
  }
 {:ticket "CLJ-1151",
  :name "tiny-reducers-cleanup.diff",
  :patch-category "Code cleanup",
  }
 {:ticket "CLJ-1157",
  :name "20130204_fix_classloader.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1160",
  :name "lazy-rmapcat.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1161",
  :name "0001-CLJ-1161-Remove-version.properties-from-sources-JAR.patch",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-1164",
  :name "CLJ-1164-typos-instant.patch",
  :patch-category "Doc string fixes only",
  }
 {:ticket "CLJ-1165",
  :name "0001-Protocol-interface-method-declarations-don-t-allow-f.patch",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-1169",
  :name "0001-CLJ-1169-proposed-patch.patch",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-1171",
  :name "0002-CLJ-1171-Obey-lexical-scope-for-class-argument-in-in.patch",
  :patch-category "Clojure language/library bug fixes",
  :patch-extra-note
     "0001-* patch adds new tests, 0002-* fixes a bug, 0003-* relies
      on 0002-* being applied before it.",
  }
 {:ticket "CLJ-1175",
  :name "delayed-exceptions.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1176",
  :name "0001-CLJ-1176-Bind-read-eval-true-in-clojure.repl-source-.patch",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-1177",
  :name "clj-1177-patch-v1.txt",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1179",
  :name "clj-1179-distinct-zero-arguments.txt",
  :patch-category "Allow more correct-looking Clojure code to work",
  }
 {:ticket "CLJ-1180",
  :name "001-CLJ-1180.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1182",
  :name "fix-CLJ-1182.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1185",
  :name "CLJ-1181-v001.patch",
  :patch-category "Language enhancement, reducers",
  }
 {:ticket "CLJ-1187",
  :name "001-CLJ-1187.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1188",
  :name "CLJ-1188-wrapper-free.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1191",
  :name "clj-1191-patch-v1.txt",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-1193",
  :name "clj-1197-make-bigint-work-on-all-doubles-v1.txt",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1197",
  :name "foldable-seq.diff",
  :patch-category "Language enhancement, reducers",
  }
 )
