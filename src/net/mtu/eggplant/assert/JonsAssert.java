/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import antlr.TokenStreamSelector;

public class Main {

  static /* package */ TokenStreamSelector selector = new TokenStreamSelector();
  static /* package */ JavaLexer javaLexer;
  static /* package */ AssertLexer assertLexer;
  /** the symbol table */
  static private Symtab _symtab;
  
  public static void main(String[] args) {
    _symtab = new Symtab();
    
    // if we have at least one command-line argument
    if (args.length > 0 ) {
      System.err.println("Parsing...");
      // for each directory/file specified on the command line
      for(int i=0; i< args.length;i++) {
        doFile(new File(args[i])); // parse it
      }
    }
    else {
      System.err.println("Parsing...");
      //Test case
      doFile(new File("org/tcfreenet/schewe/assert/testcases/")); // parse it
    }

    //Let's see what we've found
    getSymtab().instrument();
    
  }


  // This method decides what action to take based on the type of
  //   file we are looking at
  public static void doFile(File f) {
    // If this is a directory, walk each file/dir in that directory
    if (f.isDirectory()) {
      String files[] = f.list();
      for(int i=0; i < files.length; i++)
        doFile(new File(f, files[i]));
    }

    // otherwise, if this is a java file, parse it!
    else if(f.getName().endsWith("." + AssertTools.getSourceExtension())) {
      System.err.println("   "+f.getAbsolutePath());
      if(getSymtab().startFile(f)) {
        try {
          parseFile(new FileInputStream(f));
        }
        catch(IOException ioe) {
          System.err.println("Caught exception getting file input stream: " + ioe);
        }
        catch(FileAlreadyParsedException fape) {
          System.out.println("Source file is older than instrumented file, skipping: " + f.getName());
        }
        finally {
          getSymtab().finishFile();
        }
      }
    }
  }

  // Here's where we do the real work...
  public static void parseFile(InputStream s) {
    try {
      // Create a scanner that reads from the input stream passed to us
      javaLexer = new JavaLexer(s);
      javaLexer.setTokenObjectClass("org.tcfreenet.schewe.assert.MyToken");
      assertLexer = new AssertLexer(javaLexer.getInputState());
      assertLexer.setTokenObjectClass("org.tcfreenet.schewe.assert.MyToken");
      
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
      parser.setSymtab(getSymtab());
      // start parsing at the compilationUnit rule
      parser.compilationUnit();
    }
    catch(FileAlreadyParsedException fape) {
      throw fape;
    }
    catch (Exception e) {
      System.err.println("parser exception: "+e);
      e.printStackTrace();   // so we can get stack trace		
    }
  }

  static public Symtab getSymtab() {
    return _symtab;
  }
}
