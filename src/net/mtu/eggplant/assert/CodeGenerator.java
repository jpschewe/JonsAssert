/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.Assert;

import org.tcfreenet.schewe.utils.StringPair;

import antlr.Token;

import java.util.Vector;
import java.util.Enumeration;
import java.util.List;
import java.util.Iterator;

public class CodeGenerator {

  /**
     This should be inserted right after the close of the javadoc comment.
     
     @param tok the token that represents the assertion
     @return a String of code that checks this assert condition

     @pre (tok != null)
  **/
  static public String generateAssertion(AssertToken tok) {
    String condition = tok.getCondition();
    String message = tok.getMessage();

    String errorMessage = "";
    if(message != null) {
      errorMessage = message + " ";
    }
    errorMessage += condition;

    StringBuffer code = new StringBuffer();

    code.append("if(! ");
    code.append(condition);
    code.append(") { ");
    code.append("org.tcfreenet.schewe.Assert.AssertTools.assertFailed(new org.tcfreenet.schewe.Assert.AssertionViolation(");
    code.append('"');
    code.append(errorMessage);
    code.append('"');
    code.append("));");
    code.append(" }");

    return code.toString();
  }


  /**
     This should be added at the start and end of all instance methods, except
     for private ones and should be at the end of constructors that aren't
     private.
     
     @return a string of code that will call the checkInvariant method
  **/
  static public String generateInvariantCall(final AssertClass aClass) {
    String mclassName = aClass.getFullName().replace('.', '_');
    StringBuffer code = new StringBuffer();
    code.append("if(!__");
    code.append(mclassName);
    code.append("_checkInvariant()) {");
    code.append("org.tcfreenet.schewe.Assert.AssertTools.invariantFailed(org.tcfreenet.schewe.Assert.AssertTools.getCurrentAssertionViolation());");
    code.append("}");

    return code.toString();
  }

  /**
     This should be inserted right at the end of a class, just before the
     closing curly.
     
     @param assertClass the class to generate the invariant check for
     @return a string of code that actually checks the invariant conditions

     @pre (assertClass != null)
  **/
  static public String generateInvariantMethod(final AssertClass assertClass) {
    String className = assertClass.getFullName();
    String mclassName = className.replace('.', '_');
    
    StringBuffer code = new StringBuffer();
    code.append("protected boolean __");
    code.append(mclassName);
    code.append("_checkInvariant() {\n");
    code.append("Class thisClass = null;");
    code.append("Object retVal = null;\n");    
    code.append("String className = \"");
    code.append(className);
    code.append("\";\n");
    code.append("try {\n");
    code.append("thisClass = Class.forName(className);\n");
    code.append("}\n");
    code.append("catch(ClassNotFoundException cnfe) {\n");
    code.append("org.tcfreenet.schewe.Assert.AssertTools.internalError(\"Got error getting the class object for class \" + className + \" \" + cnfe);\n");
    code.append("}\n");
  
    code.append("Class[] methodArgs = new Class[0];\n");
    code.append("java.lang.reflect.Method superMethod = org.tcfreenet.schewe.Assert.AssertTools.findSuperMethod(thisClass, \"checkInvariant\", methodArgs);\n");

    code.append("if(superMethod != null) {\n");
    //invoke it, pass on exceptions
    code.append("Object[] args = new Object[0];\n");
    code.append("try {\n");
    code.append("retVal = superMethod.invoke(this, args);\n");
    code.append("}\n");
    code.append("catch(IllegalAccessException iae) {\n");
    code.append("org.tcfreenet.schewe.Assert.AssertTools.internalError(\"Not enough access executing checkInvariant method on super class: \" + iae.getMessage());\n");
    code.append("}\n");
    code.append("catch(IllegalArgumentException iae) {\n");
    code.append("org.tcfreenet.schewe.Assert.AssertTools.internalError(\"IllegalArgument executing checkInvariant method on super class: \" + iae.getMessage());\n");
    code.append("}\n");
    code.append("catch(java.lang.reflect.InvocationTargetException ite) {\n");
    code.append("ite.getTargetException().printStackTrace();\n");
    code.append("}\n");
    code.append("}\n");

    code.append("if(retVal == null) {\n");
    code.append("org.tcfreenet.schewe.Assert.AssertTools.internalError(\"got null from checkInvariant\");\n");
    code.append("}\n");
    code.append("else if(! (retVal instanceof Boolean) ) {\n");
    code.append("org.tcfreenet.schewe.Assert.AssertTools.internalError(\"got something odd from checkInvariant: \" + retVal.getClass());\n");
    code.append("}\n");

    code.append("if(!((Boolean)retVal).booleanValue()) {\n");
    code.append("return false;\n");
    code.append("}\n");
  
    //[jpschewe:20000116.1749CST] FIX still need to add to this to do interface
    //invariants first and keep track of which interface they're from

    addConditionChecks(code, assertClass.getInvariants());

    code.append("return true;\n");
    code.append("}\n");

    return code.toString();
    
  }

  /**
     This code should be inserted right at the start of the method
     
     @param assertMethod the method to generate the precondition call for
     @return the code for a call to check the pre conditions for the given method

     @pre (assertMethod != null)
  **/
  static public String generatePreConditionCall(final AssertMethod assertMethod) {
    StringBuffer code = new StringBuffer();
    StringBuffer params = new StringBuffer();
    String mclassName = assertMethod.getContainingClass().getFullName().replace('.', '_');
    String dummyClassName = assertMethod.getContainingClass().createDummyConstructorClassName();
    boolean first = true;
    Enumeration paramIter = assertMethod.getParams().elements();
    while(paramIter.hasMoreElements()) {
      if(! first) {
        params.append(", ");
        first = false;
      }
      StringPair sp = (StringPair)paramIter.nextElement();
      String paramName = sp.getStringTwo();
      params.append(paramName);
    }
    
    if(assertMethod.isConstructor()) {
      // add extra class call
      code.append("this(");
      code.append(params);
      code.append(", new ");
      code.append(dummyClassName);
      code.append("(");
      code.append(params);
      code.append("));\n");
      code.append("}\n");
      code.append("static private class ");
      code.append(dummyClassName);
      code.append(" {\n");
      code.append("public ");
      code.append(dummyClassName);
      code.append("(");
      code.append(params);
      code.append(") {\n");
    }
    
    code.append("if(!__");
    code.append(mclassName);
    code.append("_check");
    code.append(assertMethod.getName());
    code.append("PreConditions(");

    code.append(params);
    code.append(")) {\n");
    code.append("org.tcfreenet.schewe.Assert.AssertTools.preConditionFailed(org.tcfreenet.schewe.Assert.AssertTools.getCurrentAssertionViolation());\n");
    code.append("}\n");

    if(assertMethod.isConstructor()) {
      code.append("}\n");
      code.append("}\n");
      code.append("private ");
      code.append(assertMethod.getName());
      code.append("(");
      code.append(params);
      code.append(", ");
      code.append(dummyClassName);
      code.append(" ad) {\n");
    }

    return code.toString();
  }

  /**
     This code should be inserted right after the precondition and invariant calls.
     
     @param assertMethod the method to generate the old values for
     @return the code to generate all of the old values to be used when calling the post condition check

     @pre (assertMethod != null)
  **/
  static public String generateOldValues(final AssertMethod assertMethod) {
    StringBuffer code = new StringBuffer();

    Enumeration paramIter = assertMethod.getParams().elements();
    while(paramIter.hasMoreElements()) {
      StringPair sp = (StringPair)paramIter.nextElement();
      String paramType = sp.getStringOne();
      String paramName = sp.getStringTwo();
      code.append("final ");
      code.append(paramType);
      code.append(" __old");
      code.append(paramName);
      code.append(" = ");
      code.append(paramName);
      code.append(";");
    }

    return code.toString();
  }

  /**
     @param assertMethod the method to generate the post condition check call for
     @param retVal the actual return statement that is in the code before it
     is instrumented, ignored if the method has no return value
     @return code to call the post condition check for a method

     @pre (assertMethod != null)
  **/
  static public String generatePostConditionCall(final AssertMethod assertMethod, final String retVal) {
    //[jpschewe:20000206.2034CST] FIX to be two code fragments, one for before retVal and one for after retVal
    //[jpschewe:20000118.0006CST] perhaps this should return a Vector of code fragments...
    StringBuffer code = new StringBuffer();
    String retType = assertMethod.getReturnType();
    boolean voidMethod = retType.equals("void");
    String mclassName = assertMethod.getContainingClass().getFullName().replace('.', '_');
    
    if(!voidMethod) {
      code.append("final ");
      code.append(retType);
      code.append(" __retVal = ");
      code.append(retVal);
      code.append(";");
    }
    
    code.append("if(!__");
    code.append(mclassName);
    code.append("_check");
    code.append(assertMethod.getName());
    code.append("PostConditions(");

    boolean first = true;
    if(!voidMethod) {
      code.append("__retVal");
      first = false;
    }
    Enumeration paramIter = assertMethod.getParams().elements();
    while(paramIter.hasMoreElements()) {
      StringPair sp = (StringPair)paramIter.nextElement();
      String paramName = sp.getStringTwo();
      if(! first) {
        code.append(", ");
        first = false;
      }
      code.append("__old");
      code.append(paramName);
      code.append(", ");
      code.append(paramName);
    }
    code.append(")) {");
    code.append("org.tcfreenet.schewe.Assert.AssertTools.preConditionFailed(org.tcfreenet.schewe.Assert.AssertTools.getCurrentAssertionViolation());");
    code.append("}");

    //[jpschewe:20000205.1446CST] don't actually change the return, that way we can just insert code into the file rather than modifying it.
    //if(!voidMethod) {
    //  code.append("return __retVal;");
    //}

    return code.toString();
  }

  /**
     @return the code neccessary to implement the pre conditions on this method
  **/
  static public String generatePreConditionMethod(final AssertMethod assertMethod) {
    String className = assertMethod.getContainingClass().getFullName();
    String mclassName = className.replace('.', '_');
    StringBuffer code = new StringBuffer();

    if(assertMethod.isStatic() || assertMethod.isConstructor()) {
      code.append("static ");
    }
    code.append("protected boolean __");
    code.append(mclassName);
    code.append("_check");
    code.append(assertMethod.getName());
    code.append("PreConditions(");
    boolean first = true;
    Iterator paramIter = assertMethod.getParams().iterator();
    while(paramIter.hasNext()) {
      if(! first) {
        code.append(",");
        first = false;
      }
      StringPair sp = (StringPair)paramIter.next();
      code.append(sp.getStringOne());
      code.append(' ');
      code.append(sp.getStringTwo());
    }
    code.append(") {\n");
    code.append("Class thisClass;\n");
    code.append("try {\n");
    code.append("String className = \"");
    code.append(className);
    code.append("\";\n");
    code.append("thisClass = Class.forName(className);\n");
    code.append("}\n");
    code.append("catch(ClassNotFoundException cnfe) {\n");
    code.append("org.tcfreenet.schewe.Assert.AssertTools.internalError(\"Got error getting the class object for class \" + className + \" \" + cnfe);\n");
    code.append("}\n");
    

    //[jpschewe:20000213.1552CST] need method parameters here, just the class objects, use getClassObjectForClass
    code.append("Class[] methodArgs = {");
    first = true;
    paramIter = assertMethod.getParams().iterator();
    while(paramIter.hasNext()) {
      if(! first) {
        code.append(", ");
        first = false;
      }
      StringPair sp = (StringPair)paramIter.next();
      code.append(getClassObjectForClass(sp.getStringOne()));
    }
    code.append("};\n");
                
    code.append("Method superMethod = org.tcfreenet.schewe.Assert.AssertTools.findSuperMethod(thisClass, \"check");
    code.append(assertMethod.getName());
    code.append("PreConditions\", methodArgs);\n");

    code.append("if(superMethod != null) {\n");
    code.append("Object[] args = {");
    //[jpschewe:20000213.1552CST] need parameters here, just the parameter names
    first = true;
    paramIter = assertMethod.getParams().iterator();
    while(paramIter.hasNext()) {
      if(! first) {
        code.append(", ");
        first = false;
      }
      StringPair sp = (StringPair)paramIter.next();
      code.append(sp.getStringTwo());
    }    
    code.append("};\n");
    code.append("try {\n");
    code.append("retVal = superMethod.invoke(");
    if(assertMethod.isStatic() || assertMethod.isConstructor()) {
      code.append("null");
    }
    else {
      code.append("this");
    }
    code.append(", args);\n");
    code.append("}\n");
    code.append("catch(IllegalAccessException iae) {\n");
    code.append("org.tcfreenet.schewe.Assert.AssertTools.internalError(\"Not enough access executing superClass check");
    code.append(assertMethod.getName());
    code.append("PreConditions: \" + iae.getMessage());\n");
    code.append("}\n");
    code.append("catch(IllegalArgumentException iae) {\n");
    code.append("org.tcfreenet.schewe.Assert.AssertTools.internalError(\"IllegalArgument executing superClass check");
    code.append(assertMethod.getName());
    code.append("PreConditions: \" + iae.getMessage());\n");
    code.append("}\n");
    code.append("catch(java.lang.reflect.InvocationTargetException ite) {\n");
    code.append("ite.getTargetException().printStackTrace();\n");
    code.append("}\n");
    code.append("}\n");

    
    //[jpschewe:20000116.1749CST] FIX still need to add to this to do interface
    //preconditions first and keep track of which interface they're from

    addConditionChecks(code, assertMethod.getPreConditions());

    code.append("return true;\n");
    code.append("}\n");
    
    return code.toString();
  }

  /**
     @return the code neccessary to implement the post conditions on this method
  **/
  static public String generatePostConditionMethod(final AssertMethod assertMethod) {
    StringBuffer code = new StringBuffer();
    //[jpschewe:20000206.2034CST] FIX add code

    //[jpschewe:20000116.1749CST] FIX still need to add to this to do interface
    //post conditions first and keep track of which interface they're from

    addConditionChecks(code, assertMethod.getPostConditions());

    code.append("return true;\n");
    code.append("}\n");
    
    return code.toString();
  }

  /**
     Append to code code to check for the assert conditions in tokens.
  **/
  static private void addConditionChecks(final StringBuffer code,
                                         final List tokens) {
    Iterator iter = tokens.iterator();
    while(iter.hasNext()) {
      AssertToken token = (AssertToken)iter.next();
      String condition = token.getCondition();
      String message = token.getMessage();
      code.append("if(!");
      code.append(condition);
      code.append(") {\n");
      String errorMessage = "";
      if(message != null) {
        errorMessage = message + " ";
      }
      errorMessage += "+ \"" + condition + "\"";
    
      code.append("org.tcfreenet.schewe.Assert.AssertionViolation av = new org.tcfreenet.schewe.Assert.AssertionViolation(");
      code.append(errorMessage);
      code.append(");\n");
      code.append("org.tcfreenet.schewe.Assert.AssertTools.setCurrentAssertionViolation(av);\n");
    
      code.append("return false;\n");
      code.append("}\n");
    }

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
    }
    else if(cl.equals("long")) {
      return "Long.TYPE";
    }
    else if(cl.equals("float")) {
      return "Float.TYPE";
    }
    else if(cl.equals("double")) {
      return "Double.TYPE";
    }
    else if(cl.equals("boolean")) {
      return "Boolean.TYPE";
    }
    else if(cl.equals("byte")) {
      return "Byte.TYPE";
    }
    else if(cl.equals("char")) {
      return "Character.TYPE";
    }
    else if(cl.equals("short")) {
      return "Short.TYPE";
    }

    return cl + ".class";
  }
}
