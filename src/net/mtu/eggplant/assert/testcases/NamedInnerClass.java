/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.assert.test;

/**
   checks conditions on a method in a named inner class
**/
public class NamedInnerClass {

  public void pass() {
    Bar b = new Bar();
    b.doit(10);
  }

  public void fail() {
    Bar b = new Bar();
    b.doit(0);
  }
  
  public class Bar {

    public Bar() {
      System.out.println("In constructor for inner class Bar");
    }
    
    /**
       @pre (i > 0)
    **/
    public int doit(int i) {
      return i;
    }
    
  }
  
}
