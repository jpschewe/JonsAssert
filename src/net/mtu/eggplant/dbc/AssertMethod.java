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
package net.mtu.eggplant.dbc;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.mtu.eggplant.util.Named;

/**
 * Object that contains the data needed to generate instrumented code for a
 * method.
 * 
 * @version $Revision: 1.7 $
 */
public class AssertMethod implements Named {

  /**
   * @param theClass the class that this method is contained in
   * @param name the name of this method, will match the name of the class if
   * a constructor
   * @param preConditions the preconditions for this method
   * @param postConditions the postconditions for this method
   * @param params List of {@link net.mtu.eggplant.util.StringPair
   * StringPairs} (class, parameter name)
   * @param retType the return type of this method, null signals this method is a constructor
   * @param mods a Set of Strings that are the modifiers for this method
   *  
   * @pre (theClass != null)
   * @pre (name != null)
   * @pre (preConditions != null && net.mtu.eggplant.util.JPSCollections.elementsInstanceOf(postConditions, AssertToken.class))
   * @pre (postConditions != null && net.mtu.eggplant.util.JPSCollections.elementsInstanceOf(preConditions, AssertToken.class))
   * @pre (params != null && net.mtu.eggplant.util.JPSCollections.elementsInstanceOf(StringPair.class))
   * @pre (mods != null)
   */
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
    _uniqueParams.addAll(params);
    _retType = retType;
    _mods = mods;
    _exits = new HashSet(10);
    _thrownExceptions = new LinkedHashSet(10);
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
  public final AssertClass getContainingClass() {
    return _theClass;
  }
    
  /** contains the tokens that define the pre conditions **/  
  private List /*AssertToken*/ _preConditions;

  /**
     @return the preConditions for this method, list of {@link AssertToken AssertTokens}
  **/
  public final List getPreConditions() {
    return _preConditions;
  }

  /** contains the tokens that define the post conditions **/
  private List /*AssertToken*/ _postConditions;

  /**
     @return the postConditions for this method, list of {@link AssertToken AssertTokens}
  **/
  public final List getPostConditions() {
    return _postConditions;
  }
  
  /** x is the line, y is the column **/
  private CodePoint _entrance;

  /**
     Set the entrance to this method.
  **/
  public final void setMethodEntrance(final CodePoint entrance) {
    _entrance = entrance;
  }

  /**
     @return the entrance to this class, ie. the location of the open brace.
     Don't modify this Point.
  **/
  public final CodePoint getEntrance() {
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
  public final void addExit(final CodePointPair points) {
    _exits.add(points);
  }
  
  /**
     @return list of the exits of this class, all return statements and the
     closing brace if this is a void method.  Don't modify this Set.
     Set of {@link CodePointPair CodePointPairs(start of return, semicolon)}
  **/
  public final Set getExits() {
    return _exits;
  }
  
  private List /*StringPair*/ _params;

  /**
   * @return List of {@link net.mtu.eggplant.util.StringPair StringPairs}
   * (class, parameter name), don't modify this List
   */
  public final List getParams() {
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
  public final String getReturnType() {
    return _retType;
  }

  /**
     @return true if this method is a constructor, therefore do the special
     processing for the preConditions and don't check the invariant at the top
     of the method, only at the bottom.
  **/
  public final boolean isConstructor() {
    return (getReturnType() == null);
  }

  /**
     @return true if this method is a void method, this includes constructors.
  **/
  public final boolean isVoid() {
    return (getReturnType() == null || getReturnType().equals("void"));
  }

  private CodePoint _close;

  /**
     @return the point at which should be added to be just outside the method, location of '}' + 1
  **/
  public final CodePoint getClose() {
    return _close;
  }

  /**
     Set the point at which should be added to be just outside the method, location of '}' + 1
  **/
  public final void setClose(final CodePoint close) {
    _close = close;
  }

  public String toString() {
    return "[AssertMethod " + getName() + " params: " + getParams() + "]";
  }

  
  private Set _thrownExceptions;
  /**
     @pre (thrownExceptions != null)
  **/
  public final void setThrownExceptions(final Set thrownExceptions) {
    _thrownExceptions = thrownExceptions;
  }
  
  public final Set getThrownExceptions() {
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
  public final boolean isStatic() {
    return _mods.contains("static");
  }

  /**
     @return true if this method is private, therefore the invariant condition isn't checked.
  **/
  public final boolean isPrivate() {
    return _mods.contains("private");
  }


  /**
     @return true if this method is abstract or native
  **/
  public final boolean isAbstract() {
    return _mods.contains("abstract") || _mods.contains("native");
  }

  /**
     Look at the mods list and determine the visibility of this method.

     @return a string representing the visibility of this method,
     /&#42;package&#42;/ is returned for package visibility
  **/
  public final String getVisibility() {
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
  public final String getAssertMethodVisibility() {
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
