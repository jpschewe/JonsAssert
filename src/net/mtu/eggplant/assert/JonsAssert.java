/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.assert;

import org.tcfreenet.schewe.utils.Debug;

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
  static private boolean _debugLexer = false;

  /**
     Parses the command line then fires everything off.
     Starts up the parser and instruments all of the files listed in args as
     well as recursively looks in directories listed in args as well.  

     @param args see usage for details
   **/
  public static void main(final String[] args) {
    final Configuration config = new Configuration();

    //Setup all of the options
    final LongOpt[] longopts = new LongOpt[6];
    longopts[0] = new LongOpt("force", LongOpt.NO_ARGUMENT, null, 'f');
    longopts[1] = new LongOpt("destination", LongOpt.REQUIRED_ARGUMENT, null, 'd');
    longopts[2] = new LongOpt("sourceExtension", LongOpt.REQUIRED_ARGUMENT, null, 's');
    longopts[3] = new LongOpt("instrumentedExtension", LongOpt.REQUIRED_ARGUMENT, null, 'i');
    //Undocumented features for debugging
    longopts[4] = new LongOpt("debugLexer", LongOpt.NO_ARGUMENT, null, 300);
    longopts[5] = new LongOpt("debug", LongOpt.NO_ARGUMENT, null, 301);
    final Getopt g = new Getopt("JonsAssert", args, "fd:s:i:", longopts);

    int c;
    while((c = g.getopt()) != -1) {
      switch(c) {
      case 'f':
        config.setIgnoreTimeStamp(true);
        break;
      case 'd':
        config.setDestinationDirectory(g.getOptarg());
        break;
      case 's':
        config.setSourceExtension(g.getOptarg());
        break;
      case 'i':
        config.setInstrumentedExtension(g.getOptarg());
        break;
      case 300:
        _debugLexer = true;
        break;
      case 301:
        Debug.setDebugMode(true);
        break;
      default:
        //Print out usage and exit
        System.out.println("Usage: JonsAssert [options] files ...");
        System.out.println("-f, --force  force instrumentation");
        System.out.println("-d, --destination <dir> the destination directory (default: instrumented)");
        System.out.println("-s, --sourceExtension <ext> the extension on the source files (default: java)");
        System.out.println("-i, --instrumentedExtension <ext> the extension on the source files (default: java)");
        System.exit(1);
        break;
      }
    }

    _symtab = new Symtab(config);

    boolean success = true;
    // if we have at least one command-line argument
    if (args.length - g.getOptind() > 0 ) {
      System.out.println("Parsing...");
      // for each directory/file specified on the command line
      for(int i=g.getOptind(); i< args.length;i++) {
        success &= doFile(new File(args[i])); // parse it
      }
    } else {
      System.out.println("Parsing testcases...");
      config.setIgnoreTimeStamp(true);
      //Test case
      doFile(new File("org/tcfreenet/schewe/assert/testcases/")); // parse it
    }

    //Set the exit status based on errors
    System.exit((success ? 0 : 1));
  }

  /**
     This method decides what action to take based on the type of file we are
     looking at.

     @param f the file/directory to parse, if a directory recursively look
     through the directory for files that match the source extension and parse
     them.
     @return true for success
  **/
  static public boolean doFile(final File f) {
    boolean success = true;
    if (f.isDirectory()) {
      // If this is a directory, walk each file/dir in that directory      
      final String files[] = f.list();
      for(int i=0; i < files.length; i++) {
        success &= doFile(new File(f, files[i]));
      }
    } else if(f.getName().endsWith("." + getSymtab().getConfiguration().getSourceExtension())) {
      // otherwise, if this is a java file, parse it!      
      if(getSymtab().startFile(f)) {
        try {
          parseFile(new FileInputStream(f));
          success = true;
        }
        catch(IOException ioe) {
          System.err.println("Caught exception getting file input stream: " + ioe);
          success = false;
        }
        catch(FileAlreadyParsedException fape) {
          //System.out.println("Source file is older than instrumented file, skipping: " + f.getName());
          success = false;
        }
        catch (final Exception e) {
          System.err.println("parser exception: "+e);
          e.printStackTrace();   // so we can get a stack trace
          success = false;
        }
        finally {
          getSymtab().finishFile(success);
        }
      }
    }
    return success;
  }

  /**
     Actually do the work of parsing the file here.

     @param s a stream to parse  
  ***/
  static public void parseFile(final InputStream s) throws Exception {
      // Create a scanner that reads from the input stream passed to us
      javaLexer = new JavaLexer(s);
      javaLexer.setTokenObjectClass("org.tcfreenet.schewe.assert.MyToken");
      assertLexer = new AssertLexer(javaLexer.getInputState());
      assertLexer.setTokenObjectClass("org.tcfreenet.schewe.assert.MyToken");
      final ColumnTracker ct = new ColumnTracker();
      assertLexer.setColumnTracker(ct);
      javaLexer.setColumnTracker(ct);
      
      selector.addInputStream(javaLexer, "java");
      selector.addInputStream(assertLexer, "assert");
      selector.select(javaLexer);
                  
      // Create a parser that reads from the scanner
      final JavaRecognizer parser = new JavaRecognizer(selector);
      
      //for debugging the lexer
      if(_debugLexer) {
        antlr.Token tok = selector.nextToken();
        while(tok.getText() != null) {
          System.out.print("JonsAssert: " + tok);
          System.out.println(" name=" + parser.getTokenName(tok.getType()));
          tok = selector.nextToken();
        }
      } else {
        parser.setSymtab(getSymtab());
        // start parsing at the compilationUnit rule
        parser.compilationUnit();
      }
  }

  final static public Symtab getSymtab() {
    return _symtab;
  }
}
