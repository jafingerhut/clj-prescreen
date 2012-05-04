(def ^:dynamic *x* 1)
;= #'user/*x*
(binding [*x* 2]
  (with-redefs [*x* 3]
    *x*))
;= 2
*x*
;= 2
