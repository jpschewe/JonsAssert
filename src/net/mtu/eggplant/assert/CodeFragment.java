/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.Assert;

/**
   Class that represents a fragment of code that needs to be inserted into a
   file to instrument it for assertions.
**/
public class CodeFragment implements Comparable {

  /**
     create a new CodeFragment

     @pre (location != null)
     @pre (code != null)
     @pre (type != null)
  **/
  public CodeFragment(final CodePoint location, final String code, final AssertType type) {
    _code = code;
    _type = type;
    _location = location;
  }

  /**
     where to insert the code
  **/
  public CodePoint getLocation() {
    return _location;
  }
  private CodePoint _location;
  
  /**
     what code to insert
  **/
  public String getCode() {
    return _code;
  }
  private String _code;

  /**
     what type of assertion is this.
  **/
  public AssertType getType() {
    return _type;
  }
  private AssertType _type;

  public boolean equals(final Object o) {
    if(o instanceof CodeFragment) {
      return (compareTo(o) == 0);
    }
    else {
      return false;
    }
  }

  public int hashCode() {
    return getLocation().hashCode();
  }

  public String toString() {
    return "Insert " + getType() + " at " + getLocation() + ": " + getCode();
  }
  
  //Comparable
  /**
     Only compares to other CodeFragments.

     @param o the object to compare to
     
     @throws ClassCastException if o is not a CodeFragment
  **/
  public int compareTo(final Object o) throws ClassCastException {
    if(o instanceof CodeFragment) {
      CodeFragment other = (CodeFragment)o;
      int test = getLocation().compareTo(other.getLocation());
      if(test == 0) {
        if(getType().equals(other.getType())) {
          return 0;
        }
        else if(getType().getRank() < other.getType().getRank()) {
          return -1;
        }
        else {
          return 1;
        }
      }
      else {
        return test;
      }
    }
    throw new ClassCastException(o.getClass() + " is not a CodeFragment");
  }
  //end Comparable
}
