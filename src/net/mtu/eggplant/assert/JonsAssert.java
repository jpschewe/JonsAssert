/*
 * Copyright (c) 2000
 *      Jon Schewe.  All rights reserved
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * I'd appreciate comments/suggestions on the code jpschewe@mtu.net
 */
package net.mtu.eggplant.assert;

import net.mtu.eggplant.util.Debug;
import net.mtu.eggplant.util.Function;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import antlr.TokenStreamSelector;

import com.werken.opt.CommandLine;
import com.werken.opt.DuplicateOptionException;
import com.werken.opt.MissingArgumentException;
import com.werken.opt.Option;
import com.werken.opt.Options;
import com.werken.opt.UnrecognizedOptionException;

import net.mtu.eggplant.util.algorithms.Applying;

/**
 * <p>Class that starts everything off.</p>
 *
 * <a name="commandline_doc"><h2>Commandline options</h2></a>
 * <ul>
 *   <li>-f, --force  force instrumentation, regardless of file modification time</li>
 *   <li>-d, --destination &lt;dir&gt; the destination directory (default: instrumented)</li>
 *   <li>-s, --sourceExtension &lt;ext&gt; the extension on the source files (default: java)</li>
 *   <li>-i, --instrumentedExtension &lt;ext&gt; the extension on the source files (default: java)</li>
 *   <li>files all other arguments are taken to be files or directories to be parsed</li>
 * </ul>
 *
 * <p>Classes can also be instrumented by calling
 * {@link #instrument(Configuration, Collection) instrument} with a Configuration
 * object and a Collection of files.
 *
 */
public class JonsAssert {

  static /* package */ TokenStreamSelector selector = new TokenStreamSelector();
  static /* package */ JavaLexer javaLexer;
  static /* package */ AssertLexer assertLexer;
  /** the symbol table */
  static private Symtab _symtab;
  static private boolean _debugLexer = false;

  /**
   * Parses the command line then calls {@link #instrument(Configuration, Collection) instrument}.
   *
   * @param args see <a href="#commandline_doc">commandline options</a>
   */
  public static void main(final String[] args) {
    final Configuration config = new Configuration();

    //Setup all of the options
    final Options options = new Options();
    try {
      options.addOption('f', "force", false, "force instrumentation");
      options.addOption('d', "destination", true, "<dir> the destination directory (default: instrumented)");
      options.addOption('s', "sourceExtension", true, "<ext> the extension of the source files (default: java)");
      options.addOption('i', "instrumentedExtension", true, "<ext> the extension used for the instrumented files (default: java)");
      options.addOption('~', "debugLexer", false, "");
      options.addOption('!', "debug", false, "");
    } catch(final DuplicateOptionException doe) {
      System.err.println("Someone specified duplicate options in the code!");
      System.exit(1);
    }

    //list to hold files/directories to instrument
    final Collection files = new LinkedList();

    //parse options
    try {
      final CommandLine cl = options.parse(args);

      if(cl.optIsSet('f')) {
        config.setIgnoreTimeStamp(true);
      }
      if(cl.optIsSet('d')) {
        config.setDestinationDirectory(cl.getOptValue('d'));
      }
      if(cl.optIsSet('s')) {
        config.setSourceExtension(cl.getOptValue('s'));
      }
      if(cl.optIsSet('i')) {
        config.setInstrumentedExtension(cl.getOptValue('i'));
      }
      if(cl.optIsSet('~')) {
        _debugLexer = true;
      }
      if(cl.optIsSet('!')) {
        Debug.setDebugMode(true);
      }

      Applying.forEach(cl.getArgs(), new Function() {
        public void execute(final Object obj) {
          files.add(new File((String)obj));
        }
      });
      
    } catch(final MissingArgumentException mae) {
      System.err.println(mae.getMessage());
      usage(options);
      System.exit(1);
    } catch(final UnrecognizedOptionException ure) {
      System.err.println(ure.getMessage());
      usage(options);
      System.exit(1);
    }

    _symtab = new Symtab(config);

    //Set the exit status based on errors
    System.exit(instrument(config, files) ? 0 : 1);
  }

  /**
   * @pre (options != null)
   */
  static private void usage(final Options options) {
    final StringBuffer sb = new StringBuffer(256);
    sb.append("Usage: JonsAssert [options] files");
    sb.append(System.getProperty("line.separator"));

    final Iterator iter = options.getOptions().iterator();
    while(iter.hasNext()) {
      final Option option = (Option)iter.next();

      //only short options that are characters should be shown
      if(Character.isLetter(option.getOpt())) {
        sb.append('-');
        sb.append(option.getOpt());
        if(option.hasLongOpt()) {
          sb.append(", ");
        }
      } else {
        sb.append("    ");
      }
      
      if(option.hasLongOpt()) {
        sb.append("--");
        sb.append(option.getLongOpt());
      }
      
      sb.append("  ");
      if(option.getDescription() != null) {
        sb.append(option.getDescription());
      }
      sb.append(System.getProperty("line.separator"));
    }
    System.err.print(sb.toString());
  }

  /**
   * Entry point.  Starts up the parser and instruments all files.
   *
   * @param config the {@link Configuration configuration} object
   * @param files Collection of {@link java.io.File files/directories} to
   * parse.  Directories are parsed recursively.
   * @return exit status
   *
   * @pre (config != null)
   * @pre (files != null && net.mtu.eggplant.assert.CollectionUtils.checkInstanceOf(files, File.class))
   *
   */
  static public boolean instrument(final Configuration config,
                                   final Collection files) {
    _symtab = new Symtab(config);

    boolean success = true;
    // if we have at least one file to parse
    if(!files.isEmpty()) {
      System.out.println("Parsing...");
      // for each directory/file specified on the command line
      final Iterator iter = files.iterator();
      while(iter.hasNext()) {
        final File file = (File)iter.next();
        success &= doFile(file); // parse it
      }
    } else {
      System.out.println("Parsing testcases...");
      config.setIgnoreTimeStamp(true);
      //Test case
      success &= doFile(new File("net/mtu/eggplant/assert/testcases/")); // parse it
    }

    return success;
  }
  
  /**
   * This method decides what action to take based on the type of file we are
   * looking at.
   *
   * @param f the file/directory to parse, if a directory recursively look
   * through the directory for files that match the source extension and parse
   * them.
   * @return true for success
   */
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
        } catch(final IOException ioe) {
          if(Debug.isDebugMode()) {
            System.err.println("Caught exception getting file input stream: " + ioe);
          }
          success = false;
        } catch(final FileAlreadyParsedException fape) {
          //System.out.println("Source file is older than instrumented file, skipping: " + f.getName());
          success = true;
        } catch (final Exception e) {
          System.err.println("parser exception: "+e);
          if(Debug.isDebugMode()) {
            e.printStackTrace();   // so we can get a stack trace
          }
          success = false;
        } finally {
          getSymtab().finishFile(success);
        }
      }
    }
    return success;
  }

  /**
   * Actually do the work of parsing the file here.
   *
   * @param s a stream to parse  
   */
  static public void parseFile(final InputStream s) throws Exception {
      // Create a scanner that reads from the input stream passed to us
      javaLexer = new JavaLexer(s);
      assertLexer = new AssertLexer(javaLexer.getInputState());
      
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
