/*

  still need to check order of pre vs. invariant, or does it matter?

  remember only to check invariants on public instance methods, protected methods?, constructor exit
  
*/
public Object methodFoo(Object param1) {
  if(!__checkInvariant()) {
    AssertTools.invariantFailed(AssertTools.getCurrentAssertionViolation());
  }
  if(!__check<methodName>PreConditions(param1, param2)) {
    AssertTools.preConditionFailed(AssertTools.getCurrentAssertionViolation());
  }
  Object __oldParam1 = param1;


  /* was return b; */
  Object __retVal = b;
  if(!__checkInvariant()) {
    AssertTools.invariantFailed(AssertTools.getCurrentAssertionViolation());
  }
  if(!__check<methodName>PostConditions(__retVal, oldParam1, param1)) {
    AssertTools.postConditionFailed(AssertTools.getCurrentAssertionViolation());
  }
  return __retVal;
}

protected boolean __check<methodname>PostConditions(__retVal, oldParam1, param1, ...) {
    // OR, as soon as a true is seen, return true

    // do interface post conditions first and keep track of which interface they're from
    
    if(((Boolean)retVal).booleanValue()) {
        return true;
    }

}

protected boolean __check<methodname>PreConditions(param1, ...) {
    // AND, as soon as a false is seen, return false;

    // do interface pre conditions first and keep track of which interface they're from
    
    if(!((Boolean)retVal).booleanValue()) {
        return false;
    }
}

  
protected boolean __checkInvariant() {
  Class thisClass;
  try {
    String className = "<fill in class name>";
    thisClass = Class.forName(className);
  }
  catch(ClassNotFoundException cnfe) {
    AssertTools.internalError("Got error getting the class object for class " + className + " " + cnfe);
  }
  
  Class[] methodArgs = new Class[0];
  Method superMethod = AssertTools.findSuperMethod(thisClass, "__checkInvariant", methodArgs);

  if(superMethod != null) {
    //invoke it, pass on exceptions
    Object[] args = new Object[0];
    try {
      retVal = superMethod.invoke(this, args);
    }
    catch(IllegalAccessException iae) {
      AssertTools.internalError("Not enough access executing super.__checkInvariant: " + iae.getMessage());
    }
    catch(IllegalArgumentException iae) {
      AssertTools.internalError("IllegalArgument executing super.__checkInvariant: " + iae.getMessage());
      //should never see, internal error
    }
    catch(InvocationTargetException ite) {
      throw ite.getTargetException();
    }
  }

  if(retVal == null) {
    AssertTools.internalError("got null checkInvariant");
  }
  else if(! (retVal instanceof Boolean) ) {
    AssertTools.internalError("got something odd from checkInvariant: " + retVal.getClass());
  }

  if(!((Boolean)retVal).booleanValue()) {
    return false;
  }
  

  //[jpschewe:20000116.1749CST] still need to add to this do interface
  //invariants first and keep track of which interface they're from
  
  //for <condition> (conditions)
  if(<condition>) {
    AssertionViolation av = new AssertionViolation((<message> != null ? <message> : "") + "\n" + <condition>);
    AssertTools.setCurrentAssertionViolation(av);
    
    return false;
  }

  return true;
}

