;;This file assumes that the following variables and methods exist:
;;assert-jde-compile-jps - compile a file with assertions
;;regular-jde-compile-jps - compile with javac
;;assert-cat-java-jps - cat the output of AssertMate into the compilation buffer
;;jikes-jde-compile-jps - compile with jikes
;;home-root - root of home project
;;java-vm - jvm to use
;;java-home - root of java install directory
(jde-set-project-name "JonesAssert")
(jde-set-variables
 '(jde-run-working-directory (expand-file-name home-root))
 '(jde-run-read-app-args t)
 '(jde-global-classpath (list
			 (expand-file-name home-root)
			 (expand-file-name "lib/antlr.jar" home-root)
			 (expand-file-name "lib/junit.jar" home-root)
			 (expand-file-name "lib/java-getopt-1.0.7.jar" home-root)
			 (expand-file-name "jre/lib/rt.jar" java-home)
			 ))
 '(jde-key-bindings (quote (("\C-c\C-v\C-c" . assert-jde-compile-jps)
			    ("\C-c\C-v\C-j" . regular-jde-compile-jps)
			    ("\C-c\C-v\C-t" . assert-cat-java-jps)
			    ("\C-c\C-v\C-k" . jikes-jde-compile-jps)
			    ("\C-c\C-v\C-r" . jde-run)
			    ("\C-c\C-v\C-d" . jde-db)
			    ("\C-c\C-v\C-b" . jde-build)
			    ("\C-c\C-v\C-a" . jde-run-menu-run-applet)
			    ("\C-c\C-v\C-n" . jde-browse-jdk-doc)
			    ("\C-c\C-v\C-p" . jde-save-project)
			    ("\C-c\C-v\C-l" . jde-gen-println))))
 '(jde-compile-option-deprecation t)
 '(jde-run-java-vm java-vm)
 '(jde-run-option-vm-args '("-DASSERT_BEHAVIOR=CONTINUE "));-Djava.rmi.server.codebase=http://mn65-eggplant/applet/ -Djava.rmi.server.hostname=mn65-eggplant.htc.honeywell.com")); -Djava.library.path=d:\\jschewe\\projects\\IPSO\\Active-R1\\com\\honywell\\goalsetter\\bpc;d:\\jschewe\\projects\\IPSO\\Active-R1\\com\\honeywell\\goalsetter\\bpc\\bpc_ellipsoid"))
 )
