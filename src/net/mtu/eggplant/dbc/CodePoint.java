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
package net.mtu.eggplant.dbc;

/**
 * @version $Revision: 1.4 $
 */
public final class CodePoint implements Comparable {

  /**
     @pre (line > -1)
     @pre (column > 0)
  **/
  public CodePoint(final int line,
                   final int column) {
    _line = line;
    _column = column;
  }

  public int getLine() {
    return _line;
  }
  private int _line;

  public int getColumn() {
    return _column;
  }
  private int _column;

  public String toString() {
    return getLine() + ":" + getColumn();
  }

  public boolean equals(final Object o) {
    if(o instanceof CodePoint) {
      return (compareTo(o) == 0);
    } else {
      return false;
    }
  }

  public int hashCode() {
    return getLine();
  }
  
  //Comparable
  public int compareTo(final Object o) {
    if(o instanceof CodePoint) {
      CodePoint other = (CodePoint)o;
      if(getLine() == other.getLine()) {
        if(getColumn() == other.getColumn()) {
          return 0;
        } else if(getColumn() < other.getColumn()) {
          return -1;
        } else {
          return 1;
        }
      } else if(getLine() < other.getLine()) {
        return -1;
      } else {
        return 1;
      }
    }
    throw new ClassCastException(o.getClass() + " is not a CodePoint");
  }
  //end Comparable
}
