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

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

public class JonsAssert {

  static /* package */ TokenStreamSelector selector = new TokenStreamSelector();
  static /* package */ JavaLexer javaLexer;
  static /* package */ AssertLexer assertLexer;
  /** the symbol table */
  static private Symtab _symtab;
  static /*pacakge*/ boolean _ignoreTimeStamp = false;
  
  public static void main(String[] args) {
    Configuration config = new Configuration();
    LongOpt[] longopts = new LongOpt[4];

    longopts[0] = new LongOpt("force", LongOpt.NO_ARGUMENT, null, 'f');
    longopts[1] = new LongOpt("destination", LongOpt.REQUIRED_ARGUMENT, null, 'd');
    longopts[2] = new LongOpt("sourceExtension", LongOpt.REQUIRED_ARGUMENT, null, 's');
    longopts[3] = new LongOpt("instrumentedExtension", LongOpt.REQUIRED_ARGUMENT, null, 'i');
    Getopt g = new Getopt("JonsAssert", args, "fd:s:i:", longopts);

    int c;
    String arg;
    while((c = g.getopt()) != -1) {
      switch(c) {
      case 'f':
        config.setIgnoreTimeStamp(true);
        break;
      case 'd':
        AssertTools.setDestinationDirectory(g.getOptarg());
        break;
      case 's':
        AssertTools.setSourceExtension(g.getOptarg());
        break;
      case 'i':
        AssertTools.setInstrumentedExtension(g.getOptarg());
        break;
      default:
        //Print out usage and exit
        System.err.println("Usage: JonsAssert [options] files ...");
        System.err.println("-f, --force  force instrumentation");
        System.err.println("-d, --destination <dir> the destination directory (default: instrumented)");
        System.err.println("-s, --sourceExtension <ext> the extension on the source files (default: java)");
        System.err.println("-i, --instrumentedExtension <ext> the extension on the source files (default: java)");
        System.exit(1);
        break;
      }
    }

    _symtab = new Symtab(config);
    
    // if we have at least one command-line argument
    if (args.length - g.getOptind() > 0 ) {
      System.err.println("Parsing...");
      // for each directory/file specified on the command line
      for(int i=g.getOptind(); i< args.length;i++) {
        doFile(new File(args[i])); // parse it
      }
    }
    else {
      System.err.println("Parsing testcases...");
      config.setIgnoreTimeStamp(true);
      //Test case
      doFile(new File("org/tcfreenet/schewe/assert/testcases/")); // parse it
    }
    
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
      if(getSymtab().startFile(f)) {
        boolean success = true;
        try {
          parseFile(new FileInputStream(f));
        }
        catch(IOException ioe) {
          System.err.println("Caught exception getting file input stream: " + ioe);
          success = false;
        }
        catch(FileAlreadyParsedException fape) {
          //System.out.println("Source file is older than instrumented file, skipping: " + f.getName());
          success = false;
        }
        catch (Exception e) {
          System.err.println("parser exception: "+e);
          e.printStackTrace();   // so we can get a stack trace
          success = false;
        }
        finally {
          getSymtab().finishFile(success);
        }
      }
    }
  }

  // Here's where we do the real work...
  public static void parseFile(InputStream s) throws Exception {
      // Create a scanner that reads from the input stream passed to us
      javaLexer = new JavaLexer(s);
      javaLexer.setTokenObjectClass("org.tcfreenet.schewe.assert.MyToken");
      assertLexer = new AssertLexer(javaLexer.getInputState());
      assertLexer.setTokenObjectClass("org.tcfreenet.schewe.assert.MyToken");
      ColumnTracker ct = new ColumnTracker();
      assertLexer.setColumnTracker(ct);
      javaLexer.setColumnTracker(ct);
      
      selector.addInputStream(javaLexer, "java");
      selector.addInputStream(assertLexer, "assert");
      selector.select(javaLexer);
                  
      // Create a parser that reads from the scanner
      JavaRecognizer parser = new JavaRecognizer(selector);
      
      //for debugging the lexer      
//       antlr.Token tok = selector.nextToken();
//       while(tok.getText() != null) {
//         System.out.print("JonsAssert: " + tok);
//         System.out.println(" name=" + parser.getTokenName(tok.getType()));
//         tok = selector.nextToken();
//       }
//       System.exit(0);
      
       parser.setSymtab(getSymtab());
       // start parsing at the compilationUnit rule
       parser.compilationUnit();
  }

  static public Symtab getSymtab() {
    return _symtab;
  }
}
