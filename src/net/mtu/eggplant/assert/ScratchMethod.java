/*
 * Copyright (c) 2000
 *      Jon Schewe.  All rights reserved
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * I'd appreciate comments/suggestions on the code jpschewe@mtu.net
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
