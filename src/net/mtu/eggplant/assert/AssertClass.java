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

import net.mtu.eggplant.util.Named;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.List;

/**
   Object to represent an instrumented class.
**/
public class AssertClass implements Named {

  /**
     If packageName is null, then it's in the default package.  If name is
     null, it's an anonomous class.

     @param name the simple name of this class
     @param packageName the name of hte package this class is in
     @param isInterface true if this class represents an interface
     @param enclosingClass the enclosing class, this can not be another
     anonymous class, null if this is the outermost class
     @param isAnonymous true if this class represents an anonymous class
     @param superclass name of the superclass, null if none listed in the .java file
     @param interfaces list of interfaces implemented, this is where the list
     of extended interfaces appear if this object represents an interface itself.
     These names may not be fully qualified.  They should be checked against the
     imports list.
     @param imports map of imports for this class key = package, value = class/null
     @param starImports Set of star imports
     
     @pre (interfaces != null)
     @pre (imports != null)
     @pre (enclosingClass == null || !enclosingClass.isAnonymous)
  **/
  public AssertClass(final String name,
                     final String packageName,
                     final boolean isInterface,
                     final AssertClass enclosingClass,
                     final boolean isAnonymous,
                     final String superclass,
                     final List interfaces,
                     final Map imports,
                     final Set starImports) {
    _name = name;
    _packageName = packageName;
    _isInterface = isInterface;
    _anonymousClassCounter = 1;
    _constructorCounter = 0;
    _methods = new HashSet(20);
    _isAnonymous = isAnonymous;
    _enclosingClass = enclosingClass;
    _superclass = superclass;
    _interfaces = interfaces;
    _imports = imports;
  }

  private List _interfaces;
  /**
     The List of interfaces implemented/extended by this class/interface, in
     the order that they are declared on the implements line.
  **/
  final public List getInterfaces() {
    return _interfaces;
  }

  private Map _imports;
  /**
    The Map of imports from the file this class is defined in.  key =
    package, value = class.
  **/
  final public Map getImports() {
    return _imports;
  }

  private Set _starImports;
  /**
     The Set of star imports from the file this class is defined in.
  **/
  final public Set getStarImports() {
    return _starImports;
  }
  
  private String _superclass;
  final public String getSuperclass() {
    return _superclass;
  }
  
  private AssertClass _enclosingClass;
  final public AssertClass getEnclosingClass() {
    return _enclosingClass;
  }

  private boolean _isAnonymous;
  final public boolean isAnonymous() {
    return _isAnonymous;
  }
  
  private boolean _isInterface;
  final public boolean isInterface() {
    return _isInterface;
  }
  
  //Named
  private String _name;

  final public String getName() {
    return _name;
  }
  //end Named
  
  /**
     @return the fully qualified name of this class object
  **/
  final public String getFullName() {
    if(getPackage() == null || getPackage().equals("")) {
      return getName();
    } else {
      return getPackage() + "." + getName();
    }
  }

//   final public void setPackage(final String packageName) {
//     _packageName = packageName;
//   }

  final public String getPackage() {
    return _packageName;
  }

  private String _packageName;
  
  /**
     Adds this method to the list of methods defined on this class.
  **/
  final public void addMethod(final AssertMethod am) {
    /*
      see if this method exists in any of the implemented interfaces, if so
      add their pre and post conditions to the current list of pre and post
      conditions.  */
    _methods.add(am);
  }

  private Set _methods;
  
  final public Set getMethods() {
    return _methods;
  }
  
  public String toString() {
    return "[AssertClass] " + getFullName();
  }

  private List /*Token*/ _invariants;
  
  /**
     List of invariants for this class, ordered as they appear in the code.
     
     @pre (invariants != null)
  **/
  final public void setInvariants(final List invariants) {
    _invariants = invariants;
  }

  /**
     List of invariants for this class, ordered as they appear in the code.
  **/
  final public List getInvariants() {
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
  final public String createAnonymousClassName() {
    String name = getName();
    name += "$" + _anonymousClassCounter++;
    return name;
  }

  private long _constructorCounter;

  /**
     Create the name for a dummy class for the next constructors pre
     conditions.
  **/
  final public String createDummyConstructorClassName() {
    return "_AssertDummy" + _constructorCounter++;
  }

}
