header {
    /*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
    package org.tcfreenet.schewe.assert;

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
    charVocabulary = '\1'..'\377';
    filter=true;
}

{
    // for column tracking
    public void setColumnTracker(final ColumnTracker ct) {
	_ct = ct;
	_ct._tokenColumn = 0;
	_ct._column = 0;
    }

    private ColumnTracker _ct;
    
    public void consume() throws IOException {
	if(text.length()==0) {
	    // remember the token start column
	    _ct._tokenColumn = _ct._column;
	}
	_ct._column++;
	super.consume();
    }

    public void newline() {
	_ct._column = 0;
	super.newline();
    }
    
    protected Token makeToken(int t) {
	Token tok = super.makeToken(t);
	tok.setColumn(_ct._tokenColumn);
	return tok;
    }
}

NEWLINE
    : ('\n'
	| '\r'
	| '\r' '\n'
	)
        { _ttype = Token.SKIP; newline(); }
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
    | '\n' { newline(); }
    | '\r' { newline(); }
    | '\r' '\n' { newline(); }
    | ~('('|')'|'\n'|'\r')
    )*
    ')'
    //    : '(' ( ~('('|')') )* ')'
    //    | '(' CONDITION ')'
    //    : '(' ( ~('('|')') )* (CONDITION)? ( ~('('|')') )* ')'
    //	{ System.out.println("Assert: got CONDITION #" + text + "#"); }
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
MESSAGE
    :  '"' (ESC|~('"'|'\\'))* '"'
	//	{ System.out.println("Assert: got MESSAGE #" + text + "#"); }
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
    :	"@post" (SPACE)* cond:CONDITION { c = cond.getText(); } (COMMA (SPACE)* mesg:MESSAGE { m = mesg.getText(); } SEMI)?
    {
      if(c != null) {
        c = c.replace('\n', ' ');
	c = c.replace('\r', ' ');
      }
      AssertToken assertTok = new AssertToken(c, m, _ttype, $getText);
      $setToken(assertTok);
    }
    ;

PRE_CONDITION
{
  String c = null;
  String m = null;
}
    :	"@pre" (SPACE)* cond:CONDITION { c = cond.getText(); } (COMMA (SPACE)* mesg:MESSAGE { m = mesg.getText(); } SEMI)?
    {
      if(c != null) {
        c = c.replace('\n', ' ');
	c = c.replace('\r', ' ');
      }
      AssertToken assertTok = new AssertToken(c, m, _ttype, $getText);
      $setToken(assertTok);
      //System.out.println("Just found a pre condition text: " + c);
    }
    ;

ASSERT_CONDITION
{
  String c = null;
  String m = null;
}
    :	"@assert" (SPACE)* cond:CONDITION { c = cond.getText(); } (COMMA (SPACE)* mesg:MESSAGE { m = mesg.getText(); } SEMI)?
    {
      if(c != null) {
        c = c.replace('\n', ' ');
	c = c.replace('\r', ' ');
      }
      AssertToken assertTok = new AssertToken(c, m, _ttype, $getText);
      $setToken(assertTok);
    }
    ;

INVARIANT_CONDITION
{
  String c = null;
  String m = null;
}
    :	"@invariant" (SPACE)* cond:CONDITION { c = cond.getText(); } (COMMA (SPACE)* mesg:MESSAGE { m = mesg.getText(); } SEMI)?
    {
      if(c != null) {
        c = c.replace('\n', ' ');
	c = c.replace('\r', ' ');
      }
      AssertToken assertTok = new AssertToken(c, m, _ttype, $getText);
      $setToken(assertTok);
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
    :	'\\'
	(	'n'
	|	'r'
	|	't'
	|	'b'
	|	'f'
	|	'"'
	|	'\''
	|	'\\'
	|	('u')+ HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT 
	|	('0'..'3')
	    (
		options {
		    warnWhenFollowAmbig = false;
		}
	    :	('0'..'9')
		(	
		    options {
			warnWhenFollowAmbig = false;
		    }
		:	'0'..'9'
		)?
	    )?
	|	('4'..'7')
	    (
		options {
		    warnWhenFollowAmbig = false;
		}
	    :	('0'..'9')
	    )?
	)
	//{ System.out.println("Assert: got ESC #" + text + "#"); }
    ;


// hexadecimal digit (again, note it's protected!)
protected
HEX_DIGIT
    :	('0'..'9'|'A'..'F'|'a'..'f')
	//{ System.out.println("Assert: got HEX_DIGIT #" + text + "#"); }
    ;

// end condition for lexer
//[jpschewe:20000304.2232CST] FIX still need to make it handle any number of *'s
JAVADOC_CLOSE
    : //("*/" | "**/")
    ('*')+ '/'
	{
	    //System.out.println("assert: got end of javadoc comment #" + text + "#");
	    JonsAssert.selector.pop();
	}
    ;

