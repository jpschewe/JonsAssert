/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.assert;

import java.io.File;

import java.util.StringTokenizer;

/**
   Holds all the options values.
**/
public class Configuration {

  /**
     @return true if files are to be parsed reguardless of the timestamp.
  **/
  final public boolean ignoreTimeStamp() {
    return _ignoreTimeStamp;
  }
  final /*package*/ void setIgnoreTimeStamp(boolean b) {
    _ignoreTimeStamp = b;
  }
  private boolean _ignoreTimeStamp = false;

  
  private String _sourceExtension = "java";

  /**
     @see #setSourceExtension(String)
     @see #setInstrumentedExtension(String)
  **/
  public void setExtensions(final String sourceExtension,
                            final String instrumentedExtension) {
    _sourceExtension = sourceExtension;
    _instrumentedExtension = instrumentedExtension;
  }

  /**
     @return the extension for the source files
  **/
  public String getSourceExtension() {
    return _sourceExtension;
  }

  private String _instrumentedExtension = "java";  
  /**
     @return the extension for the instrumented files
  **/
  public String getInstrumentedExtension() {
    return _instrumentedExtension;
  }

  /**
     @param sourceExtension the extension on the source files, defaults to 'java'
  **/
  public void setSourceExtension(final String sourceExtension) {
    _sourceExtension = sourceExtension;
  }


  /**
     @param instrumentedExtension the extension on the instrumented files, defaults to 'java' 
  **/    
  public void setInstrumentedExtension(final String instrumentedExtension) {
    _instrumentedExtension = instrumentedExtension;
  }
  
  /**
     Take a package name that's passed in and turn it into a directory name
     and create the directories relative to the instrumented directory path.

     @return the directory to put the file in
  **/
  public String createDirectoryForPackage(final String packageName) {
    if(packageName == null) {
      //default package
      return getDestinationDirectory();
    }
    
    StringBuffer dir = new StringBuffer(getDestinationDirectory());
    File dirf = new File(dir.toString());
    if(!dirf.exists()) {
      boolean result = dirf.mkdir();
      if(!result) {
        throw new RuntimeException("Couldn't create directory: " + dir.toString());
      }
    }
    else if(!dirf.isDirectory()) {
      throw new RuntimeException("Error creating destination directories, file found where directory expected: " + dir.toString());
    }
      
    StringTokenizer packageIter = new StringTokenizer(packageName, ".");
    while(packageIter.hasMoreTokens()) {
      String subPackage = packageIter.nextToken();
      dir.append(File.separator);
      dir.append(subPackage);
      File f = new File(dir.toString());
      if(!f.exists()) {
        boolean result = f.mkdir();
        if(!result) {
          throw new RuntimeException("Couldn't create directory: " + dir.toString());
        }
      }
      else if(!f.isDirectory()) {
        throw new RuntimeException("Error creating destination directories, file found where directory expected: " + dir.toString());
      }
    }

    return dir.toString();
  }

  private String _destination = "instrumented";
  public String getDestinationDirectory() {
    return _destination;
  }

  /**
     Set the directory where the instrumented files should go.  Directories
     will be created under this directory for the packages.
  **/
  public void setDestinationDirectory(final String dir) {
    _destination = dir;
  }

  /**
     @return the instrumented filename to use, without the path
  **/
  public String getInstrumentedFilename(final File sourceFile,
                                        final String packageName) {
    final String filename = sourceFile.getAbsolutePath();
    final int indexOfSlash = filename.lastIndexOf(File.separatorChar);
    final String shortFilename = filename.substring(indexOfSlash);        
    int indexOfDot = shortFilename.lastIndexOf('.');
    final String ifilename = shortFilename.substring(0, indexOfDot) + "." + getInstrumentedExtension();
    final String path = createDirectoryForPackage(packageName);    
    return path + File.separatorChar + ifilename;
  }
                                        
  
}
