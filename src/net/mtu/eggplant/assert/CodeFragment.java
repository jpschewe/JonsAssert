/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package net.mtu.eggplant.assert;

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
  public CodeFragment(final CodePoint location, final String code, final CodeFragmentType type) {
    _code = code;
    _type = type;
    _location = location;
  }

  /**
     where to insert the code
  **/
  final public CodePoint getLocation() {
    return _location;
  }
  private CodePoint _location;
  
  /**
     what code to insert
  **/
  final public String getCode() {
    return _code;
  }
  private String _code;

  /**
     what type of assertion is this.
  **/
  final public CodeFragmentType getType() {
    return _type;
  }
  private CodeFragmentType _type;

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

  /**
     Instrument the given line.
     
     @param offset how many characters have been added to this line since it was pulled from the file
     @param line the line to modify
     @return the new offset
  **/
  public int instrumentLine(final int offset,
                            final StringBuffer line) {
    int whereToInsert = offset + getLocation().getColumn();
    while(line.length() <= whereToInsert) {
      line.append(' ');
    }
    line.insert(whereToInsert, getCode());
    return offset + getCode().length();
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
        int typeTest = getType().compareTo(other.getType());
        if(typeTest == 0) {
          return getCode().compareTo(other.getCode());
        }
        else {
          return typeTest;
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
