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
package net.mtu.eggplant.dbc;

import java.io.File;

import java.util.StringTokenizer;

/**
 * Holds all the option values.
 * 
 * @version $Revision: 1.7 $
 */
public class Configuration {

  /**
   * Enum for 1.4 source compatibility.
   *
   * @see #setSourceCompatibility(Configuration.SourceCompatibilityEnum)
   */
  public static final SourceCompatibilityEnum JAVA_1_4 = new SourceCompatibilityEnum("1.4");
  /**
   * Enum for 1.3 source compatibility.
   *
   * @see #setSourceCompatibility(Configuration.SourceCompatibilityEnum)
   */
  public static final SourceCompatibilityEnum JAVA_1_3 = new SourceCompatibilityEnum("1.3");

  /**
   * Set the source compatibility for the parser.  Defaults to JAVA_1_3.
   *
   * @see #JAVA_1_4
   * @see #JAVA_1_3
   * 
   * @pre (null != sc)
   */
  public void setSourceCompatibility(final SourceCompatibilityEnum sc) {
    _sourceCompatibility = sc;
  }

  public SourceCompatibilityEnum getSourceCompatibility() {
    return _sourceCompatibility;
  }

  private SourceCompatibilityEnum _sourceCompatibility = JAVA_1_3;
  
  /**
   * @return true if files are to be parsed reguardless of the timestamp.
   */
  public boolean ignoreTimeStamp() {
    return _ignoreTimeStamp;
  }
  
  /**
   * @see #ignoreTimeStamp()
   */
  public void setIgnoreTimeStamp(final boolean b) {
    _ignoreTimeStamp = b;
  }
  private boolean _ignoreTimeStamp = false;

  
  /**
   * @see #setSourceExtension(String)
   * @see #setInstrumentedExtension(String)
   */
  public void setExtensions(final String sourceExtension,
                            final String instrumentedExtension) {
    _sourceExtension = sourceExtension;
    _instrumentedExtension = instrumentedExtension;
  }

  /**
   * @return the extension for the source files
   */
  public String getSourceExtension() {
    return _sourceExtension;
  }

  private String _instrumentedExtension = "java";  
  /**
   * @return the extension for the instrumented files
   */
  public String getInstrumentedExtension() {
    return _instrumentedExtension;
  }

  /**
   * @param sourceExtension the extension on the source files, defaults to 'java'
   */
  public void setSourceExtension(final String sourceExtension) {
    _sourceExtension = sourceExtension;
  }

  private String _sourceExtension = "java";

  /**
   * @param instrumentedExtension the extension on the instrumented files, defaults to 'java' 
   */    
  public void setInstrumentedExtension(final String instrumentedExtension) {
    _instrumentedExtension = instrumentedExtension;
  }
  
  /**
   * Take a package name that's passed in and turn it into a directory name
   * and create the directories relative to the instrumented directory path.
   *
   * @return the directory to put the file in
   */
  public String createDirectoryForPackage(final String packageName) {
    if(packageName == null) {
      //default package
      return getDestinationDirectory();
    }
    
    final StringBuffer dir = new StringBuffer(getDestinationDirectory());
    final File dirf = new File(dir.toString());
    if(!dirf.exists()) {
      if(!dirf.mkdir()) {
        throw new RuntimeException("Couldn't create directory and it doesn't exist: " + dirf.toString());
      }
    } else if(!dirf.isDirectory()) {
      throw new RuntimeException("Error creating destination directories, file found where directory expected: " + dir.toString());
    }
      
    final StringTokenizer packageIter = new StringTokenizer(packageName, ".");
    while(packageIter.hasMoreTokens()) {
      final String subPackage = packageIter.nextToken();
      dir.append(File.separator);
      dir.append(subPackage);
      final File f = new File(dir.toString());
      if(!f.exists()) {
        if(!f.mkdir()) {
          throw new RuntimeException("Couldn't create directory and it doesn't exist: " + dir.toString());
        }
      } else if(!f.isDirectory()) {
        throw new RuntimeException("Error creating destination directories, file found where directory expected: " + dir.toString());
      }
    }

    return dir.toString();
  }

  private String _destination = "instrumented";

  /**
   * @see #setDestinationDirectory(String)
   */
  public String getDestinationDirectory() {
    return _destination;
  }

  /**
   * Set the directory where the instrumented files should go.  Directories
   * will be created under this directory for the packages.  Defaults to
   * "instrumented".
   */
  public void setDestinationDirectory(final String dir) {
    _destination = dir.trim();
  }

  /**
   * @return the instrumented filename to use, without the path
   */
  public String getInstrumentedFilename(final File sourceFile,
                                        final String packageName) {
    final String filename = sourceFile.getAbsolutePath();
    //final int indexOfSlash = filename.lastIndexOf(File.separatorChar);
    //final String shortFilename = filename.substring(indexOfSlash+1);
    final String shortFilename = sourceFile.getName();
    int indexOfDot = shortFilename.lastIndexOf('.');
    final String ifilename = shortFilename.substring(0, indexOfDot) + "." + getInstrumentedExtension();
    final String path = createDirectoryForPackage(packageName);
    return path + File.separatorChar + ifilename;
  }

  // Inner classes only below here
  /**
   * Enum for which version of source compatibility we're using.
   */
  public static final class SourceCompatibilityEnum {
    /**
     * @pre (null != name)
     */
    private SourceCompatibilityEnum(final String name) {
      _name = name;
    }
    public String getName() { return _name; }
    private final String _name;
  }

  private boolean _prettyOutput = false;
  /**
   * If carriage returns should be output in generated code.  Defaults to
   * false.
   */
  public final boolean isPrettyOutput() { return _prettyOutput; }

  /**
   * @see #isPrettyOutput()
   */
  public final void setPrettyOutput(final boolean b) { _prettyOutput = b; }

  
  private boolean _verbose = false;
  /**
   * Should we be verbose about what we're doing?  Defaults to false. 
   */
  public final boolean isVerbose() { return _verbose; }

  /**
   * @see #isVerbose()
   */
  public final void setVerbose(final boolean v) { _verbose = v; }
}
