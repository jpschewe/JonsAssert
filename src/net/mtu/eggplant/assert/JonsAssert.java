/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import antlr.TokenStreamSelector;

public class Main {

  static /* package */ boolean showTree = false;
  static /* package */ TokenStreamSelector selector = new TokenStreamSelector();
  static /* package */ JavaLexer javaLexer;
  static /* package */ AssertLexer assertLexer;
  /** the symbol table */
  static private Symtab _symtab;
  
  public static void main(String[] args) {
    _symtab = new Symtab();
    
    // Use a try/catch block for parser exceptions
    try {
      // if we have at least one command-line argument
      if (args.length > 0 ) {
        System.err.println("Parsing...");

				// for each directory/file specified on the command line
        for(int i=0; i< args.length;i++) {
          if ( args[i].equals("-showtree") ) {
            showTree = true;
          }
          else {
            doFile(new File(args[i])); // parse it
          }
        }
      }
      else {
        System.err.println("Parsing...");

				// for each directory/file specified on the command line
        doFile(new File("org/tcfreenet/schewe/Assert/test/TestAssert.java")); // parse it
        
        //         System.err.println("Usage: java JavaRecogizer [-showtree] "+
        //                            "<directory or file name>");
      }
    }
    catch(Exception e) {
      System.err.println("exception: "+e);
      e.printStackTrace(System.err);   // so we can get stack trace
    }
  }


  // This method decides what action to take based on the type of
  //   file we are looking at
  public static void doFile(File f) throws Exception {
    // If this is a directory, walk each file/dir in that directory
    if (f.isDirectory()) {
      String files[] = f.list();
      for(int i=0; i < files.length; i++)
        doFile(new File(f, files[i]));
    }

    // otherwise, if this is a java file, parse it!
    else if ((f.getName().length()>5) &&
             f.getName().substring(f.getName().length()-5).equals(".java")) {
      System.err.println("   "+f.getAbsolutePath());

      // let the symbol table know what's being parsed and parse the file if we haven't already
      if(_symtab.setCurrentFile(f)) {
        parseFile(f.getName(), new FileInputStream(f));
      }
    }
  }

  // Here's where we do the real work...
  public static void parseFile(String f, InputStream s)
    throws Exception {
    try {
      // Create a scanner that reads from the input stream passed to us
      javaLexer = new JavaLexer(s);
      assertLexer = new AssertLexer(javaLexer.getInputState());
                  
      selector.addInputStream(javaLexer, "java");
      selector.addInputStream(assertLexer, "assert");
      selector.select(javaLexer);
                  
      // Create a parser that reads from the scanner
      //for debugging the lexer
      //JavaRecognizer parser = new JavaRecognizer(lexer);
      //                   Token tok = selector.nextToken();
      //                   while(tok.getText() != null) {
      //                     System.out.println("Main: " + tok);
      //                     tok = selector.nextToken();
      //                   }
      //                   System.exit(0);
      JavaRecognizer parser = new JavaRecognizer(selector);
      parser.setSymtab(_symtab);
      // start parsing at the compilationUnit rule
      parser.compilationUnit();
    }
    catch (Exception e) {
      System.err.println("parser exception: "+e);
      e.printStackTrace();   // so we can get stack trace		
    }
  }
}
