;;This file assumes that the following variables and methods exist:
;;java-home - root of java install directory
(jde-set-project-name "JonsAssert")
(let ((project-root (file-name-directory load-file-name)))
  (jde-set-variables
   '(jde-run-working-directory (expand-file-name "src/" project-root))
   '(jde-run-read-app-args t)
   '(jde-compile-option-directory (expand-file-name "src/" project-root))
   '(jde-global-classpath (list
			   (expand-file-name "src/" project-root)
			   (expand-file-name "lib/JonsInfra-0.1.jar" project-root)
			   (expand-file-name "lib/antlr.jar" project-root)
			   (expand-file-name "lib/junit-3.6.jar" project-root)
			   (expand-file-name "lib/werken.opt.jar" project-root)
			   (expand-file-name "jre/lib/rt.jar" java-home)
			   ))
   '(jde-compile-option-deprecation t)
   '(jde-run-option-vm-args '("-DASSERT_BEHAVIOR=CONTINUE "))
   )
  )