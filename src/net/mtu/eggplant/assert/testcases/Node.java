/*
 * Copyright (c) 2000
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
 * I'd appreciate comments/suggestions on the code jpschewe@mtu.net
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

  /**
   * Test assertion with <cr> in the middle.
   *
   * @pre ( 1 != 10
   * )
   */
  final public void foo() {

  }
    
}
