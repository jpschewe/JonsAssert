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
public class TestAssert extends TestCase implements Cloneable {

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
    assert("This should throw an assertion violation", exception); 

    exception = false;
    try {
      TestAssert ta = new TestAssert(12);
    }
    catch(AssertionViolation av) {
      exception=true;
    }
    assert("This should not throw an assertion violation", !exception);
    
  }
  
  /**
     @pre (j > 10)
  **/
  public TestAssert(final int j) {
    super("null");
    System.out.println(j);
  }
  
  public void testPrecondition() {
    preCond(-5); // should fail
    preCond(10); // should pass
  }

  public void testPoscondition() {
    postCond(5); // should pass
    postCond(10); // should fail
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
    AbstractClass ac = new ConcreteClass();

    /**
       @assert (ac != null)
    **/
    ac.preCond(-5); // should fail
    ac.preCond(10); // should pass
    ac.postCond(5); // should pass
    ac.postCond(10); // should fail
    
  }

  public void testInterface() {
    Interface it = new InterfaceClass();
    
    it.preCond(-5); // should fail
    it.preCond(10); // should pass
    it.postCond(5); // should pass
    it.postCond(10); // should fail

  }

  private int foo(int i) {
    return i;
  }
}
