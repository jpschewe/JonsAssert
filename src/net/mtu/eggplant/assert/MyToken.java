/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.assert;

import antlr.CommonToken;

public class MyToken extends CommonToken {
  public MyToken() {
    super();
  }

  public MyToken(int t, String txt) {
    super(t, txt);
  }
  
  private int _column;
  final public int getColumn() {
    return _column;
  }
  final public void setColumn(int c) {
    _column = c;
  }

  public String toString() {
    return "[\""
      + getText()
      + "\","
      + getType()
      + ",line="
      + getLine()
      + ",column="
      + getColumn()
      + "]";
  }
  
}
