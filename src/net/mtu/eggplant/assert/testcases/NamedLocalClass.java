/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.Assert.test;

/**
   tests conditions in a named local class.
**/
public class NamedLocalClass {

  public void foo() {
    Bar2 b = new Bar2();
    b.doit(10);
  }
}

public class Bar2 {
  /**
     @pre (i > 0)
  **/
  public void doit(int i) {
    System.out.println("in doit " + i);
  }
    
}

