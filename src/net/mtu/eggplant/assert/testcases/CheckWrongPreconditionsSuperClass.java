/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code jpschewe@eggplant.mtu.net
*/
package net.mtu.eggplant.assert.test;

public class CheckWrongPreconditionsSuperClass {

  /**
     @pre(i > 10)
  **/
  private void privateMethod(final int i) {

  }

  /**
     @pre(i > 10)
  **/
  /*package*/ void packageMethod(final int i) {

  }
  
}
