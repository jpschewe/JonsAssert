/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.assert.test;

/**
   tests conditions on a private instance method.
**/
public class PrivateMethodTest {

  public void pass() {
    bar(10);
  }

  public void fail() {
    bar(0);
  }
  
  /**
     @pre (i != 0)
  **/
  private int bar(int i) {
    return i;
  }

}
