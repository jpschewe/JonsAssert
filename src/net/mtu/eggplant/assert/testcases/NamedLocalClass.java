/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.assert.test;

/**
   tests conditions in a named local class.
**/
public class NamedLocalClass {

  public void pass() {
    Bar2 b = new Bar2();
    b.doit(10);
  }

  public void fail() {
    Bar2 b = new Bar2();
    b.doit(-2);
  }
  
}

public class Bar2 {
  /**
     @pre (i > 0)
  **/
  public int doit(int i) {
    return i;
  }
    
}

