/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code jpschewe@eggplant.mtu.net
*/
package net.mtu.eggplant.assert.test.sub;

/**
   This class will get instrumented into test/sub so that the package method
   test is valid.
**/
public class CheckWrongPreconditionsSubClass extends net.mtu.eggplant.assert.test.CheckWrongPreconditionsSuperClass {

  public void testPrivateMethod(final int i) {
    privateMethod(i);
  }

  public void testPackageMethod(final int i) {
    packageMethod(i);
  }
  
  private void privateMethod(final int i) {
    
  }

  /*package*/ void packageMethod(final int i) {
    
  }
}
