;;This file assumes that the following variables and methods exist:
(jde-set-project-name "JonsAssert")
(let ((project-root (file-name-directory load-file-name)))
  (jde-set-variables
   '(jde-run-working-directory (expand-file-name "build/" project-root))
   '(jde-compile-option-directory (expand-file-name "src/" project-root))
   '(jde-compile-option-debug (quote ("all")))
   '(jde-run-read-app-args t)
   '(jde-compile-option-deprecation t)
   '(jde-global-classpath (list
			   (expand-file-name "build/" project-root)
			   (expand-file-name "lib/JonsInfra-0.4.jar" project-root)
			   (expand-file-name "lib/antlr.jar" project-root)
			   (expand-file-name "lib/commons-cli-1.0.jar" project-root)
			   (expand-file-name "lib/junit-3.8.jar" project-root)
			   (expand-file-name "lib/log4j-1.2.8.jar" project-root)
			   ))
   '(jde-compile-option-deprecation t)
   '(jde-run-option-vm-args '("-DASSERT_BEHAVIOR=CONTINUE "))
   '(jde-gen-buffer-boilerplate
     (quote (
	     "/*"
	     " * Copyright (c) 2000-2003"
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
	     )))
   '(jde-gen-class-buffer-template 
     (list 
      "(funcall jde-gen-boilerplate-function)"
      "(jde-gen-get-package-statement)"
      "'>\"import org.apache.log4j.Logger;\"'n"
      "\"/**\" '>'n"
      "\" * Add class comment here!\" '>'n"
      "\" */\" '>'n'"
      "\"public class \"" 
      "(file-name-sans-extension (file-name-nondirectory buffer-file-name))" 
      "\" \" (jde-gen-get-extend-class)" 
      "\"{\"'>'n"
      "'>'n"
      "'>\"private static final Logger LOG = Logger.getLogger(\"(file-name-sans-extension (file-name-nondirectory buffer-file-name))\".class);\"'n"
      "'>'n"
      "\"public \"" 
      "(file-name-sans-extension (file-name-nondirectory buffer-file-name))" 
      "\"()\"" 
      "\" {\"'>'n" 
      "'>'p'n" 
      "\"}\">" 
      "'>'n" 
      "\"}\">" 
      "'>'n"))   
   ))
