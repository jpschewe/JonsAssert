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
import net.mtu.eggplant.util.Debug;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
}

/**
  * Parser for 1.4, adds assert keyword.  This file is generated by
  * antlr.
  */
class Java14Recognizer extends JavaRecognizer;
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
