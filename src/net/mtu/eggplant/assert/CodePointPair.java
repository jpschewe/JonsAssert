/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.assert;

import org.tcfreenet.schewe.utils.Pair;

final public class CodePointPair extends Pair {

  /**
     @pre (one != null)
     @pre (two != null)
  **/
  public CodePointPair(CodePoint one, CodePoint two) {
    super(one, two);
  }

  public CodePoint getCodePointOne() {
    return (CodePoint)super.getOne();
  }

  public CodePoint getCodePointTwo() {
    return (CodePoint)super.getTwo();
  }

  public String toString() {
    return "CodePointPair one: " + getCodePointOne().toString() + " two: " + getCodePointTwo().toString();
  }
}
