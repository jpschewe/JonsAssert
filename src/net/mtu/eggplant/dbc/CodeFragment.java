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
 * Class that represents a fragment of code that needs to be inserted into a
 * file to instrument it for assertions.
 * 
 * @version $Revision: 1.4 $
 */
public class CodeFragment implements Comparable {

  /**
     create a new CodeFragment

     @pre (location != null)
     @pre (code != null)
     @pre (type != null)
  **/
  public CodeFragment(final CodePoint location, final String code, final CodeFragmentType type) {
    _code = code;
    _type = type;
    _location = location;
  }

  /**
     where to insert the code
  **/
  public final CodePoint getLocation() {
    return _location;
  }
  private CodePoint _location;
  
  /**
     what code to insert
  **/
  public final String getCode() {
    return _code;
  }
  private String _code;

  /**
     what type of assertion is this.
  **/
  public final CodeFragmentType getType() {
    return _type;
  }
  private CodeFragmentType _type;

  public boolean equals(final Object o) {
    if(o instanceof CodeFragment) {
      return (compareTo(o) == 0);
    } else {
      return false;
    }
  }

  public int hashCode() {
    return getLocation().hashCode();
  }

  public String toString() {
    return "Insert " + getType() + " at " + getLocation() + ": " + getCode();
  }

  /**
     Instrument the given line.
     
     @param offset how many characters have been added to this line since it was pulled from the file
     @param line the line to modify
     @return the new offset
  **/
  public int instrumentLine(final int offset,
                            final StringBuffer line) {
    int whereToInsert = offset + getLocation().getColumn();
    while(line.length() <= whereToInsert) {
      line.append(' ');
    }
    line.insert(whereToInsert, getCode());
    return offset + getCode().length();
  }
  
  //Comparable
  /**
     Only compares to other CodeFragments.

     @param o the object to compare to
     
     @throws ClassCastException if o is not a CodeFragment
  **/
  public int compareTo(final Object o) throws ClassCastException {
    if(o instanceof CodeFragment) {
      CodeFragment other = (CodeFragment)o;
      int test = getLocation().compareTo(other.getLocation());
      if(test == 0) {
        int typeTest = getType().compareTo(other.getType());
        if(typeTest == 0) {
          return getCode().compareTo(other.getCode());
        } else {
          return typeTest;
        }
      } else {
        return test;
      }
    }
    throw new ClassCastException(o.getClass() + " is not a CodeFragment");
  }
  //end Comparable
}
