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
package net.mtu.eggplant.dbc;

import net.mtu.eggplant.util.StringPair;
import net.mtu.eggplant.util.Pair;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.LinkedHashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

}

/**
 * Java parser for JonsAssert.  This file is generated by Antlr.
 *<pre>
 * Run 'java JonsAssert <directory full of java files>'
 *
 * Contributing authors:
 *      John Mitchell       johnm@non.net
 *      Terence Parr        parrt@magelang.com
 *      John Lilley         jlilley@empathy.com
 *      Scott Stanchfield   thetick@magelang.com
 *      Markus Mohnen       mohnen@informatik.rwth-aachen.de
 *      Peter Williams      pwilliams@netdynamics.com
 *
 * Version 1.00 December 9, 1997 -- initial release
 * Version 1.01 December 10, 1997
 *      fixed bug in octal def (0..7 not 0..8)
 * Version 1.10 August 1998 (parrt)
 *      added tree construction
 *      fixed definition of WS,comments for mac,pc,unix newlines
 *      added unary plus
 * Version 1.11 (Nov 20, 1998)
 *      Added "shutup" option to turn off last ambig warning.
 *      Fixed inner class def to allow named class defs as statements
 *      synchronized requires compound not simple statement
 *      add [] after builtInType DOT class in primaryExpression
 *      "const" is reserved but not valid..removed from modifiers
 *
 * Version 1.12 (Feb 2, 1999)
 *      Changed LITERAL_xxx to xxx in tree grammar.
 *      Updated java.g to use tokens {...} now for 2.6.0 (new feature).
 *
 * Version 1.13 (Apr 23, 1999)
 *      Didn't have (stat)? for else clause in tree parser.
 *      Didn't gen ASTs for interface extends.  Updated tree parser too.
 *      Updated to 2.6.0.
 * Version 1.14 (Jun 20, 1999)
 *      Allowed final/abstract on local classes.
 *      Removed local interfaces from methods
 *      Put instanceof precedence where it belongs...in relationalExpr
 *          It also had expr not type as arg; fixed it.
 *      Missing ! on SEMI in classBlock
 *      fixed: (expr) + "string" was parsed incorrectly (+ as unary plus).
 *      fixed: didn't like Object[].class in parser or tree parser
 *
 * This grammar is in the PUBLIC DOMAIN
 *
 * BUGS
 *</pre>
 *
 * <p>This parser has been modified from the original Recognizer to a pre-parser
 * that implements assertions in java as well as support JDK 1.4.</p>
 */
class JavaRecognizer extends Parser;
options {
  k=2;                           // k token lookahead
  exportVocab=Java;                // Call its vocabulary "Java"
  codeGenMakeSwitchThreshold = 2;  // Some optimizations
  codeGenBitsetTestThreshold = 3;
  defaultErrorHandler = false;     // generate parser error handlers
}

tokens {
  BLOCK; MODIFIERS; OBJBLOCK; SLIST; CTOR_DEF; METHOD_DEF; VARIABLE_DEF; 
  INSTANCE_INIT; STATIC_INIT; TYPE; CLASS_DEF; INTERFACE_DEF; 
  PACKAGE_DEF; ARRAY_DECLARATOR; EXTENDS_CLAUSE; IMPLEMENTS_CLAUSE;
  PARAMETERS; PARAMETER_DEF; LABELED_STAT; TYPECAST; INDEX_OP; 
  POST_INC; POST_DEC; METHOD_CALL; EXPR; ARRAY_INIT; 
  IMPORT; UNARY_MINUS; UNARY_PLUS; CASE_GROUP; ELIST; FOR_INIT; FOR_CONDITION; 
  FOR_ITERATOR; EMPTY_STAT; FINAL="final"; ABSTRACT="abstract";
  JAVADOC_OPEN; JAVADOC_CLOSE; POST_CONDITION; PRE_CONDITION;
  ASSERT_CONDITION; INVARIANT_CONDITION; CONDITION; MESSAGE;
}

// Define some helper methods
{

  private static final Log LOG = LogFactory.getLog(JavaRecognizer.class);

  public void setHelper(final GrammarHelper helper) {
    _helper = helper;
  }

  private GrammarHelper _helper;

  public GrammarHelper getHelper() {
    return _helper;
  }
  
}

unit : pre start middle end EOF ;

pre : uselessComments ;

start
{
  String packageName = null;
}
  :
    (packageName=packageDefinition uselessComments)?
    {
      getHelper().handlePackage(packageName);
    }
  ;

middle
  :
    (importDefinition uselessComments)*
    {
      getHelper().clearInvariants();
    }
  ;

end : ((invariantCondition)? typeDefinition)* ;

uselessComments :
  (javadocComment) => ( (invariantCondition typeDefinition) => /* empty */ | javadocComment uselessComments )
  | /* empty */ ;

/**
   This is a javadoc comment that we're not looking for any conditions in.
**/
javadocComment
  : JAVADOC_OPEN ( INVARIANT_CONDITION | PRE_CONDITION | POST_CONDITION | ASSERT_CONDITION )* JAVADOC_CLOSE
  ;

/**
   This is a javadoc comment that we're looking for invariants in.
**/
invariantCondition
  : JAVADOC_OPEN ( iv:INVARIANT_CONDITION { getHelper().addInvariant(iv); } | PRE_CONDITION | POST_CONDITION | ASSERT_CONDITION )* JAVADOC_CLOSE
  ;

/**
   Package statement: "package" followed by an identifier.
**/
packageDefinition returns [String packageName]
{
  Token id = null;
  packageName = null;
}
  :
    "package" id=identifier SEMI
    {
      packageName = id.getText();
    }
  ;


// Import statement: import followed by a package or class name
importDefinition
  : "import" identifierStar SEMI
  ;

// A type definition in a file is either a class or interface definition.
typeDefinition
{
  Set dummyMods;
}
  : dummyMods=modifiers
    ( classDefinition
    | interfaceDefinition
    )
  | SEMI
  ;

/** A declaration is the creation of a reference or primitive-type variable
 *  Create a separate Type/Var tree for each var in the var list.
 */
declaration
{
  Set dummyMods;
  Token dummyType;
}
  : dummyMods=modifiers dummyType=typeSpec variableDefinitions
  ;

// A list of zero or more modifiers.  We could have used (modifier)* in
//   place of a call to modifiers, but I thought it was a good idea to keep
//   this rule separate so they can easily be collected in a Set if
//   someone so desires
/**
   @return the list of modifiers as Strings
**/
modifiers returns [Set mods]
{
  mods = new HashSet(10);
  Token mod = null;
}
  : ( mod = modifier { mods.add(mod.getText()); } )*
  ;


// A type specification is a type name with possible brackets afterwards
//   (which would make it an array type).
/**
   @return the Token that represents this type spec
**/
typeSpec returns [Token t]
{
  t = null;
}
  : t=classTypeSpec
  | t=builtInTypeSpec
  ;

// A class type specification is a class type with possible brackets afterwards
//   (which would make it an array type).
/**
   @return the Token that represents this class type spec
**/
classTypeSpec returns [Token id]
{
  id = null;
}
    :
    id=identifier (LBRACK RBRACK { id.setText(id.getText() + "[]"); } )*
  ;

// A builtin type specification is a builtin type with possible brackets
// afterwards (which would make it an array type).
/**
   @return the Token that represents this builtin type spec
**/
builtInTypeSpec returns [Token id]
{
  id = null;
}
  : id=builtInType (LBRACK RBRACK { id.setText(id.getText() + "[]"); } )*
  ;

// A type name. which is either a (possibly qualified) class name or
//   a primitive (builtin) type
/**
   @return the Token that represents this type
**/
type returns [Token t]
{
  t = null;
}
  : t=identifier
  | t=builtInType
  ;

// The primitive types.
/**
   @return the Token that represents this builtin type
**/
builtInType returns [Token t]
{ t = null; }
  : tvoid:"void" { t = tvoid; }
  | tboolean:"boolean" { t = tboolean; }
  | tbyte:"byte" { t = tbyte; }
  | tchar:"char" { t = tchar; }
  | tshort:"short" { t = tshort; }
  | tint:"int" { t = tint; }
  | tfloat:"float" { t = tfloat; }
  | tlong:"long" { t = tlong; }
  | tdouble:"double" { t = tdouble; }
  ;

/**
 A (possibly-qualified) java identifier.  We start with the first IDENT
   and expand its name by adding dots and following IDENTS

   @return a Token that represents this identifier, the text of the Token is
   equal to the whole identifier, including possible dots for a fully
   qualified java class name.
**/
identifier returns [Token t]
{ t = null; }
  :
    id:IDENT { t = id; }
    ( DOT id2:IDENT { t.setText(t.getText() + "." + id2.getText()); } )*
  ;

// identifierStar returns [Token t]
// { t = null; }
//   :
//     id:IDENT { t = id; }
//     ( DOT id2:IDENT { t.setText(t.getText() + "." + id2.getText()); )*
//     ( DOT STAR { t.setText(t.getText() + ".*"); )?
//   ;

/**
   Used for import statements.  Adds this package identifier to the list of
   imports in the symbol table.
**/
identifierStar
{
  String className = "";
  String packageName = "";
}
  :
    id:IDENT        { className = id.getText(); }
    ( DOT id2:IDENT
     { packageName += "." + className; className = id2.getText(); } )*
    ( DOT STAR      { packageName += "." + className; className = null; } )?

    {
     // tell the symbol table about the import
     getHelper().getSymtab().addImport(className, packageName);
    }
  ;


// modifiers for Java classes, interfaces, class/instance vars and methods
modifier returns [Token t]
{
  t = null;
}
  : tprivate:"private" { t = tprivate; }
  | tpublic:"public" { t = tpublic; }
  | tprotected:"protected" { t = tprotected; }
  | tstatic:"static" { t = tstatic; }
  | ttransient:"transient" { t = ttransient; }
  | tfinal:"final" { t = tfinal; }
  | tabstract:"abstract" { t = tabstract; }
  | tnative:"native" { t = tnative; }
  | tthreadsafe:"threadsafe" { t = tthreadsafe; }
  | tsynchronized:"synchronized" { t = tsynchronized; }
    //  |   tconst:"const"  { t = tconst; }     // reserved word; leave out
  | tvolatile:"volatile" { t = tvolatile; }
  ;


// Definition of a Java class
classDefinition
{
  String name = null;
  Token superclass = null;
}
  : "class" id:IDENT
    {
      //handeling inner classes
      if(getHelper().getSymtab().getCurrentClass() != null) {
        name = getHelper().getSymtab().getCurrentClass().getName() + "$" + id.getText();
      }
      else {
        name = id.getText();
      }
    }
    // it _might_ have a superclass...
    superclass = superClassClause
    // it might implement some interfaces...
    implementsClause
    // now parse the body of the class
    classBlock[name, false, false, superclass]
  ;


superClassClause returns [Token id]
{ id = null; }
  : ( "extends" id=identifier )?
  ;

// Definition of a Java Interface
interfaceDefinition
{
  String name = null;
}
  : "interface" id:IDENT
    {
      //handeling inner classes
      if(getHelper().getSymtab().getCurrentClass() != null) {
        name = getHelper().getSymtab().getCurrentClass().getName() + "$" + id.getText();
      }
      else {
        name = id.getText();
      }
    }
    
    // it might extend some other interfaces
    interfaceExtends
    // now parse the body of the interface (looks like a class...)
    classBlock[name, true, false, null]
  ;


/**
   This is the body of a class or interface.  You can have fields and extra semicolons,
   That's about it (until you see what a field is...)
**/
classBlock [ String name, boolean isInterface, boolean isAnonymous, Token superclass ]
  :
    lc:LCURLY
    {
      getHelper().startClass(name, isInterface, isAnonymous, (superclass == null ? null : superclass.getText()));
    }
    //this should just be methods and constructors,
    //but can't find a better place for it.
    ( prePostField | SEMI )*
    rc:RCURLY
    {
      getHelper().finishClass(rc);
    }
  ;

prePosts 
    :
    ( post:POST_CONDITION { getHelper().addPostCondition(post); }
    | pre:PRE_CONDITION { getHelper().addPreCondition(pre); }
    | ASSERT_CONDITION
    | INVARIANT_CONDITION
    )*
  ;

prePostField 
  : (JAVADOC_OPEN prePosts JAVADOC_CLOSE)* field
  ;


// An interface can extend several other interfaces...
//[jpschewe:20000102.1415CST] FIX need stuff here too, this could get messy
interfaceExtends
{ Token id, id2; }
  : (
      "extends"
      id=identifier ( COMMA id2=identifier )*
    )?
  ;

// A class can implement several interfaces...
//[jpschewe:20000102.1414CST] FIX need stuff here
implementsClause
{ Token id, id2; }
  : (
      "implements" id=identifier { getHelper().parseImplementedInterface(id); }
      ( COMMA id2=identifier { getHelper().parseImplementedInterface(id2); } )*
    )?
  ;

/**
 Now the various things that can be defined inside a class or interface...
 Note that not all of these are really valid in an interface (constructors,
   for example), and if this grammar were used for a compiler there would
   need to be some semantic checks to make sure we're doing the right thing...
**/
field
{
  Set mods = null;
  Token retType = null;
  List params = null;
  boolean methodOrConstructor = false;
  CodePointPair startEnd = null;
  CodePointPair dummyStartEnd;
  Set thrownExceptions = null;
  Pair p = null;
}
  :

    (
      // method, constructor, or variable declaration
      //need to do something special for abstract and native methods here
      mods=modifiers
      ( p=ctorHead
    {
      params = (List)p.getOne();
      thrownExceptions = (Set)p.getTwo();
      // needs to be before compoundStatement so that I can have it set for the addExit calls
      getHelper().startMethod(null, params, null, mods);
    }
    startEnd=compoundStatement // constructor
    {
      getHelper().getSymtab().finishMethod(startEnd, thrownExceptions);
    }
    

      | classDefinition       // inner class
    
      | interfaceDefinition   // inner interface

      | retType=typeSpec  // method or variable declaration(s)
    (   methodName:IDENT  // the name of the method
      
      // parse the formal parameter declarations.
      LPAREN params=parameterDeclarationList
      {
        // needs to be before compoundStatement so that I can have it set for the addExit calls
        getHelper().startMethod(methodName.getText(), params, retType.getText(), mods);
      }
      RPAREN

      returnTypeBrackersOnEndOfMethodHead

      // get the list of exceptions that this method is declared to throw
      (thrownExceptions=throwsClause)?
      ( startEnd=compoundStatement | semi:SEMI )
      {
        if(startEnd != null) {
          getHelper().getSymtab().finishMethod(startEnd, thrownExceptions);
        }
        else {
          //abstract, native or interface method
          CodePoint close = new CodePoint(semi.getLine(), semi.getColumn()-1);
          getHelper().getSymtab().finishMethod(new CodePointPair(close, close), null);
        }
      }
    |   variableDefinitions SEMI
    )
      )

      // "static { ... }" class initializer
    |   "static" dummyStartEnd=compoundStatement

      // "{ ... }" instance initializer
    |   dummyStartEnd=compoundStatement
    )
  ;

variableDefinitions
  : variableDeclarator
    (   COMMA
      variableDeclarator
    )*
  ;

/** Declaration of a variable.  This can be a class/instance variable,
 *   or a local variable in a method
 * It can also include possible initialization.
 */
variableDeclarator
  : IDENT declaratorBrackets varInitializer
  ;

declaratorBrackets
  :
    (lb:LBRACK RBRACK)*
  ;

varInitializer
  : ( ASSIGN initializer )?
  ;

// This is an initializer used to set up an array.
arrayInitializer
  : LCURLY
    (   initializer
      (
    // CONFLICT: does a COMMA after an initializer start a new
    //           initializer or start the option ',' at end?
    //           ANTLR generates proper code by matching
    //           the comma as soon as possible.
    options {
      warnWhenFollowAmbig = false;
    }
      :
    COMMA initializer
      )*
      (COMMA)?
    )?
    RCURLY
  ;


// The two "things" that can initialize an array element are an expression
//   and another (nested) array initializer.
initializer
  : expression
  | arrayInitializer
  ;

/**
 <p>This is the header of a method.  It includes the name and parameters for
   the method.  This also watches for a list of exception classes in a
   "throws" clause.</p>

   <p>this is only used for constructors so I'm
   just going to return the params, the method name is known</p>

   @return the parameters as StringPair(type, name)
**/
ctorHead returns [Pair p]
{
  List params = null;
  Set thrownExceptions = null;
  p = null;
}
  :
    (
      IDENT  // the name of the method
      
      // parse the formal parameter declarations.
      LPAREN params=parameterDeclarationList RPAREN
      
      // get the list of exceptions that this method is declared to throw
      (thrownExceptions=throwsClause)?
    )
    
    {
      p = new Pair(params, thrownExceptions);
    }
  ;

// This is a list of exception classes that the method is declared to throw
throwsClause returns [Set exceptions]
{
  exceptions = new LinkedHashSet();
  Token id, id2;
}
  : "throws" id=identifier { exceptions.add(id.getText()); }
    ( COMMA id2=identifier {exceptions.add(id2.getText()); } )*
  ;


returnTypeBrackersOnEndOfMethodHead
  :
    (LBRACK RBRACK)*
  ;

// A list of formal parameters
parameterDeclarationList returns [List params]
{
  params = new LinkedList();
  StringPair pd = null;
}
  : ( pd=parameterDeclaration { params.add(pd); } ( COMMA pd=parameterDeclaration { params.add(pd); } )* )?
  ;

// A formal parameter.
parameterDeclaration returns [StringPair sp]
{
  StringBuffer brackets = null;
  Token type;
  sp = null;
}
  : parameterModifier type=typeSpec name:IDENT
    brackets=parameterDeclaratorBrackets
    {
      String typeText = type.getText();
      if(brackets.length() > 0) {
        typeText += brackets.toString();
      }
      sp = new StringPair(typeText, name.getText());
    }
  ;

parameterDeclaratorBrackets returns [StringBuffer text]
{
  text = new StringBuffer();
}
  : 
    (lb:LBRACK rb:RBRACK { text.append("[]"); } )*
  ;

parameterModifier
  : ("final")?
  ;

/**
   Handle assert or invariant conditions, if an invariant token is seen, clear
   the asserts, if an assert is seen, clear the invariants.  This should keep
   us out of trouble.
**/
assertOrInvariantCondition
{ List assertTokens = new LinkedList(); }
  : (JAVADOC_OPEN
    ( assertCondition:ASSERT_CONDITION { assertTokens.add(assertCondition); getHelper().clearInvariants(); }
      | PRE_CONDITION
      | POST_CONDITION
      | iv:INVARIANT_CONDITION { getHelper().addInvariant(iv); assertTokens = new LinkedList(); }
    )*
    jdc:JAVADOC_CLOSE 
    { getHelper().addAsserts(assertTokens, jdc); }
      )
  ;

// Compound statement.  This is used in many contexts:
//   Inside a class definition prefixed with "static":
//      it is a class initializer
//   Inside a class definition without "static":
//      it is an instance initializer
//   As the body of a method
//   As a completely indepdent braced block of code inside a method
//      it starts a new scope for variable definitions

/**
   @return a CodePointPair that represent the open and close curly braces
**/
compoundStatement returns [CodePointPair startEnd]
{
  startEnd = null;
}
  : 
    (
      lc:LCURLY
      // include the (possibly-empty) list of statements
      (statement)*
      rc:RCURLY
    )
    {
      CodePoint start = new CodePoint(lc.getLine(), lc.getColumn()-1);
      CodePoint end = new CodePoint(rc.getLine(), rc.getColumn()-1);
      startEnd = new CodePointPair(start, end);
    }
  ;


statement
{
  CodePointPair dummyStartEnd;
}
  :

    // A list of statements in curly braces -- start a new scope!    
    dummyStartEnd=compoundStatement

    // class definition
    |   classDefinition

    // final class definition
    |   "final" classDefinition

    // abstract class definition
    |   "abstract" classDefinition

    // declarations are ambiguous with "ID DOT" relative to expression
    // statements.  Must backtrack to be sure.  Could use a semantic
    // predicate to test symbol table to see what the type was coming
    // up, but that's pretty hard without a symbol table ;)
    |   (declaration)=> declaration SEMI

    // An expression statement.  This could be a method call,
    // assignment statement, or any other expression evaluated for
    // side-effects.
    |   expression SEMI

    // Attach a label to the front of a statement
    |   IDENT COLON statement

    // If-else statement
    |   "if" LPAREN expression RPAREN statement
    (
        // CONFLICT: the old "dangling-else" problem...
        //           ANTLR generates proper code matching
        //           as soon as possible.  Hush warning.
        options {
        warnWhenFollowAmbig = false;
        }
    :
        "else" statement
    )?

    // For statement
    |   "for"
    LPAREN
    forInit SEMI   // initializer
    forCond SEMI   // condition test
    forIter         // updater
    RPAREN
    statement                     // statement to loop over

    // While statement
    |   "while" LPAREN expression RPAREN statement

    // do-while statement
    |   "do" statement "while" LPAREN expression RPAREN SEMI

    // get out of a loop (or switch)
    |   "break" (IDENT)? SEMI

    // do next iteration of a loop
    |   "continue" (IDENT)? SEMI

    // Return an expression
    |   ret:"return" (expression)? semi:SEMI
      {
    //keep track of these points for post conditions
    CodePoint retcp = new CodePoint(ret.getLine(), ret.getColumn()-1);
    //add 1 so that code is inserted after the semi colon
    CodePoint semicp = new CodePoint(semi.getLine(), semi.getColumn());
    getHelper().getSymtab().getCurrentMethod().addExit(new CodePointPair(retcp, semicp));
      }

    // switch/case statement
    |   "switch" LPAREN expression RPAREN LCURLY
    ( casesGroup )*
    RCURLY

    // exception try-catch block
    |   tryBlock

    // throw an exception
    |   "throw" expression SEMI

    // synchronize a statement
    |   "synchronized" LPAREN expression RPAREN dummyStartEnd=compoundStatement

    // empty statement
    |   SEMI
      
      //assertion, checks invariants too for the class definitions allowed in statement
    |   assertOrInvariantCondition

    //JDK 1.4 assert keyword
    //| "assert" expression (COLON expression)? SEMI

    ;


casesGroup
  : (   // CONFLICT: to which case group do the statements bind?
      //           ANTLR generates proper code: it groups the
      //           many "case"/"default" labels together then
      //           follows them with the statements
      options {
    warnWhenFollowAmbig = false;
      }
    :
      aCase
    )+
    caseSList
  ;

aCase
  : ("case" expression | "default") COLON
  ;

caseSList
  : (statement)*
  ;

// The initializer for a for loop
forInit
// if it looks like a declaration, it is
  : (   (declaration)=> declaration
      // otherwise it could be an expression list...
    |   expressionList
    )?
  ;

forCond
  : (expression)?
  ;

forIter
  : (expressionList)?
  ;

// an exception handler try/catch block
tryBlock
{
  CodePointPair dummyStartEnd;
}
    :   "try" dummyStartEnd=compoundStatement
    (handler)*
    ( "finally" dummyStartEnd=compoundStatement )?
  ;


// an exception handler
handler
{
  CodePointPair dummyStartEnd;
  StringPair dummyParams;
}
  : "catch" LPAREN dummyParams=parameterDeclaration RPAREN dummyStartEnd=compoundStatement
  ;


/**
   the mother of all expressions

<pre>
 expressions
 Note that most of these expressions follow the pattern
   thisLevelExpression :
       nextHigherPrecedenceExpression
           (OPERATOR nextHigherPrecedenceExpression)*
 which is a standard recursive definition for a parsing an expression.
 The operators in java have the following precedences:
    lowest  (13)  = *= /= %= += -= <<= >>= >>>= &= ^= |=
            (12)  ?:
            (11)  ||
            (10)  &&
            ( 9)  |
            ( 8)  ^
            ( 7)  &
            ( 6)  == !=
            ( 5)  < <= > >=
            ( 4)  << >>
            ( 3)  +(binary) -(binary)
            ( 2)  * / %
            ( 1)  ++ -- +(unary) -(unary)  ~  !  (type)
                  []   () (method call)  . (dot -- identifier qualification)
                  new   ()  (explicit parenthesis)

 the last two are not usually on a precedence chart; I put them in
 to point out that new has a higher precedence than '.', so you
 can validy use
     new Frame().show()
 
 Note that the above precedence levels map to the rules below...
 Once you have a precedence chart, writing the appropriate rules as below
   is usually very straightfoward
</pre>



**/
expression
  : assignmentExpression
  ;


/**
   This is a list of expressions.
**/
expressionList
  : expression (COMMA expression)*
  ;


// assignment expression (level 13)
assignmentExpression
  : conditionalExpression
    (   (   ASSIGN
      |   PLUS_ASSIGN
      |   MINUS_ASSIGN
      |   STAR_ASSIGN
      |   DIV_ASSIGN
      |   MOD_ASSIGN
      |   SR_ASSIGN
      |   BSR_ASSIGN
      |   SL_ASSIGN
      |   BAND_ASSIGN
      |   BXOR_ASSIGN
      |   BOR_ASSIGN
      )
      assignmentExpression
    )?
  ;


// conditional test (level 12)
conditionalExpression
  : logicalOrExpression
    ( QUESTION conditionalExpression COLON conditionalExpression )?
  ;


/**
   logical or (||)  (level 11)

**/
logicalOrExpression
  : logicalAndExpression (LOR logicalAndExpression)*
  ;


/**
   logical and (&&)  (level 10)

**/
logicalAndExpression
  : inclusiveOrExpression (LAND inclusiveOrExpression)*
  ;


/**
   bitwise or non-short-circuiting or (|)  (level 9)

**/
inclusiveOrExpression
  : exclusiveOrExpression (BOR exclusiveOrExpression)*
  ;


/**
   exclusive or (^)  (level 8)
**/
exclusiveOrExpression
  : andExpression (BXOR andExpression)*
  ;


/**
   bitwise or non-short-circuiting and (&)  (level 7)

**/
andExpression
  : equalityExpression (BAND equalityExpression)*
  ;


/**
   equality/inequality (==/!=) (level 6)

**/
equalityExpression
  : relationalExpression ((NOT_EQUAL | EQUAL) relationalExpression)*
  ;


/**
   boolean relational expressions (level 5)

**/
relationalExpression
{
  Token dummyType;
}
  : shiftExpression
    (
      (
    (LT
    |   GT
    |   LE
    |   GE
    )
    shiftExpression
      )*
    |   "instanceof" dummyType=typeSpec
    )
  ;


/**
   bit shift expressions (level 4)

**/
shiftExpression
  : additiveExpression ((SL | SR | BSR) additiveExpression)*
  ;


/**
   binary addition/subtraction (level 3)
**/
additiveExpression
  : multiplicativeExpression ((PLUS | MINUS) multiplicativeExpression)*
  ;


/**
   multiplication/division/modulo (level 2)

**/
multiplicativeExpression
  : unaryExpression ((STAR | DIV | MOD ) unaryExpression)*
  ;

unaryExpression
  : INC unaryExpression
  | DEC unaryExpression
  | MINUS unaryExpression
  | PLUS  unaryExpression
  | unaryExpressionNotPlusMinus
  ;

unaryExpressionNotPlusMinus
{
  Token dummyToken;
}
  : BNOT unaryExpression
  | LNOT unaryExpression

  | (   // subrule allows option to shut off warnings
      options {
        // "(int" ambig with postfixExpr due to lack of sequence
        // info in linear approximate LL(k).  It's ok.  Shut up.
        generateAmbigWarnings=false;
      }
    :   // If typecast is built in type, must be numeric operand
      // Also, no reason to backtrack if type keyword like int, float...
      LPAREN dummyToken=builtInTypeSpec RPAREN
      unaryExpression

      // Have to backtrack to see if operator follows.  If no operator
      // follows, it's a typecast.  No semantic checking needed to parse.
      // if it _looks_ like a cast, it _is_ a cast; else it's a "(expr)"
    |   (LPAREN classTypeSpec RPAREN unaryExpressionNotPlusMinus)=>
      LPAREN dummyToken=classTypeSpec RPAREN
      unaryExpressionNotPlusMinus

    |   postfixExpression
    )
  ;

/**
   qualified names, array expressions, method invocation, post inc/dec

**/
postfixExpression
{ Token id; }
  : primaryExpression // start with a primary

    (   // qualified id (id.id.id.id...) -- build the name
      DOT ( IDENT
      | "this"
      | "class"
      | newExpression
      | "super" LPAREN ( expressionList )? RPAREN
      )
      // the above line needs a semantic check to make sure "class"
      //   is the _last_ qualifier.

      // allow ClassName[].class
    |   ( LBRACK RBRACK )+
      DOT "class"

      // an array indexing operation
    |   LBRACK expression RBRACK

      // method invocation
      // The next line is not strictly proper; it allows x(3)(4) or
      //  x[2](4) which are not valid in Java.  If this grammar were used
      //  to validate a Java program a semantic check would be needed, or
      //   this rule would get really ugly...
    |   LPAREN
      argList
      RPAREN
    )*

    // possibly add on a post-increment or post-decrement.
    // allows INC/DEC on too much, but semantics can check
    (   INC
    |   DEC
    |   // nothing
    )

    // look for int.class and int[].class
  | id=builtInType 
    ( LBRACK RBRACK )*
    DOT "class"
  ;

/**
   the basic element of an expression
**/
primaryExpression
  : IDENT
  | newExpression
  | constant
  | "super"
  | "true"
  | "false"
  | "this"
  | "null"
  | LPAREN assignmentExpression RPAREN
  ;

/**
   object instantiation.
**/
newExpression
{ Token t; }
  : "new" t=type
    (   LPAREN argList RPAREN ( classBlock[null, false, true, t] )?

      //[jpschewe:20000128.0740CST] FIX need to use t to figure out what
      //interfaces we need to check conditions on.
        
      //java 1.1
      // Note: This will allow bad constructs like
      //    new int[4][][3] {exp,exp}.
      //    There needs to be a semantic check here...
      // to make sure:
      //   a) [ expr ] and [ ] are not mixed
      //   b) [ expr ] and an init are not used together

    |   newArrayDeclarator (arrayInitializer)?
    )
  ;

argList
  : (   expressionList
    |   /*nothing*/
    )
  ;

newArrayDeclarator
  : (
      // CONFLICT:
      // newExpression is a primaryExpression which can be
      // followed by an array index reference.  This is ok,
      // as the generated code will stay in this loop as
      // long as it sees an LBRACK (proper behavior)
      options {
    warnWhenFollowAmbig = false;
      }
    :
      LBRACK
      (expression)?
      RBRACK
    )+
  ;

constant
  : NUM_INT
  | CHAR_LITERAL
  | STRING_LITERAL
  | NUM_FLOAT
  ;


//----------------------------------------------------------------------------
// The Java scanner
//----------------------------------------------------------------------------
class JavaLexer extends Lexer;
options {
  exportVocab=Java;      // call the vocabulary "Java"
  testLiterals=false;    // don't automatically test for literals
  k=4;                   // four characters of lookahead
}

// OPERATORS
QUESTION        :   '?'     ;
LPAREN          :   '('     ;
RPAREN          :   ')'     ;
LBRACK          :   '['     ;
RBRACK          :   ']'     ;
LCURLY          :   '{'     ;
RCURLY          :   '}'     ;
COLON           :   ':'     ;
COMMA           :   ','     ;
//DOT           :   '.'     ;
ASSIGN          :   '='     ;
EQUAL           :   "=="    ;
LNOT            :   '!'     ;
BNOT            :   '~'     ;
NOT_EQUAL       :   "!="    ;
DIV             :   '/'     ;
DIV_ASSIGN      :   "/="    ;
PLUS            :   '+'     ;
PLUS_ASSIGN     :   "+="    ;
INC             :   "++"    ;
MINUS           :   '-'     ;
MINUS_ASSIGN    :   "-="    ;
DEC             :   "--"    ;
STAR            :   '*'     ;
STAR_ASSIGN     :   "*="    ;
MOD             :   '%'     ;
MOD_ASSIGN      :   "%="    ;
SR              :   ">>"    ;
SR_ASSIGN       :   ">>="   ;
BSR             :   ">>>"   ;
BSR_ASSIGN      :   ">>>="  ;
GE              :   ">="    ;
GT              :   ">"     ;
SL              :   "<<"    ;
SL_ASSIGN       :   "<<="   ;
LE              :   "<="    ;
LT              :   '<'     ;
BXOR            :   '^'     ;
BXOR_ASSIGN     :   "^="    ;
BOR             :   '|'     ;
BOR_ASSIGN      :   "|="    ;
LOR             :   "||"    ;
BAND            :   '&'     ;
BAND_ASSIGN     :   "&="    ;
LAND            :   "&&"    ;
SEMI            :   ';'     ;


// Whitespace -- ignored
WS  :   (   ' '
    |   '\t'
    |   '\f'
      // handle newlines
    |   '\r' '\n'       {newline(); }
    |   '\r'            {newline();}
    |   '\n'            {newline();}
    )
    { $setType(Token.SKIP); }
  ;

// Single-line comments
SL_COMMENT
  : "//"
    (~('\n'|'\r'))*
    (
    |   '\r' '\n'       {newline();}
    |   '\r'            {newline();}
    |   '\n'            {newline();}
    )       
    { $setType(Token.SKIP); }
  ;

// Javadoc comments
JAVADOC_OPEN
  : "/**"
    {
      //LOG.debug("java: got start of javadoc comment #" + text + "#");
      JonsAssert.getSelector().push(JonsAssert.getAssertLexer());
    }
  ;

//JD_COMMENT
//      :   "/**"
//          (   options {
//                  generateAmbigWarnings=false;
//              }
//          :
//            { LA(2)!='/' }? '*'
//          |   '\r' '\n'       {newline();}
//          |   '\r'            {newline();}
//          |   '\n'            {newline();}
//          |   ~('*'|'\n'|'\r')
//          )*
//          "*/"
//        {
//            $setType(Token.SKIP);         
//            System.out.println("Got a javadoc comment #" + text + "#");
//        }
//
//      ;

// multiple-line comments
ML_COMMENT
    :
    "/*"
    (
        options {
        generateAmbigWarnings=false;
        }
    :       
        ~('*'|'\n'|'\r')
    |   '\r' '\n'       {newline();}
    |   '\r'            {newline();}
    |   '\n'            {newline();}
    )
    (   /*  '\r' '\n' can be matched in one alternative or by matching
                '\r' in one iteration and '\n' in another.  I am trying to
                handle any flavor of newline that comes in, but the language
                that allows both "\r\n" and "\r" and "\n" to all be valid
                newline is ambiguous.  Consequently, the resulting grammar
                must be ambiguous.  I'm shutting this warning off.
             */
      options {
    generateAmbigWarnings=false;
      }
    :
      { LA(2)!='/' }? '*'
    |   '\r' '\n'       {newline();}
    |   '\r'            {newline();}
    |   '\n'            {newline();}
    |   ~('*'|'\n'|'\r')
    )*
    "*/"
    {$setType(Token.SKIP);}
  ;

// character literals
CHAR_LITERAL
  : '\'' ( ESC | ~'\'' ) '\''
  ;

// string literals
STRING_LITERAL
  : '"' (ESC|~('"'|'\\'))* '"'
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
  : '\\'
    (   'n'
    |   'r'
    |   't'
    |   'b'
    |   'f'
    |   '"'
    |   '\''
    |   '\\'
    |   ('u')+ HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT 
    |   ('0'..'3')
      (
    options {
      warnWhenFollowAmbig = false;
    }
      : ('0'..'9')
    (   
      options {
        warnWhenFollowAmbig = false;
      }
    :   '0'..'9'
    )?
      )?
    |   ('4'..'7')
      (
    options {
      warnWhenFollowAmbig = false;
    }
      : ('0'..'9')
      )?
    )
  ;


// hexadecimal digit (again, note it's protected!)
protected
HEX_DIGIT
  : ('0'..'9'|'A'..'F'|'a'..'f')
  ;


// a dummy rule to force vocabulary to be all characters (except special
//   ones that ANTLR uses internally (0 to 2)
protected
VOCAB
  : '\3'..'\377'
  ;


// an identifier.  Note that testLiterals is set to true!  This means
// that after we match the rule, we look in the literals table to see
// if it's a literal or really an identifer
IDENT
options {testLiterals=true;}
  : ('a'..'z'|'A'..'Z'|'_'|'$') ('a'..'z'|'A'..'Z'|'_'|'0'..'9'|'$')*
  ;


// a numeric literal
NUM_INT
{boolean isDecimal=false;}
  : '.' {_ttype = DOT;}
    (('0'..'9')+ (EXPONENT)? (FLOAT_SUFFIX)? { _ttype = NUM_FLOAT; })?
  | (   '0' {isDecimal = true;} // special case for just '0'
      ( ('x'|'X')
    (                                           // hex
      // the 'e'|'E' and float suffix stuff look
      // like hex digits, hence the (...)+ doesn't
      // know when to stop: ambig.  ANTLR resolves
      // it correctly by matching immediately.  It
      // is therefor ok to hush warning.
      options {
        warnWhenFollowAmbig=false;
      }
    :   HEX_DIGIT
    )+
      | ('0'..'7')+                                 // octal
      )?
    |   ('1'..'9') ('0'..'9')*  {isDecimal=true;}       // non-zero decimal
    )
    (   ('l'|'L')
      
      // only check to see if it's a float if looks like decimal so far
    |   {isDecimal}?
      ( '.' ('0'..'9')* (EXPONENT)? (FLOAT_SUFFIX)?
      | EXPONENT (FLOAT_SUFFIX)?
      | FLOAT_SUFFIX
      )
      { _ttype = NUM_FLOAT; }
    )?
  ;


// a couple protected methods to assist in matching floating point numbers
protected
EXPONENT
  : ('e'|'E') ('+'|'-')? ('0'..'9')+
  ;


protected
FLOAT_SUFFIX
  : 'f'|'F'|'d'|'D'
  ;

