header {
  /*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org

  Notes: Check invariants where ever pre and post conditions are checked

  */
  package org.tcfreenet.schewe.assert;

  import org.tcfreenet.schewe.utils.StringPair;
  
  import java.util.Enumeration;
  import java.util.Vector;
  import java.util.Hashtable;
  
}
/** Java 1.1 Recognizer Grammar
 *<pre>
 * Run 'java Main <directory full of java files>'
 *
 * Contributing authors:
 *		John Mitchell		johnm@non.net
 *		Terence Parr		parrt@magelang.com
 *		John Lilley			jlilley@empathy.com
 *		Scott Stanchfield	thetick@magelang.com
 *		Markus Mohnen       mohnen@informatik.rwth-aachen.de
 *		Peter Williams		pwilliams@netdynamics.com
 *
 * Version 1.00 December 9, 1997 -- initial release
 * Version 1.01 December 10, 1997
 *		fixed bug in octal def (0..7 not 0..8)
 * Version 1.10 August 1998 (parrt)
 *		added tree construction
 *		fixed definition of WS,comments for mac,pc,unix newlines
 *		added unary plus
 * Version 1.11 (Nov 20, 1998)
 *		Added "shutup" option to turn off last ambig warning.
 *		Fixed inner class def to allow named class defs as statements
 *		synchronized requires compound not simple statement
 *		add [] after builtInType DOT class in primaryExpression
 *		"const" is reserved but not valid..removed from modifiers
 *
 * Version 1.12 (Feb 2, 1999)
 *		Changed LITERAL_xxx to xxx in tree grammar.
 *		Updated java.g to use tokens {...} now for 2.6.0 (new feature).
 *
 * Version 1.13 (Apr 23, 1999)
 *		Didn't have (stat)? for else clause in tree parser.
 *		Didn't gen ASTs for interface extends.  Updated tree parser too.
 *		Updated to 2.6.0.
 * Version 1.14 (Jun 20, 1999)
 *		Allowed final/abstract on local classes.
 *		Removed local interfaces from methods
 *		Put instanceof precedence where it belongs...in relationalExpr
 *			It also had expr not type as arg; fixed it.
 *		Missing ! on SEMI in classBlock
 *		fixed: (expr) + "string" was parsed incorrectly (+ as unary plus).
 *		fixed: didn't like Object[].class in parser or tree parser
 *
 * This grammar is in the PUBLIC DOMAIN
 *
 * BUGS
 *</pre>

<p>This parser has been modified from the original Recognizer to a pre-parser
that implents assertions in java.</p>
**/
class JavaRecognizer extends Parser;
options {
  k=2;                           // k token lookahead
  exportVocab=Java;                // Call its vocabulary "Java"
  codeGenMakeSwitchThreshold = 2;  // Some optimizations
  codeGenBitsetTestThreshold = 3;
  defaultErrorHandler = false;     // Don't generate parser error handlers
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
  public void print(final String s) {
    System.out.println("Parser: " + s);
  }

  /**
     set the symbol table object to use.
  **/
  public void setSymtab(final Symtab symtab) {
    _symtab = symtab;
  }

  private Symtab _symtab;

  /**
     Get the symbol table object being used.
  **/
  public Symtab getSymtab() {
    return _symtab;
  }
  
  /**
     Given the id Token from the implements clause find the package the
     interface is defined in.  Parse the interface for assertions and add
     an AssertInterface object to the current class so we can check against
     it later.
  **/
  private void parseImplementedInterface(final Token t) {
    String interfaceName = t.getText();
    String packageName = "";
    if(interfaceName.indexOf('.') > 0) {
      //already qualified interface, break up into package and name
      int lastDot = interfaceName.lastIndexOf('.');
      packageName = interfaceName.substring(0, lastDot);
      interfaceName = interfaceName.substring(lastDot+1);
    }
    else {
      //Need to figure out the right package here, search through the
      //current package, _imports
    }

    //[jpschewe:20000103.0113CST] actually parse the file so we can check
    //against it later, if no file can be found then don't add it to the
    //class, otherwise add it to the current class with
    //getSymtab().getCurrentClass().addInterface(assertInterface)

  }
  
  /**
     add an assert.  This should get cached with the file object?
  **/
  private void addAsserts(final Vector asserts, final Token jdClose) {
    if(asserts != null && asserts.size() > 0) {
      int line = jdClose.getLine();
      int column = jdClose.getColumn() + jdClose.getText().length();
      
      StringBuffer codeFrag = new StringBuffer();
      Enumeration iter = asserts.elements();
      while(iter.hasMoreElements()) {
	AssertToken assertToken = (AssertToken)iter.nextElement();
	String code = CodeGenerator.generateAssertion(assertToken);
	codeFrag.append(code);
      }
      CodeFragment codeFragment = new CodeFragment(new CodePoint(line, column), codeFrag.toString(), CodeFragmentType.ASSERT);
      getSymtab().addCodeFragment(codeFragment);
    }
  }

  
  private Vector _invariants = new Vector();

  /**
     add invariants to this class

     @pre (invariant != null)
  **/
  private void addInvariant(final Token invariant) {
    if(! (invariant instanceof AssertToken)) {
      throw new RuntimeException("Expecting AssertToken! " + invariant.getClass());
    }
    _invariants.addElement(invariant);
  }

  /**
     get the invariants for this class.
  **/
  private Vector getInvariants() {
    return _invariants;
  }

  /**
     Clear the list of invariants for this class.
  **/
  private void clearInvariants() {
    _invariants = new Vector();
  }


  private Vector _preConditions = new Vector();
  /**
     Get the list of preconditions that have been seen since the last clear.
  **/
  public Vector getPreConditions() {
    return _preConditions;
  }

  /**
     Add a precondition to the list of preconditions.
  **/
  public void addPreCondition(final Token pre) {
    if(! (pre instanceof AssertToken)) {
      throw new RuntimeException("Expecting AssertToken! " + pre.getClass());
    }
    
    _preConditions.addElement(pre);
  }

  /**
     clear out the list of preconditions.
  **/
  public void clearPreConditions() {
    _preConditions = new Vector();
  }

  private Vector _postConditions = new Vector();
  /**
     Get the list of postconditions that have been seen since the last clear.
  **/
  public Vector getPostConditions() {
    return _postConditions;
  }

  /**
     clear out the list of postconditions.
  **/
  public void addPostCondition(final Token post) {
    if(! (post instanceof AssertToken)) {
      throw new RuntimeException("Expecting AssertToken! " + post.getClass());
    }
    _postConditions.addElement(post);
  }

  /**
     clear out the list of postconditions.
  **/
  public void clearPostConditions() {
    _postConditions = new Vector();
  }    

  /**
     Used to tell the parser where we are in the file.
  **/ 
  private short _parseSection;
}

// Compilation Unit: In Java, this is a single file.  This is the start
//   rule for this parser
compilationUnit
{
  String packageName = null;
  _parseSection = 0;
}
  :


    // A compilation unit starts with an optional package definition and
    // possibly some javadoc comments
    ( {_parseSection==0}? (invariantCondition)* packageName=packageDefinition )?
    {
      //Now we just need to check to make sure the destination file is older
      if(!getSymtab().isDestinationOlderThanCurrentFile(packageName)) {
	throw new FileAlreadyParsedException();
      }
      _parseSection = 1;
      clearInvariants();
    }

    // Next we have a series of zero or more import statements with
    // intermingled javadoc comments
    ( /* (invariantCondition)* */ importDefinition )*
    {
      clearInvariants();
    }

    // Wrapping things up with any number of class or interface definitions
    // with their corresponding invariants
     ( (invariantCondition)* typeDefinition )*

    EOF
  ;

invariantCondition
  : JAVADOC_OPEN ( iv:INVARIANT_CONDITION { addInvariant(iv); } | PRE_CONDITION | POST_CONDITION | ASSERT_CONDITION )* JAVADOC_CLOSE
  ;

// Package statement: "package" followed by an identifier.
packageDefinition returns [String packageName]
options {
  defaultErrorHandler = true; // let ANTLR handle errors
}
{
  Token id = null;
  packageName = null;
}
  :
    "package" id=identifier SEMI
    {
      packageName = id.getText();
      getSymtab().setCurrentPackageName(packageName);
    }
  ;


// Import statement: import followed by a package or class name
importDefinition
options {defaultErrorHandler = true;}
  :	"import" identifierStar SEMI
  ;

// A type definition in a file is either a class or interface definition.
typeDefinition
options {defaultErrorHandler = true;}
  :	modifiers
    ( classDefinition
    | interfaceDefinition
    )
  |	SEMI
  ;

/** A declaration is the creation of a reference or primitive-type variable
 *  Create a separate Type/Var tree for each var in the var list.
 */
declaration
  :	modifiers typeSpec variableDefinitions
  ;

// A list of zero or more modifiers.  We could have used (modifier)* in
//   place of a call to modifiers, but I thought it was a good idea to keep
//   this rule separate so they can easily be collected in a Vector if
//   someone so desires
/**
   @return the list of modifiers as Strings
**/
modifiers returns [Vector mods]
{
  mods = new Vector();
  Token mod = null;
}
  :	( mod = modifier { mods.addElement(mod.getText()); } )*
  ;


// A type specification is a type name with possible brackets afterwards
//   (which would make it an array type).
/**
   @return the Token that represents this type spec
**/
typeSpec returns [Token t]
  : t=classTypeSpec
  | t=builtInTypeSpec
  ;

// A class type specification is a class type with possible brackets afterwards
//   (which would make it an array type).
/**
   @return the Token that represents this class type spec
**/
classTypeSpec returns [Token id]
    :
	id=identifier (LBRACK RBRACK { id.setText(id.getText() + "[]"); } )*
  ;

// A builtin type specification is a builtin type with possible brackets
// afterwards (which would make it an array type).
/**
   @return the Token that represents this builtin type spec
**/
builtInTypeSpec returns [Token id]
  :	id=builtInType (LBRACK RBRACK { id.setText(id.getText() + "[]"); } )*
  ;

// A type name. which is either a (possibly qualified) class name or
//   a primitive (builtin) type
/**
   @return the Token that represents this type
**/
type returns [Token t]
  :	t=identifier
  |	t=builtInType
  ;

// The primitive types.
/**
   @return the Token that represents this builtin type
**/
builtInType returns [Token t]
{ t = null; }
  :	tvoid:"void" { t = tvoid; }
  |	tboolean:"boolean" { t = tboolean; }
  |	tbyte:"byte" { t = tbyte; }
  |	tchar:"char" { t = tchar; }
  |	tshort:"short" { t = tshort; }
  |	tint:"int" { t = tint; }
  |	tfloat:"float" { t = tfloat; }
  |	tlong:"long" { t = tlong; }
  |	tdouble:"double" { t = tdouble; }
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
	 // put the overall name in the token's text
	 if (packageName.equals("")) {
	   id.setText(className);
	 }
	 else if (className == null) {
	   id.setText(packageName.substring(1));
	 }
	 else {
	   id.setText(packageName.substring(1) + "." + className);
	 }

	 // tell the symbol table about the import
	 getSymtab().addImport(className, packageName);
    }
  ;


// modifiers for Java classes, interfaces, class/instance vars and methods
modifier returns [Token t]
{
  t = null;
}
  :	tprivate:"private" { t = tprivate; }
  |	tpublic:"public" { t = tpublic; }
  |	tprotected:"protected" { t = tprotected; }
  |	tstatic:"static" { t = tstatic; }
  |	ttransient:"transient" { t = ttransient; }
  |	tfinal:"final" { t = tfinal; }
  |	tabstract:"abstract" { t = tabstract; }
  |	tnative:"native" { t = tnative; }
  |	tthreadsafe:"threadsafe" { t = tthreadsafe; }
  |	tsynchronized:"synchronized" { t = tsynchronized; }
    //	|	tconst:"const"	{ t = tconst; }		// reserved word; leave out
  |	tvolatile:"volatile" { t = tvolatile; }
  ;


// Definition of a Java class
classDefinition
{
  String name = null;
}
  :	"class" id:IDENT
    {
      if(getSymtab().getCurrentClass() != null) {
	name = getSymtab().getCurrentClass().getName() + "$" + id.getText();
      }
      else {
	name = id.getText();
      }
    }
    // it _might_ have a superclass...
    superClassClause
    // it might implement some interfaces...
    implementsClause
    // now parse the body of the class
    classBlock[name, false]
  ;


superClassClause
{ Token id; }
  :	( "extends" id=identifier )?
  ;

// Definition of a Java Interface
interfaceDefinition
{
  String name = null;
}
  :	"interface" id:IDENT
    {
      if(getSymtab().getCurrentClass() != null) {
	name = getSymtab().getCurrentClass().getName() + "$" + id.getText();
      }
      else {
	name = id.getText();
      }
    }
    
    // it might extend some other interfaces
    interfaceExtends
    // now parse the body of the interface (looks like a class...)
    classBlock[name, true]
  ;


/**
   This is the body of a class or interface.  You can have fields and extra semicolons,
   That's about it (until you see what a field is...)
**/
classBlock [ String name, boolean isInterface ]
  :
    lc:LCURLY
    {
      getSymtab().startClass(name, getInvariants(), isInterface);
      clearInvariants();
    }
    //this should just be methods and constructors,
    //but can't find a better place for it.
    ( prePostField | SEMI )*
    rc:RCURLY
    {
      clearInvariants();
      getSymtab().finishClass(new CodePoint(rc.getLine(), rc.getColumn()));
    }
  ;

prePosts 
    :
    ( post:POST_CONDITION { addPostCondition(post); }
    | pre:PRE_CONDITION { addPreCondition(pre); }
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
  :	(
      "extends"
      id=identifier ( COMMA id2=identifier )*
    )?
  ;

// A class can implement several interfaces...
//[jpschewe:20000102.1414CST] FIX need stuff here
implementsClause
{ Token id, id2; }
  :	(
      "implements" id=identifier { parseImplementedInterface(id); }
      ( COMMA id2=identifier { parseImplementedInterface(id2); } )*
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
  Vector mods = null;
  Token retType = null;
  Vector params = null;
  boolean methodOrConstructor = false;
  CodePointPair startEnd = null;
}
  :

    (
    // method, constructor, or variable declaration
    //[jpschewe:20000215.2254CST] FIX need to do something special for abstract and native methods here
    mods=modifiers
    (	params=ctorHead
      {
	// needs to be before compoundStatement so that I can have it set for the addExit calls
	getSymtab().startMethod(null, getPreConditions(), getPostConditions(), params, null, mods);
	clearPreConditions();
	  clearPostConditions();
	//print("just called startMethod for constructor");
      }
      startEnd=compoundStatement // constructor
		{
		    getSymtab().finishMethod(startEnd);
		    //print("Found finish: " + methodName);
		}
 

    |	classDefinition       // inner class
      
    |	interfaceDefinition   // inner interface

    |	retType=typeSpec  // method or variable declaration(s)
      (	methodName:IDENT  // the name of the method

	// parse the formal parameter declarations.
	LPAREN params=parameterDeclarationList
	{
	  // needs to be before compoundStatement so that I can have it set for the addExit calls
	  getSymtab().startMethod(methodName.getText(), getPreConditions(), getPostConditions(), params, retType.getText(), mods);
	  //print("just called startMethod: " + methodName.getText());
	    clearPreConditions();
	    clearPostConditions();
	}
	RPAREN

	returnTypeBrackersOnEndOfMethodHead

	// get the list of exceptions that this method is declared to throw
	(throwsClause)?
	( startEnd=compoundStatement | semi:SEMI )
		    {
			if(startEnd != null) {
			    getSymtab().finishMethod(startEnd);
			}
			else {
			    //abstract, native or interface method
			    CodePoint close = new CodePoint(semi.getLine(), semi.getColumn());
			    getSymtab().finishMethod(new CodePointPair(close, close));
			}
			//print("Found finish: " + methodName);
		    }
      |	variableDefinitions SEMI
      )
    )

    // "static { ... }" class initializer
  |	"static" compoundStatement

    // "{ ... }" instance initializer
  |	compoundStatement
    )
  ;

variableDefinitions
  :	variableDeclarator
    (	COMMA
      variableDeclarator
    )*
  ;

/** Declaration of a variable.  This can be a class/instance variable,
 *   or a local variable in a method
 * It can also include possible initialization.
 */
variableDeclarator
  :	IDENT declaratorBrackets varInitializer
  ;

declaratorBrackets
  :
    (lb:LBRACK RBRACK)*
  ;

varInitializer
  :	( ASSIGN initializer )?
  ;

// This is an initializer used to set up an array.
arrayInitializer
  :	LCURLY
    (	initializer
      (
	// CONFLICT: does a COMMA after an initializer start a new
	//           initializer or start the option ',' at end?
	//           ANTLR generates proper code by matching
	//			 the comma as soon as possible.
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
  :	expression
  |	arrayInitializer
  ;

/**
 <p>This is the header of a method.  It includes the name and parameters for
   the method.  This also watches for a list of exception classes in a
   "throws" clause.</p>

   <p>[jpschewe:20000204.2127CST] this is only used for constructors so I'm
   just going to return the params, the method name is known</p>

   @return the parameters as StringPair(type, name)
**/
ctorHead returns [Vector params]
{
  params = null;
}
  :	IDENT  // the name of the method

    // parse the formal parameter declarations.
    LPAREN params=parameterDeclarationList RPAREN

    // get the list of exceptions that this method is declared to throw
    (throwsClause)?
  ;

// This is a list of exception classes that the method is declared to throw
throwsClause
{ Token id, id2; }
  :	"throws" id=identifier ( COMMA id2=identifier )*
  ;


returnTypeBrackersOnEndOfMethodHead
  :
    (LBRACK RBRACK)*
  ;

// A list of formal parameters
parameterDeclarationList returns [Vector params]
{
  params = new Vector();
  StringPair pd = null;
}
  :	( pd=parameterDeclaration { params.addElement(pd); } ( COMMA pd=parameterDeclaration { params.addElement(pd); } )* )?
  ;

// A formal parameter.
parameterDeclaration returns [StringPair sp]
{
  StringBuffer brackets = null;
  Token type;
  sp = null;
}
  :	parameterModifier type=typeSpec name:IDENT
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
  :	("final")?
  ;

// should be before all statements and most compoundStatements, but not all
// so this needs to be added to the places that call compoundStatement
assertOrInvariantCondition
{ Vector assertTokens = new Vector(); }
  : (JAVADOC_OPEN
    ( assert:ASSERT_CONDITION { assertTokens.addElement(assert); }
      | PRE_CONDITION
      | POST_CONDITION
      | iv:INVARIANT_CONDITION { addInvariant(iv); }
    )*
    jdc:JAVADOC_CLOSE 
    { addAsserts(assertTokens, jdc); }
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
      CodePoint start = new CodePoint(lc.getLine(), lc.getColumn());
      CodePoint end = new CodePoint(rc.getLine(), rc.getColumn());
      startEnd = new CodePointPair(start, end);
    }
  ;


statement

  :

    // A list of statements in curly braces -- start a new scope!    
    compoundStatement

	// class definition
    |	classDefinition

	// final class definition
    |	"final" classDefinition

	// abstract class definition
    |	"abstract" classDefinition

	// declarations are ambiguous with "ID DOT" relative to expression
	// statements.  Must backtrack to be sure.  Could use a semantic
	// predicate to test symbol table to see what the type was coming
	// up, but that's pretty hard without a symbol table ;)
    |	(declaration)=> declaration SEMI

	// An expression statement.  This could be a method call,
	// assignment statement, or any other expression evaluated for
	// side-effects.
    |	expression SEMI

	// Attach a label to the front of a statement
    |	IDENT COLON statement

	// If-else statement
    |	"if" LPAREN expression RPAREN statement
	(
	    // CONFLICT: the old "dangling-else" problem...
	    //           ANTLR generates proper code matching
	    //			 as soon as possible.  Hush warning.
	    options {
		warnWhenFollowAmbig = false;
	    }
	:
	    "else" statement
	)?

	// For statement
    |	"for"
	LPAREN
	forInit SEMI   // initializer
	forCond	SEMI   // condition test
	forIter         // updater
	RPAREN
	statement                     // statement to loop over

	// While statement
    |	"while" LPAREN expression RPAREN statement

	// do-while statement
    |	"do" statement "while" LPAREN expression RPAREN SEMI

	// get out of a loop (or switch)
    |	"break" (IDENT)? SEMI

	// do next iteration of a loop
    |	"continue" (IDENT)? SEMI

	// Return an expression
    |	ret:"return" (expression)? semi:SEMI
      {
	//[jpschewe:20000216.0717CST] keep track of these points for post conditions
	CodePoint retcp = new CodePoint(ret.getLine(), ret.getColumn());
	//[jpschewe:20000216.2231CST] add 1 so that code is inserted after the semi colon
	CodePoint semicp = new CodePoint(semi.getLine(), semi.getColumn()+1);
	getSymtab().getCurrentMethod().addExit(new CodePointPair(retcp, semicp));
      }

	// switch/case statement
    |	"switch" LPAREN expression RPAREN LCURLY
	( casesGroup )*
	RCURLY

	// exception try-catch block
    |	tryBlock

	// throw an exception
    |	"throw" expression SEMI

	// synchronize a statement
    |	"synchronized" LPAREN expression RPAREN compoundStatement

	// empty statement
    |	SEMI
      
      //assertion, checks invariants too for the class definitions allowed in statement
    |   assertOrInvariantCondition
    ;


casesGroup
  :	(	// CONFLICT: to which case group do the statements bind?
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
  :	("case" expression | "default") COLON
  ;

caseSList
  :	(statement)*
  ;

// The initializer for a for loop
forInit
// if it looks like a declaration, it is
  :	(	(declaration)=> declaration
      // otherwise it could be an expression list...
    |	expressionList
    )?
  ;

forCond
  :	(expression)?
  ;

forIter
  :	(expressionList)?
  ;

// an exception handler try/catch block
tryBlock
    :	"try" compoundStatement
    (handler)*
    ( "finally" compoundStatement )?
  ;


// an exception handler
handler
  :	"catch" LPAREN parameterDeclaration RPAREN compoundStatement
  ;


/**
   the mother of all expressions

<pre>
// expressions
// Note that most of these expressions follow the pattern
//   thisLevelExpression :
//       nextHigherPrecedenceExpression
//           (OPERATOR nextHigherPrecedenceExpression)*
// which is a standard recursive definition for a parsing an expression.
// The operators in java have the following precedences:
//    lowest  (13)  = *= /= %= += -= <<= >>= >>>= &= ^= |=
//            (12)  ?:
//            (11)  ||
//            (10)  &&
//            ( 9)  |
//            ( 8)  ^
//            ( 7)  &
//            ( 6)  == !=
//            ( 5)  < <= > >=
//            ( 4)  << >>
//            ( 3)  +(binary) -(binary)
//            ( 2)  * / %
//            ( 1)  ++ -- +(unary) -(unary)  ~  !  (type)
//                  []   () (method call)  . (dot -- identifier qualification)
//                  new   ()  (explicit parenthesis)
//
// the last two are not usually on a precedence chart; I put them in
// to point out that new has a higher precedence than '.', so you
// can validy use
//     new Frame().show()
// 
// Note that the above precedence levels map to the rules below...
// Once you have a precedence chart, writing the appropriate rules as below
//   is usually very straightfoward
</pre>



**/
expression
  :	assignmentExpression
  ;


/**
   This is a list of expressions.
**/
expressionList
  :	expression (COMMA expression)*
  ;


// assignment expression (level 13)
assignmentExpression
  :	conditionalExpression
    (	(	ASSIGN
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
  :	logicalOrExpression
    ( QUESTION conditionalExpression COLON conditionalExpression )?
  ;


/**
   logical or (||)  (level 11)

**/
logicalOrExpression
  :	logicalAndExpression (LOR logicalAndExpression)*
  ;


/**
   logical and (&&)  (level 10)

**/
logicalAndExpression
  :	inclusiveOrExpression (LAND inclusiveOrExpression)*
  ;


/**
   bitwise or non-short-circuiting or (|)  (level 9)

**/
inclusiveOrExpression
  :	exclusiveOrExpression (BOR exclusiveOrExpression)*
  ;


/**
   exclusive or (^)  (level 8)
**/
exclusiveOrExpression
  :	andExpression (BXOR andExpression)*
  ;


/**
   bitwise or non-short-circuiting and (&)  (level 7)

**/
andExpression
  :	equalityExpression (BAND equalityExpression)*
  ;


/**
   equality/inequality (==/!=) (level 6)

**/
equalityExpression
  :	relationalExpression ((NOT_EQUAL | EQUAL) relationalExpression)*
  ;


/**
   boolean relational expressions (level 5)

**/
relationalExpression
  :	shiftExpression
    (
      (
	(LT
	|	GT
	|	LE
	|	GE
	)
	shiftExpression
      )*
    |	"instanceof" typeSpec
    )
  ;


/**
   bit shift expressions (level 4)

**/
shiftExpression
  :	additiveExpression ((SL | SR | BSR) additiveExpression)*
  ;


/**
   binary addition/subtraction (level 3)
**/
additiveExpression
  :	multiplicativeExpression ((PLUS | MINUS) multiplicativeExpression)*
  ;


/**
   multiplication/division/modulo (level 2)

**/
multiplicativeExpression
  :	unaryExpression ((STAR | DIV | MOD ) unaryExpression)*
  ;

unaryExpression
  :	INC unaryExpression
  |	DEC unaryExpression
  |	MINUS unaryExpression
  |	PLUS  unaryExpression
  |	unaryExpressionNotPlusMinus
  ;

unaryExpressionNotPlusMinus
  :	BNOT unaryExpression
  |	LNOT unaryExpression

  |	(	// subrule allows option to shut off warnings
      options {
	// "(int" ambig with postfixExpr due to lack of sequence
	// info in linear approximate LL(k).  It's ok.  Shut up.
	generateAmbigWarnings=false;
      }
    :	// If typecast is built in type, must be numeric operand
      // Also, no reason to backtrack if type keyword like int, float...
      LPAREN builtInTypeSpec RPAREN
      unaryExpression

      // Have to backtrack to see if operator follows.  If no operator
      // follows, it's a typecast.  No semantic checking needed to parse.
      // if it _looks_ like a cast, it _is_ a cast; else it's a "(expr)"
    |	(LPAREN classTypeSpec RPAREN unaryExpressionNotPlusMinus)=>
      LPAREN classTypeSpec RPAREN
      unaryExpressionNotPlusMinus

    |	postfixExpression
    )
  ;

/**
   qualified names, array expressions, method invocation, post inc/dec

**/
postfixExpression
{ Token id; }
  :	primaryExpression // start with a primary

    (	// qualified id (id.id.id.id...) -- build the name
      DOT ( IDENT
      | "this"
      | "class"
      | newExpression
      | "super" LPAREN ( expressionList )? RPAREN
      )
      // the above line needs a semantic check to make sure "class"
      //   is the _last_ qualifier.

      // allow ClassName[].class
    |	( LBRACK RBRACK )+
      DOT "class"

      // an array indexing operation
    |	LBRACK expression RBRACK

      // method invocation
      // The next line is not strictly proper; it allows x(3)(4) or
      //  x[2](4) which are not valid in Java.  If this grammar were used
      //  to validate a Java program a semantic check would be needed, or
      //   this rule would get really ugly...
    |	LPAREN
      argList
      RPAREN
    )*

    // possibly add on a post-increment or post-decrement.
    // allows INC/DEC on too much, but semantics can check
    (	INC
    |	DEC
    |	// nothing
    )

    // look for int.class and int[].class
  |	id=builtInType 
    ( LBRACK RBRACK )*
    DOT "class"
  ;

/**
   the basic element of an expression
**/
primaryExpression
  :	IDENT
  |	newExpression
  |	constant
  |	"super"
  |	"true"
  |	"false"
  |	"this"
  |	"null"
  |	LPAREN assignmentExpression RPAREN
  ;

/**
   object instantiation.
**/
newExpression
{ Token t; }
  :	"new" t=type
    (	LPAREN argList RPAREN ( classBlock[getSymtab().getCurrentClass().createAnonymousClassName(), false] )?

      //[jpschewe:20000128.0740CST] FIX need to use t to figure out what
      //interfaces we need to check conditions on.
	    
      //java 1.1
      // Note: This will allow bad constructs like
      //    new int[4][][3] {exp,exp}.
      //    There needs to be a semantic check here...
      // to make sure:
      //   a) [ expr ] and [ ] are not mixed
      //   b) [ expr ] and an init are not used together

    |	newArrayDeclarator (arrayInitializer)?
    )
  ;

argList
  :	(	expressionList
    |	/*nothing*/
    )
  ;

newArrayDeclarator
  :	(
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
  :	NUM_INT
  |	CHAR_LITERAL
  |	STRING_LITERAL
  |	NUM_FLOAT
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

{
    // for column tracking
  protected int tokColumn = 0;
  protected int column = 0;
  public void consume() throws IOException {
    if(text.length()==0) {
      // remember the token start column
      tokColumn = column;
    }
//    if(LA(1) == '\n' || LA(1) == '\r') {
//	column = 0;
//    }
    column++;
//	  if(inputState.guessing > 0) {
//	      if(text.length() == 0) {
//		  // remember token start column
//		  tokColumn = column;
//	      }
//	      if (LA(1) == '\n') {
//		  column = -1;
//	      }
//	      else {
//		  column++;
//	      }
//	  }
    super.consume();
  }

  public void newline() {
    column = 0;
    super.newline();
  }
  
  protected Token makeToken(int t) {
    Token tok = super.makeToken(t);
    tok.setColumn(tokColumn);
    return tok;
  }
  
}




// OPERATORS
QUESTION		:	'?'		;
LPAREN			:	'('		;
RPAREN			:	')'		;
LBRACK			:	'['		;
RBRACK			:	']'		;
LCURLY			:	'{'		;
RCURLY			:	'}'		;
COLON			:	':'		;
COMMA			:	','		;
//DOT			:	'.'		;
ASSIGN			:	'='		;
EQUAL			:	"=="	;
LNOT			:	'!'		;
BNOT			:	'~'		;
NOT_EQUAL		:	"!="	;
DIV				:	'/'		;
DIV_ASSIGN		:	"/="	;
PLUS			:	'+'		;
PLUS_ASSIGN		:	"+="	;
INC				:	"++"	;
MINUS			:	'-'		;
MINUS_ASSIGN	:	"-="	;
DEC				:	"--"	;
STAR			:	'*'		;
STAR_ASSIGN		:	"*="	;
MOD				:	'%'		;
MOD_ASSIGN		:	"%="	;
SR				:	">>"	;
SR_ASSIGN		:	">>="	;
BSR				:	">>>"	;
BSR_ASSIGN		:	">>>="	;
GE				:	">="	;
GT				:	">"		;
SL				:	"<<"	;
SL_ASSIGN		:	"<<="	;
LE				:	"<="	;
LT				:	'<'		;
BXOR			:	'^'		;
BXOR_ASSIGN		:	"^="	;
BOR				:	'|'		;
BOR_ASSIGN		:	"|="	;
LOR				:	"||"	;
BAND			:	'&'		;
BAND_ASSIGN		:	"&="	;
LAND			:	"&&"	;
SEMI			:	';'		;


// Whitespace -- ignored
WS	:	(	' '
    |	'\t'
    |	'\f'
      // handle newlines
	|	'\r' '\n'		{newline();}
	|	'\r'			{newline();}
	|	'\n'			{newline();}
    )
    { _ttype = Token.SKIP; }
  ;

// Single-line comments
SL_COMMENT
  :	"//"
    (~('\n'|'\r'))* (
	|	'\r' '\n'		{newline();}
	|	'\r'			{newline();}
	|	'\n'			{newline();}
	)	    
    {$setType(Token.SKIP); }
  ;

// Javadoc comments
JAVADOC_OPEN
  : "/**"
    {
      //System.out.println("java: got start of javadoc comment #" + text + "#");
      Main.selector.push(Main.assertLexer);
    }
  ;

//JD_COMMENT
//	    :	"/**"
//		    (	options {
//				    generateAmbigWarnings=false;
//			    }
//		    :
//			  { LA(2)!='/' }? '*'
//		    |	'\r' '\n'		{newline();}
//		    |	'\r'			{newline();}
//		    |	'\n'			{newline();}
//		    |	~('*'|'\n'|'\r')
//		    )*
//		    "*/"
//		  {
//			  $setType(Token.SKIP);			
//			  System.out.println("Got a javadoc comment #" + text + "#");
//		  }
//
//	    ;

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
	|	'\r' '\n'		{newline();}
	|	'\r'			{newline();}
	|	'\n'			{newline();}
	)
	(	/*	'\r' '\n' can be matched in one alternative or by matching
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
	|	'\r' '\n'		{newline();}
	|	'\r'			{newline();}
	|	'\n'			{newline();}
    |	~('*'|'\n'|'\r')
    )*
    "*/"
    {$setType(Token.SKIP);}
  ;

// character literals
CHAR_LITERAL
  :	'\'' ( ESC | ~'\'' ) '\''
  ;

// string literals
STRING_LITERAL
  :	'"' (ESC|~('"'|'\\'))* '"'
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
  ;


// hexadecimal digit (again, note it's protected!)
protected
HEX_DIGIT
  :	('0'..'9'|'A'..'F'|'a'..'f')
  ;


// a dummy rule to force vocabulary to be all characters (except special
//   ones that ANTLR uses internally (0 to 2)
protected
VOCAB
  :	'\3'..'\377'
  ;


// an identifier.  Note that testLiterals is set to true!  This means
// that after we match the rule, we look in the literals table to see
// if it's a literal or really an identifer
IDENT
options {testLiterals=true;}
  :	('a'..'z'|'A'..'Z'|'_'|'$') ('a'..'z'|'A'..'Z'|'_'|'0'..'9'|'$')*
  ;


// a numeric literal
NUM_INT
{boolean isDecimal=false;}
  :	'.' {_ttype = DOT;}
    (('0'..'9')+ (EXPONENT)? (FLOAT_SUFFIX)? { _ttype = NUM_FLOAT; })?
  |	(	'0' {isDecimal = true;} // special case for just '0'
      (	('x'|'X')
	(											// hex
	  // the 'e'|'E' and float suffix stuff look
	  // like hex digits, hence the (...)+ doesn't
	  // know when to stop: ambig.  ANTLR resolves
	  // it correctly by matching immediately.  It
	  // is therefor ok to hush warning.
	  options {
	    warnWhenFollowAmbig=false;
	  }
	:	HEX_DIGIT
	)+
      |	('0'..'7')+									// octal
      )?
    |	('1'..'9') ('0'..'9')*  {isDecimal=true;}		// non-zero decimal
    )
    (	('l'|'L')
      
      // only check to see if it's a float if looks like decimal so far
    |	{isDecimal}?
      (	'.' ('0'..'9')* (EXPONENT)? (FLOAT_SUFFIX)?
      |	EXPONENT (FLOAT_SUFFIX)?
      |	FLOAT_SUFFIX
      )
      { _ttype = NUM_FLOAT; }
    )?
  ;


// a couple protected methods to assist in matching floating point numbers
protected
EXPONENT
  :	('e'|'E') ('+'|'-')? ('0'..'9')+
  ;


protected
FLOAT_SUFFIX
  :	'f'|'F'|'d'|'D'
  ;

