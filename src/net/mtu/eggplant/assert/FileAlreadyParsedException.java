/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.assert;

/**
   Exception thrown to break out of the parser if the destination file is
   newer than the source file.
**/
public class FileAlreadyParsedException extends RuntimeException {

  public FileAlreadyParsedException() {
    super();
  }

}
