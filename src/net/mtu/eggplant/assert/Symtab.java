/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.assert;

import org.tcfreenet.schewe.utils.Debug;

import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;

import java.net.URL;

import java.util.Stack;
import java.util.Iterator;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.Enumeration;
import java.util.Set;
import java.util.HashSet;

/*
  Need to read the PDF again to make sure I've got these right.

  Things to keep track of:
  methods: pre/post
  classes: methods, invariants

  This to keep track of from class to class:
  interfaces: pre/post, methods, invariant

  I figure I can just write a method called __checkPre<methodName>(same
  signature) and __checkPost<methodName>(same signature) to get pre and post
  conditions and then just use introspection to call the super method if it
  exists.  Need to explicitly code all interface pre/post/invariant methods
  into all classes that implement an interface.


  DataStructures to be maintained:
    Set of all classes parsed, so we don't parse one twice
    
    Map of files: HashMap (File, SortedSet)
      classes associated with those files?
      CodeFragments associated with those files
    Map of classes:
      Key = full classname
      Value = AssertClass (contains AssertMethods)
    Map of interfaces:
      Key = full interface name
      Value = AssertClass (contains AssertMethods)

    AssertClass:
      Set of Methods in this class
      Set of invariants (Set -> AssertToken)
      
    AssertMethod:
      Set of preconditions, in order that they appear in the code (Set -> AssertToken)
      Entrance point (line, column)
      Set of exit points (Set->(line, column))
      Set of post conditions, order matters (Set -> AssertToken)
      Return type (String)
      Static or not (boolean)
      
  At end of method build CodeFragments for all pre, post, invariants and
  associate them with the current file being processed at the correct lines.
  Also put the checkPost and checkPre methods on the end of the class, right
  after the '}'

  Associate asserts with the current file as they appear

  At the end of a class create a CodeFragment that contains the checkInvariant
  method and associate it with the current file, put it at '}', get this from
  the parser token.
  

*/

/**
   Class that keeps track of all classes and interfaces parsed in this run.
   This is the place where most of the work for instrumentation gets done.
   All lookups are done here.
**/
public class Symtab {

  /**
     @pre (config != null)
  **/
  public Symtab(final Configuration config) {
    _classStack = new Stack();
    _currentClass = null;
    _currentFile = null;
    _fileStack = new Stack();
    _currentPackageName = "";
    _allPackages = new HashMap();
    _allFiles = new HashSet();
    _methodStack = new Stack();
    _currentMethod = null;
    _config = config;
  }

  private Configuration _config;
  /**
     @return the configuration object used with this symbol table.
  **/
  final public Configuration getConfiguration() {
    return _config;
  }
  
  private String _currentPackageName;
  /**
     set the package the the next class(es) belong to
  **/
  final public void setCurrentPackageName(final String packageName) {
    if(_allPackages.get(packageName) == null) {
      _allPackages.put(packageName, new HashMap());
    }
    _currentPackageName = packageName;
  }

  /**
     @return the package that we're currently defining classes for
  **/
  final public String getCurrentPackageName() {
    return _currentPackageName;
  }
  
  private InstrumentedFile _currentFile;

  final public InstrumentedFile getCurrentFile() {
    return _currentFile;
  }

  /**
     Start a file.  Sets this file as the current one and saves the state of
     the current file being processed, if there is one, nothing happens if
     this file has already been seen.
     
     @return false if the file has already been seen.
  **/
  public boolean startFile(final File f) {
    if(_allFiles.contains(f)) {
      return false;
    }

    //Keep track of the state
    if(_currentFile != null) {
      _fileStack.push(new FileState(_currentFile, _imports, _starImports));
    }

    
    final InstrumentedFile ifile = new InstrumentedFile(f);
    ifile.getFragments().add(_taglineFragment);
    
    _allFiles.add(f);
    _currentFile = ifile;
    _imports = new HashMap(50);
    _starImports = new HashSet(50);
    
    return true;
  }

  /**
     Just resets the internal pointers to files to be the last file we pushed
     on the file stack, or null if no other files are being processed.
  **/
  public void finishFile(final boolean success) {
    if(success) {
      instrument(_currentFile);
    } else {
      _allFiles.remove(_currentFile.getFile());
    }

    if(!_fileStack.isEmpty()) {
      final FileState fs = (FileState)_fileStack.pop();
      _currentFile = fs._file;
      _imports = fs._imports;
      _starImports = fs._starImports;
    } else {
      _currentFile = null;
      _imports = null;
      _starImports= null;
    }
  }
  
  /**
     Push a class onto the stack of classes to handle inner classes.  By the
     time this method has been called all of the imports have been seen as
     well as the superclass and the implemented/extended interfaces.

     @param name the name of the class, can be null in the case of anonomous
     classes
     @param invariants the invariants for this class
     
     @pre (invariants != null)
  **/
  public void startClass(final String name,
                         final Set invariants,
                         final boolean isInterface,
                         final boolean isAnonymous,
                         final String superclass) {
    final Set interfaces = new HashSet();
    
    AssertClass enclosingClass = _currentClass;
    if(_currentClass != null) {
      _classStack.push(_currentClass);
    }

    if(enclosingClass != null) {
      while(enclosingClass != null && enclosingClass.isAnonymous()) {
        enclosingClass = enclosingClass.getEnclosingClass();
      }
    }
    String className = name;
    if(isAnonymous) {
      className = enclosingClass.createAnonymousClassName();
    }
    _currentClass = new AssertClass(className, getCurrentPackageName(), isInterface, enclosingClass, isAnonymous, superclass, interfaces, _imports, _starImports);
    _currentClass.setInvariants(invariants);
    
    //System.out.println("in Symtab.startClass " + _currentClass);
    
    // add to the current package
    HashMap h = (HashMap)_allPackages.get(getCurrentPackageName());
    if(h == null) {
      _allPackages.put(getCurrentPackageName(), new HashMap());
      h = (HashMap)_allPackages.get(getCurrentPackageName());
    }
    h.put(className, getCurrentClass());

    // associate it with a file too
    //Set v = (Set)_allFiles.get(getCurrentFile());
    //v.addElement(getCurrentClass());    
  }

  /**
     Take all of the assertion methods that have been cached up and dump them
     out with this class.  This should dump out pre and post methods for each
     method defined as well as a checkInvariants method.

     @param cp the point that represents the closing curly brace of the class,
     the checkInvariant method will be inserted here.

     @pre (cp != null)
  **/
  public void finishClass(final CodePoint cp) {
    final boolean extendsObject = extendsObject(_currentClass);
    //add the invariant method, not on interfaces though
    if(!_currentClass.isInterface() && !(extendsObject && _currentClass.getInvariants().isEmpty())) {
      final String code = CodeGenerator.generateInvariantMethod(_currentClass);
      final CodeFragment cf = new CodeFragment(cp, code, CodeFragmentType.INVARIANT);
      _currentFile.getFragments().add(cf);
    }
    
    _currentFile.getClasses().addElement(_currentClass);
    
    if(!_classStack.isEmpty()) {
      _currentClass = (AssertClass)_classStack.pop();
    } else {
      _currentClass = null;
    }
  }

  /**
     Accessor for the currnet class being instrumented.

     @return the current class being parsed, may return null
  **/
  final public AssertClass getCurrentClass() {
    return _currentClass;
  }

  /**
     instrument a file we've parsed.
  **/
  public void instrument(final InstrumentedFile ifile) {
    String packageName = null;
    //Add CodeFragments from all of the classes that are in ifile
    final Iterator classIter = ifile.getClasses().iterator();
    while(classIter.hasNext()) {
      final AssertClass aClass = (AssertClass)classIter.next();
      packageName = aClass.getPackage();
      addClassInstrumentation(ifile, aClass);
      //System.out.println(aClass);
    }
    //System.out.println(ifile.getFragments());
    //sort the list
    //dump out the fragments and instrument the file
    try {
      final LineNumberReader reader = new LineNumberReader(new FileReader(ifile.getFile()));
      final String ifilename = getConfiguration().getInstrumentedFilename(ifile.getFile(), packageName);
        
      final BufferedWriter writer = new BufferedWriter(new FileWriter(ifilename));

      // instrument lines
      final Iterator fragIter = ifile.getFragments().iterator();
      CodeFragment curFrag = null;
      if(fragIter.hasNext()) {
        //Have something to instrument
        curFrag = (CodeFragment)fragIter.next();
        String line = reader.readLine();
        while(line != null) {
          final StringBuffer buf = new StringBuffer(line);        
          int offset = 0;        
          while(curFrag != null && reader.getLineNumber() == curFrag.getLocation().getLine()) {
            //instrument line
            offset = curFrag.instrumentLine(offset, buf);
            if(fragIter.hasNext()) {
              curFrag = (CodeFragment)fragIter.next();
            }
            else {
              curFrag = null;
            }
          }
          //now write out the modified line
          writer.write(buf.toString());
          writer.newLine();
          line = reader.readLine();
        }
      } else {
        //short-circuit and just copy the file over one line at a time
        String line = reader.readLine();
        while(line != null) {
          writer.write(line);
          writer.newLine();
          line = reader.readLine();
        }
      }

      writer.close();
      reader.close();
      //while(fragIter.hasNext()) {
      //  CodeFragment fragment = (CodeFragment)fragIter.next();
      //  System.out.println(fragment);
      //}
    } catch(final IOException ioe) {
      ioe.printStackTrace();
    }
  }

  /**
     Associate this CodeFragment with the current file begin parsed.

     @param cf fragment of code that needs to be inserted

     @pre (cf != null)
  **/
  public void addCodeFragment(final CodeFragment cf) {

    SortedSet fragments = getCurrentFile().getFragments();
    if(fragments.contains(cf)) {
      throw new RuntimeException("CodeFragment matches another one already associated with the file: " + cf);
    }
    fragments.add(cf);
  }

  /**
     Add to the list of imports for this class.

     @param className the name of class from the import statement, null if a star import
     @param packageName the name of the package package from the import statement
  **/
  public void addImport(final String className, final String packageName) {
    final String shortenedPackageName = packageName.substring(1);
    if(className != null) {
      Set v = (Set)_imports.get(shortenedPackageName);
      if(v == null) {
	v = new HashSet(20);
      }
      //add the class to the list of classes for this package import
      v.add(className);
      _imports.put(shortenedPackageName, v);
    } else {
      _starImports.add(shortenedPackageName);
    }
  }

  /**
     Creates a new AssertMethod object for the current class and sets this as
     the current method.

     @param name the name of this method, null for a constructor
     @param preConditions the preconditions for this method
     @param postConditions the postconditions for this method
     @param params Set of StringPairs, (class, parameter name)
     @param retType the return type of this method, null signals this method is a constructor
     @param mods the modifiers for the method, Set of Strings
     
     @pre (preConditions != null && org.tcfreenet.schewe.utils.JPSCollections.elementsInstanceOf(postConditions, AssertToken.class))
     @pre (postConditions != null && org.tcfreenet.schewe.utils.JPSCollections.elementsInstanceOf(preConditions, AssertToken.class))
     @pre (params != null && org.tcfreenet.schewe.utils.JPSCollections.elementsInstanceOf(StringPair.class))
     @pre (mods != null && org.tcfreenet.schewe.utils.JPSCollections.elementsInstanceOf(String.class))
  **/
  public void startMethod(final String name,
                          final Set preConditions,
                          final Set postConditions,
                          final Set params,
                          final String retType,
                          final Set mods) {
    
    if(_currentMethod != null) {
      _methodStack.push(_currentMethod);
    }
    String theName;
    if(name == null) {
      theName = _currentClass.getName();
      //get rid of outer classes if this is an inner class
      int indexofdollar = theName.lastIndexOf('$');
      if(indexofdollar > 0) {
        theName = theName.substring(indexofdollar+1);
      }
    } else {
      theName = name;
    }
    
    _currentMethod = new AssertMethod(_currentClass, theName, preConditions, postConditions, params, retType, mods);
  }

  /**
     Finishes the method that is currently being parsed.  This will pop a
     method off the method stack, if one exists.  This will also set the
     method entrance and add a method exit if the method is void.

     @param startEnd two code points representing the opening and closing braces of the method, these points are equal if the method is abstract/native
     @param thrownExceptions list of exceptions this method declares in it's throws clause, null if no exceptions are declared
     
     @pre (startEnd != null)
  **/
  public void finishMethod(final CodePointPair startEnd,
                           final Set thrownExceptions) {
    //System.out.println("in finish method startEnd: " + startEnd + " _currentMethod: " + _currentMethod + " thrownExceptions: " + thrownExceptions);

    if(thrownExceptions != null) {
      _currentMethod.setThrownExceptions(thrownExceptions);
    }
    
    if(!_currentMethod.isAbstract()) {
      //[jpschewe:20000216.0742CST] add 1 so that we add the pre and post calls in the right place 
      _currentMethod.setMethodEntrance(new CodePoint(startEnd.getCodePointOne().getLine(),
                                                     startEnd.getCodePointOne().getColumn() + 1));
    }
    //[jpschewe:20000216.0718CST] keep track of the closing brace, add 1 so we insert code after the '}'
    CodePoint close = new CodePoint(startEnd.getCodePointTwo().getLine(),
                                    startEnd.getCodePointTwo().getColumn() + 1);
    _currentMethod.setClose(close);
    
    //[jpschewe:20000216.0718CST] if it's a void method this is an exit
    //if(_currentMethod.isVoid()) {
    //  _currentMethod.addExit(new CodePointPair(startEnd.getCodePointTwo(), startEnd.getCodePointTwo()));
    //}
    
    _currentClass.addMethod(_currentMethod);
    
    if(!_methodStack.isEmpty()) {
      _currentMethod = (AssertMethod)_methodStack.pop();
    }
    else {
      _currentMethod = null;
    }
  }

  /**
     Get at the method that's currently being parsed.
  **/
  public AssertMethod getCurrentMethod() {
    return _currentMethod;
  }

  /**
     Add the pre and post condition calls for this class to ifile as well as
     calls to the checkInvariant method.
  **/
  private void addClassInstrumentation(final InstrumentedFile ifile,
                                       final AssertClass aClass) {
    if(aClass.isInterface()) {
      //Don't touch interfaces
      return;
    }
    //If the superclass is null and the interfaces list is empty we know it
    //extends java.lang.Object and we can skip assertion checks on the
    //superclass.  Any class that explicitly extends java.lang.Object will
    //still have the calls until I figure out a better way to check.  However
    //this should fix the infinite recursion when compiling this package with
    //itself.
    final boolean extendsObject = extendsObject(aClass);
    final String invariantCall = CodeGenerator.generateInvariantCall(aClass);
    
    final Iterator methodIter = aClass.getMethods().iterator();
    while(methodIter.hasNext()) {
      final AssertMethod method = (AssertMethod)methodIter.next();
      String shortmclassName = method.getContainingClass().getName().replace('.', '_');
      shortmclassName = shortmclassName.replace('$', '_');
      
      //System.out.println("method: " + method);

      if(method.isConstructor()) {
        //[jpschewe:20000416.2142CST] FIX skip constructors for now, still needs some thought
        //ifile.getFragments().add(new CodeFragment(method.getEntrance(), CodeGenerator.generateConstrauctorAssertions(method), CodeFragmentType.PRECONDITION));

      }
      else if(!method.isAbstract()) {
        //can't put calls inside abstract methods        
        final CodePoint entrance = method.getEntrance();      

        if(! (extendsObject && method.getPreConditions().isEmpty()) ) {
          //Add a call to the precondition method at entrance
          final String preCall = CodeGenerator.generatePreConditionCall(method);      
          ifile.getFragments().add(new CodeFragment(entrance, preCall, CodeFragmentType.PRECONDITION));
        }

//         String oldValues = CodeGenerator.generateOldValues(method);
        //Put a try-finally around void methods for post & invariant condition checks
        if(method.isVoid() && !(extendsObject && method.getContainingClass().getInvariants().isEmpty() && method.getPostConditions().isEmpty()) ) {
          ifile.getFragments().add(new CodeFragment(entrance, " boolean _JPS_foundException" + shortmclassName + " = false; try { ", CodeFragmentType.OLDVALUES));
        }

        
        if(!method.isStatic() && !method.isPrivate() && !method.isConstructor()
           && !(extendsObject && method.getContainingClass().getInvariants().isEmpty()) ) {
          //Add a call to the invariant method at entrance
          ifile.getFragments().add(new CodeFragment(entrance, invariantCall, CodeFragmentType.INVARIANT));
        }

        //build the code fragments outside the loop for effiency
        //[jpschewe:20000216.0704CST] need to keep track of retVal
        final String postSetup = "final " + method.getReturnType() + " __retVal" + shortmclassName + " ="; 
        final String postCall = CodeGenerator.generatePostConditionCall(method);
        if(!method.isVoid()) {
          final Iterator exits = method.getExits().iterator();
          while(exits.hasNext()) {
            final CodePointPair exit = (CodePointPair)exits.next();

            if(! (extendsObject && method.getContainingClass().getInvariants().isEmpty()) ) {
              //create a new scope around each exit
              if(!method.isStatic() && !method.isPrivate()) {
                //Add a call to the invariant at each exit
                final String myinvariantCall = "{" + invariantCall;
                ifile.getFragments().add(new CodeFragment(exit.getCodePointOne(), myinvariantCall, CodeFragmentType.INVARIANT));
              }
              else {
                ifile.getFragments().add(new CodeFragment(exit.getCodePointOne(), "{", CodeFragmentType.INVARIANT));
              }
            }
            

            if(! (extendsObject && method.getPostConditions().isEmpty()) ) {
              //Add a call to the postCondition at each exit
              //save the return value
              ifile.getFragments().add(new CodeModification(exit.getCodePointOne(), "return", postSetup, CodeFragmentType.POSTCONDITION));
            }

            if(! (extendsObject && method.getContainingClass().getInvariants().isEmpty()) ) {
              //finish the scope we just created and call the post condition method
              String myPostCall = postCall + "}";
              ifile.getFragments().add(new CodeFragment(exit.getCodePointTwo(), myPostCall, CodeFragmentType.POSTCONDITION2));
            }
          }
        }
        else if( !(extendsObject && method.getContainingClass().getInvariants().isEmpty() && method.getPostConditions().isEmpty()) ) {
          //Add in a finally clause
          //finally {
          //checkInvariant
          //checkPost
          //}

          //Subtract 1 so that we add just before the '}'
          CodePoint insertFinallyAt = new CodePoint(method.getClose().getLine(), method.getClose().getColumn() - 1);
          StringBuffer codeToInsert = new StringBuffer();
          codeToInsert.append("}");
          //catch programmers exceptions first so my catches are reachable
          boolean catchRuntime = true;
          boolean catchError = true;
          Iterator exceptionIter = method.getThrownExceptions().iterator();
          while(exceptionIter.hasNext()) {
            String exception = (String)exceptionIter.next();
            //Make sure we don't try and catch some exceptions twice
            if(exception.equals("RuntimeException")
               || exception.equals("java.lang.RuntimeException")
               || exception.equals("Exception")
               || exception.equals("java.lang.RuntimeException")) {
              catchRuntime = false;
            }
            else if(exception.equals("Error")
                    || exception.equals("java.lang.Error")) {
              catchError = false;
            }
            else if(exception.equals("Throwable")
                    || exception.equals("java.lang.Throwable")) {
              catchError = false;
              catchRuntime = false;
            }
            codeToInsert.append("catch(");
            codeToInsert.append(exception);
            codeToInsert.append(" _JPS_exception");
            codeToInsert.append(shortmclassName);
            codeToInsert.append(") {");
            codeToInsert.append("_JPS_foundException");
            codeToInsert.append(shortmclassName);
            codeToInsert.append(" = true;");
            codeToInsert.append("throw _JPS_exception");
            codeToInsert.append(shortmclassName);
            codeToInsert.append(";");
            codeToInsert.append("}"); //end catch
          }
          if(catchError) {
            //catch java.lang.Error
            codeToInsert.append("catch(Error _JPS_exception");
            codeToInsert.append(shortmclassName);
            codeToInsert.append(") {");
            codeToInsert.append("_JPS_foundException");
            codeToInsert.append(shortmclassName);
            codeToInsert.append("= true;");
            codeToInsert.append("throw _JPS_exception");
            codeToInsert.append(shortmclassName);
            codeToInsert.append(";");
            codeToInsert.append("}"); //end catch
          }
          if(catchRuntime) {
            //catch java.lang.RuntimeException
            codeToInsert.append("catch(RuntimeException _JPS_exception");
            codeToInsert.append(shortmclassName);
            codeToInsert.append(") {");
            codeToInsert.append("_JPS_foundException");
            codeToInsert.append(shortmclassName);
            codeToInsert.append(" = true;");
            codeToInsert.append("throw _JPS_exception");
            codeToInsert.append(shortmclassName);
            codeToInsert.append(";");
            codeToInsert.append("}"); //end catch          
          }
          codeToInsert.append("finally { ");
          codeToInsert.append("if(!_JPS_foundException");
          codeToInsert.append(shortmclassName);
          codeToInsert.append(") {");
          if(!method.isStatic() && !method.isPrivate() && !(extendsObject && method.getContainingClass().getInvariants().isEmpty()) ) {
            codeToInsert.append(invariantCall);
          }
          if(!(extendsObject && method.getPostConditions().isEmpty())) {
            codeToInsert.append(postCall);
          }
          codeToInsert.append("}"); // end if
          codeToInsert.append("}"); // end finally
          ifile.getFragments().add(new CodeFragment(insertFinallyAt, codeToInsert.toString(), CodeFragmentType.POSTCONDITION));
        }
      }//end if not abstract

      final CodePoint close = method.getClose();
      //Add the pre and post check methods at the end of the method      
      if(!method.isConstructor()) {
        //[jpschewe:20001008.2209CST] skip on constructor for now
        if(!(extendsObject && method.getPreConditions().isEmpty())) {
          final String preMethod = CodeGenerator.generatePreConditionMethod(method);
          ifile.getFragments().add(new CodeFragment(close, preMethod, CodeFragmentType.PRECONDITION));
        }
      }
      if(!(extendsObject && method.getPostConditions().isEmpty())) {
        final String postMethod = CodeGenerator.generatePostConditionMethod(method);
        ifile.getFragments().add(new CodeFragment(close, postMethod, CodeFragmentType.POSTCONDITION));
      }
    }

  }

  /**
     @return true if the destination for the current file is older than the
     source file, or doesn't exist.  Which means that we should parse this
     file.
  **/
  public boolean isDestinationOlderThanCurrentFile(final String packageName) {
    File destFile = new File(getConfiguration().getInstrumentedFilename(getCurrentFile().getFile(), packageName));
    return !destFile.exists() || (destFile.lastModified() < getCurrentFile().getFile().lastModified());
  }
    

  /**
     @return true if clazz extends Object and has no implemented interfaces.
     This signals a case where optimizations can be done by not generated
     assertion methods.
  **/
  static public boolean extendsObject(final AssertClass clazz) {
    final String superclass = clazz.getSuperclass();
    return (superclass == null || superclass.equals("java.lang.Object")) &&
      clazz.getInterfaces().isEmpty();
  }
  
  private AssertClass _currentClass;
  private HashMap _allPackages;
  /**
     Set of Files.
  **/
  private Set _allFiles;
  private Stack _classStack;
  private Stack _fileStack;
  private Stack _methodStack;
  private AssertMethod _currentMethod;
  
  /**
     HashMap of imports, each key is a package name, each value is a Set of
     class names.
  **/
  private HashMap _imports;
  /**
     Collection of packages that are imported via star imports.
  **/
  private Set _starImports;
  
  /**
     Code fragment to insert at the top of each file.
  **/
  static final private CodeFragment _taglineFragment = new CodeFragment(new CodePoint(1, 0), "/*This file preprocessed with Jon's Assert Package*/", CodeFragmentType.PRECONDITION);


  //------------- Innter classes below here -------------------
  static private class FileState {
    public FileState(final InstrumentedFile file, final HashMap imports, final Set starImports) {
      _imports = imports;
      _starImports = starImports;
      _file = file;
    }

    /*package*/ HashMap _imports;
    /*package*/ InstrumentedFile _file;
    /*package*/ Set _starImports;
  }
  
}

