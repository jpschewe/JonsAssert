/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.Assert;

import org.tcfreenet.schewe.utils.StringPair;
import org.tcfreenet.schewe.utils.Named;

import java.util.Vector;

import java.awt.Point;

/**
   Object that contains the data needed to generate instrumented code for a
   method.
**/
public class AssertMethod implements Named {

  /**
     @param theClass the class that this method is contained in
     @param name the name of this method
     @param preConditions the preconditions for this method
     @param postConditions the postconditions for this method
     @param params Vector of StringPairs, (class, parameter name)
     @param retType the return type of this method
     
     @pre (theClass != null)
     @pre (name != null)
     @pre (preConditions != null && org.tcfreenet.schewe.utils.JPSCollections.elementsInstanceOf(postConditions, AssertToken.class))
     @pre (postConditions != null && org.tcfreenet.schewe.utils.JPSCollections.elementsInstanceOf(preConditions, AssertToken.class))
     @pre (params != null && org.tcfreenet.schewe.utils.JPSCollections.elementsInstanceOf(StringPair.class))
     @pre (retType != null)
  **/
  public AssertMethod(final AssertClass theClass,
                      final String name,
                      final Vector preConditions,
                      final Vector postConditions,
                      final Vector params,
                      final String retType) {
    _name = name;
    _preConditions = preConditions;
    _postConditions = postConditions;
    _theClass = theClass;
    _params = params;
    _retType = retType;
    _exits = new Vector();
  }

  //Named
  private String _name;

  public String getName() {
    return _name;
  }
  //end Named

  private AssertClass _theClass;
  
  private AssertClass getContainingClass() {
    return _theClass;
  }
    
  /** contains the tokens that define the pre conditions **/  
  private Vector _preConditions;

  /**
     @return the preConditions for this method, list of AssertTokens
  **/
  public Vector getPreConditions() {
    return _preConditions;
  }

  /** contains the tokens that define the post conditions **/
  private Vector _postConditions;

  /**
     @return the postConditions for this method, list of AssertTokens
  **/
  public Vector getPostConditions() {
    return _postConditions;
  }
  
  /** x is the line, y is the column **/
  private Point _entrance;

  /**
     Set the entrance to this method.
  **/
  public void setMethodEntrance(final Point entrance) {
    _entrance = entrance;
  }

  /**
     @return the entrance to this class, ie. the location of the open brace.
     Don't modify this Point.
  **/
  public Point getEntrance() {
    return _entrance;
  }
  
  /** Vector of Points **/
  private Vector _exits;

  /**
     Add an exit to this method.
  **/
  public void addExit(final Point exit) {
    _exits.addElement(exit);
  }

  /**
     @return list of the exits of this class, all return statements and the
     closing brace if this is a void method.  Don't modify this Vector.
  **/
  public Vector getExits() {
    return _exits;
  }
  
  private Vector _params;

  /**
     @return Vector of StringPairs, (class, parameter name), don't modify this
     Vector
  **/
  public Vector getParams() {
    return _params;
  }

  private String _retType;
  
  /**
     @return the return type of this method, used for building post checks
  **/
  public String getReturnType() {
    return _retType;
  }

}
