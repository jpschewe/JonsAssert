/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.assert.test;


/**
   tests implementing methods from an interface and checking hte pre and post
   conditions.
**/
public class InterfaceClass implements Interface {

  public boolean preCond(int i) {
    return false;
  }

  public int postCond(int i) {
    return i+5;
  }
  

}
