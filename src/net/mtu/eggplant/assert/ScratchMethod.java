/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package net.mtu.eggplant.assert;

/**
   Scratch class with information to create a method.
**/
final /*package*/ class ScratchMethod {

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
    _hashCode = _theClass.getName().hashCode() ^ _methodName.hashCode(); //cache it once
  }

  /*package*/ Class _theClass;

  /*package*/ String _methodName;

  /*package*/ Class[] _methodArgs;

  private int _hashCode;
  
  public boolean equals(Object o) {
    if(o == this) {
      return true;
    }
                    
    if(o instanceof ScratchMethod) {
      ScratchMethod other = (ScratchMethod)o;
      if(_theClass.equals(other._theClass)
         && _methodName.equals(other._methodName)) {
        Class[] params1 = _methodArgs;
        Class[] params2 = other._methodArgs;
        if(params1.length == params2.length) {
          for(int i=0; i<params1.length; i++) {
            if(!params1[i].equals(params2[i])) {
              return false;
            }
          }
          return true;          
        }
      }
    }
    return false;
  }

  public int hashCode() {
    return _hashCode;
  }
  
}
