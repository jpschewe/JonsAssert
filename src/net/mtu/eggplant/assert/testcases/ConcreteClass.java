/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.Assert.test;


/**
   tests pre and post conditions on abstract methods, this is the concrete
   class.
**/
public class ConcreteClass extends AbstractClass {

  public boolean preCond(int i) {
    return false;
  }

  public int postCond(int i) {
    return i+5;
  }
  
}
