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

import net.mtu.eggplant.util.Named;

/**
   Represents a type of assertion.
**/
public class CodeFragmentType implements Named, Comparable {


  final static public CodeFragmentType PRECONDITION = new CodeFragmentType("PRECONDITION", 0);
  final static public CodeFragmentType OLDVALUES = new CodeFragmentType("OLDVALUES", 1);
  final static public CodeFragmentType ASSERT = new CodeFragmentType("ASSERT", 2);
  final static public CodeFragmentType INVARIANT = new CodeFragmentType("INVARIANT", 3);
  final static public CodeFragmentType POSTCONDITION = new CodeFragmentType("POSTCONDITION", 4);
  final static public CodeFragmentType POSTCONDITION2 = new CodeFragmentType("POSTCONDITION2", 5);

  /**
     @param name the name of the type
     @param rank the rank for sorting
  **/
  private CodeFragmentType(final String name, final long rank) {
    _name = name;
    _rank = rank;
  }

  private long getRank() {
    return _rank;
  }
  private long _rank;
  
  final public String getName() {
    return _name;
  }
  private String _name;
  
  public String toString() {
    return getName();
  }

  public boolean equals(Object o) {
    if(o instanceof CodeFragmentType) {
      return (getRank() == ((CodeFragmentType)o).getRank());
    }
    return false;
  }

  public int hashCode() {
    return (new Long(getRank())).hashCode();
  }


  //Comparable
  /**
     @throws ClassCastException if other is not an CodeFragmentType
  **/
  public int compareTo(final Object o) {
    if(o instanceof CodeFragmentType) {
      CodeFragmentType other = (CodeFragmentType)o;
      if(other.equals(this)) {
        return 0;
      }
      else if(getRank() < other.getRank()) {
        return -1;
      }
      else {
        return 1;
      }
    }
    else {
      throw new ClassCastException(o.getClass() + " is not a CodeFragmentType");
    }
  }
  //end Comparable
}
