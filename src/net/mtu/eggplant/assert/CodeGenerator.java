/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.assert;

import org.tcfreenet.schewe.utils.StringPair;

import gnu.regexp.RE;
import gnu.regexp.REException;

import java.util.Set;
import java.util.Iterator;

public class CodeGenerator {

  /**
     This should be inserted right after the close of the javadoc comment.
     
     @param tok the token that represents the assertion
     @return a String of code that checks this assert condition

     @pre (tok != null)
  **/
  static public String generateAssertion(final AssertToken tok) {
    final String condition = tok.getCondition();
    final String message = tok.getMessage();

    final StringBuffer errorMessage = new StringBuffer();
    if(message != null) {
      errorMessage.append(message).append(" + ");
    }
    errorMessage.append("\" ").append(condition).append("\"");

    final StringBuffer code = new StringBuffer();
    code.append("{");
    code.append("if(! ");
    code.append(condition);
    code.append(") { ");
    code.append("org.tcfreenet.schewe.assert.AssertTools.assertFailed(new org.tcfreenet.schewe.assert.AssertionViolation(");
    //code.append("\"");
    code.append(errorMessage.toString());
    //code.append("\"");
    code.append("));");
    code.append(" }");
    code.append("}");
    
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
    mclassName = mclassName.replace('$', '_');
    final StringBuffer code = new StringBuffer();
    code.append("if(!__");
    code.append(mclassName);
    code.append("_checkInvariant()) {");
    code.append("org.tcfreenet.schewe.assert.AssertTools.invariantFailed(org.tcfreenet.schewe.assert.AssertTools.getCurrentAssertionViolation());");
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
    final String className = assertClass.getFullName();
    String mclassName = className.replace('.', '_');
    mclassName = mclassName.replace('$', '_');
    final String methodName = "__" + mclassName + "_checkInvariant";
    
    final StringBuffer code = new StringBuffer();
    code.append("protected boolean ");
    code.append(methodName);
    code.append("() {");
    code.append("if(!org.tcfreenet.schewe.assert.AssertTools.lockMethod(\"");
    code.append(methodName);
    code.append("\")) { return true; }");
    code.append("Object _JPS_retVal = null;");
    
    //Get the class object
    code.append("final String _JPS_className = \"");
    code.append(className);
    code.append("\";");
    code.append("final Class _JPS_thisClass = org.tcfreenet.schewe.assert.AssertTools.classForName(_JPS_className);");
    code.append("if(_JPS_thisClass == null) {");
    code.append("org.tcfreenet.schewe.assert.AssertTools.internalError(\"Could not find class \" + _JPS_className);");
    code.append("}");
      
  
    code.append("final Class[] _JPS_methodArgs = new Class[0];");
    code.append("final java.lang.reflect.Method _JPS_superMethod = org.tcfreenet.schewe.assert.AssertTools.findSuperMethod(_JPS_thisClass, \"checkInvariant\", _JPS_methodArgs);");

    code.append("if(_JPS_superMethod != null) {");
    //invoke it, pass on exceptions
    code.append("final Object[] _JPS_args = new Object[0];");
    code.append("try {");
    code.append("_JPS_retVal = _JPS_superMethod.invoke(this, _JPS_args);");
    code.append("}");
    code.append("catch(IllegalAccessException _JPS_iae) {");
    //[jpschewe:20000220.0936CST] just means that the super method is private and we really shouldn't be calling it in the first place          
    //code.append("org.tcfreenet.schewe.assert.AssertTools.internalError(\"Not enough access executing checkInvariant method on super class: \" + _JPS_iae.getMessage());");
    //Pretend it returned true :)
    code.append("_JPS_retVal = Boolean.TRUE;");
    code.append("}");
    code.append("catch(IllegalArgumentException _JPS_iae) {");
    code.append("org.tcfreenet.schewe.assert.AssertTools.internalError(\"IllegalArgument executing checkInvariant method on super class: \" + _JPS_iae.getMessage() + \" methodArgs \" + org.tcfreenet.schewe.utils.Functions.printArray(_JPS_methodArgs) + \" args \" + org.tcfreenet.schewe.utils.Functions.printArray(_JPS_args));");
    code.append("}");
    code.append("catch(java.lang.reflect.InvocationTargetException _JPS_ite) {");
    code.append("_JPS_ite.getTargetException().printStackTrace();");
    code.append("}");
    code.append("if(_JPS_retVal == null) {");
    code.append("org.tcfreenet.schewe.assert.AssertTools.internalError(\"got null from checkInvariant\");");
    code.append("}");
    code.append("else if(! (_JPS_retVal instanceof Boolean) ) {");
    code.append("org.tcfreenet.schewe.assert.AssertTools.internalError(\"got something odd from checkInvariant: \" + _JPS_retVal.getClass());");
    code.append("}");

    code.append("if(_JPS_retVal != null && !((Boolean)_JPS_retVal).booleanValue()) {");
    code.append("org.tcfreenet.schewe.assert.AssertTools.unlockMethod(\"");
    code.append(methodName);
    code.append("\");");
    code.append("return false;");
    code.append("}");
    code.append("}");

  
    //[jpschewe:20000116.1749CST] FIX still need to add to this to do interface
    //invariants first and keep track of which interface they're from

    addConditionChecks(code, assertClass.getInvariants(), false);

    code.append("org.tcfreenet.schewe.assert.AssertTools.unlockMethod(\"");
    code.append(methodName);
    code.append("\");");
    code.append("return _JPS_retVal == null || ((Boolean)_JPS_retVal).booleanValue();");
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
    final StringBuffer code = new StringBuffer();
    String mclassName = assertMethod.getContainingClass().getFullName().replace('.', '_');
    mclassName = mclassName.replace('$', '_');
    
    code.append("if(!__");
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
        }
        else {
          first = false;
        }
        final StringPair sp = (StringPair)paramIter.next();
        final String paramName = sp.getStringTwo();
        code.append(paramName);
      }
    }
    
    code.append(")) {");
    code.append("org.tcfreenet.schewe.assert.AssertTools.preConditionFailed(org.tcfreenet.schewe.assert.AssertTools.getCurrentAssertionViolation());");
    code.append("}");
    
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
    final String dummyClassName = assertMethod.getContainingClass().createDummyConstructorClassName();
    
    /*
      insert:
      this(param0, param1, new AssertDummy#(param0, param1));
      checkInvariant();
      checkPostConditions(param0, param0, param1, param1);
      }
      static private class AssertDummy# {
      public AssertDummy#(param0, param1) {
      checkPreConditions(param0, param1);
      }
      }
      private className(param0, param1) {
    */
    code.append("this(");
    //put param names in here once
    {
      boolean first = true;
      final Iterator paramIter = assertMethod.getParams().iterator();
      while(paramIter.hasNext()) {
        if(!first) {
          code.append(", ");
        }
        else {
          first = false;
        }
        final StringPair sp = (StringPair)paramIter.next();
        final String paramName = sp.getStringTwo();
        code.append(paramName);
      }
    }

    if(!assertMethod.getParams().isEmpty()) {
      code.append(",");
    }
    code.append("new ");
    code.append(dummyClassName);
    code.append("(");

    //put param names in here once
    {
      boolean first = true;
      final Iterator paramIter = assertMethod.getParams().iterator();
      while(paramIter.hasNext()) {
        if(!first) {
          code.append(", ");
        }
        else {
          first = false;
        }
        final StringPair sp = (StringPair)paramIter.next();
        final String paramName = sp.getStringTwo();
        code.append(paramName);
      }
    }
      
    code.append("));");

    //call checkInvariant
    code.append(generateInvariantCall(assertMethod.getContainingClass()));
                
    //call post conditions
    code.append(generatePostConditionCall(assertMethod));
    
    code.append("}");
    code.append("static private class ");
    code.append(dummyClassName);
    code.append(" {");
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
        }
        else {
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

    //Just put in a call to the pre conditions
    code.append(generatePreConditionCall(assertMethod));
                
    code.append("}");
    code.append("}");
    code.append("private ");
    code.append(assertMethod.getName());
    code.append("(");

    //put params with types in here with old values after each value
    {
      boolean first = true;
      final Iterator paramIter = assertMethod.getParams().iterator();
      while(paramIter.hasNext()) {
        if(!first) {
          code.append(", ");
        }
        else {
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
    code.append(" _JPS_ad) {");

    return code.toString();
  }
  
    
  //   /**
  //      This code should be inserted right after the precondition and invariant calls.
     
  //      @param assertMethod the method to generate the old values for
  //      @return the code to generate all of the old values to be used when calling the post condition check

  //      @pre (assertMethod != null)
  //   **/
  //   static public String generateOldValues(final AssertMethod assertMethod) {
  //     StringBuffer code = new StringBuffer();

  //     Iterator paramIter = assertMethod.getParams().iterator();
  //     while(paramIter.hasNext()) {
  //       StringPair sp = (StringPair)paramIter.next();
  //       String paramType = sp.getStringOne();
  //       String paramName = sp.getStringTwo();
  //       code.append("final ");
  //       code.append(paramType);
  //       code.append(" __old");
  //       code.append(paramName);
  //       code.append(" = ");
  //       code.append(paramName);
  //       code.append(";");
  //     }

  //     return code.toString();
  //   }

  /**
     @param assertMethod the method to generate the post condition check call for
     @param retVal the actual return statement that is in the code before it
     is instrumented, ignored if the method has no return value
     @return code to call the post condition check for a method

     @pre (assertMethod != null)
  **/
  static public String generatePostConditionCall(final AssertMethod assertMethod) {
    final StringBuffer code = new StringBuffer();
    final String retType = assertMethod.getReturnType();
    String mclassName = assertMethod.getContainingClass().getFullName().replace('.', '_');
    mclassName = mclassName.replace('$', '_');
    String shortmclassName = assertMethod.getContainingClass().getName().replace('.', '_');
    shortmclassName = shortmclassName.replace('$', '_');

      
    code.append("if(!__");
    code.append(mclassName);
    code.append("_check");
    code.append(assertMethod.getName());
    code.append("PostConditions(");

    boolean first = true;
    if(!assertMethod.isVoid()) {
      code.append("__retVal");
      code.append(shortmclassName);
      first = false;
    }
    else {
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
      }
      else {
        first = false;
      }
      //       if(!assertMethod.isConstructor()) {
      //         //constructors just pass the param name in twice since they're inside
      //         //a special class anyway
      //         code.append("__old");
      //       }
      //       code.append(paramName);
      //       code.append(", ");
      code.append(paramName);
    }
    code.append(")) {");
    code.append("org.tcfreenet.schewe.assert.AssertTools.postConditionFailed(org.tcfreenet.schewe.assert.AssertTools.getCurrentAssertionViolation());");
    code.append("}");

    if(!assertMethod.isVoid()) {
      code.append("return __retVal");
      code.append(shortmclassName);
      code.append(";");
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
    final String methodName = "__" + mclassName + "_check" + assertMethod.getName() + "PreConditions";
    
    final StringBuffer code = new StringBuffer();

    if(assertMethod.isStatic() || assertMethod.isConstructor()) {
      code.append("static ");
    }
    if(assertMethod.isPrivate()) {
      code.append("private ");
    }
    else {
      code.append("protected ");
    }
    code.append("boolean ");
    code.append(methodName);
    code.append("(");

    boolean first = true;
    Iterator paramIter = assertMethod.getParams().iterator();
    while(paramIter.hasNext()) {
      if(! first) {
        code.append(",");
      }
      else {
        first = false;
      }
      final StringPair sp = (StringPair)paramIter.next();
      code.append("final ");
      code.append(sp.getStringOne());
      code.append(' ');
      code.append(sp.getStringTwo());
    }
    code.append(") {");
    code.append("if(!org.tcfreenet.schewe.assert.AssertTools.lockMethod(\"");
    code.append(methodName);
    code.append("\")) { return true; }");
    if(!assertMethod.isPrivate() && !assertMethod.isConstructor()) {
      //[jpschewe:20000220.0933CST] don't check super class conditions if the method is private or a constructor
      code.append("Object _JPS_retVal = null;");

      //Get the class object
      code.append("final String _JPS_className = \"");
      code.append(className);
      code.append("\";");
      code.append("final Class _JPS_thisClass = org.tcfreenet.schewe.assert.AssertTools.classForName(_JPS_className);");
      code.append("if(_JPS_thisClass == null) {");
      code.append("org.tcfreenet.schewe.assert.AssertTools.internalError(\"Could not find class \" + _JPS_className);");
      code.append("}");      

      //[jpschewe:20000213.1552CST] need method parameters here, just the class objects, use getClassObjectForClass
      code.append("final Class[] _JPS_methodArgs = {");
      first = true;
      paramIter = assertMethod.getParams().iterator();
      while(paramIter.hasNext()) {
        if(! first) {
          code.append(", ");
        }
        else {
          first = false;
        }
        final StringPair sp = (StringPair)paramIter.next();
        code.append(getClassObjectForClass(sp.getStringOne()));
      }
      code.append("};");
                
      code.append("final java.lang.reflect.Method _JPS_superMethod = org.tcfreenet.schewe.assert.AssertTools.findSuperMethod(_JPS_thisClass, \"check");
      code.append(assertMethod.getName());
      code.append("PreConditions\", _JPS_methodArgs);");

      code.append("if(_JPS_superMethod != null) {");
      code.append("final Object[] _JPS_args = {");
      
      first = true;      
      //Need parameters here, just the parameter names
      paramIter = assertMethod.getParams().iterator();
      while(paramIter.hasNext()) {
        if(! first) {
          code.append(", ");
        }
        else {
          first = false;
        }
        final StringPair sp = (StringPair)paramIter.next();
        code.append(getObjectForParam(sp.getStringOne(), sp.getStringTwo()));
      }    
      code.append("};");
      code.append("try {");
      code.append("_JPS_retVal = _JPS_superMethod.invoke(");
      if(assertMethod.isStatic() || assertMethod.isConstructor()) {
        code.append("null");
      }
      else {
        code.append("this");
      }
      code.append(", _JPS_args);");
      code.append("}");
      code.append("catch(IllegalAccessException _JPS_iae) {");
      //[jpschewe:20000220.0936CST] just means that the super method is private and we really shouldn't be calling it in the first place      
      //code.append("org.tcfreenet.schewe.assert.AssertTools.internalError(\"Not enough access executing superClass check");
      //code.append(assertMethod.getName());
      //code.append("PreConditions: \" + _JPS_iae.getMessage());");
      //Pretend it returned true :)
      code.append("_JPS_retVal = Boolean.TRUE;");
      code.append("}");
      code.append("catch(IllegalArgumentException _JPS_iae) {");
      code.append("org.tcfreenet.schewe.assert.AssertTools.internalError(\"IllegalArgument executing superClass check");
      code.append(assertMethod.getName());
      code.append("PreConditions: \" + _JPS_iae.getMessage() + \" methodArgs \" + org.tcfreenet.schewe.utils.Functions.printArray(_JPS_methodArgs) + \" args \" + org.tcfreenet.schewe.utils.Functions.printArray(_JPS_args));");
      code.append("}");
      code.append("catch(java.lang.reflect.InvocationTargetException _JPS_ite) {");
      code.append("_JPS_ite.getTargetException().printStackTrace();");
      code.append("}");
      code.append("if(_JPS_retVal == null) {");
      code.append("org.tcfreenet.schewe.assert.AssertTools.internalError(\"got null from checkPreConditions\");");
      code.append("}");
      //PreConditions are ORed
      code.append("else if(((Boolean)_JPS_retVal).booleanValue()) {");
      code.append("org.tcfreenet.schewe.assert.AssertTools.unlockMethod(\"");
      code.append(methodName);
      code.append("\");");
      code.append("return true;");
      code.append("}");
      
      code.append("}");


    
      //[jpschewe:20000116.1749CST] FIX still need to add to this to do interface
      //preconditions first and keep track of which interface they're from
    }
    
    addConditionChecks(code, assertMethod.getPreConditions(), false);

    code.append("org.tcfreenet.schewe.assert.AssertTools.unlockMethod(\"");
    code.append(methodName);
    code.append("\");");
    if(assertMethod.isPrivate() || assertMethod.isConstructor()) {
      code.append("return true;");
    }
    else {
      code.append("return _JPS_retVal == null || ((Boolean)_JPS_retVal).booleanValue();");
    }
    code.append("}");
    
    return code.toString();
  }

  /**
     @return the code neccessary to implement the post conditions on this method
  **/
  static public String generatePostConditionMethod(final AssertMethod assertMethod) {
    final String className = assertMethod.getContainingClass().getFullName();
    String mclassName = className.replace('.', '_');
    mclassName = mclassName.replace('$', '_');
    final String methodName = "__" + mclassName + "_check" + assertMethod.getName() + "PostConditions";
    StringBuffer code = new StringBuffer();

    if(assertMethod.isStatic() || assertMethod.isConstructor()) {
      code.append("static ");
    }

    if(assertMethod.isPrivate()) {
      code.append("private ");
    }
    else {
      code.append("protected ");
    }
    code.append("boolean ");
    code.append(methodName);
    code.append("(");
    boolean first = true;
    if(!assertMethod.isVoid()) {
      code.append(assertMethod.getReturnType());
      code.append(" ");
      code.append("__retVal");
      first = false;
    }
    else {
      code.append("Object __dummyretVal");
      first = false;
    }
      
    Iterator paramIter = assertMethod.getParams().iterator();
    while(paramIter.hasNext()) {
      final StringPair sp = (StringPair)paramIter.next();
      final String paramType = sp.getStringOne();
      final String paramName = sp.getStringTwo();
      if(! first) {
        code.append(",");
      }
      else {
        first = false;
      }
      //         code.append(paramType);
      //         code.append(" ");
      //         code.append("__old");
      //         code.append(paramName);
      //         code.append(", ");
      code.append("final ");
      code.append(paramType);
      code.append(' ');
      code.append(paramName);
    }
    code.append(") {");
    code.append("if(!org.tcfreenet.schewe.assert.AssertTools.lockMethod(\"");
    code.append(methodName);
    code.append("\")) { return true; }");
    if(!assertMethod.isPrivate() && !assertMethod.isConstructor()) {
      //don't bother checking for super method if we're private or a constructor
      code.append("Object _JPS_retVal = null;");

      //Get the class object
      code.append("final String _JPS_className = \"");
      code.append(className);
      code.append("\";");
      code.append("final Class _JPS_thisClass = org.tcfreenet.schewe.assert.AssertTools.classForName(_JPS_className);");
      code.append("if(_JPS_thisClass == null) {");
      code.append("org.tcfreenet.schewe.assert.AssertTools.internalError(\"Could not find class \" + _JPS_className);");
      code.append("}");
    
      //need method parameters here, just the class objects, use getClassObjectForClass
      code.append("final Class[] _JPS_methodArgs = {");
      first = true;
      
      //Need return value here too
      if(!assertMethod.isVoid()) {
        code.append(getClassObjectForClass(assertMethod.getReturnType()));
        first = false;
      }
      else {
        code.append("Object.class"); //For dummy return value
        first = false;
      }
      paramIter = assertMethod.getParams().iterator();
      while(paramIter.hasNext()) {
        if(! first) {
          code.append(", ");
        }
        else {
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
                
      code.append("final java.lang.reflect.Method _JPS_superMethod = org.tcfreenet.schewe.assert.AssertTools.findSuperMethod(_JPS_thisClass, \"check");
      code.append(assertMethod.getName());
      code.append("PostConditions\", _JPS_methodArgs);");

      code.append("if(_JPS_superMethod != null) {");
      code.append("final Object[] _JPS_args = {");
      first = true;      
      //[jpschewe:20000213.1552CST] need parameters here, just the parameter names
      if(!assertMethod.isVoid()) {
        code.append(getObjectForParam(assertMethod.getReturnType(), "__retVal"));
        first = false;
      }
      else {
        code.append("null");
        first = false;
      }
      
      paramIter = assertMethod.getParams().iterator();
      while(paramIter.hasNext()) {
        if(! first) {
          code.append(", ");
        }
        else {
          first = false;
        }
        StringPair sp = (StringPair)paramIter.next();
        code.append(getObjectForParam(sp.getStringOne(), sp.getStringTwo()));
        //         // once for old
        //         code.append(", ");
        //         code.append(getObjectForParam(sp.getStringOne(), "__old" + sp.getStringTwo()));
      }
      code.append("};");
      code.append("try {");
      code.append("_JPS_retVal = _JPS_superMethod.invoke(");
      if(assertMethod.isStatic() || assertMethod.isConstructor()) {
        code.append("null");
      }
      else {
        code.append("this");
      }
      code.append(", _JPS_args);");
      code.append("}");
      code.append("catch(IllegalAccessException _JPS_iae) {");
      //[jpschewe:20000220.0936CST] just means that the super method is private and we really shouldn't be calling it in the first place
      //code.append("org.tcfreenet.schewe.assert.AssertTools.internalError(\"Not enough access executing superClass check");
      //code.append(assertMethod.getName());
      //code.append("PostConditions: \" + _JPS_iae.getMessage());");
      //Pretend it returned true :)
      code.append("_JPS_retVal = Boolean.TRUE;");
      code.append("}");
      code.append("catch(IllegalArgumentException _JPS_iae) {");
      code.append("org.tcfreenet.schewe.assert.AssertTools.internalError(\"IllegalArgument executing superClass check");
      code.append(assertMethod.getName());
      code.append("PostConditions: \" + _JPS_iae.getMessage() + \" methodArgs \" + org.tcfreenet.schewe.utils.Functions.printArray(_JPS_methodArgs) + \" args: \" + org.tcfreenet.schewe.utils.Functions.printArray(_JPS_args));");
      code.append("}");
      code.append("catch(java.lang.reflect.InvocationTargetException _JPS_ite) {");
      code.append("_JPS_ite.getTargetException().printStackTrace();");
      code.append("}");
      code.append("if(_JPS_retVal == null) {");
      code.append("org.tcfreenet.schewe.assert.AssertTools.internalError(\"got null from checkPostConditions\");");
      code.append("}");
      //PostConditions are ANDed
      code.append("else if(!((Boolean)_JPS_retVal).booleanValue()) {");
      code.append("org.tcfreenet.schewe.assert.AssertTools.unlockMethod(\"");
      code.append(methodName);
      code.append("\");");
      code.append("return false;");
      code.append("}");
      
      code.append("}");


    
      //[jpschewe:20000116.1749CST] FIX still need to add to this to do interface
      //postconditions first and keep track of which interface they're from
    }
    
    addConditionChecks(code, assertMethod.getPostConditions(), true);
    code.append("org.tcfreenet.schewe.assert.AssertTools.unlockMethod(\"");
    code.append(methodName);
    code.append("\");");
    if(assertMethod.isPrivate() || assertMethod.isConstructor()) {
      code.append("return true;");
    }
    else {
      code.append("return _JPS_retVal == null || ((Boolean)_JPS_retVal).booleanValue();");
    }
    code.append("}");
    
    return code.toString();
  }

  /**
     The RE used to rewrite post conditions.
  **/
  static private RE _postConditionRewrite;
  /**
     Used to escape quotes
  **/
  static private RE _escapeQuotes;
  static {
    try {
      _postConditionRewrite = new RE("\\$return");
      _escapeQuotes = new RE("(\"|\')");
    }
    catch(REException re) {
      System.err.println("This is really bad!");
      re.printStackTrace();
      System.exit(1);
    }
  }
  
    
  /**
     Append to code code to check for the assert conditions in tokens.

     @param postCondition true if we're adding checks for a postcondition method. 
  **/
  static private void addConditionChecks(final StringBuffer code,
                                         final Set tokens,
                                         final boolean postCondition) {
    final Iterator iter = tokens.iterator();
    while(iter.hasNext()) {
      final AssertToken token = (AssertToken)iter.next();
      final String condition = token.getCondition();
      final String message = token.getMessage();
      code.append("if(!");
      if(postCondition) {
        code.append(_postConditionRewrite.substituteAll(condition, "__retVal"));
      }
      else {
        if(_postConditionRewrite.getMatch(condition) != null) {
          System.err.println("$return found in precondition! " + token.getText());
        }
        code.append(condition);
      }
      code.append(") {");
      String errorMessage = "";
      if(message != null) {
        errorMessage = message + " + ";
      }
      errorMessage += "\" " + _escapeQuotes.substituteAll(condition, "\\$1") + "\"";
    
      code.append("org.tcfreenet.schewe.assert.AssertionViolation _JPS_av = new org.tcfreenet.schewe.assert.AssertionViolation(");
      code.append(errorMessage);
      code.append(");");
      code.append("org.tcfreenet.schewe.assert.AssertTools.setCurrentAssertionViolation(_JPS_av);");
    
      code.append("return false;");
      code.append("}");
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

  /**
     Take paramName and turn it into an object.  This only effects primatives,
     if paramType is a primative, return code that will generate a properly
     wrapped primative object, otherwise just return paramName.
  **/
  static public String getObjectForParam(final String paramType,
                                         final String paramName) {
    if(paramType.equals("int")) {
      return "new Integer(" + paramName + ")";
    }
    else if(paramType.equals("long")) {
      return "new Long(" + paramName + ")";
    }
    else if(paramType.equals("float")) {
      return "new Float(" + paramName + ")";
    }
    else if(paramType.equals("double")) {
      return "new Double(" + paramName + ")";
    }
    else if(paramType.equals("boolean")) {
      return "new Boolean(" + paramName + ")";
    }
    else if(paramType.equals("byte")) {
      return "new Byte(" + paramName + ")";
    }
    else if(paramType.equals("char")) {
      return "new Character(" + paramName + ")";
    }
    else if(paramType.equals("short")) {
      return "new Short(" + paramName + ")";
    }

    return paramName;
  }
}
