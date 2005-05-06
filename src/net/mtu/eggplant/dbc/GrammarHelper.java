/*
 Copyright (C) 2005
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper methods for the grammar.
 */
/*package*/ class GrammarHelper {
  
  private static final Log LOG = LogFactory.getLog(GrammarHelper.class);
  
  public GrammarHelper(final Symtab symtab) {
    _symtab = symtab;
  }

  private final Symtab _symtab;
  public Symtab getSymtab() {
    return _symtab;
  }
  
  /**
   * Called when a return statement is found.
   */
  public void handleReturn(final Token returnStatement,
                    final Token semiColon) {
    //keep track of these points for post conditions
    final CodePoint retcp = new CodePoint(returnStatement.getLine(), returnStatement.getColumn()-1);
    //add 1 so that code is inserted after the semi colon
    final CodePoint semicp = new CodePoint(semiColon.getLine(), semiColon.getColumn());
    getSymtab().getCurrentMethod().addExit(new CodePointPair(retcp, semicp));
  }

  /**
     Given the id Token from the implements clause find the package the
     interface is defined in.  Parse the interface for assertions and add
     an AssertInterface object to the current class so we can check against
     it later.
  **/
  public void parseImplementedInterface(final Token t) {
    String interfaceName = t.getText();
    String packageName = "";
    if(interfaceName.indexOf('.') > 0) {
      //already qualified interface, break up into package and name
      int lastDot = interfaceName.lastIndexOf('.');
      packageName = interfaceName.substring(0, lastDot);
      interfaceName = interfaceName.substring(lastDot+1);
    } else {
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
  public void addAsserts(final List asserts, final Token jdClose) {
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
  public void addInvariant(final Token invariant) {
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
  public void clearInvariants() {
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

  /**
   * Action for the package rule
   */
  public void handlePackage(final String packageName) {
    getSymtab().setCurrentPackageName(packageName);
    if(LOG.isDebugEnabled()) {
      LOG.debug("just found the package: " + packageName);
    }
    //Now we just need to check to make sure the destination file is older
    if(!getSymtab().getConfiguration().ignoreTimeStamp()) {
      if(!getSymtab().isDestinationOlderThanCurrentFile(packageName)) {
        // already parsed this file and force is off, skip it
        throw new FileAlreadyParsedException();
      }
    }

    //Put the print here so you don't see it unless we're really parsing the files
    if(getSymtab().getConfiguration().isVerbose()) {
      System.out.println("  " + getSymtab().getCurrentFile().getFile().getAbsolutePath());
    }
  }

  public void startClass(final String name,
                         final boolean isInterface,
                         final boolean isAnonymous,
                         final String superclass) {
    getSymtab().startClass(name, getInvariants(), isInterface, isAnonymous, superclass);
    clearInvariants();
  }
  
  public void finishClass(final Token rcurly) {
    clearInvariants();
    getSymtab().finishClass(new CodePoint(rcurly.getLine(), rcurly.getColumn()-1));
  }

  public void startMethod(final String methodName,
                          final List params,
                          final String returnType,
                          final Set mods) {
    getSymtab().startMethod(methodName, getPreConditions(), getPostConditions(), params, returnType, mods);
    clearPreConditions();
    clearPostConditions();
  }

}
