/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code jpschewe@eggplant.mtu.net
*/
package net.mtu.eggplant.assert.test;

import net.mtu.eggplant.assert.Configuration;
import net.mtu.eggplant.assert.JonsAssert;

import java.io.File;
import java.io.FilenameFilter;

import java.net.URL;

import java.util.Collections;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Test cases for the overall parser.  These tests will not run inside of a
 * jar file.
 */
public class JonsAssertTest extends TestCase {

  public static void main(final String args[]) {
//     TestSuite suite = new TestSuite();
//     suite.addTest(new JonsAssertTest("testUniqueParams2"));
    TestRunner.run(suite());
    
    System.exit(0);
  }
  
  public JonsAssertTest(final String name) {
    super(name);
  }

  public static TestSuite suite() {
    TestSuite suite = new TestSuite(JonsAssertTest.class);
    return suite;
  }

  /**
   * Test parsing the files in testcases.
   **/
  public void testParseTestCases() {
    final Configuration config = new Configuration();
    config.setIgnoreTimeStamp(true);
    final String testcaseDirectory = "testcases/";
    final URL url = JonsAssert.class.getResource(testcaseDirectory);
    final File directory = new File(url.getFile());
    final File[] files = directory.listFiles(new FilenameFilter() {
      public boolean accept(final File dir, final String name) {
        return name.endsWith(".java");
      }
    });

    assertNotNull(files);
    
    for(int i=0; i<files.length; i++) {
      final boolean result = JonsAssert.instrument(config, Collections.singleton(files[i]));
      assert("Parse failed: " + files[i], result);
    }
  }

}
