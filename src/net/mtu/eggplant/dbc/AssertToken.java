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

import antlr.Token;

import net.mtu.eggplant.util.StringUtils;

/**
 * @version $Revision: 1.3 $
 */
public class AssertToken extends Token {

  /**
     @pre (condition != null)
  **/
  public AssertToken(final String condition, final String message, final int type, final String text) {
    super(type, text);
    init(condition, message);
  }

  /**
     @pre (condition != null)
  **/
  public AssertToken(final String condition, final String message) {
    super();
    init(condition, message);
  }

  private void init(final String condition, final String message) {
    _condition = condition;
    _message = message;
    //normalize message and condition
    // handles wrapped comments with possible continuation stars
    if(_condition != null) {
      _condition = StringUtils.searchAndReplace(_condition, "\r\n", "\n");
      _condition = _condition.replace('\r', '\n');
    
      final StringBuffer newCondition = new StringBuffer();
      int prevIndex = 0;
      int crIndex = _condition.indexOf("\n", prevIndex);
      while(crIndex != -1) {
        newCondition.append(_condition.substring(prevIndex, crIndex));
        prevIndex = crIndex + 1;
        boolean watchForStar = true;
        boolean done = false;
        while(!done) {
          if(_condition.charAt(prevIndex) == '*' && watchForStar) {
            watchForStar = false;
            prevIndex++;
          } else if(_condition.charAt(prevIndex) != '\n' && Character.isWhitespace(_condition.charAt(prevIndex))) {
            prevIndex++;
          } else {
            done = true;
          }
        }
        crIndex = _condition.indexOf("\n", prevIndex);
      }
      _condition = newCondition.append(_condition.substring(prevIndex, _condition.length())).toString();
    }
  
    if(_message != null) {
      _message = StringUtils.searchAndReplace(_message, "\r\n", "\n");
      _message = _message.replace('\r', '\n');

      final StringBuffer newMessage = new StringBuffer();
      int prevIndex = 0;
      int crIndex = _message.indexOf("\n", prevIndex);
      while(crIndex != -1) {
        newMessage.append(_message.substring(prevIndex, crIndex));
        prevIndex = crIndex + 1;
        boolean watchForStar = true;
        boolean done = false;
        while(!done) {
          if(_message.charAt(prevIndex) == '*' && watchForStar) {
            watchForStar = false;
            prevIndex++;
          } else if(_message.charAt(prevIndex) != '\n' && Character.isWhitespace(_message.charAt(prevIndex))) {
            prevIndex++;
          } else {
            done = true;
          }
        }
        crIndex = _message.indexOf("\n", prevIndex);
      }
      _message = newMessage.append(_message.substring(prevIndex, _message.length())).toString();
      
    }

    
    
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
