/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.Assert;

import org.tcfreenet.schewe.utils.Named;

/**
   Represents a type of assertion.
**/
public class AssertType implements Named, Comparable {

  final static public AssertType ASSERT = new AssertType("ASSERT", 3);
  final static public AssertType PRECONDITION = new AssertType("PRECONDITION", 0);
  final static public AssertType INVARIANT = new AssertType("INVARIANT", 1);
  final static public AssertType POSTCONDITION = new AssertType("POSTCONDITION", 2);

  /**
     @param name the name of the type
     @param rank the rank for sorting
  **/
  private AssertType(final String name, final long rank) {
    _name = name;
    _rank = rank;
  }

  private long getRank() {
    return _rank;
  }
  private long _rank;
  
  public String getName() {
    return _name;
  }
  private String _name;
  
  public String toString() {
    return getName();
  }

  public boolean equals(Object o) {
    if(o instanceof AssertType) {
      return (getRank() == ((AssertType)o).getRank());
    }
    return false;
  }

  public int hashCode() {
    return (new Long(getRank())).hashCode();
  }


  //Comparable
  /**
     @throws ClassCastException if other is not an AssertType
  **/
  public int compareTo(final Object o) {
    if(o instanceof AssertType) {
      AssertType other = (AssertType)o;
      if(other.equals(this)) {
        return 0;
      }
      else if(getRank() < other.getRank()) {
        return -1;
      }
      else {
        return 1;
      }
    }
    else {
      throw new ClassCastException(o.getClass() + " is not a AssertType");
    }
  }
  //end Comparable
}
