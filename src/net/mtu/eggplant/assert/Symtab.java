/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.Assert;

import org.tcfreenet.schewe.utils.StringPair;

import java.io.File;

import java.util.Stack;
import java.util.Hashtable;
import java.util.Vector;

/*
  Need to read the PDF again to make sure I've got these right.
  
  Things to keep track of:
  methods: pre/post
  classes: methods, invariants

  This to keep track of from class to class:
  interfaces: pre/post, methods, invariant

  I figure I can just write a method called __checkPre<methodName>(same signature) and __checkPost<methodName>(same signature) to get pre and post conditions and then just use introspection to call the super method if it exists.  Need to explicitly code all interface pre/post/invariant methods into all classes that implement an interface.




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
    _currentPackageName = "";
    _allPackages = new Hashtable();
    _allFiles = new Hashtable();
  }

  private String _currentPackageName;
  /**
     set the package the the next class(es) belong to
  **/
  public void setCurrentPackageName(String packageName) {
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
     Set the current file
     @return false if the file has already been seen.
  **/
  public boolean setCurrentFile(File f) {
    _currentFile = f;
    _imports = new Hashtable();
    if(_allFiles.get(f) != null) {
      return false;
    }
    else {
      _allFiles.put(f, new Vector());
      return true;
    }
  }
  
  /**
     Push a class onto the stack of classes to handle inner classes.
  **/
  public void startClass(String name) {
    if(_currentClass != null) {
      _classStack.push(_currentClass);
    }
    _currentClass = new AssertClass(name, getCurrentPackageName());

    System.out.println("in Symtab.startClass " + _currentClass);
    
    // add to the current package
    Hashtable h = (Hashtable)_allPackages.get(getCurrentPackageName());
    h.put(name, getCurrentClass());

    // associate it with a file too
    Vector v = (Vector)_allFiles.get(getCurrentFile());
    v.addElement(getCurrentClass());    
  }

  /**
     Take all of the assertion methods that have been cached up and dump them
     out with this class.  This should dump out pre and post methods for each
     method defined as well as a checkInvariants method.
  **/
  public void finishClass() {
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
  
  private AssertClass _currentClass;
  private Hashtable _allPackages;
  private Hashtable _allFiles;
  private Stack _classStack;
  /**
     Hashtable of Vectors, each key is a class name, each value is a package
     name.
  **/
  private Hashtable _imports;
}

