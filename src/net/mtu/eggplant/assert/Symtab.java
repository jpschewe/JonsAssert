/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.assert;

import org.tcfreenet.schewe.utils.StringPair;
import org.tcfreenet.schewe.utils.Pair;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.FileReader;
import java.io.FileWriter;

import java.net.URL;

import java.util.Stack;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Vector;
import java.util.SortedSet;
import java.util.Enumeration;
import java.util.List;

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
    List of all classes parsed, so we don't parse one twice
    
    List of files: HashTable (File, SortedSet)
      classes associated with those files?
      CodeFragments associated with those files
    List of classes:
      Key = full classname
      Value = AssertClass (contains AssertMethods)
    List of interfaces:
      Key = full interface name
      Value = AssertClass (contains AssertMethods)

    AssertClass:
      List of Methods in this class
      List of invariants (List -> AssertToken)
      
    AssertMethod:
      List of preconditions, in order that they appear in the code (List -> AssertToken)
      Entrance point (line, column)
      List of exit points (List->(line, column))
      List of post conditions, order matters (List -> AssertToken)
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

  public Symtab() {
    _classStack = new Stack();
    _currentClass = null;
    _currentFile = null;
    _fileStack = new Stack();
    _currentPackageName = "";
    _allPackages = new Hashtable();
    _allFiles = new Vector();
    _methodStack = new Stack();
    _currentMethod = null;
  }

  private String _currentPackageName;
  /**
     set the package the the next class(es) belong to
  **/
  public void setCurrentPackageName(final String packageName) {
    if(_allPackages.get(packageName) == null) {
      _allPackages.put(packageName, new Hashtable());
    }
    _currentPackageName = packageName;
  }

  /**
     @return the package that we're currently defining classes for
  **/
  public String getCurrentPackageName() {
    return _currentPackageName;
  }
  
  private InstrumentedFile _currentFile;

  public InstrumentedFile getCurrentFile() {
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
    InstrumentedFile ifile = new InstrumentedFile(f);
    ifile.getFragments().add(_taglineFragment);
    
    _allFiles.add(f);
    _currentFile = ifile;
    _imports = new Hashtable();
    
    if(_currentFile != null) {
      _fileStack.push(new Pair(_currentFile, _imports));
    }

    return true;
  }

  /**
     Associate this import statement with the current file being processed so
     that it may be used to lookup class and interface names.
  **/
  public void addImport(final String importLine) {
    //[jpschewe:20000129.0924CST] need to handle * imports, ewww
    
  }
  
  /**
     Just resets the internal pointers to files to be the last file we pushed
     on the file stack, or null if no other files are being processed.
  **/
  public void finishFile(boolean success) {
    if(success) {
      instrument(_currentFile);
    }
    else if(success) {
      _allFiles.remove(_currentFile.getFile());
    }

    if(!_fileStack.isEmpty()) {
      Pair p = (Pair)_fileStack.pop();
      _currentFile = (InstrumentedFile)p.getOne();
      _imports = (Hashtable)p.getTwo();
    }
    else {
      _currentFile = null;
      _imports = null;
    }
  }
  
  /**
     Push a class onto the stack of classes to handle inner classes.

     @param name the name of the class, can be null in the case of anonomous
     classes
     @param invariants the invariants for this class
     
     @pre (invariants != null)
     
  **/
  public void startClass(final String name, final List invariants, final boolean isInterface) {
    if(_currentClass != null) {
      _classStack.push(_currentClass);
    }
    _currentClass = new AssertClass(name, getCurrentPackageName(), isInterface);
    _currentClass.setInvariants(invariants);
    
    //System.out.println("in Symtab.startClass " + _currentClass);
    
    // add to the current package
    Hashtable h = (Hashtable)_allPackages.get(getCurrentPackageName());
    if(h == null) {
      _allPackages.put(getCurrentPackageName(), new Hashtable());
      h = (Hashtable)_allPackages.get(getCurrentPackageName());
    }
    h.put(name, getCurrentClass());

    // associate it with a file too
    //List v = (List)_allFiles.get(getCurrentFile());
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
    //add the invariant method, not on interfaces though
    if(!_currentClass.isInterface()) {
      String code = CodeGenerator.generateInvariantMethod(_currentClass);
      CodeFragment cf = new CodeFragment(cp, code, CodeFragmentType.INVARIANT);
      _currentFile.getFragments().add(cf);
    }
    
    _currentFile.getClasses().addElement(_currentClass);
    
    if(!_classStack.isEmpty()) {
      _currentClass = (AssertClass)_classStack.pop();
    }
    else {
      _currentClass = null;
    }
  }

  /**
     Accessor for the currnet class being instrumented.

     @return the current class being parsed, may return null
  **/
  public AssertClass getCurrentClass() {
    return _currentClass;
  }

  /**
     instrument a file we've parsed.
  **/
  public void instrument(final InstrumentedFile ifile) {
    String packageName = null;
    //Add CodeFragments from all of the classes that are in ifile
    Iterator classIter = ifile.getClasses().iterator();
    while(classIter.hasNext()) {
      AssertClass aClass = (AssertClass)classIter.next();
      packageName = aClass.getPackage();
      addClassInstrumentation(ifile, aClass);
      //System.out.println(aClass);
    }
    //System.out.println(ifile.getFragments());
    //sort the list
    //dump out the fragments and instrument the file
    try {
      LineNumberReader reader = new LineNumberReader(new FileReader(ifile.getFile()));
      String ifilename = AssertTools.getInstrumentedFilename(ifile.getFile(), packageName);
        
      FileWriter writer = new FileWriter(ifilename);

      // instrument lines
      Iterator fragIter = ifile.getFragments().iterator();
      CodeFragment curFrag = null;
      if(fragIter.hasNext()) {
        curFrag = (CodeFragment)fragIter.next();
      }
      else {
        //short-circuit and just copy the file over
      }
      String line = reader.readLine();
      while(line != null) {
        StringBuffer buf = new StringBuffer(line);        
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
        writer.write(buf.toString() + "\n");
        line = reader.readLine();
      }
      writer.close();
      reader.close();
      //while(fragIter.hasNext()) {
      //  CodeFragment fragment = (CodeFragment)fragIter.next();
      //  System.out.println(fragment);
      //}
    }
    catch(final IOException ioe) {
      ioe.printStackTrace();
    }
  }

  /**
     Resolve a class or interface.  Given a name of a class/interface, find the package it
     belongs in.  We're assuming that if it isn't a fully qualified name it's
     in the list of imports or in the current package.  

     @return a StringPair where the first object is the package and the second
     is the class/interface name.  If we can't find the class file for the
     class/interface, return null.

     @pre (name != null)
  **/
  public StringPair resolveClass(final String name) {
    String packageName = null;
    String className = null;
    /*
      Should first check if it's already been parsed.  Maybe do this at a
      higher level?  */
    //[jpschewe:20000130.0009CST] what about inner classes?
    /*
      if the base name up to the first period can be found normally, then it's
      an innerclass.
    */
    if(name.indexOf('.') != -1) {
      className = name;
      if(findClassSource(getCurrentPackageName(), name) != null) {
        packageName = getCurrentPackageName();
      }
      else if(findClassSource("java.lang", name) != null) {
        packageName = "java.lang";
      }
      else {
        Enumeration iter = _imports.keys();
        while(iter.hasMoreElements() && packageName == null) {
          String pn = (String)iter.nextElement();
          List possibles = (List)_imports.get(pn);
          if(possibles.size() > 0) {
            if(possibles.contains(name)) {
              packageName = pn;
            }
          }
          else {
            //star import, ewww              
            if(findClassSource(pn, name) != null) {
              packageName = pn;
            }
          }
        }
      }
    }
    else {
      //break name at last '.' and make the first piece packageName and the second className      
      int lastDot = name.lastIndexOf('.');
      packageName = name.substring(0, lastDot);
      className = name.substring(lastDot+1);
    }

    //now see if we actually found something
    if(packageName != null) {
      return new StringPair(packageName, className);
    }
    
    return null;
  }

  /**
     Parse the given class.

     @param name the name of the interface.  If this is not a fully-qualified
     name, it will be resolved and then parsed.

     @return true on a successful parse
     
     @pre (name != null)
  **/
  public boolean parseClass(final String name) {
    StringPair result = resolveClass(name);
    if(result == null) {
      return false;
    }

    String packageName = result.getStringOne();
    String className = result.getStringTwo();
    URL url = findClassSource(packageName, className);
    if(url == null) {
      return false;
    }
    // parse it
    try {
      InputStream is = url.openStream();
      //[jpschewe:20000129.0959CST] pass off to Main
    
      return true;    
    }
    catch (IOException ioe) {
      System.err.println("Got error parsing file: " + ioe);
      return false;
    }
  }

                                       
  static public URL findClass(final String packageName, final String name) {
    return findResource(packageName, name, "class");
  }

  static public URL findClassSource(final String packageName, final String name) {
    return findResource(packageName, name, AssertTools.getSourceExtension());
  }
  
  /**
     <p>Find the URL that points to the source for this package.  If it can't be
     found return null.</p>

     @param packageName the package to look in
     @param name the name of the file
     @param extension the extension of the file
     
     [jpschewe:20000129.0914CST] doesn't yet handle inner interfaces, search for '.' in name and use that for the file to look for
     [jpschewe:20000129.0918CST] should be modified to sort results, prefer a file reference
     [jpschewe:20000129.0919CST] how does one do local interfaces?, jikes can't do it unless the class is already compiled.
  **/
  static private URL findResource(final String packageName, final String name, final String extension) {
    String fileName = packageName.replace('.', '/') + "/" + name + '.' + extension;
    try {
      Enumeration enum = ClassLoader.getSystemClassLoader().getResources(fileName);
      if(enum.hasMoreElements()) {
        return (URL)enum.nextElement();
      }
      else {
        return null;
      }
    }
    catch(IOException ioe) {
      System.err.println("Got error looking for interface source " + packageName + "." + name + " skipping");
      return null;
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
  **/
  public void addImport(final String className, final String packageName) {
    String shortenedPackageName = packageName.substring(1);
    //System.out.println("in addImport token className " + className + " packageName " + packageName + " shortedPackageName " + shortenedPackageName);
    if(className != null) {
      List v = (List)_imports.get(shortenedPackageName);
      if(v == null) {
	v = new Vector();
      }
      //add the class to the list of classes for this package import
      v.add(className);
      _imports.put(shortenedPackageName, v);
    }
    else {
      //empty vector means everything in this package
      _imports.put(shortenedPackageName, new Vector());
    }
  }

  /**
     Creates a new AssertMethod object for the current class and sets this as
     the current method.

     @param name the name of this method, null for a constructor
     @param preConditions the preconditions for this method
     @param postConditions the postconditions for this method
     @param params List of StringPairs, (class, parameter name)
     @param retType the return type of this method, null signals this method is a constructor
     @param mods the modifiers for the method, List of Strings
     
     @pre (preConditions != null && org.tcfreenet.schewe.utils.JPSCollections.elementsInstanceOf(postConditions, AssertToken.class))
     @pre (postConditions != null && org.tcfreenet.schewe.utils.JPSCollections.elementsInstanceOf(preConditions, AssertToken.class))
     @pre (params != null && org.tcfreenet.schewe.utils.JPSCollections.elementsInstanceOf(StringPair.class))
     @pre (mods != null && org.tcfreenet.schewe.utils.JPSCollections.elementsInstanceOf(String.class))
  **/
  public void startMethod(final String name,
                          final List preConditions,
                          final List postConditions,
                          final List params,
                          final String retType,
                          final List mods) {
    
    boolean isPrivate = mods.contains("private");
    boolean isStatic = mods.contains("static");
    boolean isAbstract = mods.contains("abstract");
    boolean isNative = mods.contains("native");
    
    if(_currentMethod != null) {
      _methodStack.push(_currentMethod);
    }
    String theName;
    if(name == null) {
      theName = _currentClass.getName();
    } else {
      theName = name;
    }
    
    _currentMethod = new AssertMethod(_currentClass, theName, preConditions, postConditions, params, retType, isStatic, isPrivate, (isAbstract || isNative));

  }

  /**
     Finishes the method that is currently being parsed.  This will pop a
     method off the method stack, if one exists.  This will also set the
     method entrance and add a method exit if the method is void.

     @param startEnd two code points representing the opening and closing braces of the method, these points are equal if the method is abstract/native

     @pre (startEnd != null)
  **/
  public void finishMethod(final CodePointPair startEnd) {
    //System.out.println("in finish method");
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
    if(_currentMethod.isVoid()) {
      _currentMethod.addExit(new CodePointPair(startEnd.getCodePointTwo(), startEnd.getCodePointTwo()));
    }
    
    String retType = _currentMethod.getReturnType();
    if(_currentMethod.isVoid()) {
      //_currentMethod.addExit(startEnd.getCodePointTwo(), null);
    }
    
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
    String invariantCall = CodeGenerator.generateInvariantCall(aClass);
    
    Iterator methodIter = aClass.getMethods().iterator();
    while(methodIter.hasNext()) {
      AssertMethod method = (AssertMethod)methodIter.next();
      //System.out.println("method: " + method);

      //can't put calls inside abstract methods
      if(!method.isAbstract()) {
        CodePoint entrance = method.getEntrance();      
      
        //Add a call to the precondition method at entrance
        String preCall = CodeGenerator.generatePreConditionCall(method);      
        ifile.getFragments().add(new CodeFragment(entrance, preCall, CodeFragmentType.PRECONDITION));

        //Add old Values, only if not constructor, constructors do it differently
        if(!method.isConstructor()) {
          String oldValues = CodeGenerator.generateOldValues(method);      
          ifile.getFragments().add(new CodeFragment(entrance, oldValues, CodeFragmentType.OLDVALUES));
        }
        
        if(!method.isStatic() && !method.isPrivate() && !method.isConstructor()) {
          //Add a call to the invariant method at entrance
          ifile.getFragments().add(new CodeFragment(entrance, invariantCall, CodeFragmentType.INVARIANT));
        }

        //build the code fragments outside the loop for effiency
        //[jpschewe:20000216.0704CST] need to keep track of retVal      
        String postSetup = "final " + method.getReturnType() + " __retVal ="; 
        String postCall = CodeGenerator.generatePostConditionCall(method);      
        Iterator exits = method.getExits().iterator();
        while(exits.hasNext()) {
          CodePointPair exit = (CodePointPair)exits.next();
          if(!method.isStatic() && !method.isPrivate()) {      
            //Add a call to the invariant at each exit
            ifile.getFragments().add(new CodeFragment(exit.getCodePointOne(), invariantCall, CodeFragmentType.INVARIANT));
          }
          //Add a call to the postCondition at each exit
          if(!method.isVoid()) {
            ifile.getFragments().add(new CodeModification(exit.getCodePointOne(), "return", postSetup, CodeFragmentType.POSTCONDITION));
          }

          ifile.getFragments().add(new CodeFragment(exit.getCodePointTwo(), postCall, CodeFragmentType.POSTCONDITION2));
        }
      }
      
      //Add the pre and post check methods at the end of the method
      CodePoint close = method.getClose();
      String preMethod = CodeGenerator.generatePreConditionMethod(method);
      String postMethod = CodeGenerator.generatePostConditionMethod(method);
      ifile.getFragments().add(new CodeFragment(close, preMethod, CodeFragmentType.PRECONDITION));
      ifile.getFragments().add(new CodeFragment(close, postMethod, CodeFragmentType.POSTCONDITION));
    }

  }

  /**
     @return true if the destination for the current file is older than the
     source file, or doesn't exist.  Which means that we should parse this
     file.
  **/
  public boolean isDestinationOlderThanCurrentFile(final String packageName) {
    File destFile = new File(AssertTools.getInstrumentedFilename(getCurrentFile().getFile(), packageName));
    return !destFile.exists() || (destFile.lastModified() < getCurrentFile().getFile().lastModified());
  }
    
  
  
  private AssertClass _currentClass;
  private Hashtable _allPackages;
  /**
     List of Files.
  **/
  private List _allFiles;
  private Stack _classStack;
  private Stack _fileStack;
  private Stack _methodStack;
  private AssertMethod _currentMethod;
  
  /**
     Hashtable of List, each key is a class name, each value is a package
     name.
  **/
  private Hashtable _imports;

  /**
     Code fragment to insert at the top of each file.
  **/
  static private CodeFragment _taglineFragment = new CodeFragment(new CodePoint(1, 0), "/*This file preprocessed with Jon's Assert Package*/", CodeFragmentType.PRECONDITION);
}

