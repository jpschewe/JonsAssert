/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package net.mtu.eggplant.assert.test;

/**
   Tests pre and post conditions on abstract methods, this is the abstract
   class.
**/
abstract public class AbstractClass {

  /**
     @pre (i > 0)
  **/
  abstract public boolean preCond(int i);

  /**
     @post (__retVal < 12)
  **/
  abstract public int postCond(int i);
  
}
