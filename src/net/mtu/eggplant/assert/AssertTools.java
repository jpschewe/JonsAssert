/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.Assert;

import java.lang.reflect.Method;

/**
   class of static helper methods for assertions.
**/
public class AssertTools {

  /**
     find the superclasses method
     
     @return the method found, null for no such method.

     @pre (thisClass != null)
     @pre (methodName != null)
     @pre (methodArgs != null)
  **/
  final static public Method findSuperMethod(Class thisClass, String methodName, Class[] methodArgs) {
    Class superClass = thisClass.getSuperclass();
    Method superMethod = null;
    
    if(superClass != null) {
      try {
        superMethod = superClass.getDeclaredMethod(methodName, methodArgs);
      }
      catch(NoSuchMethodException nsme) {
        // no method, don't bother
        superMethod = null;
      }
      catch(SecurityException se) {
        //This is real bad, spit out internal error here
        System.err.println("Security exception trying to find method " + methodName + ": " + se);
        return null;
      }
    }

    return superMethod;
  }

  final static private AssertionViolation _currentAssertionViolation = null;

  /**
     set the assertion violation that should be throw next.
  **/
  final static public void setCurrentAssertionViolation(AssertionViolation violation) {
    _currentAssertionViolation = violation;
  }

  final static public AssertionViolation getCurrentAssertionViolation() {
    return _currentAssertionViolation;
  }
  
}
