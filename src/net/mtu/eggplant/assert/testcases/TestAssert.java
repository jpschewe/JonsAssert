/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.Assert.test;

import org.tcfreenet.schewe.Assert.AssertionViolation;

import java.util.Vector;
import java.io.*;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
   This is a test class for testing my assertions.  Tests all kinds of things.
   TODO:
   Add junit tests.
   AnonomousClass
   NamedInnerClass
   NamedLocalClass
   PrivateMethodTest
   StaticMethod
   
   
   @invariant (_invariant), "This is an invariant";
**/
public class TestAssert extends TestCase {

  private boolean _invariant = true;
  
  static public void main(final String[] args) {
    System.setProperty("ASSERT_BEHAVIOR", "EXCEPTION");

    TestSuite suite = new TestSuite();
    suite.addTest(suite());
    TestRunner.run(suite);
    
  }

  public TestAssert(String name) {
    super(name);
  }

  static public TestSuite suite() {
    return new TestSuite(TestAssert.class);
  }

  public void testConstructorPreCondition() {
    boolean exception = false;
    try {
      TestAssert ta = new TestAssert(9);
    }
    catch(AssertionViolation av) {
      exception = true;
    }
    assert("a1:This should throw an assertion violation", exception); 

    exception = false;
    try {
      TestAssert ta = new TestAssert(12);
    }
    catch(AssertionViolation av) {
      exception=true;
    }
    assert("a2:This should not throw an assertion violation", !exception);
    
  }
  
  /**
     @pre (j > 10)
  **/
  public TestAssert(final int j) {
    super("null");
    System.out.println(j);
  }
  
  public void testPrecondition() {
    boolean exception = false;
    try {
      preCond(-5); // should fail
    }
    catch(AssertionViolation av) {
      exception = true;
    }
    assert("a1:This should throw an assertion violation", exception);

    exception = false;
    try {
      preCond(10); // should pass
    }
    catch(AssertionViolation av) {
      exception = true;
    }
    assert("a2:This not should throw an assertion violation", !exception);
  }

  public void testPostcondition() {
    boolean exception = false;
    try {
      postCond(4); // should pass
    }
    catch(AssertionViolation av) {
      exception = true;
    }
    assert("a3:This should not throw an assertion violation", !exception); 

    exception = false;
    try {
      postCond(10); // should fail
    }
    catch(AssertionViolation av) {
      exception = true;
    }
    assert("a4:This should throw an assertion violation", exception); 
      
  }

  /**
     @pre (foo(i) > 0)
  **/
  public boolean preCond(int i) {
    return false;
  }

  /**
     @post (__retVal < 10), "Post condition";
  **/
  public int postCond(int i) {
    return i+5;
  }

  public void testAbstractMethod() {
    boolean exception = false;
    AbstractClass ac = new ConcreteClass();

    /**
       @assert (ac != null)
    **/
    try {
      ac.preCond(-5); // should fail
    }
    catch(AssertionViolation av) {
      exception = true;
    }
    assert("a1:This should throw an assertion violation", exception); 

    exception = false;
    try {
      ac.preCond(10); // should pass
    }
    catch(AssertionViolation av) {
      exception = true;
    }
    assert("a2:This should not throw an assertion violation", !exception); 

    exception = false;
    try {
      ac.postCond(5); // should pass
    }
    catch(AssertionViolation av) {
      exception = true;
    }
    assert("a3:This should not throw an assertion violation", !exception); 

    exception = false;
    try {
      ac.postCond(10); // should fail
    }
    catch(AssertionViolation av) {
      exception = true;
    }
    assert("a4:This should throw an assertion violation", exception); 
    
    
  }

  public void testInterface() {
    boolean exception = false;
    Interface it = new InterfaceClass();

    try {
      it.preCond(-5); // should fail
    }
    catch(AssertionViolation av) {
      exception = true;
    }
    assert("a1:This should throw an assertion violation", exception); 

    exception = false;
    try {
      it.preCond(10); // should pass
    }
    catch(AssertionViolation av) {
      exception = true;
    }
    assert("a2:This should not throw an assertion violation", !exception); 

    exception = false;
    try {
      it.postCond(5); // should pass
    }
    catch(AssertionViolation av) {
      exception = true;
    }
    assert("a3:This should not throw an assertion violation", !exception); 

    exception = false;
    try {
      it.postCond(10); // should fail
    }
    catch(AssertionViolation av) {
      exception = true;
    }
    assert("a4:This should throw an assertion violation", exception); 
    

  }

  private int foo(int i) {
    return i;
  }
}
