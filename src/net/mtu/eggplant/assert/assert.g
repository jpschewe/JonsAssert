header {
/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.Assert;

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


NEWLINE
	: ('\n'|'\r'('\n')?)
        { _ttype = Token.SKIP; newline(); }
        ;

protected
CONDITION
	: '(' (~')')* ')'
//	{ System.out.println("Assert: got CONDITION #" + text + "#"); }
	;

protected
MESSAGE
	: ',' (' '|'\t'|'\f')* '"' (ESC|~('"'|'\\'))* '"' ';'
//	{ System.out.println("Assert: got MESSAGE #" + text + "#"); }
	;

POST_CONDITION
	:	"@post" (' '|'\t'|'\f')* CONDITION (MESSAGE)? 
//	{ System.out.println("Assert: Got post condition #" + text + "#"); }
	;

PRE_CONDITION
	:	"@pre" (' '|'\t'|'\f')* CONDITION (MESSAGE)?
//	{ System.out.println("Assert: Got pre condition #" + text + "#"); }
	;

ASSERT_CONDITION
	:	"@assert" (' '|'\t'|'\f')* CONDITION (MESSAGE)?
//	{ System.out.println("Assert: Got assert condition #" + text + "#"); }
	;

INVARIANT_CONDITION
	:	"@invariant" (' '|'\t'|'\f')* CONDITION (MESSAGE)?
	{
        //System.out.println("Assert: Got invariant condition #" + text + "#");
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
{ System.out.println("Assert: got ESC #" + text + "#"); }
	;


// hexadecimal digit (again, note it's protected!)
protected
HEX_DIGIT
	:	('0'..'9'|'A'..'F'|'a'..'f')
{ System.out.println("Assert: got HEX_DIGIT #" + text + "#"); }
	;

// end condition for lexer
JAVADOC_CLOSE
	: "**/"
	{
	  //System.out.println("assert: got end of javadoc comment #" + text + "#");
	  Main.selector.pop();
	}
	;

