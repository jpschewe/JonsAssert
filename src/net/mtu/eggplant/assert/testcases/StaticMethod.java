/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.Assert.test;

public class StaticMethod {

  static public void fail() {
    foo(10);
  }

  static public void pass() {
    foo(-10);
  }

  /**
     @pre (i > 0)
  **/
  static public int foo(int i) {
    return i;
  }
  
}
