(
 {:ticket "CLJ-15",
  :name "lazy-incremental-hashes.diff",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-99",
  :name "clj-99-v1.diff",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-107",
  :name "clj-107-v1.diff",
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
 {:ticket "CLJ-200",
  :name "clj-200-cond-let-clauses-fixed-test-v2.diff",
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
  :name "CLJ-373-nested-ops.patch",
  :patch-category "Allow more correct-looking Clojure code to work",
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
 {:ticket "CLJ-428",
  :name "clj-428-v4.diff",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-457",
  :name "clj-457-3.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-666",
  :name "0001-Add-Big-support-to-Reflector-Updated.patch",
  :patch-category "Allow more correct-looking Clojure code to work",
  }
 {:ticket "CLJ-700",
  :name "clj-700-7.diff",
  :patch-category "Allow more correct-looking Clojure code to work",
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
 {:ticket "CLJ-735",
  :name "protocolerr.diff",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-738",
  :name "738.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-771",
  :name "clj-771-move-unchecked-casts-patch-v5.txt",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-787",
  :name "CLJ-787-p1.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-803",
  :name "0001-atom-interface.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-825",
  :name "clj-825-1.patch",
  :patch-category "Clojure language/library bug fixes",
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
  :patch-extra-note
     "Applied cleanly until Jul 7 2013 when a conflicting change with
      subject \"don't presume Integer for LINE and COLUMN, fixes
      round-trip\" was made.",
  }
 {:ticket "CLJ-862",
  :name "pmap-chunking-862.diff",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-865",
  :name "clj-865.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-887",
  :name "0001-don-t-remove-meta-from-arg-vector-in-maybe-destructu.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-888",
  :name "0001-Forbid-vararg-declaration-in-defprotocol-definterfac.patch",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-957",
  :name "clj-957-allow-typehinting-of-method-signatures-in-deftype-patch2.txt",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-958",
  :name "0001-Make-APersistentVector.iterator-slightly-more-effici.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-970",
  :name "clj-970-extend-implement-parameterized-types-patch2.txt",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-971",
  :name "clj-971-2.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-976",
  :name "clj-976-queue-literal-eval-and-synquote-patch-v3.txt",
  :patch-category "Language/library enhancement",
  :stale-patch-last-time-applied-cleanly "Oct 2012",
  :patch-extra-note
     "Applied cleanly until Oct 20 2012 when a conflicting patch for
      CLJ-1070 was applied.",
  }
 {:ticket "CLJ-978",
  :name "clojure--bean-support-for-private-implementation-classes-v3.diff",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-991",
  :name "reducer-partition-by4.diff",
  :patch-category "Language enhancement, reducers",
  }
 {:ticket "CLJ-994",
  :name "0001-repeat-for-clojure.core.reducers.patch",
  :patch-category "Language enhancement, reducers",
  :stale-patch-last-time-applied-cleanly "Jul 2012",
  :patch-extra-note
     "None of the patches apply cleanly, but did on Jul 26, 2012.",
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
 {:ticket "CLJ-1020",
  :name "clj-1020-inspect-table-skip-nil-rows-patch2.txt",
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
  :name "string-coerce-to-int.diff",
  :patch-category "Better error reporting",
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
 {:ticket "CLJ-1073",
  :name "clj-1073-add-print-interruptibly-patch-v2.txt",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-1074",
  :name "clj-1074-read-infinity-and-nan-patch-v2-plus-edn-reader.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1077",
  :name "thread-bound.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1078",
  :name "clj-1048-add-queue-functions.diff",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1079",
  :name "CLJ-1079.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1080",
  :name "clj-1080-v5.txt",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1087",
  :name "clj-1087-diff-perf-enhance-patch-v1.txt",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1088",
  :name "0001-Add-support-for-protocol-fns-to-repl-source.-CLJ-1088.patch",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-1093",
  :name "0001-CLJ-1093-fix-empty-records-literal-v2.patch",
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
  :name "inline-get-keyword.diff",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1097",
  :name "node-seq.diff",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1099",
  :name "better-error-message-for-seq.patch",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-1103",
  :name "clj-1103-4.diff",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1104",
  :name "clj-1104-doc-unsafety-of-concurrent-with-redefs-v1.txt",
  :patch-category "Doc string fixes only",
  }
 {:ticket "CLJ-1107",
  :name "clj-1107-throw-on-unsupported-get-v4.patch",
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
 {:ticket "CLJ-1117",
  :name "clj-1117.patch",
  :patch-category "Doc string fixes only",
  }
 {:ticket "CLJ-1120",
  :name "0001-CLJ-1120-ex-message-ex-cause.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1128",
  :name "0002-Improve-merge-with.patch",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1130",
  :name "clj-1130-v5.diff",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-1134",
  :name "clj-1134-star-directive-in-cl-format.txt",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1137",
  :name "CLJ-1137-eval-metadata-once.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1148",
  :name "clj-1148-defonce-3.patch",
  :patch-category "Allow more correct-looking Clojure code to work",
  }
 {:ticket "CLJ-1151",
  :name "tiny-reducers-cleanup.diff",
  :patch-category "Code cleanup",
  }
 {:ticket "CLJ-1157",
  :name "20140121_fix_classloader.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1162",
  :name "CLJ-1162-p1.patch",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-1169",
  :name "0001-CLJ-1169-proposed-patch.patch",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-1180",
  :name "001-CLJ-1180.patch",
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
 {:ticket "CLJ-1189",
  :name "CLJ-1189-p1.patch",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-1191",
  :name "clj-1191-patch-v1.txt",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-1209",
  :name "0002-CLJ-1209-show-ex-data-in-clojure-test.patch",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-1210",
  :name "extend-io-factory-to-nil.diff",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-1216",
  :name "0001-Create-a-DoExpr.Parser-class-that-delegates-to-BodyE.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1217",
  :name "0001-Don-t-realize-seq-exprs-in-for-unless-necessary.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1219",
  :name "0001-CLJ-1219-make-identical-variadic.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1224",
  :name "0001-CLJ-1224-cache-hasheq-and-hashCode-for-records.patch",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1225",
  :name "clj-1225-2.txt",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1226",
  :name "0001-fix-CLJ-1226.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1229",
  :name "clj-1229-count-overflow-patch-v1.txt",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1237",
  :name "CLJ-1237c.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1239",
  :name "0002-CLJ-1239-protocol-dispatch-for-clojure.walk.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1240",
  :name "0001-CLJ-1240-Note-limits-of-clojure.walk-macroexpand-all.patch",
  :patch-category "Doc string fixes only",
  }
 {:ticket "CLJ-1241",
  :name "0001-fix-CLJ-1241.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1242",
  :name "0001-fix-for-CLJ-1242-tests.patch",
  :patch-category "Allow more correct-looking Clojure code to work",
  }
 {:ticket "CLJ-1250",
  :name "CLJ-1250-AllInvokeSites-20140320.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1251",
  :name "update.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1253",
  :name "clj-1253-1.txt",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1254",
  :name "clj-1254-2.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1257",
  :name "clj-1257-2.diff",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-1259",
  :name "clj-1259-1.txt",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1261",
  :name "clj-1261-2.diff",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-1266",
  :name "floats-intrinsics.diff",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1274",
  :name "CLJ-1274.patch",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-1275",
  :name "0001-Don-t-use-shorthand-for-typehints-when-print-dup.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1277",
  :name "clj-1277-1.txt",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1278",
  :name "CLJ-1278-2.patch",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-1282",
  :name "CLJ-1282-p2.patch",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-1289",
  :name "CLJ-1289-p1.patch",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1293",
  :name "CLJ-1293-v001.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1295",
  :name "clj-1295-1.diff",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1297",
  :name "better-error-messages-for-require.diff",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-1313",
  :name "clj-1313-v2.diff",
  :patch-category "Code cleanup",
  }
 {:ticket "CLJ-1314",
  :name "clj-1314-v2.diff",
  :patch-category "Code cleanup",
  }
 {:ticket "CLJ-1315",
  :name "0001-Don-t-initialize-classes-during-import.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1317",
  :name "0001-CLJ-1317-fix-seq-zip-to-avoid-spurious-nils.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1319",
  :name "0001-CLJ-1319-Throw-on-odd-arguments-to-PersistentArrayMa.patch",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-1323",
  :name "clj-1323-disable.diff",
  :patch-category "Clojure language/library bug fixes",
  :patch-extra-note
     "This patch was committed shortly before the 1.6.0 release.
      It merely disables the test that fails on JDK8.  This is a
      temporary measure until Clojure is modified to use a version
      of the ASM lib that makes the test pass again.",
  }
 {:ticket "CLJ-1324",
  :name "clj-1324-1.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1325",
  :name "boxed.diff",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1329",
  :name "fix.patch",
  :patch-category "Code cleanup",
  }
 {:ticket "CLJ-1330",
  :name "0001-Fix-CLJ-1330-make-top-level-named-functions-classnam.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1337",
  :name "0001-Update-defprotocol-s-docstring-to-remove-an-out-of-d.patch",
  :patch-category "Doc string fixes only",
  }
 {:ticket "CLJ-1340",
  :name "primitive-cohercion.diff",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1341",
  :name "keyword-1341-2014-02-12.2.patch",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-1351",
  :name "0001-remove-unused-swapThunk-method-generation.patch",
  :patch-category "Code cleanup",
  }
 {:ticket "CLJ-1357",
  :name "CLJ-1357-its-typo.patch",
  :patch-category "Doc string fixes only",
  }
 {:ticket "CLJ-1358",
  :name "CLJ-1358.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1361",
  :name "simple-ns-pprint-fix.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1362",
  :name "clj-1362-v1.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1366",
  :name "0002-make-the-reader-return-the-same-empty-map-when-it-re.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1372",
  :name "clj-1372-2.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1373",
  :name "clj-1373.diff",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1375",
  :name "clj-1375-v1.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1378",
  :name "clj-1378-v2.diff",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1379",
  :name "fix-quoting-in-pass-case.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1380",
  :name "clj-1380.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1383",
  :name "clj-1383.diff",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1384",
  :name "CLJ-1384-p1.patch",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1386",
  :name "0002-Add-transient-predicate.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1388",
  :name "0001-FIX-CLJ-1388.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1390",
  :name "0001-CLJ-1390.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1394",
  :name "pr-str-dispatch-value-safe.diff",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1398",
  :name "0001-update-apache-commons-javadoc-location.patch",
  :patch-category "Language/library enhancement",
  :patch-extra-note
     "There are 3 separate patches, and it appears the author is
      suggesting all 3 should be applied.  The other two are:

        0002-add-javadoc-lookup-for-guava-and-apache-commons-lang.patch
        0003-add-javadoc-lookup-for-jdk8.patch",
  }
 {:ticket "CLJ-1399",
  :name "clj-1399.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 )
