(jde-set-project-name "JonsAssert") ;; important that it be unique

(let ((project-root (file-name-directory load-file-name)))
  ;; Setup TAGS list
  (let ((tag-cons (cons
                   (expand-file-name "src" project-root)
                   (expand-file-name "src" project-root))))
    (if (boundp 'tag-table-alist)
        (add-to-list 'tag-table-alist tag-cons)
      (setq tag-table-alist (list tag-cons))))
  ;; JDE customizations
  (jde-set-variables
   ;; Only look for all jars in lib and jar directories when they appear in
   ;; the classpath, but still leave it set so that jde-ant works using the
   ;; Java invocation method
   '(jde-lib-directory-names (list "^lib$" "^jar$")) 
   '(jde-sourcepath			(list (expand-file-name "src" project-root)))
   '(jde-run-read-app-args        	t)
   '(jde-run-option-debug         	nil) ;; don't try and debug on run
   '(jde-run-option-vm-args		(list "-DASSERT_BEHAVIOR=CONTINUE"))
   '(jde-run-working-directory 	  	(expand-file-name "build" project-root))
   '(jde-compile-option-directory 	(expand-file-name "build/classes" project-root))
   '(jde-compile-option-sourcepath 	(list (expand-file-name "src" project-root)))
   '(jde-compile-option-debug 	  	(quote ("all")))
   '(jde-compile-option-deprecation 	t)
   '(jde-build-function 		'(jde-ant-build))
   '(jde-ant-working-directory		project-root)
   '(jde-ant-read-target 		t) ;; prompt for the target name
   '(jde-ant-enable-find 		t) ;; make jde-ant look for the build file
   '(jde-ant-complete-target 		nil) ;; don't parse the build file for me
   '(jde-ant-invocation-method  	'("Java"))
   '(jde-ant-user-jar-files
     (list
      (jde-convert-cygwin-path (expand-file-name "lib/junit-3.8.1.jar" project-root))
      (jde-convert-cygwin-path (expand-file-name "lib/antlr-2.7.4.jar" project-root))
      ))
   ;;'(jde-ant-invocation-method 		'("Script"))
   ;; while not perfect, this handles the case where jde-ant isn't loaded before this file is parsed
   ;; issue is that jde-ant-args doesn't exsit yet, but it's default value is "-emacs"
   ;;'(jde-ant-args (concat
   ;;     	   "-emacs"
   ;;     	   " -lib " (jde-convert-cygwin-path (expand-file-name "lib/junit-3.8.1.jar" project-root))
   ;;     	   ))
   '(jde-import-sorted-groups 		'asc)
   '(jde-import-excluded-packages
     '("\\(bsh.*\\|sched-infra.*\\|com.sun.*\\|sunw.*\\|sun.*\\)"))
   '(jde-import-group-of-rules
     (quote
      (
       ("^\\(com\\.honeywell\\.htc\\.[^.]+\\([.][^.]+[.]\\)*\\)" . 1)
       ("^\\(com\\.honeywell\\.[^.]+\\([.][^.]+[.]\\)*\\)" . 1)
       ;;("^javax?\\.")
       ("^\\([^.]+\\([.][^.]+[.]\\)*\\)" . 1)
       )))   
   '(jde-global-classpath
     (list
      ;; this project's classes
      "./build/classes"
      
      ;; additional jars
      "./lib/JonsInfra-0.5.jar"
      "./lib/antlr-2.7.4.jar"
      "./lib/commons-cli-1.0.jar"
      "./lib/commons-collections-3.1.jar"
      "./lib/commons-logging-1.0.3.jar"
      "./lib/junit-3.8.1.jar"
      "./lib/log4j-1.2.8.jar"
      ))
   '(jde-gen-buffer-boilerplate
     (list
      "/*"
      (concat " Copyright (C) " (int-to-string (nth 5 (decode-time (current-time)))))
      " *      Jon Schewe.  All rights reserved"
      " *"
      " * Redistribution and use in source and binary forms, with or without"
      " * modification, are permitted provided that the following conditions"
      " * are met:"
      " * 1. Redistributions of source code must retain the above copyright"
      " *    notice, this list of conditions and the following disclaimer."
      " * 2. Redistributions in binary form must reproduce the above copyright"
      " *    notice, this list of conditions and the following disclaimer in the"
      " *    documentation and/or other materials provided with the distribution."
      " *"
      " * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND"
      " * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE"
      " * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE"
      " * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE"
      " * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL"
      " * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS"
      " * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)"
      " * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT"
      " * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY"
      " * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF"
      " * SUCH DAMAGE."
      " *"
      " * I'd appreciate comments/suggestions on the code jpschewe@mtu.net"
      " */"
      ))
   '(jde-gen-class-buffer-template 
     (list 
      "(funcall jde-gen-boilerplate-function)"
      "(jde-gen-get-package-statement)"
      "'>\"import org.apache.commons.logging.Log;\"'n"
      "'>\"import org.apache.commons.logging.LogFactory;\"'n"
      "'>'n"
      "(progn (require 'jde-javadoc) (jde-javadoc-insert-start-block))"
      "\" * Add class comment here!\" '>'n"
      "\" \" (jde-javadoc-insert-empty-line)"
      "\" * @version $Revision: 1.19 $\" '>'n"
      "\" \" (jde-javadoc-insert 'tempo-template-jde-javadoc-end-block \"*/\")"
      "\"public class \"" 
      "(file-name-sans-extension (file-name-nondirectory buffer-file-name))" 
      "\" \" (jde-gen-get-extend-class)" 
      "\"{\"'>'n"
      "'>'n"
      "'>\"private static final Log LOG = LogFactory.getLog(\"(file-name-sans-extension (file-name-nondirectory buffer-file-name))\".class);\"'n"
      "'>'n"
      "\"public \"" 
      "(file-name-sans-extension (file-name-nondirectory buffer-file-name))" 
      "\"()\"" 

      "\" {\"'>'n" 
      "'>'p'n" 
      "\"}\">" 
      "'>'n" 
      "(jde-gen-get-interface-implementation)"
      "'>'n"
      "\"}\">" 
      "'>'n"))
   '(jde-gen-interface-buffer-template 
     (list 
      "(funcall jde-gen-boilerplate-function)"
      "(jde-gen-get-package-statement)"
      "(progn (require 'jde-javadoc) (jde-javadoc-insert-start-block))"
      "\" * Add interface comment here!\" '>'n"
      "\" \" (jde-javadoc-insert-empty-line)"
      "\" * @version $Revision: 1.19 $\" '>'n"
      "\" \" (jde-javadoc-insert 'tempo-template-jde-javadoc-end-block \"*/\")"
      "\"public interface \"" 
      "(file-name-sans-extension (file-name-nondirectory buffer-file-name))" 
      "\" \" (jde-gen-get-extend-class)" 
      "\"{\"'>'n"
      "'>'p'n"
      "\"}\">" 
      "'>'n"))
   
   ;;here so I can remove the auto javadoc for abstract, delegate, interface implementations 
   '(jde-gen-method-template
     (list
      "(p \"Method modifiers: \" modifiers 'noinsert)"
      "(p \"Method return type: \" return-type 'noinsert)"
      "(p \"Method name: \" name 'noinsert)"
      "(p \"Method parameters: \" parameters 'noinsert)"
      "(p \"Method exceptions: \" exceptions 'noinsert)"
      "(p \"Method body: \" default-body 'noinsert)"
      "(jde-gen-delete-preceding-whitespace) 'n 'n '> 'p"
      "(jde-gen-method-signature"
      "  (tempo-lookup-named 'modifiers)"
      "  (tempo-lookup-named 'return-type)"
      "  (tempo-lookup-named 'name)"
      "  (tempo-lookup-named 'parameters)"
      "  (tempo-lookup-named 'exceptions)"
      " )"
      "'> 'p"
      
      ;;we open the bracket according to k&r style or not
      "(if jde-gen-k&r "
      " ()"
      " 'n)"
      "\"{\"'>'n"
      "(s default-body) '>'r'n"
      "\"}\"'>'n'>"
      "(progn (tempo-backward-mark) (beginning-of-line) nil)"
      ))
   
   )
  )

;;;
