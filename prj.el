;;This file assumes that the following variables and methods exist:
;;java-home - root of java install directory
(jde-set-project-name "JonsAssert")
(jde-set-variables
 '(jde-run-working-directory (expand-file-name "src/" (file-name-directory load-file-name)))
 '(jde-run-read-app-args t)
 '(jde-global-classpath (list
			 (expand-file-name "src/" (file-name-directory load-file-name))
			 (expand-file-name "lib/JonsInfra-0.1.jar" (file-name-directory load-file-name))
			 (expand-file-name "lib/antlr.jar" (file-name-directory load-file-name))
			 (expand-file-name "lib/junit-3.6.jar" (file-name-directory load-file-name))
			 (expand-file-name "lib/java-getopt-1.0.7.jar" (file-name-directory load-file-name))
			 (expand-file-name "lib/werken.opt.jar" (file-name-directory load-file-name))
			 (expand-file-name "jre/lib/rt.jar" java-home)
			 ))
 '(jde-compile-option-deprecation t)
 '(jde-run-option-vm-args '("-DASSERT_BEHAVIOR=CONTINUE "))
 )
