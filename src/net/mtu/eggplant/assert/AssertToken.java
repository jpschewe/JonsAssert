/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package net.mtu.eggplant.assert;

import antlr.Token;

public class AssertToken extends MyToken {

  /**
     @pre (condition != null)
  **/
  public AssertToken(final String condition, final String message, final int type, final String text) {
    super(type, text);
    _condition = condition;
    _message = message;
  }

  /**
     @pre (condition != null)
  **/
  public AssertToken(final String condition, final String message) {
    super();
    _condition = condition;
    _message = message;
  }
  
  private String _message;
  /**
     @return the message for this assertion, this may be null
  **/
  final public String getMessage() {
    return _message;
  }

  private String _condition;
  /**
     @return the condition for this assertion
  **/
  final public String getCondition() {
    return _condition;
  }
  
}
