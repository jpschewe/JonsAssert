/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.assert;

import org.tcfreenet.schewe.utils.StringPair;

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
      errorMessage = message + " + ";
    }
    errorMessage += "\" " + condition + "\"";

    StringBuffer code = new StringBuffer();
    code.append("{");
    code.append("if(! ");
    code.append(condition);
    code.append(") { ");
    code.append("org.tcfreenet.schewe.assert.AssertTools.assertFailed(new org.tcfreenet.schewe.assert.AssertionViolation(");
    code.append('"');
    code.append(errorMessage);
    code.append('"');
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
    StringBuffer code = new StringBuffer();
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
    String className = assertClass.getFullName();
    String mclassName = className.replace('.', '_');
    mclassName = mclassName.replace('$', '_');
    
    StringBuffer code = new StringBuffer();
    code.append("protected boolean __");
    code.append(mclassName);
    code.append("_checkInvariant() {");
    code.append("Class _JPS_thisClass = null;");
    code.append("Object _JPS_retVal = null;");    
    code.append("String _JPS_className = \"");
    code.append(className);
    code.append("\";");
    code.append("try {");
    code.append("_JPS_thisClass = Class.forName(_JPS_className);");
    code.append("}");
    code.append("catch(ClassNotFoundException _JPS_cnfe) {");
    code.append("org.tcfreenet.schewe.assert.AssertTools.internalError(\"Got error getting the class object for class \" + _JPS_className + \" \" + _JPS_cnfe);");
    code.append("}");
  
    code.append("Class[] _JPS_methodArgs = new Class[0];");
    code.append("java.lang.reflect.Method _JPS_superMethod = org.tcfreenet.schewe.assert.AssertTools.findSuperMethod(_JPS_thisClass, \"checkInvariant\", _JPS_methodArgs);");

    code.append("if(_JPS_superMethod != null) {");
    //invoke it, pass on exceptions
    code.append("Object[] _JPS_args = new Object[0];");
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
    code.append("org.tcfreenet.schewe.assert.AssertTools.internalError(\"IllegalArgument executing checkInvariant method on super class: \" + _JPS_iae.getMessage() + \" methodArgs \" + _JPS_methodArgs + \" args \" + _JPS_args);");
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
    code.append("return false;");
    code.append("}");
    code.append("}");

  
    //[jpschewe:20000116.1749CST] FIX still need to add to this to do interface
    //invariants first and keep track of which interface they're from

    addConditionChecks(code, assertClass.getInvariants());

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
    StringBuffer code = new StringBuffer();
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
      Iterator paramIter = assertMethod.getParams().iterator();
      while(paramIter.hasNext()) {
        if(!first) {
          code.append(", ");
        }
        else {
          first = false;
        }
        StringPair sp = (StringPair)paramIter.next();
        String paramName = sp.getStringTwo();
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
  static public String generateConstrauctorAssertions(final AssertMethod assertMethod) {
    StringBuffer code = new StringBuffer();
    String dummyClassName = assertMethod.getContainingClass().createDummyConstructorClassName();
    
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
      Iterator paramIter = assertMethod.getParams().iterator();
      while(paramIter.hasNext()) {
        if(!first) {
          code.append(", ");
        }
        else {
          first = false;
        }
        StringPair sp = (StringPair)paramIter.next();
        String paramName = sp.getStringTwo();
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
      Iterator paramIter = assertMethod.getParams().iterator();
      while(paramIter.hasNext()) {
        if(!first) {
          code.append(", ");
        }
        else {
          first = false;
        }
        StringPair sp = (StringPair)paramIter.next();
        String paramName = sp.getStringTwo();
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
      Iterator paramIter = assertMethod.getParams().iterator();
      while(paramIter.hasNext()) {
        if(!first) {
          code.append(", ");
        }
        else {
          first = false;
        }
        StringPair sp = (StringPair)paramIter.next();
        String paramType = sp.getStringOne();
        String paramName = sp.getStringTwo();
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
      Iterator paramIter = assertMethod.getParams().iterator();
      while(paramIter.hasNext()) {
        if(!first) {
          code.append(", ");
        }
        else {
          first = false;
        }
        StringPair sp = (StringPair)paramIter.next();
        String paramType = sp.getStringOne();
        String paramName = sp.getStringTwo();
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
  
    
  /**
     This code should be inserted right after the precondition and invariant calls.
     
     @param assertMethod the method to generate the old values for
     @return the code to generate all of the old values to be used when calling the post condition check

     @pre (assertMethod != null)
  **/
  static public String generateOldValues(final AssertMethod assertMethod) {
    StringBuffer code = new StringBuffer();

    Iterator paramIter = assertMethod.getParams().iterator();
    while(paramIter.hasNext()) {
      StringPair sp = (StringPair)paramIter.next();
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
  static public String generatePostConditionCall(final AssertMethod assertMethod) {
    StringBuffer code = new StringBuffer();
    String retType = assertMethod.getReturnType();
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
    Iterator paramIter = assertMethod.getParams().iterator();
    while(paramIter.hasNext()) {
      StringPair sp = (StringPair)paramIter.next();
      String paramName = sp.getStringTwo();
      if(! first) {
        code.append(", ");
      }
      else {
        first = false;
      }
      if(!assertMethod.isConstructor()) {
        //constructors just pass the param name in twice since they're inside
        //a special class anyway
        code.append("__old");
      }
      code.append(paramName);
      code.append(", ");
      code.append(paramName);
    }
    code.append(")) {");
    code.append("org.tcfreenet.schewe.assert.AssertTools.preConditionFailed(org.tcfreenet.schewe.assert.AssertTools.getCurrentAssertionViolation());");
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
    String className = assertMethod.getContainingClass().getFullName();
    String mclassName = className.replace('.', '_');
    mclassName = mclassName.replace('$', '_');    
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
    code.append("boolean __");
    code.append(mclassName);
    code.append("_check");
    code.append(assertMethod.getName());
    code.append("PreConditions(");

    boolean first = true;
    Iterator paramIter = assertMethod.getParams().iterator();
    while(paramIter.hasNext()) {
      if(! first) {
        code.append(",");
      }
      else {
        first = false;
      }
      StringPair sp = (StringPair)paramIter.next();
      code.append(sp.getStringOne());
      code.append(' ');
      code.append(sp.getStringTwo());
    }
    code.append(") {");
    if(!assertMethod.isPrivate() && !assertMethod.isConstructor()) {
      //[jpschewe:20000220.0933CST] don't check super class conditions if the method is private or a constructor
      code.append("Object _JPS_retVal = null;");
      code.append("Class _JPS_thisClass = null;");
      code.append("String _JPS_className = \"");
      code.append(className);
      code.append("\";");
      code.append("try {");
      code.append("_JPS_thisClass = Class.forName(_JPS_className);");
      code.append("}");
      code.append("catch(ClassNotFoundException _JPS_cnfe) {");
      code.append("org.tcfreenet.schewe.assert.AssertTools.internalError(\"Got error getting the class object for class \" + _JPS_className + \" \" + _JPS_cnfe);");
      code.append("}");
    

      //[jpschewe:20000213.1552CST] need method parameters here, just the class objects, use getClassObjectForClass
      code.append("Class[] _JPS_methodArgs = {");
      first = true;
      paramIter = assertMethod.getParams().iterator();
      while(paramIter.hasNext()) {
        if(! first) {
          code.append(", ");
        }
        else {
          first = false;
        }
        StringPair sp = (StringPair)paramIter.next();
        code.append(getClassObjectForClass(sp.getStringOne()));
      }
      code.append("};");
                
      code.append("java.lang.reflect.Method _JPS_superMethod = org.tcfreenet.schewe.assert.AssertTools.findSuperMethod(_JPS_thisClass, \"check");
      code.append(assertMethod.getName());
      code.append("PreConditions\", _JPS_methodArgs);");

      code.append("if(_JPS_superMethod != null) {");
      code.append("Object[] _JPS_args = {");
      //[jpschewe:20000213.1552CST] need parameters here, just the parameter names
      first = true;
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
      code.append("PreConditions: \" + _JPS_iae.getMessage() + \" methodArgs \" + _JPS_methodArgs + \" args \" + _JPS_args);");
      code.append("}");
      code.append("catch(java.lang.reflect.InvocationTargetException _JPS_ite) {");
      code.append("_JPS_ite.getTargetException().printStackTrace();");
      code.append("}");
      code.append("if(_JPS_retVal == null) {");
      code.append("org.tcfreenet.schewe.assert.AssertTools.internalError(\"got null from checkInvariant\");");
      code.append("}");
      code.append("else if(!((Boolean)_JPS_retVal).booleanValue()) {");
      code.append("return false;");
      code.append("}");
      
      code.append("}");


    
      //[jpschewe:20000116.1749CST] FIX still need to add to this to do interface
      //preconditions first and keep track of which interface they're from
    }
    
    addConditionChecks(code, assertMethod.getPreConditions());

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
    String className = assertMethod.getContainingClass().getFullName();
    String mclassName = className.replace('.', '_');
    mclassName = mclassName.replace('$', '_');    
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
    code.append("boolean __");
    code.append(mclassName);
    code.append("_check");
    code.append(assertMethod.getName());
    code.append("PostConditions(");
      boolean first = true;
      if(!assertMethod.isVoid()) {
        code.append(assertMethod.getReturnType());
        code.append(" ");
        code.append("__retVal");
        first = false;
      }
      Iterator paramIter = assertMethod.getParams().iterator();
      while(paramIter.hasNext()) {
        StringPair sp = (StringPair)paramIter.next();
        String paramType = sp.getStringOne();
        String paramName = sp.getStringTwo();
        if(! first) {
          code.append(",");
        }
        else {
          first = false;
        }
        code.append(paramType);
        code.append(" ");
        code.append("__old");
        code.append(paramName);
        code.append(", ");
        code.append(paramType);
        code.append(' ');
        code.append(paramName);
      }
      code.append(") {");
    if(!assertMethod.isPrivate() && !assertMethod.isConstructor()) {
      //[jpschewe:20000220.0934CST] don't bother checking for super method if we're private or a constructor
      code.append("Object _JPS_retVal = null;");
      code.append("Class _JPS_thisClass = null;");
      code.append("String _JPS_className = \"");
      code.append(className);
      code.append("\";");
      code.append("try {");
      code.append("_JPS_thisClass = Class.forName(_JPS_className);");
      code.append("}");
      code.append("catch(ClassNotFoundException _JPS_cnfe) {");
      code.append("org.tcfreenet.schewe.assert.AssertTools.internalError(\"Got error getting the class object for class \" + _JPS_className + \" \" + _JPS_cnfe);");
      code.append("}");
    

      //[jpschewe:20000213.1552CST] need method parameters here, just the class objects, use getClassObjectForClass
      code.append("Class[] _JPS_methodArgs = {");
      first = true;
      
      //Need return value here too
      if(!assertMethod.isVoid()) {
        code.append(getClassObjectForClass(assertMethod.getReturnType()));
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
        String classObj = getClassObjectForClass(sp.getStringOne());
        code.append(classObj);
        //for old value
        code.append(", ");
        code.append(classObj);
      }
      code.append("};");
                
      code.append("java.lang.reflect.Method _JPS_superMethod = org.tcfreenet.schewe.assert.AssertTools.findSuperMethod(_JPS_thisClass, \"check");
      code.append(assertMethod.getName());
      code.append("PostConditions\", _JPS_methodArgs);");

      code.append("if(_JPS_superMethod != null) {");
      code.append("Object[] _JPS_args = {");
      first = true;      
      //[jpschewe:20000213.1552CST] need parameters here, just the parameter names
      if(!assertMethod.isVoid()) {
        code.append(getObjectForParam(assertMethod.getReturnType(), "__retVal"));
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
        // once for old
        code.append(", ");
        code.append(getObjectForParam(sp.getStringOne(), "__old" + sp.getStringTwo()));
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
      code.append("PostConditions: \" + _JPS_iae.getMessage() + \" methodArgs \" + _JPS_methodArgs + \" args \" + _JPS_args);");
      code.append("}");
      code.append("catch(java.lang.reflect.InvocationTargetException _JPS_ite) {");
      code.append("_JPS_ite.getTargetException().printStackTrace();");
      code.append("}");
      code.append("if(_JPS_retVal == null) {");
      code.append("org.tcfreenet.schewe.assert.AssertTools.internalError(\"got null from checkInvariant\");");
      code.append("}");
      code.append("else if(((Boolean)_JPS_retVal).booleanValue()) {");
      code.append("return true;");
      code.append("}");
      
      code.append("}");


    
      //[jpschewe:20000116.1749CST] FIX still need to add to this to do interface
      //postconditions first and keep track of which interface they're from
    }
    
    addConditionChecks(code, assertMethod.getPostConditions());

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
      code.append(") {");
      String errorMessage = "";
      if(message != null) {
        errorMessage = message + " + ";
      }
      errorMessage += "\" " + condition + "\"";
    
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
