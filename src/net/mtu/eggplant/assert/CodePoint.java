/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.Assert;

final public class CodePoint implements Comparable {

  /**
     @pre (line > -1)
     @pre (line > -1)
  **/
  public CodePoint(int line, int column) {
    _line = line;
    _column = column;
  }

  public int getLine() {
    return _line;
  }
  private int _line;

  public int getColumn() {
    return _column;
  }
  private int _column;

  public String toString() {
    return getLine() + "." + getColumn();
  }

  public boolean equals(Object o) {
    if(o instanceof CodePoint) {
      return (compareTo(o) == 0);
    }
    else {
      return false;
    }
  }

  public int hashCode() {
    return getLine();
  }
  
  //Comparable
  public int compareTo(Object o) {
    if(o instanceof CodePoint) {
      CodePoint other = (CodePoint)o;
      if(getLine() == other.getLine()) {
        if(getColumn() == other.getColumn()) {
          return 0;
        }
        else if(getColumn() < other.getColumn()) {
          return -1;
        }
        else {
          return 1;
        }
      }
      else if(getLine() < other.getLine()) {
        return -1;
      }
      else {
        return 1;
      }
    }
    throw new ClassCastException(o.getClass() + " is not a CodePoint");
  }
  //end Comparable
}
