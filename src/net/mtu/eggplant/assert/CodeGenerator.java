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
    code.append("AssertTools.assertFailed(new AssertionViolation(");
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
  static public String generateInvariantCall() {
    StringBuffer code = new StringBuffer();
    code.append("if(!__checkInvariant()) {");
    code.append("AssertTools.invariantFailed(AssertTools.getCurrentAssertionViolation());");
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
  

    //[jpschewe:20000116.1749CST] FIX still need to add to this to do interface
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

  /**
     This code should be inserted right at the start of the method
     
     @param assertMethod the method to generate the precondition call for
     @return the code for a call to check the pre conditions for the given method

     @pre (assertMethod != null)
  **/
  static public String generatePreConditionCall(final AssertMethod assertMethod) {
    StringBuffer code = new StringBuffer();
    
    code.append("if(!__check");
    code.append(assertMethod.getName());
    code.append("PreConditions(");

    boolean first = true;
    Enumeration paramIter = assertMethod.getParams().elements();
    while(paramIter.hasMoreElements()) {
      StringPair sp = (StringPair)paramIter.nextElement();
      String paramName = sp.getStringTwo();
      if(! first) {
        code.append(",");
        first = false;
      }
      code.append(paramName);
    }
    code.append(")) {");
    code.append("AssertTools.preConditionFailed(AssertTools.getCurrentAssertionViolation());");
    code.append("}");


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
    
    if(!voidMethod) {
      code.append("final ");
      code.append(retType);
      code.append(" __retVal = ");
      code.append(retVal);
      code.append(";");
    }
    
    code.append("if(!__check");
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
        code.append(",");
        first = false;
      }
      code.append("__old");
      code.append(paramName);
      code.append(", ");
      code.append(paramName);
    }
    code.append(")) {");
    code.append("AssertTools.preConditionFailed(AssertTools.getCurrentAssertionViolation());");
    code.append("}");

    //[jpschewe:20000205.1446CST] don't actually change the return, that way we can just insert code into the file rather than modifying it.
    //if(!voidMethod) {
    //  code.append("return __retVal;");
    //}

    return code.toString();
  }

//
//    idea for preconditions on constructor
//    stuff to add after open { for constructor:

    //start example
//   /**
//      @pre (i > 0)
//      @pre (checkValue(i))
//   **/
//   public Test (int i) {
//     this(i, new AssertDummy0(i));
//   }
//   static private class AssertDummy0 {
//     public AssertDummy0(int i) {
//       __checkConstructorPreConditions(i);
//     }
//   }
//     static /*package*/ void __checkConstructorPreConditions(int i) {
//       System.out.println("i > 0 " + (i > 0));
//       System.out.println("checkValue(i) " + checkValue(i));
//     }
    
//   private Test(int i, AssertDummy0 ad) {
//     System.out.println("in constructor " + i);
//   }
//   //end example
  
//     String params; // grabbed from parser tokens
//     long # = 0;
//     foreach constructor (constructors) {
//        Vector preconditions = constructor.getPreConditions();
       
//         this(params, new AssertDummy#( (cond0), "mesg0", (cond1), "mesg1", ...));
//       }
//       static private class AssertDummy# {
//         public AssertDummy#(boolean cond0, String mesg0, boolean cond1, String mesg1, ...) {
//           // check against params here and do regular fail stuff
//         }
//       }
//       private constructorName(params, AssertDummy#) {
      
//       CodeFragment codeFrag = new CodeFragment(constructor.getEntrance().line, constructor.getEntrance().column, code, AssertType.PRECONDITION);
//       symtab.associateCodeWithCurrentFile(codeFrag);
//       #++;
//     }

  /**
     @return the code neccessary to implement the pre conditions on this method
  **/
  static public String generatePreConditionMethod(final AssertMethod assertMethod) {
    //[jpschewe:20000206.2033CST] FIX
    return null;
  }

  /**
     @return the code neccessary to implement the post conditions on this method
  **/
  static public String generatePostConditionMethod(final AssertMethod assertMethod) {
    //[jpschewe:20000206.2034CST] FIX
    return null;
  }
  
}
