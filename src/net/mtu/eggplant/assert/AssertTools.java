/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package org.tcfreenet.schewe.assert;

import java.lang.reflect.Method;

import java.io.File;

import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.Map;

/**
   class of static helper methods for assertions.

**/
final public class AssertTools {

  static private Map _superMethods = new HashMap();
  
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
//     System.out.println("findSuperMethod:"
//                        + " thisClass: " + thisClass
//                        + " methodName: " + methodName
//                        + " methodArgs: " + methodArgs
//                        );
    //Use a scratch class for the key
    ScratchMethod sm = new ScratchMethod(thisClass, methodName, methodArgs);

    //Now see if it's cached
    if(_superMethods.containsKey(sm)) {
//       System.out.println("Found in table");
      return (Method)_superMethods.get(sm);
    }
    
    Class superClass = thisClass.getSuperclass();
    Method superMethod = null;
    if(superClass != null) {
      while(superMethod == null && superClass != null) {
        try {
          String fullSuperClassName = superClass.getName().replace('.', '_');
          fullSuperClassName = fullSuperClassName.replace('$', '_');          
          String supermname = "__" + fullSuperClassName + "_" + methodName;
          superMethod = superClass.getDeclaredMethod(supermname, methodArgs);
        }
        catch(NoSuchMethodException nsme) {
          // no method, don't bother
          //Try up another level
          superClass = superClass.getSuperclass();
        }
        catch(SecurityException se) {
          //This is real bad, spit out internal error here
          internalError("Security exception trying to find method " + methodName + ": " + se);
          return null;
        }
      }
    }
    
    //put it in the cache
    _superMethods.put(sm, superMethod);

//     System.out.println("had to lookup");
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
    if(assertBehavior.equalsIgnoreCase("CONTINUE")) {
      av.printStackTrace();
    }
    else if(assertBehavior.equalsIgnoreCase("EXCEPTION")) {
      throw av;
    }
    //default behavior
    else {//(assertBehavior.equalsIgnoreCase("EXIT")) {
      av.printStackTrace();
      System.exit(1);
    }

  }

  /**
     called whenever an erorr occurs in the code that was generated that is
     not caused by the user.  Generate some useful message and thwn blow up.
  **/
  static public void internalError(final String message) {
    throw new RuntimeException("You have found a bug!  Please see the README for instructions on reporting bugs.\n" + message);
  }


  static private String _sourceExtension = "java";

  /**
     @see #setSourceExtension(String)
     @see #setInstrumentedExtension(String)
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

  static private String _instrumentedExtension = "java";  
  /**
     @return the extension for the instrumented files
  **/
  static public String getInstrumentedExtension() {
    return _instrumentedExtension;
  }

  /**
     @param sourceExtension the extension on the source files, defaults to 'java'
  **/
  static public void setSourceExtension(final String sourceExtension) {
    _sourceExtension = sourceExtension;
  }


  /**
     @param instrumentedExtension the extension on the instrumented files, defaults to 'java' 
  **/    
  static public void setInstrumentedExtension(final String instrumentedExtension) {
    _instrumentedExtension = instrumentedExtension;
  }
  
  /**
     Take a package name that's passed in and turn it into a directory name
     and create the directories relative to the instrumented directory path.

     @return the directory to put the file in
  **/
  static public String createDirectoryForPackage(final String packageName) {
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

  static private String _destination = "instrumented";
  static public String getDestinationDirectory() {
    return _destination;
  }

  /**
     Set the directory where the instrumented files should go.  Directories
     will be created under this directory for the packages.
  **/
  static public void setDestinationDirectory(final String dir) {
    _destination = dir;
  }

  /**
     @return the instrumented filename to use, without the path
  **/
  static public String getInstrumentedFilename(final File sourceFile,
                                               final String packageName) {
    String filename = sourceFile.getAbsolutePath();
    int indexOfSlash = filename.lastIndexOf(File.separatorChar);
    String shortFilename = filename.substring(indexOfSlash);        
    int indexOfDot = shortFilename.lastIndexOf('.');
    String ifilename = shortFilename.substring(0, indexOfDot) + "." + getInstrumentedExtension();
    String path = createDirectoryForPackage(packageName);    
    return path + File.separatorChar + ifilename;
  }
                                        

//   static private HashMap _classMap = new HashMap();
  /**
     Get the class object for this class name.  Just like {@link
     Class#forName(String) Class.forName()}, but catches the exceptions

     @return null for no such class found

     @pre (className != null)
  **/
  static public Class classForName(final String className) {
//     if(_classMap.containsKey(className)) {
//       return (Class)_classMap.get(className);
//     }
//     else {
      Class thisClass = null;
      try {
        thisClass = Class.forName(className);
      }
      catch(ClassNotFoundException cnfe) {
        //ignore it, return null instead
      }
//       _classMap.put(className, thisClass);
      
      return thisClass;
//     }
  }
  
}
