/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.Assert;

import java.util.Stack;
import java.util.Hashtable;

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
**/
public class Symtab {

  public Symtab() {
    _classStack = new Stack();
    _allClasses = new Hashtable();
    _currentClass = null;
  }

  private Stack _classStack;
  private Hashtable _allClasses;
  private AssertClass _currentClass;
  
  /**
     Push a class onto the stack of classes to handle inner classes.
  **/
  public void startClass() {
    if(_classStack != null) {
      _classStack.push(_currentClass);
    }
    _currentClass = new AssertClass();
  }

  /**
     Take all of the assertion methods that have been cached up and dump them
     out with this class.  This should dump out pre and post methods for each
     method defined as well as a checkInvariants method.
  **/
  public void finishClass() {
    // note what methods need to be dumped at this line
    _allClasses.put(_currentClass.getFullName(), _currentClass);
    
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
  
}

/*
protected void __checkInvariant() throws AssertionViolationException {
  Class thisClass = this.getClass();
  Class superClass = thisClass.getSuperclass();
  Method superMethod = null;
  if(superClass != null) {
    try {
      Class[] classArgs = new Class[0];
      superMethod = superClass.getDeclaredMethod("__checkInvariant", classArgs);
    }
    catch(NoSuchMethodException nsme) {
      // no method, don't call it
      superMethod = null;
    }
    catch(SecurityException se) {
      //This is real bad, spit out internal error here
    }
  
    if(superMethod != null) {
      //invoke it, pass on exceptions
      Object[] args = new Object[0];
      try {
        superMethod.invoke(this, args);
      }
      catch(IllegalAccessException iae) {
        //bad, internal error, not enough access
      }
      catch(IllegalArgumentException iae) {
        //should never see, internal error
      }
      catch(InvocationTargetException ite) {
        throw ite.getTargetException(); // should be the AssertionViolationException
      }
    }
  }

  // add users conditions here and throw an exception on the first failure
  //for <condition> (conditions)
  if(<condition>) {
    throw new AssertionViolationException("Failed invariant\n" + (<message> != null ? <message> : "") + "\n" + <condition>);
  }
  
}
*/
