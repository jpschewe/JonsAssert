/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.Assert;

import antlr.Token;

public class InvariantCondition {
  public InvariantCondition(Token condition, Token message) {
    _condition = condition;
    _message = message;
  }

  private Token _condition;
  public Token getCondition() {
    return _condition;
  }

  private Token _message;
  public Token getMessage() {
    return _message;
  }

  public String toString() {
    return "Condition: " + _condition + " Message: " + _message + ":";
  }
}
