/*
 * Copyright (c) 2000
 *      Jon Schewe.  All rights reserved
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * I'd appreciate comments/suggestions on the code jpschewe@mtu.net
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
