/*

  still need to check order of pre vs. invariant, or does it matter?

  remember only to check invariants on public instance methods, protected methods?, constructor exit
  
*/
public Object methodFoo(Object param1) {
  Object __oldParam1 = param1;
  if(!__checkInvariant()) {
    throw AssertTools.getCurrentAssertionViolation().fillInStackTrace();
  }
  if(!__check<methodName>PreConditions(oldParam1, param1)) {
    throw AssertTools.getCurrentAssertionViolation().fillInStackTrace();
  }



  /* was return b; */
  Object __retVal = b;
  if(!__checkInvariant()) {
    throw AssertTools.getCurrentAssertionViolation().fillInStackTrace();
  }
  if(!__check<methodName>PostConditions(__retVal, oldParam1, param1)) {
    throw AssertTools.getCurrentAssertionViolation().fillInStackTrace();
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

protected boolean __check<methodname>PreConditions(oldParam1, param1, ...) {
    // AND, as soon as a false is seen, return false;

    // do interface pre conditions first and keep track of which interface they're from
    
    if(!((Boolean)retVal).booleanValue()) {
        return false;
    }
}

  
protected boolean __checkInvariant() {
  Class thisClass = this.getClass();
  //Class superClass = thisClass.getSuperclass();
  Class[] methodArgs = new Class[0];
  Method superMethod = AssertTools.findSuperMethod(thisClass, "__checkInvariant", methodArgs);

  if(superMethod != null) {
    //invoke it, pass on exceptions
    Object[] args = new Object[0];
    try {
      retVal = superMethod.invoke(this, args);
    }
    catch(IllegalAccessException iae) {
      //bad, internal error, not enough access
    }
    catch(IllegalArgumentException iae) {
      //should never see, internal error
    }
    catch(InvocationTargetException ite) {
      throw ite.getTargetException(); // should be the AssertionViolationException
    }
  }

  if(retVal == null) {
    System.err.println("got null checkInvariant");
  }
  else if(! (retVal instanceof Boolean) ) {
    System.err.println("got something odd from checkInvariant");
  }

  if(!((Boolean)retVal).booleanValue()) {
    return false; // just return
  }
  
  //[jpschewe:20000103.1845CST] need to think about this one, should we return a value and store some state somewhere else or just throw the exception?

  // do interface invariants first and keep track of which interface they're from
  
  //for <condition> (conditions)
  if(<condition>) {
    AssertionViolation av = new AssertionViolation("Failed invariant\n" + (<message> != null ? <message> : "") + "\n" + <condition>);
    AssertTools.setCurrentAssertionViolation(av);
    //AssertTools.setCurrentAssertionViolation("Failed invariant\n" + (<message> != null ? <message> : "") + "\n" + <condition>);
    
    return false;
  }

  return true;
}

