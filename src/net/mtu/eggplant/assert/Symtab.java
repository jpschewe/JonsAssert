/*
  Need to read the PDF again to make sure I've got these right.
  
  Things to keep track of:
  methods: pre/post
  classes: methods, invariants

  This to keep track of from class to class:
  interfaces: pre/post, methods, invariant

  I figure I can just write a method called __checkPre<methodName>(same signature) and __checkPost<methodName>(same signature) to get pre and post conditions and then just use introspection to call the super method if it exists.  Need to explicitly code all interface pre/post/invariant methods into all classes that implement an interface.




*/
  
protected void __checkInvariant() throws AssertionViolationException {
  Class thisClass = this.getClass();
  Class superClass = thisClass.getSuperclass();
  Method superMethod = null;
  if(superClass != null) {
    try {
      Class[] classArgs = new Class[0];
      superMethod = superClass.getDeclaredMethod("__checkInvariant", classArgs);
    }
    catch(NoSuchMethodException nsme) {
      // no method, don't call it
      superMethod = null;
    }
    catch(SecurityException se) {
      //This is real bad, spit out internal error here
    }
  
    if(superMethod != null) {
      //invoke it, pass on exceptions
      Object[] args = new Object[0];
      try {
        superMethod.invoke(this, args);
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
  }

  // add users conditions here and throw an exception on the first failure
  //for <condition> (conditions)
  if(<condition>) {
    throw new AssertionViolationException("Failed invariant\n" + (<message> != null ? <message> : "") + "\n" + <condition>);
  }
  
}