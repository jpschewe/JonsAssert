header {
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
package net.mtu.eggplant.assert;

import net.mtu.eggplant.util.Debug;
  
}

//----------------------------------------------------------------------------
// The Assert scanner
//----------------------------------------------------------------------------
// only things between /** and */ get passed to this
/*
need to recognize
@invariant
@pre
@post
@assert (cond)(,"message")?(;)?

general form as regexp

@(invariant|pre|post|assert) (\(.*\)) (,".*";)?
*/
class AssertLexer extends Lexer;

options {
    importVocab=Java;      // import vocabulary "Java"
    testLiterals=false;    // don't automatically test for literals
    k=4;                   // four characters of lookahead
    filter=true; //ignore things we can't match
    defaultErrorHandler = false;     // generate parser error handlers
}

//{
//  // for column tracking
//  public void setColumnTracker(final ColumnTracker ct) {
//    _ct = ct;
//  }
//
//  private ColumnTracker _ct;
//  
//  public void consume() throws IOException {
//    if(text.length()==0) {
//      // remember the token start column
//      _ct._tokenColumn = _ct._column;
//    }
//    _ct._column++;
//    super.consume();
//  }
//
//  public void newline() {
//    _ct._column = 0;
//    _ct._line++;
//    super.newline();
//  }
//  
//  protected Token makeToken(int t) {
//    Token tok = super.makeToken(t);
//    tok.setColumn(_ct._tokenColumn);
//    tok.setLine(_ct._line);
//    return tok;
//  }
//}

// a dummy rule to force vocabulary to be all characters (except special
//   ones that ANTLR uses internally (0 to 2)
protected
VOCAB
  :     '\3'..'\377'
  ;

NEWLINE
    : (
        '\r' '\n'
        | '\n'
        | '\r'
        )
    {
      newline();
      $setType(Token.SKIP);
    }
    ;

protected
CONDITION
{ int count = 0; }
  : '('
    (
      options {
                generateAmbigWarnings=false;
      }
    :
      { count > 0 }? ')' { count--; }
    | '(' { count++; }
    | '\r' '\n' { newline(); }
    | '\n' { newline(); }
    | '\r' { newline(); }
    | ~('('|')'|'\n'|'\r')
    )+
    ')'
    //    : '(' ( ~('('|')') )* ')'
    //    | '(' CONDITION ')'
    //    : '(' ( ~('('|')') )* (CONDITION)? ( ~('('|')') )* ')'
        { Debug.println("Assert: got CONDITION #" + text + "#"); }
  ;

protected
COMMA
  : ','
  ;

protected
SEMI
  : ';'
  ;

protected
STRING_LITERAL 
    :  '"' (ESC|~('"'|'\\'))* '"'
        { Debug.println("Assert: got MESSAGE #" + text + "#"); }
    ;

protected
SPACE
  : (' '|'\t'|'\f')
  ;

POST_CONDITION
{
  String c = null;
  String m = null;
}
    :   "@post" (SPACE)* cond:CONDITION { c = cond.getText(); } (COMMA (SPACE)* mesg:STRING_LITERAL { m = mesg.getText(); } SEMI)?
    {
      if(c != null) {
//        c = c.replace('\n', ' ');
//        c = c.replace('\r', ' ');
        AssertToken assertTok = new AssertToken(c, m, _ttype, $getText);
        $setToken(assertTok);
      } else {
        $setType(Token.SKIP);
      }
    }
    ;

PRE_CONDITION
{
  String c = null;
  String m = null;
}
  :     "@pre" (SPACE)* cond:CONDITION { c = cond.getText(); } (COMMA (SPACE)* mesg:STRING_LITERAL { m = mesg.getText(); } SEMI)?
    {
      if(c != null) {
//        c = c.replace('\n', ' ');
//        c = c.replace('\r', ' ');
        final AssertToken assertTok = new AssertToken(c, m, _ttype, $getText);
        $setToken(assertTok);
      } else {
        $setType(Token.SKIP);
      }
    }
  ;

ASSERT_CONDITION
{
  String c = null;
  String m = null;
}
    :   "@assert" (SPACE)* cond:CONDITION { c = cond.getText(); } (COMMA (SPACE)* mesg:STRING_LITERAL { m = mesg.getText(); } SEMI)?
    {
      if(c != null) {
//        c = c.replace('\n', ' ');
//        c = c.replace('\r', ' ');
        AssertToken assertTok = new AssertToken(c, m, _ttype, $getText);
        $setToken(assertTok);
      } else {
        $setType(Token.SKIP);
      }
    }
    ;

INVARIANT_CONDITION
{
  String c = null;
  String m = null;
}
    :   "@invariant" (SPACE)* cond:CONDITION { c = cond.getText(); } (COMMA (SPACE)* mesg:STRING_LITERAL { m = mesg.getText(); } SEMI)?
    {
      if(c != null) {
//        c = c.replace('\n', ' ');
//        c = c.replace('\r', ' ');
        AssertToken assertTok = new AssertToken(c, m, _ttype, $getText);
        $setToken(assertTok);
      } else {
        $setType(Token.SKIP);
      }
    }
    ;

// escape sequence -- note that this is protected; it can only be called
//   from another lexer rule -- it will not ever directly return a token to
//   the parser
// There are various ambiguities hushed in this rule.  The optional
// '0'...'9' digit matches should be matched here rather than letting
// them go back to STRING_LITERAL to be matched.  ANTLR does the
// right thing by matching immediately; hence, it's ok to shut off
// the FOLLOW ambig warnings.
protected
ESC
    :   '\\'
        (       'n'
        |       'r'
        |       't'
        |       'b'
        |       'f'
        |       '"'
        |       '\''
        |       '\\'
        |       ('u')+ HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT 
        |       ('0'..'3')
            (
                options {
                    warnWhenFollowAmbig = false;
                }
            :   ('0'..'9')
                (       
                    options {
                        warnWhenFollowAmbig = false;
                    }
                :       '0'..'9'
                )?
            )?
        |       ('4'..'7')
            (
                options {
                    warnWhenFollowAmbig = false;
                }
            :   ('0'..'9')
            )?
        )
        //{ Debug.println("Assert: got ESC #" + text + "#"); }
    ;


// hexadecimal digit (again, note it's protected!)
protected
HEX_DIGIT
    :   ('0'..'9'|'A'..'F'|'a'..'f')
        //{ Debug.println("Assert: got HEX_DIGIT #" + text + "#"); }
    ;

STAR_TEXT
  :
    (
        { LA(2) !='/' }? '*'
    )+
    {
          $setType(Token.SKIP);
    }
  ;
    
// end condition for lexer
JAVADOC_CLOSE
    :
    "*/"
        {
          Debug.println("Assert: got end of javadoc comment #" + text + "#");
          JonsAssert.selector.pop();
      //JonsAssert.selector.push(JonsAssert.javaLexer);
        }
    ;

