/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.assert;

/**
   Holds all the options values.
**/
public class Configuration {

  public boolean ignoreTimeStamp() {
    return _ignoreTimeStamp;
  }
  /*package*/ void setIgnoreTimeStamp(boolean b) {
    _ignoreTimeStamp = b;
  }
  private boolean _ignoreTimeStamp = false;

}
