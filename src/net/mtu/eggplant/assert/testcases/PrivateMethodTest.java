/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.Assert.test;

public class PrivateMethodTest {

  public PrivateMethodTest() {
    bar(10);
  }
  
  /**
     @pre (i != 0)
  **/
  private void bar(int i) {
    System.out.println("bar: " + i);
  }

}
