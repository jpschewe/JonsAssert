/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.assert.test;

/**
   This tests the instanceof rule in the parser as well as an empty compound
   statement and end of javadoc comment.
*/
public class Node {
  public void insert(Object child, int index) {
    if(!(child instanceof Node)) {
      throw new IllegalArgumentException("Tree must contain only Nodes!");
    }
    else {
      /** @assert (true) **/
    }
  }
}
