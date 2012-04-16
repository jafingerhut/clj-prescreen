# clj-prescreen

Automate some of the steps in "prescreening" Clojure patches on JIRA.

Currently it performs these checks:

1. Check whether the patch is in git format: first a quick and dirty
   regular-expression based method, then more thoroughly by trying to
   apply it as a git patch to a Clojure tree.

2. For git format patches, verify that all authors listed in the patch
   are on the list of contributors.  Both names and email addresses
   are checked for exact matches to one of the names, aliases, or
   email addresses in the file `data/people-data.clj`.  If the patch
   summary says it is "not-CA-clean", it may be that the file needs to
   have contributors added to it.

3. For patches that apply successfully, run the command 'ant'.  Any
   warnings or errors in the output (except for a select few that are
   permitted), or a non-0 exit status, is considered a failure.  If no
   problems occur, it is considered a success.

See the last `(comment ...)` block in core.clj for some instructions
on how to do perform these steps.  It is currently only set up to run
by copying and pasting some commands from that file to a Clojure REPL.


This code repeatedly makes copies of a Clojure source tree so that
each patch attempt should have no dependence on any earlier patch
attempts.

git version 1.7.9.2 or later are recommended.  I've experienced
failures using this code's patching method with git version 1.7.5.4:
the command `git am -s --keep-cr < patch-file.txt` fails in the copied
Clojure source tree, whereas it succeeds on a Clojure source tree
created by git.  Likely earlier versions would have a similar problem
(an unconfirmed guess).  I've had more success with git versions
1.7.9.2 and 1.7.10.

## Usage

See last `(comment ...)` block in core.clj

## License

Copyright (C) 2012 Andy Fingerhut

Distributed under the Eclipse Public License, the same as Clojure.
