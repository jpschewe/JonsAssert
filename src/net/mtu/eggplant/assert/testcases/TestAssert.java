/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.Assert.test;

import java.util.Vector;
import java.io.*;

/**
   This is a test class for testing my assertions.  Tests all kinds of things.
   
   @invariant (true), "This is an invariant";
**/
public class TestAssert implements Cloneable {

  /**
     @pre (j > 10)
  **/
  public TestAssert(final int j) {
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
