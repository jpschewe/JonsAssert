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
import net.mtu.eggplant.util.StringUtils;
import net.mtu.eggplant.util.Debug;

import java.util.List;
import java.util.Iterator;

/**
 * @version $Revision: 1.5 $
 */
public class CodeGenerator {

  //some handy constants
  private static final String ASSERT_TOOLS_CLASSNAME = AssertTools.class.getName();
  private static final String ASSERTION_VIOLATION_CLASSNAME = AssertionViolation.class.getName();
  
  /**
   * This should be inserted right after the close of the javadoc comment.
   *  
   *  @param tok the token that represents the assertion
   *  @return a String of code that checks this assert condition
   *
   *  @pre (tok != null)
   */
  static public String generateAssertion(final AssertToken tok) {
    final String condition = tok.getCondition();
    final String message = tok.getMessage();

    final StringBuffer errorMessage = new StringBuffer();
    if(message != null) {
      errorMessage.append(message).append(" + ");
    }
    errorMessage.append("\" ").append(condition).append("\"");

    final StringBuffer code = new StringBuffer();
    carriageReturn(code);
    code.append("{");
    carriageReturn(code);
    code.append("if(" + ASSERT_TOOLS_CLASSNAME + ".ENFORCE_ASSERT_CONDITION && ! ");
    code.append(condition);
    code.append(") { ");
    carriageReturn(code);
    code.append(ASSERT_TOOLS_CLASSNAME + ".assertFailed(new " + ASSERTION_VIOLATION_CLASSNAME + "(");
    code.append(errorMessage.toString());
    carriageReturn(code);
    code.append("));");
    carriageReturn(code);
    code.append(" }");
    carriageReturn(code);
    code.append("}");
    carriageReturn(code);
    
    return code.toString();
  }


  /**
   * This should be added at the start and end of all instance methods, except
   * for private ones and should be at the end of constructors that aren't
   * private.
   *  
   *  @return a string of code that will call the checkInvariant method
   */
  static public String generateInvariantCall(final AssertClass aClass) {
    String mclassName = aClass.getFullName().replace('.', '_');
    mclassName = mclassName.replace('$', '_');
    final StringBuffer code = new StringBuffer();
    carriageReturn(code);
    code.append("if(" + ASSERT_TOOLS_CLASSNAME + ".ENFORCE_INVARIANT_CONDITION && !jps__");
    code.append(mclassName);
    code.append("_checkInvariant()) {");
    carriageReturn(code);
    code.append(ASSERT_TOOLS_CLASSNAME + ".invariantFailed(" + ASSERT_TOOLS_CLASSNAME + ".getCurrentAssertionViolation());");
    carriageReturn(code);
    code.append("}");
    carriageReturn(code);
    
    return code.toString();
  }

  /**
   * This should be inserted right at the end of a class, just before the
   *  closing curly.
   *  
   *  @param assertClass the class to generate the invariant check for
   *  @return a string of code that actually checks the invariant conditions
   *
   *  @pre (assertClass != null)
   */
  static public String generateInvariantMethod(final AssertClass assertClass) {
    final String className = assertClass.getFullName();
    String mclassName = className.replace('.', '_');
    mclassName = mclassName.replace('$', '_');
    final String methodName = "jps__" + mclassName + "_checkInvariant";
    final String methodVariableName = "_" + methodName;
    String methodKey = methodName; //for lock and unlock method calls
    
    final StringBuffer code = new StringBuffer();
    
    //create a variable to hold the super method
    carriageReturn(code);
    code.append("private transient java.lang.reflect.Method ");
    code.append(methodVariableName);
    code.append(';');
    carriageReturn(code);
                
    code.append("final protected boolean ");
    code.append(methodName);
    code.append("() {");
    carriageReturn(code);

    code.append("if(!" + ASSERT_TOOLS_CLASSNAME + ".ENFORCE_INVARIANT_CONDITION) { return true; }");
    carriageReturn(code);
    
    //check for recursive calls
    code.append("final " + ASSERT_TOOLS_CLASSNAME + ".MethodLock jps__methodLock = " + ASSERT_TOOLS_CLASSNAME + ".lockMethod(\"" + methodKey + "\", this);");
    carriageReturn(code);
    code.append("if(null == jps__methodLock) { return true; }");
    carriageReturn(code);

    code.append("Object jps__retval = null;");
    carriageReturn(code);
    code.append("if(" + ASSERT_TOOLS_CLASSNAME + ".ENFORCE_INHERITED_CONDITIONS) {");
    carriageReturn(code);
      
    //Get the class object
    code.append("final String jps_className = \"");
    code.append(className);
    code.append("\";");
    carriageReturn(code);
    code.append("final Class jps_thisClass = " + ASSERT_TOOLS_CLASSNAME + ".classForName(jps_className);");
    carriageReturn(code);
    code.append("if(jps_thisClass == null) {");
    carriageReturn(code);
    code.append(ASSERT_TOOLS_CLASSNAME + ".internalError(\"Could not find class \" + jps_className);");
    carriageReturn(code);
    code.append("}");
    carriageReturn(code);
    
    //find the super method
    code.append("if(");
    code.append(methodVariableName);
    code.append(" == null) {");
    carriageReturn(code);
    //create the argument list
    code.append("final Class[] jps_methodArgs = new Class[0];");
    carriageReturn(code);
    code.append(methodVariableName);
    code.append(" = " + ASSERT_TOOLS_CLASSNAME + ".findSuperMethod(jps_thisClass, \"checkInvariant\", jps_methodArgs);");
    carriageReturn(code);

    code.append("if(");
    code.append(methodVariableName);
    code.append(" == null) {");
    carriageReturn(code);
    code.append(methodVariableName);
    code.append(" = " + ASSERT_TOOLS_CLASSNAME + ".NO_METHOD;");
    carriageReturn(code);
    code.append("}");
    carriageReturn(code);
    code.append("}");
    carriageReturn(code);

    code.append("if(");
    code.append(methodVariableName);
    code.append(" != " + ASSERT_TOOLS_CLASSNAME + ".NO_METHOD) {");
    carriageReturn(code);
    
    //invoke it, pass on exceptions
    code.append("final Object[] jps_args = new Object[0];");
    carriageReturn(code);
    code.append("try {");
    carriageReturn(code);
    code.append("jps__retval = ");
    code.append(methodVariableName);
    code.append(".invoke(this, jps_args);");
    carriageReturn(code);
    code.append("} catch(IllegalAccessException jps_iae) {");
    carriageReturn(code);
    //just means that the super method is private and we really shouldn't be calling it in the first place          
    //code.append(ASSERT_TOOLS_CLASSNAME + ".internalError(\"Not enough access executing checkInvariant method on super class: \" + jps_iae.getMessage());");
    //Pretend it returned true, which is just like not calling it
    code.append("jps__retval = Boolean.TRUE;");
    carriageReturn(code);
    code.append("} catch(IllegalArgumentException jps_iae) {");
    carriageReturn(code);
    code.append(ASSERT_TOOLS_CLASSNAME + ".internalError(\"IllegalArgument executing checkInvariant method on super class: \" + jps_iae.getMessage());");
    carriageReturn(code);
    code.append("} catch(java.lang.reflect.InvocationTargetException jps_ite) {");
    carriageReturn(code);
    code.append("jps_ite.getTargetException().printStackTrace();");
    carriageReturn(code);
    code.append("}");
    carriageReturn(code);
    
    code.append("if(jps__retval == null) {");
    carriageReturn(code);
    code.append(ASSERT_TOOLS_CLASSNAME + ".internalError(\"got null from checkInvariant\");");
    carriageReturn(code);
    code.append("} else if(! (jps__retval instanceof Boolean) ) {");
    carriageReturn(code);
    code.append(ASSERT_TOOLS_CLASSNAME + ".internalError(\"got something odd from checkInvariant: \" + jps__retval.getClass());");
    carriageReturn(code);
    code.append("}");
    carriageReturn(code);

    code.append("if(!((Boolean)jps__retval).booleanValue()) {");
    carriageReturn(code);
    code.append(ASSERT_TOOLS_CLASSNAME + ".unlockMethod(jps__methodLock);");
    carriageReturn(code);
    code.append("return false;");
    carriageReturn(code);
    code.append("}");
    carriageReturn(code);
    code.append("}"); //end if(superMethod != AssertTools.NO_METHOD)
    carriageReturn(code);

  
    //[jpschewe:20000116.1749CST] FIX still need to add to this to do interface
    //invariants first and keep track of which interface they're from

    code.append("}"); //end enforce inherited conditions
    carriageReturn(code);
                
    addConditionChecks(code, assertClass.getInvariants(), false);

    code.append(ASSERT_TOOLS_CLASSNAME + ".unlockMethod(jps__methodLock);");
    carriageReturn(code);

    code.append("return true;"); //nothing failed, so just return
    carriageReturn(code);
    code.append("}");
    carriageReturn(code);

    return code.toString();
    
  }

  /**
     This code should be inserted right at the start of the method
     
     @param assertMethod the method to generate the precondition call for
     @return the code for a call to check the pre conditions for the given method

     @pre (assertMethod != null)
  **/
  static public String generatePreConditionCall(final AssertMethod assertMethod) {
    final StringBuffer code = new StringBuffer();
    carriageReturn(code);
    String mclassName = assertMethod.getContainingClass().getFullName().replace('.', '_');
    mclassName = mclassName.replace('$', '_');
    
    code.append("if(" + ASSERT_TOOLS_CLASSNAME + ".ENFORCE_PRE_CONDITION && !jps__");
    code.append(mclassName);
    code.append("_check");
    code.append(assertMethod.getName());
    code.append("PreConditions(");

    //put params in here once
    {
      boolean first = true;
      final Iterator paramIter = assertMethod.getParams().iterator();
      while(paramIter.hasNext()) {
        if(!first) {
          code.append(", ");
        } else {
          first = false;
        }
        final StringPair sp = (StringPair)paramIter.next();
        final String paramName = sp.getStringTwo();
        code.append(paramName);
      }
    }
    
    code.append(")) {");
    carriageReturn(code);
    code.append(ASSERT_TOOLS_CLASSNAME + ".preConditionFailed(" + ASSERT_TOOLS_CLASSNAME + ".getCurrentAssertionViolation());");
    carriageReturn(code);
    code.append("}");
    carriageReturn(code);
    
    return code.toString();
  }

  /**
     Generate the calls to pre, post and invariant conditions for a
     constructor.  Constructors are different enough to need they're own
     generator.
     
     @pre (assertMethod.isConstructor())
  **/
  static public String generateConstructorAssertions(final AssertMethod assertMethod) {
    final StringBuffer code = new StringBuffer();
    carriageReturn(code);
    final String dummyClassName = "JPS_" + assertMethod.getContainingClass().createDummyConstructorClassName();
    final List uniqueParams = assertMethod.getUniqueParams();

    /*
      insert:
      this(param0, param1, true, ..., new JPS_AssertDummy#(param0, param1)); //getUniqueParams()
      checkInvariant(); //standard
      checkPostConditions(param0, param0, param1, param1); //standard
      }
      final static private class JPS_AssertDummy# {
      public JPS_AssertDummy#(param0, param1) {
      checkPreConditions(param0, param1); //standard
      }
      }
      private className(param0, param1, boolean, ..., JPS_AssertDummy#) { //getUniqueParams()
    */

    //generate this call
    code.append("this(");
    //put unique param names in here once
    {
      boolean first = true;
      final Iterator paramIter = assertMethod.getUniqueParams().iterator();
      while(paramIter.hasNext()) {
        if(!first) {
          code.append(", ");
        } else {
          first = false;
        }
        final StringPair sp = (StringPair)paramIter.next();
        final String paramName = sp.getStringTwo();
        code.append(paramName);
      }
    }

    //call to dummy class
    if(!assertMethod.getParams().isEmpty()) {
      code.append(",");
    }
    code.append(" new ");
    code.append(dummyClassName);
    code.append("(");

    //put param names in here once
    {
      boolean first = true;
      final Iterator paramIter = assertMethod.getParams().iterator();
      while(paramIter.hasNext()) {
        if(!first) {
          code.append(", ");
        } else {
          first = false;
        }
        final StringPair sp = (StringPair)paramIter.next();
        final String paramName = sp.getStringTwo();
        code.append(paramName);
      }
    }
    code.append("));");
    carriageReturn(code);

    if(!assertMethod.getContainingClass().getInvariants().isEmpty()) {
      //call checkInvariant
      code.append(generateInvariantCall(assertMethod.getContainingClass()));
    }

    if(!assertMethod.getPostConditions().isEmpty()) {
      //call post conditions
      code.append(generatePostConditionCall(assertMethod));
    }
    
    code.append("}"); //end regular constructor
    carriageReturn(code);

    //dummy class
    code.append("final static private class ");
    code.append(dummyClassName);
    code.append(" { ");
    carriageReturn(code);
    code.append("public ");
    code.append(dummyClassName);
    code.append("(");

    //put params with types in here once
    {
      boolean first = true;
      final Iterator paramIter = assertMethod.getParams().iterator();
      while(paramIter.hasNext()) {
        if(!first) {
          code.append(", ");
        } else {
          first = false;
        }
        final StringPair sp = (StringPair)paramIter.next();
        final String paramType = sp.getStringOne();
        final String paramName = sp.getStringTwo();
        code.append("final ");
        code.append(paramType);
        code.append(" ");
        code.append(paramName);
      }
    }
    code.append(") {");
    carriageReturn(code);

    if(assertMethod.getPreConditions().isEmpty()) {
      //Just put in a call to the pre conditions
      code.append(generatePreConditionCall(assertMethod));
    }
    
    code.append("}");
    carriageReturn(code);
    code.append("}");
    carriageReturn(code);

    //extra constructor
    code.append("private ");
    code.append(assertMethod.getName());
    code.append("(");

    //put unique params with types in here
    {
      boolean first = true;
      final Iterator paramIter = assertMethod.getParams().iterator();
      while(paramIter.hasNext()) {
        if(!first) {
          code.append(", ");
        } else {
          first = false;
        }
        final StringPair sp = (StringPair)paramIter.next();
        final String paramType = sp.getStringOne();
        final String paramName = sp.getStringTwo();
        code.append("final ");
        code.append(paramType);
        code.append (" ");
        code.append(paramName);
      }
    }      

    if(!assertMethod.getParams().isEmpty()) {
      code.append(", ");
    }
    code.append(dummyClassName);
    code.append(" jps_ad) {");
    carriageReturn(code);

    return code.toString();
  }
  
  /**
     @param assertMethod the method to generate the post condition check call for
     @param retVal the actual return statement that is in the code before it
     is instrumented, ignored if the method has no return value
     @return code to call the post condition check for a method

     @pre (assertMethod != null)
  **/
  static public String generatePostConditionCall(final AssertMethod assertMethod) {
    final StringBuffer code = new StringBuffer();
    carriageReturn(code);
    final String retType = assertMethod.getReturnType();
    String mclassName = assertMethod.getContainingClass().getFullName().replace('.', '_');
    mclassName = mclassName.replace('$', '_');
    String shortmclassName = assertMethod.getContainingClass().getName().replace('.', '_');
    shortmclassName = shortmclassName.replace('$', '_');

      
    code.append("if(" + ASSERT_TOOLS_CLASSNAME + ".ENFORCE_POST_CONDITION && !jps__");
    code.append(mclassName);
    code.append("_check");
    code.append(assertMethod.getName());
    code.append("PostConditions(");

    boolean first = true;
    if(!assertMethod.isVoid()) {
      code.append("jps__retVal");
      code.append(shortmclassName);
      first = false;
    } else {
      //Need dummy slot
      code.append("null");
      first = false;
    }
    
    final Iterator paramIter = assertMethod.getParams().iterator();
    while(paramIter.hasNext()) {
      final StringPair sp = (StringPair)paramIter.next();
      final String paramName = sp.getStringTwo();
      if(! first) {
        code.append(", ");
      } else {
        first = false;
      }
      code.append(paramName);
    }
    code.append(")) {");
    carriageReturn(code);
    code.append(ASSERT_TOOLS_CLASSNAME + ".postConditionFailed(" + ASSERT_TOOLS_CLASSNAME + ".getCurrentAssertionViolation());");
    carriageReturn(code);
    code.append("}");
    carriageReturn(code);

    if(!assertMethod.isVoid()) {
      code.append("return jps__retVal");
      code.append(shortmclassName);
      code.append(";");
      carriageReturn(code);
    }
    
    return code.toString();
  }

  /**
     @return the code neccessary to implement the pre conditions on this method
  **/
  static public String generatePreConditionMethod(final AssertMethod assertMethod) {
    final String className = assertMethod.getContainingClass().getFullName();
    String mclassName = className.replace('.', '_');
    mclassName = mclassName.replace('$', '_');
    final String methodName = "jps__" + mclassName + "_check" + assertMethod.getName() + "PreConditions";

    final StringBuffer methodVariableNameBuf = new StringBuffer();
    methodVariableNameBuf.append('_');
    methodVariableNameBuf.append(methodName);
    methodVariableNameBuf.append('_');
    { //generate a unique name for the method variable
      final Iterator paramIter = assertMethod.getParams().iterator();
      while(paramIter.hasNext()) {
        final StringPair sp = (StringPair)paramIter.next();
        methodVariableNameBuf.append(sp.getStringOne());
        methodVariableNameBuf.append('_');
      }
    }
    final String methodVariableName = methodVariableNameBuf.toString().replace('.', '_').replace('[', 'L').replace(']', 'R');
    
    String methodKey = methodName; //for lock and unlock method calls
    
    final StringBuffer code = new StringBuffer();

    //create a variable to cache the super method
    carriageReturn(code);
    code.append("private ");
    if(assertMethod.isStatic() || assertMethod.isConstructor()) {
      code.append("static ");
    } else {
      code.append("transient ");
    }
    code.append("java.lang.reflect.Method ");
    code.append(methodVariableName);
    code.append(';');
    carriageReturn(code);
    
    if(assertMethod.isStatic() || assertMethod.isConstructor()) {
      code.append("static ");
    }
    code.append("final ");
    
    code.append(assertMethod.getAssertMethodVisibility());
    code.append(' ');
    code.append("boolean ");
    code.append(methodName);
    code.append("(");

    boolean first = true;
    Iterator paramIter = assertMethod.getParams().iterator();
    while(paramIter.hasNext()) {
      if(! first) {
        code.append(",");
      } else {
        first = false;
      }
      final StringPair sp = (StringPair)paramIter.next();
      code.append("final ");
      code.append(sp.getStringOne());
      methodKey += "_" + sp.getStringOne();
      code.append(' ');
      code.append(sp.getStringTwo());
    }
    code.append(") {");
    carriageReturn(code);

    code.append("if(!" + ASSERT_TOOLS_CLASSNAME + ".ENFORCE_PRE_CONDITION) { return true; }");
    carriageReturn(code);
    
    //check for recursive calls
    code.append("final " + ASSERT_TOOLS_CLASSNAME + ".MethodLock jps__methodLock = " + ASSERT_TOOLS_CLASSNAME + ".lockMethod(\"" + methodKey + "\", ");
    if(assertMethod.isStatic()) {
      code.append("null");
    } else {
      code.append("this");
    }
    code.append(");");
    carriageReturn(code);
    code.append("if(null == jps__methodLock) { return true; }");
    carriageReturn(code);
    
    code.append("Object jps__retval = null;");
    carriageReturn(code);
    
    if(!assertMethod.isPrivate() && !assertMethod.isConstructor()) {
      //don't check super class conditions if the method is private or a constructor
      
      code.append("if(" + ASSERT_TOOLS_CLASSNAME + ".ENFORCE_INHERITED_CONDITIONS) {");
      carriageReturn(code);

      //Get the class object
      code.append("final String jps_className = \"");
      code.append(className);
      code.append("\";");
      carriageReturn(code);
      code.append("final Class jps_thisClass = " + ASSERT_TOOLS_CLASSNAME + ".classForName(jps_className);");
      carriageReturn(code);
      code.append("if(jps_thisClass == null) {");
      carriageReturn(code);
      code.append(ASSERT_TOOLS_CLASSNAME + ".internalError(\"Could not find class \" + jps_className);");
      carriageReturn(code);
      code.append("}");      
      carriageReturn(code);

      //check if we need to find the super method
      code.append("if(");
      code.append(methodVariableName);
      code.append(" == null) {");
      carriageReturn(code);
        
      //need method parameters here, just the class objects, use getClassObjectForClass
      code.append("final Class[] jps_methodArgs = {");
      first = true;
      paramIter = assertMethod.getParams().iterator();
      while(paramIter.hasNext()) {
        if(! first) {
          code.append(", ");
        } else {
          first = false;
        }
        final StringPair sp = (StringPair)paramIter.next();
        code.append(getClassObjectForClass(sp.getStringOne()));
      }
      code.append("};");
      carriageReturn(code);

      //find super method
      code.append(methodVariableName);
      code.append(" = " + ASSERT_TOOLS_CLASSNAME + ".findSuperMethod(jps_thisClass, \"check");
      code.append(assertMethod.getName());
      code.append("PreConditions\", jps_methodArgs);");
      carriageReturn(code);
      code.append("if(");
      code.append(methodVariableName);
      code.append(" == null) {");
      carriageReturn(code);
      code.append(methodVariableName);
      code.append(" = " + ASSERT_TOOLS_CLASSNAME + ".NO_METHOD;");
      carriageReturn(code);
      code.append("}");
      carriageReturn(code);
      code.append("}");
      carriageReturn(code);
  
      code.append("if(");
      code.append(methodVariableName);
      code.append(" != " + ASSERT_TOOLS_CLASSNAME + ".NO_METHOD) {");
      carriageReturn(code);
      
      code.append("final Object[] jps_args = {");
      first = true;      
      //Need parameters here, just the parameter names
      paramIter = assertMethod.getParams().iterator();
      while(paramIter.hasNext()) {
        if(! first) {
          code.append(", ");
        } else {
          first = false;
        }
        final StringPair sp = (StringPair)paramIter.next();
        code.append(getObjectForParam(sp.getStringOne(), sp.getStringTwo()));
      }    
      code.append("};");
      carriageReturn(code);
      code.append("try {");
      carriageReturn(code);
      code.append("jps__retval = ");
      code.append(methodVariableName);
      code.append(".invoke(");
      if(assertMethod.isStatic() || assertMethod.isConstructor()) {
        code.append("null");
      } else {
        code.append("this");
      }
      code.append(", jps_args);");
      carriageReturn(code);
      code.append("} catch(IllegalAccessException jps_iae) {");
      carriageReturn(code);
      //just means that the super method is private and we really shouldn't be calling it in the first place      
      //code.append(ASSERT_TOOLS_CLASSNAME + ".internalError(\"Not enough access executing superClass check");
      //code.append(assertMethod.getName());
      //code.append("PreConditions: \" + jps_iae.getMessage());");
      //Pretend it returned true :)
      code.append("jps__retval = Boolean.TRUE;");
      carriageReturn(code);
      code.append("} catch(IllegalArgumentException jps_iae) {");
      carriageReturn(code);
      code.append(ASSERT_TOOLS_CLASSNAME + ".internalError(\"IllegalArgument executing superClass check");
      code.append(assertMethod.getName());
      code.append("PreConditions: \" + jps_iae.getMessage());");
      carriageReturn(code);
      code.append("} catch(java.lang.reflect.InvocationTargetException jps_ite) {");
      carriageReturn(code);
      code.append("jps_ite.getTargetException().printStackTrace();");
      carriageReturn(code);
      code.append("}");
      carriageReturn(code);
      code.append("if(jps__retval == null) {");
      carriageReturn(code);
      code.append(ASSERT_TOOLS_CLASSNAME + ".internalError(\"got null from checkPreConditions\");");
      carriageReturn(code);
      code.append("} else if(((Boolean)jps__retval).booleanValue()) {"); //PreConditions are ORed
      carriageReturn(code);
      code.append(ASSERT_TOOLS_CLASSNAME + ".unlockMethod(jps__methodLock);");
      carriageReturn(code);
      code.append("return true;");
      carriageReturn(code);
      code.append("}");
      carriageReturn(code);
      
      code.append("}");
      carriageReturn(code);


    
      //[jpschewe:20000116.1749CST] FIX still need to add to this to do interface
      //preconditions first and keep track of which interface they're from

      code.append("}"); //end if enforce inherited conditions
      carriageReturn(code);
    }

    addConditionChecks(code, assertMethod.getPreConditions(), false);
    
    code.append(ASSERT_TOOLS_CLASSNAME + ".unlockMethod(jps__methodLock);");
    carriageReturn(code);
    
    //nothing failed locally
    code.append("if(null == jps__retval) {"); //didn't call superclass checks, so return true
    carriageReturn(code);
    code.append("return true;");
    carriageReturn(code);
    code.append("} else {"); //superclass must have failed
    carriageReturn(code);
    code.append("return false;");
    carriageReturn(code);
    code.append("}");
    carriageReturn(code);
    
    code.append("}");
    carriageReturn(code);
    
    return code.toString();
  }

  /**
     @return the code neccessary to implement the post conditions on this method
  **/
  static public String generatePostConditionMethod(final AssertMethod assertMethod) {
    final String className = assertMethod.getContainingClass().getFullName();
    String mclassName = className.replace('.', '_');
    mclassName = mclassName.replace('$', '_');
    final String methodName = "jps__" + mclassName + "_check" + assertMethod.getName() + "PostConditions";

    final StringBuffer methodVariableNameBuf = new StringBuffer();
    methodVariableNameBuf.append('_');
    methodVariableNameBuf.append(methodName);
    methodVariableNameBuf.append('_');
    { //generate a unique name for the method variable
      final Iterator paramIter = assertMethod.getParams().iterator();
      while(paramIter.hasNext()) {
        final StringPair sp = (StringPair)paramIter.next();
        methodVariableNameBuf.append(sp.getStringOne());
        methodVariableNameBuf.append('_');
      }
    }
    final String methodVariableName = methodVariableNameBuf.toString().replace('.', '_').replace('[', 'L').replace(']', 'R');
    
    String methodKey = methodName; //for lock and unlock method calls
    
    StringBuffer code = new StringBuffer();

    //create a variable to cache the super method
    carriageReturn(code);
    code.append("private ");
    if(assertMethod.isStatic() || assertMethod.isConstructor()) {
      code.append("static ");
    } else {
      code.append("transient ");
    }
    code.append("java.lang.reflect.Method ");
    code.append(methodVariableName);
    code.append(';');
    carriageReturn(code);
    
    if(assertMethod.isStatic() || assertMethod.isConstructor()) {
      code.append("static ");
    }
    code.append("final ");
    code.append(assertMethod.getAssertMethodVisibility());
    code.append(' ');
    code.append("boolean ");
    code.append(methodName);
    code.append("(");
    boolean first = true;
    if(!assertMethod.isVoid()) {
      code.append(assertMethod.getReturnType());
      methodKey += "_" + assertMethod.getReturnType();
      code.append(" ");
      code.append("jps__retVal");
      first = false;
    } else {
      code.append("final Object jps__dummyretVal");
      methodKey += "_Object";
      first = false;
    }
      
    Iterator paramIter = assertMethod.getParams().iterator();
    while(paramIter.hasNext()) {
      final StringPair sp = (StringPair)paramIter.next();
      final String paramType = sp.getStringOne();
      final String paramName = sp.getStringTwo();
      if(! first) {
        code.append(",");
      } else {
        first = false;
      }
      //         code.append(paramType);
      //         code.append(" ");
      //         code.append("jps__old");
      //         code.append(paramName);
      //         code.append(", ");
      code.append("final ");
      code.append(paramType);
      methodKey += "_" + paramType;
      code.append(' ');
      code.append(paramName);
    }
    code.append(") {");
    carriageReturn(code);

    code.append("if(!" + ASSERT_TOOLS_CLASSNAME + ".ENFORCE_POST_CONDITION) { return true; }");
    carriageReturn(code);
    
    //check for recursive calls
    code.append("final " + ASSERT_TOOLS_CLASSNAME + ".MethodLock jps__methodLock = " + ASSERT_TOOLS_CLASSNAME + ".lockMethod(\"" + methodKey + "\", ");
    if(assertMethod.isStatic()) {
      code.append("null");
    } else {
      code.append("this");
    }
    code.append(");");
    carriageReturn(code);
    code.append("if(null == jps__methodLock) { return true; }");
    carriageReturn(code);
    
    if(!assertMethod.isPrivate() && !assertMethod.isConstructor()) {
      //don't bother checking for super method if we're private or a constructor
      
      code.append("Object jps__retval = null;");
      carriageReturn(code);
      
      code.append("if(" + ASSERT_TOOLS_CLASSNAME + ".ENFORCE_INHERITED_CONDITIONS) {");
      carriageReturn(code);

      //Get the class object
      code.append("final String jps_className = \"");
      code.append(className);
      code.append("\";");
      carriageReturn(code);
      code.append("final Class jps_thisClass = " + ASSERT_TOOLS_CLASSNAME + ".classForName(jps_className);");
      carriageReturn(code);
      code.append("if(jps_thisClass == null) {");
      carriageReturn(code);
      code.append(ASSERT_TOOLS_CLASSNAME + ".internalError(\"Could not find class \" + jps_className);");
      carriageReturn(code);
      code.append("}");
      carriageReturn(code);

      //check if we need to find the super method
      code.append("if(");
      code.append(methodVariableName);
      code.append(" == null) {");
      carriageReturn(code);
      
      //need method parameters here, just the class objects, use getClassObjectForClass
      code.append("final Class[] jps_methodArgs = {");
      first = true;
      
      //Need return value here too
      if(!assertMethod.isVoid()) {
        code.append(getClassObjectForClass(assertMethod.getReturnType()));
        first = false;
      } else {
        code.append("Object.class"); //For dummy return value
        first = false;
      }
      paramIter = assertMethod.getParams().iterator();
      while(paramIter.hasNext()) {
        if(! first) {
          code.append(", ");
        } else {
          first = false;
        }
        final StringPair sp = (StringPair)paramIter.next();
        final String classObj = getClassObjectForClass(sp.getStringOne());
        code.append(classObj);
        //for old value
        //         code.append(", ");
        //         code.append(classObj);
      }
      code.append("};");
      carriageReturn(code);

      //find super method
      code.append(methodVariableName);
      code.append(" = " + ASSERT_TOOLS_CLASSNAME + ".findSuperMethod(jps_thisClass, \"check");
      code.append(assertMethod.getName());
      code.append("PostConditions\", jps_methodArgs);");
      carriageReturn(code);
      code.append("if(");
      code.append(methodVariableName);
      code.append(" == null) {");
      carriageReturn(code);
      code.append(methodVariableName);
      code.append(" = " + ASSERT_TOOLS_CLASSNAME + ".NO_METHOD;");
      carriageReturn(code);
      code.append("}");
      carriageReturn(code);
      code.append("}");
      carriageReturn(code);

      code.append("if(");
      code.append(methodVariableName);
      code.append(" != " + ASSERT_TOOLS_CLASSNAME + ".NO_METHOD) {");
      carriageReturn(code);
      
      code.append("final Object[] jps_args = {");
      first = true;      
      //need parameters here, just the parameter names
      if(!assertMethod.isVoid()) {
        code.append(getObjectForParam(assertMethod.getReturnType(), "jps__retVal"));
        first = false;
      } else {
        code.append("null");
        first = false;
      }
      
      paramIter = assertMethod.getParams().iterator();
      while(paramIter.hasNext()) {
        if(! first) {
          code.append(", ");
        } else {
          first = false;
        }
        StringPair sp = (StringPair)paramIter.next();
        code.append(getObjectForParam(sp.getStringOne(), sp.getStringTwo()));
        //         // once for old
        //         code.append(", ");
        //         code.append(getObjectForParam(sp.getStringOne(), "jps__old" + sp.getStringTwo()));
      }
      code.append("};");
      carriageReturn(code);
      code.append("try {");
      carriageReturn(code);
      code.append("jps__retval = ");
      code.append(methodVariableName);
      code.append(".invoke(");
      if(assertMethod.isStatic() || assertMethod.isConstructor()) {
        code.append("null");
      } else {
        code.append("this");
      }
      code.append(", jps_args);");
      carriageReturn(code);
      code.append("} catch(IllegalAccessException jps_iae) {");
      carriageReturn(code);
      //just means that the super method is private and we really shouldn't be calling it in the first place
      //code.append(ASSERT_TOOLS_CLASSNAME + ".internalError(\"Not enough access executing superClass check");
      //code.append(assertMethod.getName());
      //code.append("PostConditions: \" + jps_iae.getMessage());");
      //Pretend it returned true :)
      code.append("jps__retval = Boolean.TRUE;");
      carriageReturn(code);
      code.append("} catch(IllegalArgumentException jps_iae) {");
      carriageReturn(code);
      code.append(ASSERT_TOOLS_CLASSNAME + ".internalError(\"IllegalArgument executing superClass check");
      code.append(assertMethod.getName());
      code.append("PostConditions: \" + jps_iae.getMessage());");
      carriageReturn(code);
      code.append("} catch(java.lang.reflect.InvocationTargetException jps_ite) {");
      carriageReturn(code);
      code.append("jps_ite.getTargetException().printStackTrace();");
      carriageReturn(code);
      code.append("}");
      carriageReturn(code);
      code.append("if(jps__retval == null) {");
      carriageReturn(code);
      code.append(ASSERT_TOOLS_CLASSNAME + ".internalError(\"got null from checkPostConditions\");");
      carriageReturn(code);
      code.append("} else if(!((Boolean)jps__retval).booleanValue()) {"); //PostConditions are ANDed
      carriageReturn(code);
      code.append(ASSERT_TOOLS_CLASSNAME + ".unlockMethod(jps__methodLock);");
      carriageReturn(code);
      code.append("return false;");
      carriageReturn(code);
      code.append("}");
      carriageReturn(code);
      
      code.append("}");
      carriageReturn(code);

      //[jpschewe:20000116.1749CST] FIX still need to add to this to do interface
      //postconditions first and keep track of which interface they're from

      code.append("}"); //end if enforce inherited conditions
      carriageReturn(code);
    }
    
    addConditionChecks(code, assertMethod.getPostConditions(), true);
    code.append(ASSERT_TOOLS_CLASSNAME + ".unlockMethod(jps__methodLock);");
    carriageReturn(code);
    code.append("return true;"); //nothing failed, just return true
    carriageReturn(code);
    code.append("}");
    carriageReturn(code);
    
    return code.toString();
  }

  /**
   * Append to code code to check for the assert conditions in tokens.
   * Generated code returns fales if any condition fails
   *
   * @param postCondition true if we're adding checks for a postcondition method. 
   */
  static private void addConditionChecks(final StringBuffer code,
                                         final List tokens,
                                         final boolean postCondition) {
    final Iterator iter = tokens.iterator();
    while(iter.hasNext()) {
      final AssertToken token = (AssertToken)iter.next();
      final String condition = token.getCondition();
      final String message = token.getMessage();
      code.append("if(!");
      if(postCondition) {
        code.append(StringUtils.searchAndReplace(condition, "$return", "jps__retVal"));
      } else {
        if(condition.indexOf("$return") != -1) {
          System.err.println("$return found in something other than postcondition! " + token.getText());
        }
        code.append(condition);
      }
      code.append(") {");
      carriageReturn(code);
      String errorMessage = "";
      if(message != null) {
        errorMessage = message + " + ";
      }

      errorMessage += "\" " + StringUtils.searchAndReplace(StringUtils.searchAndReplace(condition, "\"", "\\\""), "\'", "\\\'") + "\"";
      
      code.append(ASSERTION_VIOLATION_CLASSNAME + " jps_av = new " + ASSERTION_VIOLATION_CLASSNAME + "(");
      code.append(errorMessage);
      code.append(");");
      carriageReturn(code);
      code.append(ASSERT_TOOLS_CLASSNAME + ".setCurrentAssertionViolation(jps_av);");
      carriageReturn(code);

      code.append("return false;");
      carriageReturn(code);
      code.append("}");
      carriageReturn(code);
    }
  }

  /**
     Assertions on interfaces are checked by using a delegate class.  This
     method generates that class.
     
     @pre (assertInterface != null && assertInterface.isInterface())
  **/
  static public String generateAssertClassForInterface(final AssertClass assertInterface) {
    throw new RuntimeException("Not implemented");
  }
  

    
  /**
     Convert a class name to a class object call.  Normally this is just
     appending '.class', however primatives need to be converted to the
     appropriate wrapper class and then append '.TYPE'.

     @pre (cl != null)
  **/
  static public String getClassObjectForClass(final String cl) {
    if(cl.equals("int")) {
      return "Integer.TYPE";
    } else if(cl.equals("long")) {
      return "Long.TYPE";
    } else if(cl.equals("float")) {
      return "Float.TYPE";
    } else if(cl.equals("double")) {
      return "Double.TYPE";
    } else if(cl.equals("boolean")) {
      return "Boolean.TYPE";
    } else if(cl.equals("byte")) {
      return "Byte.TYPE";
    } else if(cl.equals("char")) {
      return "Character.TYPE";
    } else if(cl.equals("short")) {
      return "Short.TYPE";
    }

    return cl + ".class";
  }

  /**
     Take paramName and turn it into an object.  This only effects primatives,
     if paramType is a primative, return code that will generate a properly
     wrapped primative object, otherwise just return paramName.
  **/
  static public String getObjectForParam(final String paramType,
                                         final String paramName) {
    if(paramType.equals("int")) {
      return "new Integer(" + paramName + ")";
    } else if(paramType.equals("long")) {
      return "new Long(" + paramName + ")";
    } else if(paramType.equals("float")) {
      return "new Float(" + paramName + ")";
    } else if(paramType.equals("double")) {
      return "new Double(" + paramName + ")";
    } else if(paramType.equals("boolean")) {
      return "new Boolean(" + paramName + ")";
    } else if(paramType.equals("byte")) {
      return "new Byte(" + paramName + ")";
    } else if(paramType.equals("char")) {
      return "new Character(" + paramName + ")";
    } else if(paramType.equals("short")) {
      return "new Short(" + paramName + ")";
    }

    return paramName;
  }

  /**
   * Add a carriage return to code if pretty-output is turned on.
   */
  private static final void carriageReturn(final StringBuffer code) {
    if(JonsAssert.getSymtab().getConfiguration().isPrettyOutput()) {
      code.append(System.getProperty("line.separator"));
    }
  }
}
