/*
 * Copyright (c) 2000-2002
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
 * Send appreciate comments/suggestions on the code to jpschewe@mtu.net
 */
package net.mtu.eggplant.dbc.test;

import net.mtu.eggplant.dbc.AssertTools;
import net.mtu.eggplant.dbc.AssertionViolation;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import junit.textui.TestRunner;

/**
 * Checks to make sure that the flag ENFORCE_INHERITED_CONDITIONS works
 * properly.  Must be called with ENFORCE_INHERITED_CONDITIONS set to FALSE.
 * Must be called with ASSERT_BEHAVIOR_CONDITIONS set to EXCEPTION.
 * 
 * @version $Revision: 1.3 $
 */
public class TestEnforceInherited extends TestCase {

  static public void main(final String[] args) {
    final TestSuite suite = new TestSuite();
    suite.addTest(suite());
    TestRunner.run(suite);
  }

  public TestEnforceInherited(final String name) {
    super(name);
    if(AssertTools.ENFORCE_INHERITED_CONDITIONS) {
      fail("ENFORCE_INHERITED_CONDITIONS must be set to FALSE");
    }
    if(!"EXCEPTION".equalsIgnoreCase(AssertTools.ASSERT_BEHAVIOR)) {
      fail("ASSERT_BEHAVIOR must be set to EXCEPTION");
    }
  }

  static public TestSuite suite() {
    return new TestSuite(TestEnforceInherited.class);
  }
  
  public void testInheritedConditions() {
    boolean exception = false;
    final AbstractClass ac = new ConcreteClass();

    /**
       @assert (ac != null)
    **/
    try {
      ac.preCond(-5); // should fail
    }
    catch(final AssertionViolation av) {
      exception = true;
    }
    assertTrue("a1:This should not throw an assertion violation", !exception); 
  }
}
