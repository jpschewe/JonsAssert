/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.Assert.test;

/**
   tests pre and post conditions in an interface.
**/
public interface Interface {
  
  /**
     @pre (i > 0)
  **/
  public boolean preCond(int i);

  /**
     @post (__retVal < 12)
  **/
  public int postCond(int i);

}
