/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.assert;

import org.tcfreenet.schewe.utils.StringPair;
import org.tcfreenet.schewe.utils.Named;
import org.tcfreenet.schewe.utils.Pair;
import org.tcfreenet.schewe.utils.algorithms.Copying;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.LinkedList;

/**
   Object that contains the data needed to generate instrumented code for a
   method.
**/
public class AssertMethod implements Named {

  /**
     @param theClass the class that this method is contained in
     @param name the name of this method, will match the name of the class if a constructor
     @param preConditions the preconditions for this method
     @param postConditions the postconditions for this method
     @param params List of {@link StringPair StringPairs, (class, parameter name)}
     @param retType the return type of this method, null signals this method is a constructor
     @param mods a Set of Strings that are the modifiers for this method
     
     @pre (theClass != null)
     @pre (name != null)
     @pre (preConditions != null && org.tcfreenet.schewe.utils.JPSCollections.elementsInstanceOf(postConditions, AssertToken.class))
     @pre (postConditions != null && org.tcfreenet.schewe.utils.JPSCollections.elementsInstanceOf(preConditions, AssertToken.class))
     @pre (params != null && org.tcfreenet.schewe.utils.JPSCollections.elementsInstanceOf(StringPair.class))
     @pre (mods != null)
  **/
  public AssertMethod(final AssertClass theClass,
                      final String name,
                      final List preConditions,
                      final List postConditions,
                      final List params,
                      final String retType,
                      final Set mods) {
    _name = name;
    _preConditions = preConditions;
    _postConditions = postConditions;
    _theClass = theClass;
    _params = params;
    _uniqueParams = new LinkedList();
    Copying.copy(params, _uniqueParams);
    _retType = retType;
    _mods = mods;
    _exits = new HashSet(10);
    _thrownExceptions = new HashSet(10);
  }

  //Named
  private String _name;

  public String getName() {
    return _name;
  }
  //end Named

  private AssertClass _theClass;

  /**
     @return the class that this method belongs to.
  **/
  final public AssertClass getContainingClass() {
    return _theClass;
  }
    
  /** contains the tokens that define the pre conditions **/  
  private List /*AssertToken*/ _preConditions;

  /**
     @return the preConditions for this method, list of {@link AssertToken AssertTokens}
  **/
  final public List getPreConditions() {
    return _preConditions;
  }

  /** contains the tokens that define the post conditions **/
  private List /*AssertToken*/ _postConditions;

  /**
     @return the postConditions for this method, list of {@link AssertToken AssertTokens}
  **/
  final public List getPostConditions() {
    return _postConditions;
  }
  
  /** x is the line, y is the column **/
  private CodePoint _entrance;

  /**
     Set the entrance to this method.
  **/
  final public void setMethodEntrance(final CodePoint entrance) {
    _entrance = entrance;
  }

  /**
     @return the entrance to this class, ie. the location of the open brace.
     Don't modify this Point.
  **/
  final public CodePoint getEntrance() {
    return _entrance;
  }
  
  /** Set of Points **/
  private Set /*CodePointPair*/ _exits;

  /**
     Add an exit to this method.

     @param points The first point is the start of the return token, the
     second is the location of the semiColon at the end of the return.  If the
     method is void or a constructor, these two locations are equal because
     the first one will be ignored on instrumentation.
     
     @pre (exit != null)
  **/
  final public void addExit(final CodePointPair points) {
    _exits.add(points);
  }
  
  /**
     @return list of the exits of this class, all return statements and the
     closing brace if this is a void method.  Don't modify this Set.
     Set of {@link CodePointPair CodePointPairs(start of return, semicolon)}
  **/
  final public Set getExits() {
    return _exits;
  }
  
  private List /*StringPair*/ _params;

  /**
     @return List of {@link StringPair StringPairs, (class, parameter name)}, don't modify this
     List
  **/
  final public List getParams() {
    return _params;
  }

  /**
     Get the unique parameters for this method.  This is initialized to the
     parameter list passed into the constructor.  If this object represents a
     constructor then this list may be changed in a post processing phase
     before code generation.
   **/
  public List getUniqueParams() {
    return _uniqueParams;
  }
  private List _uniqueParams;
  public void setUniqueParams(final List uniqueParams) {
    _uniqueParams = uniqueParams;
  }
  
  
  private String _retType;
  
  /**
     @return the return type of this method, used for building post checks
  **/
  final public String getReturnType() {
    return _retType;
  }

  /**
     @return true if this method is a constructor, therefore do the special
     processing for the preConditions and don't check the invariant at the top
     of the method, only at the bottom.
  **/
  final public boolean isConstructor() {
    return (getReturnType() == null);
  }

  /**
     @return true if this method is a void method, this includes constructors.
  **/
  final public boolean isVoid() {
    return (getReturnType() == null || getReturnType().equals("void"));
  }

  private CodePoint _close;

  /**
     @return the point at which should be added to be just outside the method, location of '}' + 1
  **/
  final public CodePoint getClose() {
    return _close;
  }

  /**
     Set the point at which should be added to be just outside the method, location of '}' + 1
  **/
  final public void setClose(final CodePoint close) {
    _close = close;
  }

  public String toString() {
    return "[AssertMethod] " + getName();
  }

  
  private Set _thrownExceptions;
  /**
     @pre (thrownExceptions != null)
  **/
  final public void setThrownExceptions(final Set thrownExceptions) {
    _thrownExceptions = thrownExceptions;
  }
  
  final public Set getThrownExceptions() {
    return _thrownExceptions;
  }

  //Mods checks
  private Set _mods;
  /**
     @return the set of modifiers for this method.
  **/
  public Set getMods() {
    return _mods;
  }
  
  /**
     @return true if this method is static, therefore the pre and post checks
     need to be static and the invariant condition isn't checked.
  **/
  final public boolean isStatic() {
    return _mods.contains("static");
  }

  /**
     @return true if this method is private, therefore the invariant condition isn't checked.
  **/
  final public boolean isPrivate() {
    return _mods.contains("private");
  }


  /**
     @return true if this method is abstract or native
  **/
  final public boolean isAbstract() {
    return _mods.contains("abstract") || _mods.contains("native");
  }

  /**
     Look at the mods list and determine the visibility of this method.

     @return a string representing the visibility of this method,
     /&#42;package&#42;/ is returned for package visibility
  **/
  final public String getVisibility() {
    if(_mods.contains("private")) {
      return "private";
    } else if(_mods.contains("protected")) {
      return "protected";
    } else if(_mods.contains("public")) {
      return "public";
    } else {
      //if nothing else is found, must be package
      return "/*package*/";
    }
  }

  /**
     Look at the mods list and determine what the visibility of the method
     that checks assertions should be.  This returns the visibility of the
     method in all cases except for public.  In this case "protected" is
     returned.

     @return a string representing the visibility of this method,
     /&#42;package&#42;/ is returned for package visibility
  **/
  final public String getAssertMethodVisibility() {
    if(isConstructor()) {
      //Constructors are a special case, never called from subclass
      return "private";
    } else if(_mods.contains("private")) {
      return "private";
    } else if(_mods.contains("protected")) {
      return "protected";
    } else if(_mods.contains("public")) {
      return "protected";
    } else {
      //if nothing else is found, must be package
      return "/*package*/";
    }
  }
  
}
