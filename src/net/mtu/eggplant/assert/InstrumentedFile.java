/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package net.mtu.eggplant.assert;

import java.util.Vector;
import java.util.SortedSet;
import java.util.TreeSet;

import java.io.File;

/**
   Represents the instrumentation that needs to be added to a file.  This
   contains a list of CodeFragments that are the assertions and a list of
   classes that are parsed, but still need to be turned into CodeFragments.
**/
/*package*/ class InstrumentedFile {

  private SortedSet /*CodeFragment*/ _fragments;
  private Vector /*AssertClass*/ _classes;
  private File _file;
  
  /**
  **/
  public InstrumentedFile(final File file) {
    _file = file;
    _fragments = new TreeSet();
    _classes = new Vector();
  }

  final public File getFile() {
    return _file;
  }
  
  final public SortedSet getFragments() {
    return _fragments;
  }

  final public Vector getClasses() {
    return _classes;
  }

  public boolean equals(Object other) {
    if(other instanceof InstrumentedFile) {
      return ((InstrumentedFile)other).getFile().equals(getFile());
    }
    return false;
  }

  public int hashCode() {
    return getFile().hashCode();
  }
}
