/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.assert;

import org.tcfreenet.schewe.utils.Named;

/**
   Represents a type of assertion.
**/
public class CodeFragmentType implements Named, Comparable {


  final static public CodeFragmentType PRECONDITION = new CodeFragmentType("PRECONDITION", 0);
  final static public CodeFragmentType OLDVALUES = new CodeFragmentType("OLDVALUES", 1);
  final static public CodeFragmentType ASSERT = new CodeFragmentType("ASSERT", 2);
  final static public CodeFragmentType INVARIANT = new CodeFragmentType("INVARIANT", 3);
  final static public CodeFragmentType POSTCONDITION = new CodeFragmentType("POSTCONDITION", 4);
  final static public CodeFragmentType POSTCONDITION2 = new CodeFragmentType("POSTCONDITION2", 5);

  /**
     @param name the name of the type
     @param rank the rank for sorting
  **/
  private CodeFragmentType(final String name, final long rank) {
    _name = name;
    _rank = rank;
  }

  private long getRank() {
    return _rank;
  }
  private long _rank;
  
  final public String getName() {
    return _name;
  }
  private String _name;
  
  public String toString() {
    return getName();
  }

  public boolean equals(Object o) {
    if(o instanceof CodeFragmentType) {
      return (getRank() == ((CodeFragmentType)o).getRank());
    }
    return false;
  }

  public int hashCode() {
    return (new Long(getRank())).hashCode();
  }


  //Comparable
  /**
     @throws ClassCastException if other is not an CodeFragmentType
  **/
  public int compareTo(final Object o) {
    if(o instanceof CodeFragmentType) {
      CodeFragmentType other = (CodeFragmentType)o;
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
      throw new ClassCastException(o.getClass() + " is not a CodeFragmentType");
    }
  }
  //end Comparable
}
