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
final public class AssertTools {

  /**
     find the superclasses method, this is my version of a superClass method,
     this means that the method name is __<packageName>_<className>_methodName
     where packageName is the package the class is in with the '.'s replaced
     with '_' and className is the name of the class.
     
     @return the method found, null for no such method.

     @pre (thisClass != null)
     @pre (methodName != null)
     @pre (methodArgs != null)
  **/
  static public Method findSuperMethod(final Class thisClass, final String methodName, final Class[] methodArgs) {
    Class superClass = thisClass.getSuperclass();
    Method superMethod = null;

    if(superClass != null) {
      while(superMethod == null && superClass != null) {
        try {
          String fullClassName = superClass.getName().replace('.', '_');
          String mname = "__" + fullClassName + "_" + methodName;
          superMethod = superClass.getDeclaredMethod(mname, methodArgs);
        }
        catch(NoSuchMethodException nsme) {
          // no method, don't bother
          //superMethod = null;
          //Try up another level
          superClass = superClass.getSuperclass();
        }
        catch(SecurityException se) {
          //This is real bad, spit out internal error here
          System.err.println("Security exception trying to find method " + methodName + ": " + se);
          return null;
        }
      }
    }

    return superMethod;
  }

  static private AssertionViolation _currentAssertionViolation = null;

  /**
     set the assertion violation that should be throw next.
  **/
  static public void setCurrentAssertionViolation(final AssertionViolation violation) {
    _currentAssertionViolation = violation;
  }

  static public AssertionViolation getCurrentAssertionViolation() {
    return _currentAssertionViolation;
  }

  /**
     called when an assert fails.  Checks the system property ASSERT_CONDITION
     to decide what to do.

     <ul>
     <li>TRUE - signal the failure (default)</li>
     <li>FALSE - ignore the failure</li>
     </ul>
     
     @param av AssertionViolation with information about the failure
     
     @pre (av != null)
  **/
  static public void assertFailed(final AssertionViolation av) {
    String behavior = System.getProperty("ASSERT_CONDITION", "TRUE");
    if(behavior.equalsIgnoreCase("TRUE")) {
      fail(av);
    }
  }

  /**
     called when an invariant fails.  Checks the system property INVARIANT_CONDITION
     to decide what to do.

     <ul>
     <li>TRUE - signal the failure (default)</li>
     <li>FALSE - ignore the failure</li>
     </ul>
     
     @param av AssertionViolation with information about the failure
     
     @pre (av != null)
  **/
  static public void invariantFailed(final AssertionViolation av) {
    String behavior = System.getProperty("INVARIANT_CONDITION", "TRUE");
    if(behavior.equalsIgnoreCase("TRUE")) {
      fail(av);
    }
  }
  
  /**
     called when a post condition fails.  Checks the system property
     POST_CONDITION to decide what to do.

     <ul>
     <li>TRUE - signal the failure (default)</li>
     <li>FALSE - ignore the failure</li>
     </ul>
     
     @param av AssertionViolation with information about the failure
     
     @pre (av != null)
  **/
  static public void postConditionFailed(final AssertionViolation av) {
    String behavior = System.getProperty("POST_CONDITION", "TRUE");
    if(behavior.equalsIgnoreCase("TRUE")) {
      fail(av);
    }
  }

  /**
     Called when a precondition fails.  Checks the system property
     PRE_CONDITION to decide what to do.

     <ul>
     <li>TRUE - signal the failure (default)</li>
     <li>FALSE - ignore the failure</li>
     </ul>
     
     @param av AssertionViolation with information about the failure

     @pre (av != null)
  **/
  static public void preConditionFailed(final AssertionViolation av) {
    String behavior = System.getProperty("PRE_CONDITION", "TRUE");
    if(behavior.equalsIgnoreCase("TRUE")) {
      fail(av);
    }
  }

  /**
     Called to signal an assertion failure.  This checks the system property
     ASSERT_BEHAVIOR to decide what to do.
     
     <ul>
     <li>EXIT - print out the stack trace and exit (default)</li>
     <li>CONTINUE - print out the stack trace and continue on</li>
     <li>EXCEPTION - throw the AssertionViolation</li>
     </ul>
  **/
  static public void fail(final AssertionViolation av) {
    String assertBehavior = System.getProperty("ASSERT_BEHAVIOR", "EXIT");    
    if(assertBehavior.equalsIgnoreCase("EXIT")) {
      av.printStackTrace();
      System.exit(1);
    }
    else if(assertBehavior.equalsIgnoreCase("CONTINUE")) {
      av.printStackTrace();
    }
    else if(assertBehavior.equalsIgnoreCase("EXCEPTION")) {
      throw av;
    }
  }

  /**
     called whenever an erorr occurs in the code that was generated that is
     not caused by the user.  Generate some useful message and thwn blow up.
  **/
  static public void internalError(final String message) {
    throw new RuntimeException("You have found a bug!  Please see the README for instructions on reporting bugs.\n" + message);
  }


  /**
     Set the extensions to be used for files.

     @param sourceExtension the extension on the source files, defaults to 'java'
     @param instrumentedExtension the extension on the instrumented files, defalts to 'ijava'
  **/
  static public void setExtensions(final String sourceExtension,
                                   final String instrumentedExtension) {
    _sourceExtension = sourceExtension;
    _instrumentedExtension = instrumentedExtension;
  }

  /**
     @return the extension for the source files
  **/
  static public String getSourceExtension() {
    return _sourceExtension;
  }

  /**
     @return the extension for the instrumented files
  **/
  static public String getInstrumentedExtension() {
    return _instrumentedExtension;
  }

  static private String _sourceExtension = "java";
  static private String _instrumentedExtension = "ijava";
}
