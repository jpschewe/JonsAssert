/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.assert;

/**
   Scratch class to track column information across multiple lexers.
**/
/*package*/ class ColumnTracker {
  /*package*/ ColumnTracker() {
    _column = 0;
    _tokenColumn = 0;
    _line = 1;
  }
  /*package*/ int _line;
  /*package*/ int _column;
  /*package*/ int _tokenColumn;
}
