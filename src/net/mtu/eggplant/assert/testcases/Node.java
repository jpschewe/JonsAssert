/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package net.mtu.eggplant.assert.test;

import java.io.IOException;
import java.io.File;

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

  /**
     Check one line if and else statements with returns as the one line.
  **/
  public Object method(Object o) {

    if(o == null) return null;
    else return o;
  }

  public static class FindIISResult
  {
    public FindIISResult(int r, int c)
    {
      numrows = r;
      numcols = c;
    }
    public int numrows;
    public int numcols;
  }

  private boolean _exception = false;
  /**
     Used to test exception postconditions.  Postconditions should not be
     checked when exceptions are thrown.
     *
     * stuff
     *
     @post (!_exception)
  **/
  public void exceptionMethod(int i) throws IOException {
    if(i > 0) {
      _exception = true;
      throw new IOException();
    }
  }

  /**
     Test for quotes in the condition, at one point this caused an infinite
     loop when doing the search and replace to escape these quotes.
     
     @pre (!dateStr.equals(""))
  **/
  final public static long stringDateToLong(String dateStr) {
    return 10L;
  }
  
}
