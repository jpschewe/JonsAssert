;;This file assumes that the following variables and methods exist:
;;assert-root - root of the project
;;java-vm - jvm to use
;;java-home - root of java install directory
(jde-set-project-name "JonsAssert")
(jde-set-variables
 '(jde-run-working-directory (expand-file-name "src/" assert-root))
 '(jde-run-read-app-args t)
 '(jde-global-classpath (list
			 (expand-file-name "src/" assert-root)
			 (expand-file-name "lib/JonsInfra-0.1.jar" assert-root)
			 (expand-file-name "lib/antlr.jar" assert-root)
			 (expand-file-name "lib/junit.jar" assert-root)
			 (expand-file-name "lib/java-getopt-1.0.7.jar" assert-root)
			 (expand-file-name "jre/lib/rt.jar" java-home)
			 ))
 '(jde-compile-option-deprecation t)
 '(jde-run-java-vm java-vm)
 '(jde-run-option-vm-args '("-DASSERT_BEHAVIOR=CONTINUE "))
 )
