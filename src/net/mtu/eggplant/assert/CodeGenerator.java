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
  
    Enumeration iter = assertClass.getInvariants().elements();
    while(iter.hasMoreElements()) {
      AssertToken token = (AssertToken)iter.nextElement();
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
    String mclassName = assertMethod.getContainingClass().getFullName().replace('.', '_');
    
    code.append("if(!__");
    code.append(mclassName);
    code.append("_check");
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
    code.append("org.tcfreenet.schewe.Assert.AssertTools.preConditionFailed(org.tcfreenet.schewe.Assert.AssertTools.getCurrentAssertionViolation());");
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
        code.append(",");
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
