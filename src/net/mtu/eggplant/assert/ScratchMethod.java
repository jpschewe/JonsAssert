/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.assert;

/**
   Scratch class with information to create a method.
**/
/*package*/ class ScratchMethod {

  /**
     @pre (theClass != null)
     @pre (methodName != null)
     @pre (methodArgs != null)
  **/
  public ScratchMethod(final Class theClass,
                       final String methodName,
                       final Class[] methodArgs) {
    _theClass = theClass;
    _methodName = methodName;
    _methodArgs = methodArgs;
  }

  private Class _theClass;
  public Class getTheClass() {
    return _theClass;
  }

  private String _methodName;
  public String getMethodName() {
    return _methodName;
  }

  private Class[] _methodArgs;
  public Class[] getMethodArgs() {
    return _methodArgs;
  }
  
  public boolean equals(Object o) {
    if(o instanceof ScratchMethod) {
      ScratchMethod other = (ScratchMethod)o;
      return _theClass.equals(other.getTheClass())
        && _methodName.equals(other.getMethodName())
        && _methodArgs.equals(other.getMethodArgs());
    }
    return false;
  }

}
