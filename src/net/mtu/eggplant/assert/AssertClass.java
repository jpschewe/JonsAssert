/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.Assert;

import org.tcfreenet.schewe.utils.Named;

import java.util.Vector;
import java.util.List;

/**
   Object to represent an instrumented class.
**/
public class AssertClass implements Named {

  /**
     If packageName is null, then it's in the default package.  If name is
     null, it's an anonomous class.
     
  **/
  public AssertClass(final String name,
                     final String packageName,
                     final boolean isInterface) {
    _name = name;
    _packageName = packageName;
    _isInterface = isInterface;
    _anonymousClassCounter = 1;
    _constructorCounter = 0;
    _methods = new Vector();
  }

  private boolean _isInterface;
  public boolean isInterface() {
    return _isInterface;
  }
  
  //Named
  private String _name;

  public String getName() {
    return _name;
  }
  //end Named
  
  /**
     @return the fully qualified name of this class object
  **/
  public String getFullName() {
    return getPackage() + "." + getName();
  }

  public void setPackage(final String packageName) {
    _packageName = packageName;
  }

  public String getPackage() {
    return _packageName;
  }

  private String _packageName;
  
  /**
     Adds this method to the list of methods defined on this class.
  **/
  public void addMethod(final AssertMethod am) {
    /*
      see if this method exists in any of the implemented interfaces, if so
      add their pre and post conditions to the current list of pre and post
      conditions.  */
    _methods.add(am);
    System.out.println("just added: " + am + " methods: " + _methods);
  }

  private List _methods;
  
  public List getMethods() {
    return _methods;
  }
  
  public String toString() {
    return "[AssertClass] " + getFullName();
  }

  private List /*Token*/ _invariants;
  
  /**
     @pre (invariants != null)
  **/
  public void setInvariants(final List invariants) {
    _invariants = invariants;
  }

  public List getInvariants() {
    return _invariants;
  }

  /**
     counter to keep track of number fo anonymous classes for naming.
  **/
  private long _anonymousClassCounter;

  /**
     Create the name for the next anonymous class that is defined
     inside this class.
  **/
  public String createAnonymousClassName() {
    String name = getName();
    name += "$" + _anonymousClassCounter++;
    return name;
  }

  private long _constructorCounter;

  /**
     Create the name for a dummy class for the next constructors pre
     conditions.
  **/
  public String createDummyConstructorClassName() {
    String prefix = getPackage().replace('.', '_');
    return prefix + "_AssertDummy" + _constructorCounter++;
  }
    
}
