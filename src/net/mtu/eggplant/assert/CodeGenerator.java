/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.Assert;

import antlr.Token;

import java.util.Vector;
import java.util.Enumeration;

public class CodeGenerator {

  /**
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
    code.append("AssertTools.assertFailed(new AssertionViolation(");
    code.append('"');
    code.append(errorMessage);
    code.append('"');
    code.append("));");
    code.append(" }");

    return code.toString();
  }


  /**
     @return a string of code that will call the checkInvariant method
  **/
  static public String generateInvariantCall() {
    StringBuffer code = new StringBuffer();
    code.append("if(!__checkInvariant()) {");
    code.append("AssertTools.invariantFailed(AssertTools.getCurrentAssertionViolation());");
    code.append("}");

    return code.toString();
  }

  /**
     @param assertClass the class to generate the invariant check for
     @return a string of code that actually checks the invariant conditions

     @pre (assertClass != null)
  **/
  static public String generateInvariantMethod(final AssertClass assertClass) {
    String className = assertClass.getFullName();

    StringBuffer code = new StringBuffer();
    code.append("protected boolean __checkInvariant() {");
    code.append("Class thisClass;");
    code.append("try {");
    code.append("String className = \"");
    code.append(className);
    code.append("\";");
    code.append("thisClass = Class.forName(className);");
    code.append("}");
    code.append("catch(ClassNotFoundException cnfe) {");
    code.append("AssertTools.internalError(\"Got error getting the class object for class \" + className + \" \" + cnfe);");
    code.append("}");
  
    code.append("Class[] methodArgs = new Class[0];");
    code.append("java.lang.reflect.Method superMethod = AssertTools.findSuperMethod(thisClass, \"__checkInvariant\", methodArgs);");

    code.append("if(superMethod != null) {");
    //invoke it, pass on exceptions
    code.append("Object[] args = new Object[0];");
    code.append("try {");
    code.append("retVal = superMethod.invoke(this, args);");
    code.append("}");
    code.append("catch(IllegalAccessException iae) {");
    code.append("AssertTools.internalError(\"Not enough access executing super.__checkInvariant: \" + iae.getMessage());");
    code.append("}");
    code.append("catch(IllegalArgumentException iae) {");
    code.append("AssertTools.internalError(\"IllegalArgument executing super.__checkInvariant: \" + iae.getMessage());");
    code.append("}");
    code.append("catch(InvocationTargetException ite) {");
    code.append("throw ite.getTargetException();");
    code.append("}");
    code.append("}");

    code.append("if(retVal == null) {");
    code.append("AssertTools.internalError(\"got null checkInvariant\");");
    code.append("}");
    code.append("else if(! (retVal instanceof Boolean) ) {");
    code.append("AssertTools.internalError(\"got something odd from checkInvariant: \" + retVal.getClass());");
    code.append("}");

    code.append("if(!((Boolean)retVal).booleanValue()) {");
    code.append("return false;");
    code.append("}");
  

    //[jpschewe:20000116.1749CST] still need to add to this do interface
    //invariants first and keep track of which interface they're from
  
    Enumeration iter = assertClass.getInvariants().elements();
    while(iter.hasMoreElements()) {
      AssertToken token = (AssertToken)iter.nextElement();
      String condition = token.getCondition();
      String message = token.getMessage();
      code.append("if(!");
      code.append(condition);
      code.append(") {");
      String errorMessage = "";
      if(message != null) {
        errorMessage = message + " ";
      }
      errorMessage += condition;
    
      code.append("AssertionViolation av = new AssertionViolation(");
      code.append(errorMessage);
      code.append(")");
      code.append("AssertTools.setCurrentAssertionViolation(av);");
    
      code.append("return false;");
      code.append("}");
    }
    
    code.append("return true;");
    code.append("}");

    return code.toString();
    
  }

}
