/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.Assert;

import org.tcfreenet.schewe.utils.StringPair;
import org.tcfreenet.schewe.utils.Pair;

import java.io.File;

import java.util.Stack;
import java.util.Hashtable;
import java.util.Vector;
import java.util.SortedSet;
import java.util.TreeSet;

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
      List of invariants (Vector -> AssertToken)
      
    AssertMethod:
      List of preconditions, in order that they appear in the code (Vector -> AssertToken)
      Entrance point (line, column)
      List of exit points (Vector->(line, column))
      List of post conditions, order matters (Vector -> AssertToken)
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
    _allFiles = new Hashtable();
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
  
  private File _currentFile;

  public File getCurrentFile() {
    return _currentFile;
  }

  /**
     Start a file.  Sets this file as the current one and saves the state of
     the current file being processed, if there is one.
     
     @return false if the file has already been seen.
  **/
  public boolean startFile(final File f) {
    if(_currentFile != null) {
      _fileStack.push(new Pair(_currentFile, _imports));
    }
    
    _currentFile = f;
    _imports = new Hashtable();
    if(_allFiles.get(f) != null) {
      return false;
    }
    else {
      _allFiles.put(f, new TreeSet());
      return true;
    }
  }

  /**
     Just resets the internal pointers to files to be the last file we pushed
     on the file stack, or null if no other files are being processed.
  **/
  public void finishFile() {
    if(!_fileStack.isEmpty()) {
      Pair p = (Pair)_classStack.pop();
      _currentFile = (File)p.getOne();
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
  public void startClass(final String name, final Vector invariants) {
    if(_currentClass != null) {
      _classStack.push(_currentClass);
    }
    _currentClass = new AssertClass(name, getCurrentPackageName());
    _currentClass.setInvariants(invariants);
    
    System.out.println("in Symtab.startClass " + _currentClass);
    
    // add to the current package
    Hashtable h = (Hashtable)_allPackages.get(getCurrentPackageName());
    h.put(name, getCurrentClass());

    // associate it with a file too
    //Vector v = (Vector)_allFiles.get(getCurrentFile());
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
     instrument all of the files we've parsed so far.
  **/
  public void instrument() {
    /*
      walk over _allFiles and parse each class writing out to the instrument directory.
    */
  }

  /**
     Resolve an interface.  Given a name of an interface, find the package it
     belongs in.  We're assuming that if it isn't a fully qualified name it's
     in the list of imports or in the current package.  Leave it up to the
     real compiler to check for mistakes.

     @return a StringPair where the first object is the package and the second
     is the interface name.
  **/
  public StringPair resolveInterface(String name) {
    return null;
  }

  /**
     Associate this CodeFragment with the current file begin parsed.

     @param cf fragment of code that needs to be inserted

     @pre (cf != null)
  **/
  public void addCodeFragment(final CodeFragment cf) {
    SortedSet fragments = (SortedSet)_allFiles.get(getCurrentFile());
    if(!fragments.add(cf)) {
      throw new RuntimeException("CodeFragment matches another one already associated with the file: " + cf);
    }
  }
  
  private AssertClass _currentClass;
  private Hashtable _allPackages;
  private Hashtable _allFiles;
  private Stack _classStack;
  private Stack _fileStack;
  
  /**
     Hashtable of Vectors, each key is a class name, each value is a package
     name.
  **/
  private Hashtable _imports;
}

