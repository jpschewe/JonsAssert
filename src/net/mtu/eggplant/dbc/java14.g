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
  * Parser for 1.4, adds assert keyword.  This file is generated by
  * antlr.
  *
  * @version $Revision: 1.5 $
  */
class Java14Recognizer extends JavaRecognizer;

{

  private static final Log LOG = LogFactory.getLog(Java14Recognizer.class);

// Define some helper methods, JPS copied in from java.g because antlr is being stupid

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

    //actually parse the file so we can check
    //against it later, if no file can be found then don't add it to the
    //class, otherwise add it to the current class with
    //getSymtab().getCurrentClass().addInterface(assertInterface)

  }
  
  /**
     add an assert.  This should get cached with the file object?
     @param asserts asserts, ordered as they appear in the code. 
  **/
  private void addAsserts(final List asserts, final Token jdClose) {
    if(asserts != null && asserts.size() > 0) {
      int line = jdClose.getLine();
      int column = jdClose.getColumn()-1 + jdClose.getText().length();
      //System.out.println("adding assert"
      //  + " line: " + line
      //  + " column: " + column
      //  );
      StringBuffer codeFrag = new StringBuffer();
      Iterator iter = asserts.iterator();
      while(iter.hasNext()) {
        AssertToken assertToken = (AssertToken)iter.next();
        String code = CodeGenerator.generateAssertion(assertToken);
        codeFrag.append(code);
      }
      CodeFragment codeFragment = new CodeFragment(new CodePoint(line, column), codeFrag.toString(), CodeFragmentType.ASSERT);
      getSymtab().addCodeFragment(codeFragment);
    }
  }

  
  private List _invariants = new LinkedList();

  /**
     add invariants to this class

     @pre (invariant != null)
  **/
  private void addInvariant(final Token invariant) {
    if(! (invariant instanceof AssertToken)) {
      throw new RuntimeException("Expecting AssertToken! " + invariant.getClass());
    }
    _invariants.add(invariant);
  }

  /**
     get the invariants for this class.
     Invariants are in the order they appear in the source.
  **/
  private List getInvariants() {
    return _invariants;
  }

  /**
     Clear the list of invariants for this class.
  **/
  private void clearInvariants() {
    _invariants = new LinkedList();
  }


  private List _preConditions = new LinkedList();
  /**
     Get the list of preconditions that have been seen since the last clear.
     Preconditions are in the order they appear in the source.
  **/
  public List getPreConditions() {
    return _preConditions;
  }

  /**
     Add a precondition to the list of preconditions.
  **/
  public void addPreCondition(final Token pre) {
    if(! (pre instanceof AssertToken)) {
      throw new RuntimeException("Expecting AssertToken! " + pre.getClass());
    }
    _preConditions.add(pre);
  }

  /**
     clear out the list of preconditions.
  **/
  public void clearPreConditions() {
    _preConditions = new LinkedList();
  }

  private List _postConditions = new LinkedList();
  /**
     Get the list of postconditions that have been seen since the last clear.
     Postconditions are in the order they appear in the source.
  **/
  public List getPostConditions() {
    return _postConditions;
  }

  /**
     clear out the list of postconditions.
  **/
  public void addPostCondition(final Token post) {
    if(! (post instanceof AssertToken)) {
      throw new RuntimeException("Expecting AssertToken! " + post.getClass());
    }
    _postConditions.add(post);
  }

  /**
     clear out the list of postconditions.
  **/
  public void clearPostConditions() {
    _postConditions = new LinkedList();
  }    

}

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
    getSymtab().getCurrentMethod().addExit(new CodePointPair(retcp, semicp));
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
