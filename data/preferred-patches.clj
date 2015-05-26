(
 {:ticket "CLJ-15",
  :name "lazy-incremental-hashes.diff",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-99",
  :name "clj-99-v2.patch",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-107",
  :name "clj-107-v1.diff",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-124",
  :name "clj-124-daemonthreads-v1.patch",
  :patch-category "Language/library enhancement",
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
 {:ticket "CLJ-130",
  :name "0001-CLJ-130-preserve-metadata-for-AOT-compiled-namespace.patch",
  :patch-category "Clojure language/library bug fixes",
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
 {:ticket "CLJ-668",
  :name "slurp-perf-patch.diff",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-700",
  :name "clj-700-rt.patch",
  :patch-category "Allow more correct-looking Clojure code to work",
  }
 {:ticket "CLJ-701",
  :name "hoistedmethod-pass-5.diff",
  :patch-category "Clojure language/library bug fixes",
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
 {:ticket "CLJ-771",
  :name "clj-771-move-unchecked-casts-patch-v5.txt",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-787",
  :name "CLJ-787-p1.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-792",
  :name "clj-793-v5.patch",
  :patch-category "Code cleanup",
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
 {:ticket "CLJ-994",
  :name "0001-repeat-for-clojure.core.reducers.patch",
  :patch-category "Language enhancement, reducers",
  :stale-patch-last-time-applied-cleanly "Jul 2012",
  :patch-extra-note
     "None of the patches apply cleanly, but did on Jul 26, 2012.",
  }
 {:ticket "CLJ-1002",
  :name "document_chunk_fns_v2.patch",
  :patch-category "Doc string fixes only",
  }
 {:ticket "CLJ-1004",
  :name "arraychunk-seq-10004.diff",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1005",
  :name "CLJ-1005-zipmap-iterators.patch",
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
  :name "0001-CLJ-1093-v2.patch",
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
  :name "clj-1103-6.diff",
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
  :name "clj-1148-defonce-5.patch",
  :patch-category "Allow more correct-looking Clojure code to work",
  }
 {:ticket "CLJ-1151",
  :name "tiny-reducers-cleanup.diff",
  :patch-category "Code cleanup",
  }
 {:ticket "CLJ-1152",
  :name "protocol_multifn_weak_ref_cache.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1157",
  :name "clj-1157-v2.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1161",
  :name "0001-CLJ-1161-Remove-version.properties-from-sources-JAR.patch",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-1162",
  :name "CLJ-1162-p1.patch",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-1180",
  :name "001-CLJ-1180.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1189",
  :name "CLJ-1189-p1.patch",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-1208",
  :name "0001-CLJ-1208-load-own-namespace-in-deftype-defrecord-cla-v3.patch",
  :patch-category "Language/library enhancement",
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
  :name "0001-cache-hasheq-and-hashCode-for-records.patch",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1225",
  :name "clj-1225-2.txt",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1226",
  :name "0001-CLJ-1226-fix-set-of-instance-field-expression-that-r-v2.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1229",
  :name "clj-1229-count-overflow-patch-v1.txt",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1232",
  :name "0001-auto-qualify-arglists-class-names-v3.patch",
  :patch-category "Better error reporting",
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
 {:ticket "CLJ-1242",
  :name "0001-fix-for-CLJ-1242-tests.patch",
  :patch-category "Allow more correct-looking Clojure code to work",
  }
 {:ticket "CLJ-1250",
  :name "CLJ-1250-08-29-ws.patch",
  :patch-category "Clojure language/library bug fixes",
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
 {:ticket "CLJ-1266",
  :name "floats-intrinsics.diff",
  :patch-category "Language/library enhancement",
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
 {:ticket "CLJ-1284",
  :name "CLJ-1284-store-demunged-names.patch",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-1289",
  :name "CLJ-1289-p1.patch",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1290",
  :name "CLJ-1290.patch",
  :patch-category "Doc string fixes only",
  }
 {:ticket "CLJ-1292",
  :name "clj-1292.diff",
  :patch-category "Doc string fixes only",
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
  :name "clj-1297-v3.patch",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-1305",
  :name "0001-add-not-found-to-sets-and-vecs-as-functions-refs-130.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1313",
  :name "clj-1313-v3.patch",
  :patch-category "Code cleanup",
  }
 {:ticket "CLJ-1314",
  :name "clj-1314-v2.diff",
  :patch-category "Code cleanup",
  }
 {:ticket "CLJ-1317",
  :name "0001-CLJ-1317-fix-seq-zip-to-avoid-spurious-nils.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1319",
  :name "0002-CLJ-1319-Throw-on-odd-arguments-to-PersistentArrayMa.patch",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-1322",
  :name "doseq.patch",
  :patch-category "Clojure language/library bug fixes",
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
 {:ticket "CLJ-1329",
  :name "clj-1329-2.patch",
  :patch-category "Code cleanup",
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
 {:ticket "CLJ-1358",
  :name "CLJ-1358.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1361",
  :name "simple-ns-pprint-fix.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1366",
  :name "0002-make-the-reader-return-the-same-empty-map-when-it-re.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1372",
  :name "0007-CLJ-1372-consistent-hasheq-for-java.util.-List-Map-M.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1373",
  :name "clj-1373-2.diff",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1375",
  :name "clj-1375-v1.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1379",
  :name "fix-quoting-in-pass-case.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1380",
  :name "clj-1380.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1383",
  :name "clj-1383.diff",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1385",
  :name "CLJ-1385-reword-docstrings-on-transient-update-funct-2.patch",
  :patch-category "Doc string fixes only",
  }
 {:ticket "CLJ-1386",
  :name "0004-Add-transient-predicate.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1390",
  :name "CLJ-1390-pprint-GregorianCalendar.patch",
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
  :name "clj-1399-with-test.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1400",
  :name "clj-1400-4.diff",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-1402",
  :name "CLJ-1402-v2.patch",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1403",
  :name "0001-CLJ-1403-ns-resolve-returns-nil-if-class-is-not-foun.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1406",
  :name "0001-modify-clojure.core-load-lib-so-that-it-removes-the-.patch",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-1410",
  :name "CLJ-1410.patch",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1412",
  :name "0001-Add-2-arity-version-of-cycle-that-takes-the-number-o.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1414",
  :name "clj-1414-v1.patch",
  :patch-category "Doc string fixes only",
  }
 {:ticket "CLJ-1416",
  :name "0003-CLJ-1416-transients-hash-caching-interop-improvement.patch",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1420",
  :name "0001-rand-using-ThreadLocalRandom-and-tests-for-random.patch",
  :patch-category "Performance enhancement",
  :patch-extra-note
     "This patch requires JDK7 or later, breaking with JDK6.",
  }
 {:ticket "CLJ-1423",
  :name "apply-var.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1425",
  :name "0001-Fix-map-unquote-splicing.patch",
  :patch-category "Language/library enhancement", 
  }
 {:ticket "CLJ-1442",
  :name "0003-Annotate-generated-symbols-with-metadata.patch",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-1444",
  :name "0001-Fix-unquote-splicing-for-empty-seqs-This-required-ma.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1445",
  :name "clj-1445-workaround-v2.clj",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1449",
  :name "clj-1449-more-v1.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1451",
  :name "0001-CLJ-1451-add-take-until.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1452",
  :name "CLJ-1452.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1453",
  :name "0001-Throw-NSEE-in-gvec-iterator.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1456",
  :name "0001-CLJ-1456-counting-forms-to-catch-malformed-throw-for.patc",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-1458",
  :name "CLJ-1458-transient-merge3.patch",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1467",
  :name "0001-first-try-for-adding-compare.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1469",
  :name "kwinvoke.patch",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1470",
  :name "CLJ-1470-v1.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1472",
  :name "0001-CLJ-1472-Locking-macro-without-explicit-monitor-ente.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1473",
  :name "CLJ-1473_v03.patch",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-1475",
  :name "clj-1475.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1482",
  :name "0001-Replace-a-couple-of-filter-complement-usages-with-re.patch",
  :patch-category "Code cleanup",
  }
 {:ticket "CLJ-1483",
  :name "0001-Clarify-the-usage-of-replace-first-with-pattern-func.patch",
  :patch-category "Doc string fixes only",
  }
 {:ticket "CLJ-1485",
  :name "clj-1485.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1486",
  :name "0001-make-fnil-vararg.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1488",
  :name "0001-Implement-clojure.lang.Named-over-Vars.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1489",
  :name "0001-Implement-var-symbol.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1490",
  :name "CLJ-1490.1.patch",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-1492",
  :name "0001-Exclude-PersistentQueue-from-IPersistentList-eval-co.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1493",
  :name "fast_keyword_intern.diff",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1496",
  :name "ex_info_arity.diff",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1502",
  :name "clj-1502-v1.patch",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-1504",
  :name "0001-add-inline-to-some-core-predicates.patch",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1506",
  :name "fast_syntax_quote_reader.diff",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1507",
  :name "fix_npe_eval_reader.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1508",
  :name "supplied_p.diff",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1514",
  :name "0001-Use-fully-qualified-class-names-for-return-type-hint.patch",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-1516",
  :name "0001-throw-an-exception-on-def-names-containing-dots.patch",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-1517",
  :name "unrolled-collections-2.diff",
  :patch-category "Performance enhancement",
  :patch-extra-note
     "There are 2 separate patches, and it appears the author is
      suggesting both should be applied.  The other is:
        unrolled-vector-2.patch",
  }
 {:ticket "CLJ-1519",
  :name "new-ns-arity.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1521",
  :name "improve_parse_let_expr.diff",
  :patch-category "Code cleanup",
  }
 {:ticket "CLJ-1523",
  :name "doreduced2.diff",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1528",
  :name "fix-CLJ-1528.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1530",
  :name "0001-fix-LispReader-and-EdnReader-so-that-foo-bar-baz-is-.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1533",
  :name "clj-1533-2.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1534",
  :name "clj_1534.diff",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1544",
  :name "0001-CLJ-1544-force-reloading-of-namespaces-during-AOT-co-v3.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1545",
  :name "CLJ-1545-2.diff",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1556",
  :name "0001-CLJ-1556-Generate-type-functions-with-instance-check.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1562",
  :name "fix-CLJ-1418_and_1562.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1565",
  :name "CLJ-1565.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1566",
  :name "refer.patch",
  :patch-category "Doc string fixes only",
  }
 {:ticket "CLJ-1567",
  :name "0001-Remove-unused-local-in-clojure.core-condp.patch",
  :patch-category "Code cleanup",
  }
 {:ticket "CLJ-1573",
  :name "0001-transient-field-deftype.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1575",
  :name "0001-Test-for-analyzer-bug-CLJ-1575.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1579",
  :name "0001-Read-src-in-appropriate-ns-context.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1582",
  :name "0001-Allow-overriding-of-clojure.core-in-ns-and-clojure.c.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1583",
  :name "0001-make-RT.boundedLength-lazier.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1585",
  :name "clj-1585.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1586",
  :name "0001-Compiler-doesn-t-preserve-metadata-for-lazyseq-liter.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1587",
  :name "0001-PersistentArrayMap-s-assoc-doesn-t-respect-HASHTABLE.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1588",
  :name "clj-1588-2.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1595",
  :name "doseq_leaks.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1597",
  :name "0001-allow-ISeq-args-to-map-conj.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1598",
  :name "0001-if-test-expr-of-an-if-statement-is-a-literal-don-t-e.patch",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1599",
  :name "get-and-set.diff",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1607",
  :name "CLJ-1607-p1.patch",
  :patch-category "Doc string fixes only",
  }
 {:ticket "CLJ-1609",
  :name "reflector_method_bug.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1611",
  :name "drupp-clj-1611-2.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1613",
  :name "0001-CLJ-1613-evaluate-or-defaults-in-enclosing-scope-in-.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1615",
  :name "CLJ-1615-entryAt.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1620",
  :name "clj-1620-v5.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1624",
  :name "clj-1624.diff",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1630",
  :name "no-multiple-rest-params-v1.patch",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-1643",
  :name "clj-1643-gen-seq-test-v1.patch",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-1644",
  :name "CLJ-1644-array-first-nil-v2.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1645",
  :name "CLJ-1645-protocol-class-has-no-source-file-information.patch",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-1647",
  :name "kworam-clj-1647.patch",
  :patch-category "Better error reporting",
  }
 {:ticket "CLJ-1650",
  :name "0001-CLJ-1650-compile-forces-namespace-reloading-from-AOT.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1653",
  :name "clj-1653-2.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1654",
  :name "0000-reuse-seq-in-some.patch",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1656",
  :name "CLJ-1656-v5.patch",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1657",
  :name "CLJ-1657-patch.diff",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1659",
  :name "clj-1659.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1661",
  :name "CLJ-1661-v1.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1665",
  :name "CLJ-1665-faster-take-nth-transducer-without-rem.patch",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1671",
  :name "clj-1671-4.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1673",
  :name "clj-1673-2.patch",
  :patch-category "Debug/tooling enhancement",
  }
 {:ticket "CLJ-1675",
  :name "fix-string-protocol.diff",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1679",
  :name "CLJ-1679-v3.patch",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1680",
  :name "clj-1680_no_div0.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1689",
  :name "clj-1689-v3.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1690",
  :name "clj-1690.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1705",
  :name "clj-1705-3.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1714",
  :name "CLJ-1714.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1715",
  :name "0001-CLJ-1715-Use-AFn.applyToHelper-rather-than-IFn.apply.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1719",
  :name "CLJ-1719_v01.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1720",
  :name "CLJ-1720_v02.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1721",
  :name "CLJ-1721_v01.patch",
  :patch-category "Language/library enhancement",
  }
 {:ticket "CLJ-1722",
  :name "fixwithbindingsdocs.patch",
  :patch-category "Doc string fixes only",
  }
 {:ticket "CLJ-1724",
  :name "clj-1724.diff",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1730",
  :name "refer-perf.patch",
  :patch-category "Performance enhancement",
  }
 {:ticket "CLJ-1733",
  :name "clj-1733-tagged-literals-throw-on-sorted-set.patch",
  :patch-category "Clojure language/library bug fixes",
  }
 {:ticket "CLJ-1737",
  :name "clearer-CompilerException-messase.patch",
  :patch-category "Better error reporting",
  }
 )
