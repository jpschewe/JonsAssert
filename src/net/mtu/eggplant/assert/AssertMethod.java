/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.Assert;

import org.tcfreenet.schewe.utils.StringPair;
import org.tcfreenet.schewe.utils.Named;
import org.tcfreenet.schewe.utils.Pair;

import java.util.Vector;
import java.util.List;

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
     @param isStatic true if this method is static
     @param isPrivate true if this method is private
     @param isAbstract true if this method is abstract or native
     
     @pre (theClass != null)
     @pre (name != null)
     @pre (preConditions != null && org.tcfreenet.schewe.utils.JPSCollections.elementsInstanceOf(postConditions, AssertToken.class))
     @pre (postConditions != null && org.tcfreenet.schewe.utils.JPSCollections.elementsInstanceOf(preConditions, AssertToken.class))
     @pre (params != null && org.tcfreenet.schewe.utils.JPSCollections.elementsInstanceOf(StringPair.class))
  **/
  public AssertMethod(final AssertClass theClass,
                      final String name,
                      final List preConditions,
                      final List postConditions,
                      final List params,
                      final String retType,
                      final boolean isStatic,
                      final boolean isPrivate,
                      final boolean isAbstract) {
    _name = name;
    _preConditions = preConditions;
    _postConditions = postConditions;
    _theClass = theClass;
    _params = params;
    _retType = retType;
    _static = isStatic;
    _private = isPrivate;
    _abstract = isAbstract;
    _exits = new Vector();
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
  public AssertClass getContainingClass() {
    return _theClass;
  }
    
  /** contains the tokens that define the pre conditions **/  
  private List /*AssertToken*/ _preConditions;

  /**
     @return the preConditions for this method, list of {@link AssertToken AssertTokens}
  **/
  public List getPreConditions() {
    return _preConditions;
  }

  /** contains the tokens that define the post conditions **/
  private List /*AssertToken*/ _postConditions;

  /**
     @return the postConditions for this method, list of {@link AssertToken AssertTokens}
  **/
  public List getPostConditions() {
    return _postConditions;
  }
  
  /** x is the line, y is the column **/
  private CodePoint _entrance;

  /**
     Set the entrance to this method.
  **/
  public void setMethodEntrance(final CodePoint entrance) {
    _entrance = entrance;
  }

  /**
     @return the entrance to this class, ie. the location of the open brace.
     Don't modify this Point.
  **/
  public CodePoint getEntrance() {
    return _entrance;
  }
  
  /** List of Points **/
  private List /*CodePointPair*/ _exits;

  /**
     Add an exit to this method.

     @param points The first point is the start of the return token, the
     second is the location of the semiColon at the end of the return.  If the
     method is void or a constructor, these two locations are equal because
     the first one will be ignored on instrumentation.
     
     @pre (exit != null)
  **/
  public void addExit(final CodePointPair points) {
    _exits.add(points);
  }
  
  /**
     @return list of the exits of this class, all return statements and the
     closing brace if this is a void method.  Don't modify this List.
     List of {@link CodePointPair CodePointPairs(start of return, semicolon)}
  **/
  public List getExits() {
    return _exits;
  }
  
  private List /*StringPair*/ _params;

  /**
     @return List of {@link StringPair StringPairs, (class, parameter name)}, don't modify this
     List
  **/
  public List getParams() {
    return _params;
  }

  private String _retType;
  
  /**
     @return the return type of this method, used for building post checks
  **/
  public String getReturnType() {
    return _retType;
  }

  /**
     @return true if this method is static, therefore the pre and post checks
     need to be static and the invariant condition isn't checked.
  **/
  public boolean isStatic() {
    return _static;
  }
  private boolean _static;

  /**
     @return true if this method is private, therefore the invariant condition isn't checked.
  **/
  public boolean isPrivate() {
    return _private;
  }
  private boolean _private;

  /**
     @return true if this method is a constructor, therefore do the special
     processing for the preConditions and don't check the invariant at the top
     of the method, only at the bottom.
  **/
  public boolean isConstructor() {
    return (getReturnType() == null);
  }

  /**
     @return true if this method is a void method, this includes constructors.
  **/
  public boolean isVoid() {
    return (getReturnType() == null || getReturnType().equals("void"));
  }

  private CodePoint _close;

  /**
     @return the point at which should be added to be just outside the method, location of '}' + 1
  **/
  public CodePoint getClose() {
    return _close;
  }

  /**
     Set the point at which should be added to be just outside the method, location of '}' + 1
  **/
  public void setClose(final CodePoint close) {
    _close = close;
  }

  public String toString() {
    return "[AssertMethod] " + getName();
  }

  private boolean _abstract;
  
  /**
     @return true if this method is abstract or native
  **/
  public boolean isAbstract() {
    return _abstract;
  }
}