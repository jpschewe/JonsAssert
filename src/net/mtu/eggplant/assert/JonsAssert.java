/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.Assert;

import JavaRecognizer;
import JavaLexer;
import java.io.*;
import antlr.collections.AST;
import antlr.collections.impl.*;
import antlr.debug.misc.*;
import antlr.*;

public class Main {

  static boolean showTree = false;
  static TokenStreamSelector selector = new TokenStreamSelector();
  static JavaLexer javaLexer;
  static AssertLexer assertLexer;
  //static AssertHelperLexer assertHelperLexer;
  
  public static void main(String[] args) {
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
        doFile(new File("org/tcfreenet/schewe/Assert/TestAssert.java")); // parse it
        
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
  public static void doFile(File f)
    throws Exception {
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
      parseFile(f.getName(), new FileInputStream(f));
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
      //selector.addInputStream(assertHelperLexer);
      selector.select(javaLexer);
                  
      // Create a parser that reads from the scanner
      //JavaRecognizer parser = new JavaRecognizer(lexer);
      //                   Token tok = selector.nextToken();
      //                   while(tok.getText() != null) {
      //                     System.out.println("Main: " + tok);
      //                     tok = selector.nextToken();
      //                   }
      //                   System.exit(0);
      JavaRecognizer parser = new JavaRecognizer(selector);
      // start parsing at the compilationUnit rule
      parser.compilationUnit();
                  
      // do something with the tree
      doTreeAction(f, parser.getAST(), parser.getTokenNames());
    }
    catch (Exception e) {
      System.err.println("parser exception: "+e);
      e.printStackTrace();   // so we can get stack trace		
    }
  }
	
  public static void doTreeAction(String f, AST t, String[] tokenNames) {
    if ( t==null ) return;
    if ( showTree ) {
      ((CommonAST)t).setVerboseStringConversion(true, tokenNames);
      ASTFactory factory = new ASTFactory();
      AST r = factory.create(0,"AST ROOT");
      r.setFirstChild(t);
      ASTFrame frame = new ASTFrame("Java AST", r);
      frame.setVisible(true);
      //		System.out.println(t.toStringList());
    }
    JavaTreeParser tparse = new JavaTreeParser();
    try {
      tparse.compilationUnit(t);
      // System.out.println("successful walk of result AST for "+f);
    }
    catch (ParserException e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
    }

  }
}
