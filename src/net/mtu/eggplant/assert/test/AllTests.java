/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code jpschewe@eggplant.mtu.net
*/
package org.tcfreenet.schewe.assert.test;

import junit.framework.TestSuite;
import junit.framework.Test;
import junit.textui.TestRunner;

public class AllTests {

  public static void main(String[] args) {
    TestRunner.run(suite());
    System.exit(0);
  }


  /**
     Runs all of the unit tests for the project.  As tests are added for a top
     level package, add a refernece to them here.
  **/
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(org.tcfreenet.schewe.assert.test.AssertToolsTest.suite());
    return suite;
  }
  

}
