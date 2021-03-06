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

import java.lang.reflect.Method;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.mtu.eggplant.util.StringPair;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

/**
 * class of static helper methods for assertions.
 * 
 * @version $Revision: 1.9 $
 */
public final class AssertTools {

  /**
   * if inherited conditions should be enforced
   * 
   * @see net.mtu.eggplant.dbc#systemProperties
   */
  public static final boolean ENFORCE_INHERITED_CONDITIONS = "TRUE".equalsIgnoreCase(System.getProperty("ENFORCE_INHERITED_CONDITIONS", "TRUE"));
  
  /**
   * if assert conditions should be enforced
   * 
   * @see net.mtu.eggplant.dbc#systemProperties
   */
  public static final boolean ENFORCE_ASSERT_CONDITION = "TRUE".equalsIgnoreCase(System.getProperty("ENFORCE_ASSERT_CONDITION", "TRUE"));

  /**
   * if invariant conditions should be enforced
   * 
   * @see net.mtu.eggplant.dbc#systemProperties
   */
  public static final boolean ENFORCE_INVARIANT_CONDITION = "TRUE".equalsIgnoreCase(System.getProperty("ENFORCE_INVARIANT_CONDITION", "TRUE"));

  /**
   * if pre conditions should be enforced
   *
   * @see net.mtu.eggplant.dbc#systemProperties
   */
  public static final boolean ENFORCE_PRE_CONDITION = "TRUE".equalsIgnoreCase(System.getProperty("ENFORCE_PRE_CONDITION", "TRUE"));

  /**
   * if post conditions should be enforced
   * 
   * @see net.mtu.eggplant.dbc#systemProperties
   */
  public static final boolean ENFORCE_POST_CONDITION = "TRUE".equalsIgnoreCase(System.getProperty("ENFORCE_POST_CONDITION", "TRUE"));

  /**
   * what to do if an assertion violation is raised
   * 
   * @see net.mtu.eggplant.dbc#systemProperties
   */
  public static final String ASSERT_BEHAVIOR = System.getProperty("ASSERT_BEHAVIOR", "EXIT");

  private static final Log LOG = LogFactory.getLog(JonsAssert.class);
  
  private AssertTools() {} //no instances

  /**
   * Do a depth first search of each interface heirarchy and return the class
   * objects that implement the DBC conditions, if found.
   *
   * @param interfaces the interfaces to look in
   * @return the DBC classes, may be an empty array if no DBC classes can be
   * found
   *
   * @pre (null != interfaces)
   * @post (null != $return)
   */
  public static Class[] findAssertClassInterfaces(final Class[] interfaces) {
    final Set dbcInterfaces = new HashSet();
    findAssertClassInterfacesInternal(interfaces, dbcInterfaces);
    return (Class[])dbcInterfaces.toArray(new Class[dbcInterfaces.size()]);
  }

  /**
   * Do the work for findAssertClassInterfaces.
   *
   * @param interfaces the interfaces to look in
   * @param dbcInterfaces the Set of interfaces that implement the DBC
   * conditions.  This Set will be changed by this method.
   */
  private static void findAssertClassInterfacesInternal(final Class[] interfaces,
                                                        final Set dbcInterfaces) {
    for(int i=0; i<interfaces.length; i++) {
      final String dbcClassName = interfaces[i].getName() + ".JPS_" + interfaces[i].getName().replace('.', '_') + "_DBCClass";
      final Class dbcClass = classForName(dbcClassName);
      if(null != dbcClass) {
        dbcInterfaces.add(dbcClass);
      } else {
        findAssertClassInterfacesInternal(interfaces[i].getInterfaces(), dbcInterfaces);
      }
    }
  }
    
  /**
   * Find the superclasses method, this is my version of a superClass method,
   * this means that the method name is __<packageName>_<className>_methodName
   * where packageName is the package the class is in with the characters '.'
   * and '$' replaced with '_' and className is the name of the class.
   * 
   * @return the method found, if no such method can be found null is returned
   * 
   * @pre (thisClass != null)
   * @pre (methodName != null)
   * @pre (methodArgs != null)
   */
  public static Method findSuperMethod(final Class thisClass,
                                       final String methodName,
                                       final Class[] methodArgs) {
    if(LOG.isDebugEnabled()) {
      LOG.debug("findSuperMethod:"
                + " thisClass: " + thisClass
                + " methodName: " + methodName
                + " methodArgs: " + net.mtu.eggplant.util.Functions.printArray(methodArgs)
                );
    }
    
    Class superClass = thisClass.getSuperclass();
    Method superMethod = null;
    if(superClass != null) {
      while(superMethod == null && superClass != null) {
        try {
          String fullSuperClassName = superClass.getName().replace('.', '_');
          fullSuperClassName = fullSuperClassName.replace('$', '_');          
          final String supermname = "jps__" + fullSuperClassName + "_" + methodName;
          superMethod = superClass.getDeclaredMethod(supermname, methodArgs);
        } catch(final NoSuchMethodException nsme) {
          // no method, don't bother
          //Try up another level
          superClass = superClass.getSuperclass();
        } catch(final SecurityException se) {
          //This is real bad, spit out internal error here
          internalError("Security exception trying to find method " + methodName + ": " + se);
          return null;
        }
      }
    }
    return superMethod;
  }

  private static AssertionViolation _currentAssertionViolation = null;

  /**
     set the assertion violation that should be throw next.
  **/
  public static void setCurrentAssertionViolation(final AssertionViolation violation) {
    _currentAssertionViolation = violation;
  }

  public static AssertionViolation getCurrentAssertionViolation() {
    return _currentAssertionViolation;
  }

  /**
     Called when an assert fails.
     
     @param av AssertionViolation with information about the failure
     @see #ENFORCE_ASSERT_CONDITION
     
     @pre (av != null)
  **/
  public static void assertFailed(final AssertionViolation av) {
    if(ENFORCE_ASSERT_CONDITION) {
      fail(av);
    }
  }
  
  /**
     called when an invariant fails.
     
     @param av AssertionViolation with information about the failure
     @see #ENFORCE_INVARIANT_CONDITION
     
     @pre (av != null)
  **/
  public static void invariantFailed(final AssertionViolation av) {
    if(ENFORCE_INVARIANT_CONDITION) {
      fail(av);
    }
  }
  
  /**
     called when a post condition fails.
     
     @param av AssertionViolation with information about the failure
     @see #ENFORCE_POST_CONDITION
     
     @pre (av != null)
  **/
  public static void postConditionFailed(final AssertionViolation av) {
    if(ENFORCE_POST_CONDITION) {
      fail(av);
    }
  }

  /**
     Called when a precondition fails.
     
     @param av AssertionViolation with information about the failure
     @see #ENFORCE_PRE_CONDITION

     @pre (av != null)
  **/
  public static void preConditionFailed(final AssertionViolation av) {
    if(ENFORCE_PRE_CONDITION) {
      fail(av);
    }
  }

  /**
     Called to signal an assertion failure.
     
     @see #ASSERT_BEHAVIOR

     @pre (null != av)
  **/
  public static void fail(final AssertionViolation av) {
    if("CONTINUE".equalsIgnoreCase(ASSERT_BEHAVIOR)) {
      av.printStackTrace();
    } else if("EXCEPTION".equalsIgnoreCase(ASSERT_BEHAVIOR)) {
      throw av;
    } else {//("EXIT".equalsIgnoreCase(ASSERT_BEHAVIOR)) {
      av.printStackTrace();
      System.exit(1);
    }

  }

  /**
     called whenever an erorr occurs in the code that was generated that is
     not caused by the user.  Generate some useful message and then blow up.
  **/
  public static void internalError(final String message) {
    throw new RuntimeException("You have found a bug!  Please see the README for instructions on reporting bugs." + System.getProperty("line.separator") + message);
  }



  /**
   * Get the class object for this class name.  Just like
   * {@link Class#forName(String) Class.forName()}, but returns
   * null on an exception.
   * 
   * @return null for no such class found
   */
  public static Class classForName(final String className) {
    if(className == null) {
      return null;
    }
    Class thisClass;
    try {
      thisClass = Class.forName(className);
    } catch(ClassNotFoundException cnfe) {
      //ignore it, return null instead
      thisClass = null;
    }
      
    return thisClass;
  }

  private static final Set LOCKED_METHODS = new HashSet(20);
  /**
   * Lock a method.  This is used at the top of the pre, post and invariant
   * check methods so that the methods don't get called recursively.  
   *
   * @param signature the signature of the method to lock
   * @param instance the instance that should be locked.  this or null (for
   * static methods)
   * @return the lock if it succeeded, null if it failed
   **/
  public static synchronized MethodLock lockMethod(final String signature,
                                                   final Object instance) {
    final Thread thread = Thread.currentThread();
    final MethodLock lock = new MethodLock(signature, instance, thread);
    if(LOCKED_METHODS.contains(lock)) {
      return null;
    } else {
      LOCKED_METHODS.add(lock);
      return lock;
    }
  }

  /**
   * Unlock a method.
   * 
   * @param lock the lock that was recieved from lockMethod()
   * @return true if the method was locked
   * 
   * @see #lockMethod(String, Object)
   */
  public static synchronized boolean unlockMethod(final MethodLock lock) {
    return LOCKED_METHODS.remove(lock);
  }

  /**
   * <p>Calculate the unique parameters for all constructors for class and set
   * those values on the AssertMethod objects.  This is for creating a unique
   * parameter list for checking preconditions on constructors.  If in a class
   * there exists a constructor C1 such that there is another constructor C2
   * that has number parameters N+1, where N is the number of parameters to
   * C1, and the first N parameter types match exactly and the N+1 parameter
   * is not a primative there is a possibility for ambiguity errors with the
   * generated constructors.  So boolean parameters are added to such
   * constructors to guarentee uniqueness.</p>
   *
   * <p>Here is an example:
   * <pre>
   * This won't work:
   * public class A {
   *   public A(int i) {
   *     this(i, new AssertDummy(i));
   *   }
   *   private A(int i, AssertDummy ad) {
   *     //generated to handle preconditions
   *   }
   * 
   *   public A(int i, int j) {
   *    this(i, null);
   *    //should call following constructor, but with generated constructor is ambiguous
   *   }
   *   
   *   public A(int i, Component c) {
   *     //do something
   *   }
   * }
   * This will work:
   * public class A {
   *   public A(int i) {
   *     this(i, true, new AssertDummy(i));
   *   }
   *   private A(int i, boolean dummy0, AssertDummy ad) {
   *     //generated to handle preconditions
   *   }
   * 
   *   public A(int i, int j) {
   *    this(i, null);
   *    //should call following constructor, and now will since the code is no longer ambigous
   *   }
   *   
   *   public A(int i, Component c) {
   *     //do something
   *   }
   * }
   * </pre>
   * </p>
   *     
   * @pre (assertClass != null)
   */
  public static void setUniqueParams(final AssertClass assertClass) {
    final Set methods = assertClass.getMethods();

    //Sort by parameter list size    
    final SortedSet constructors = new TreeSet(CONSTRUCTOR_PARAM_COMPARATOR);
    //Filter down to just constructors    
    CollectionUtils.select(methods, new Predicate() {
      public boolean evaluate(final Object obj) {
        return ((AssertMethod)obj).isConstructor();
      }
    }, constructors);


    /* Now do the smart stuff.  Take the first constructor off the SortedSet,
    C1, and find out if there is any constructor, C2, whose parameter list is
    one longer than the parameter list of C1.  If C2 exists, check if the
    first N parameter types are equal, where N is the number of parameters to
    C1.  If this is true and the last parameter, N+1, of C2 is not a primative
    then add a boolean parameter.  Now check if this new parameter list
    exactly matches an existing one, if so add another boolean parameter,
    repeat until no more constructors parameter lists match exactly.  Then
    requeue. */
    
    //keep dummy names unique
    long dummyCount = 0;
    while(constructors.size() > 1) {
      final AssertMethod constructor = (AssertMethod)constructors.first();
      
      constructors.remove(constructor); //maybe add it later
      final List constructorParams = constructor.getUniqueParams();

      //Check if we're done with the first loop
      boolean done = false;
      //Check if we've added extra parameters
      boolean addedDummy = false;
      final Iterator constructorIter = constructors.iterator();
      while(constructorIter.hasNext() && !done) {
        final AssertMethod compare = (AssertMethod)constructorIter.next();
        final List compareParams = compare.getUniqueParams();
        if(compareParams.size() == constructorParams.size()+1) {
          //Need to check for exact list up to constructorParams.size()
          boolean matches = true;
          final Iterator paramIter = constructorParams.iterator();
          final Iterator compareParamIter = compareParams.iterator();
          while(paramIter.hasNext() && matches) {
            final String paramType = ((StringPair)paramIter.next()).getStringOne();
            final String compareParamType = ((StringPair)compareParamIter.next()).getStringOne();
            if(!paramType.equals(compareParamType)) {
              matches = false;
            }
          }
          if(matches) {
            final String lastCompareParamType = ((StringPair)compareParamIter.next()).getStringOne();
            if(!isPrimative(lastCompareParamType)) {
              constructorParams.add(new StringPair("boolean", "_JPS_dummy" + dummyCount++));
              constructor.setUniqueParams(constructorParams);
              //We're done for now
              done = true;
              addedDummy = true;
            } 
          } 
        } else if(compareParams.size() > constructorParams.size()+1) {
          //No use checking any farther
          done = true;
        }
      }

      //Need to make sure we're still unique
      if(addedDummy) {
        //Keep working off the same iterator
        done = false;
        while(constructorIter.hasNext() && !done) {
          final AssertMethod compare = (AssertMethod)constructorIter.next();
          final List compareParams = compare.getParams();
          if(compareParams.size() == constructorParams.size()) {
            //Possible conflict
            boolean matches = true;
            final Iterator paramIter = constructorParams.iterator();
            final Iterator compareParamIter = compareParams.iterator();
            while(paramIter.hasNext() && matches) {
              final String paramType = ((StringPair)paramIter.next()).getStringOne();
              final String compareParamType = ((StringPair)compareParamIter.next()).getStringOne();
              if(!paramType.equals(compareParamType)) {
                matches = false;
              }
            }
            if(matches) {
              constructorParams.add(new StringPair("boolean", "_JPS_dummy" + dummyCount++));
              constructor.setUniqueParams(constructorParams);
              //Need to try again
              done = false;
            } 
          } else if(compareParams.size() > constructorParams.size()) {
            //We're all done, no more possible conflicts
            done = true;
          }
        }

        //requeue, can't requeue until parameter lists are unique! 
        constructors.add(constructor);
      } 
    }
  }
  
  /**
     Sort for constructors.  First by size of unique parameter list.  Then
     string compare of parameter types, boolean are always last.  This makes
     my uniqueness algorithm a little quicker.
  **/
  private static final Comparator CONSTRUCTOR_PARAM_COMPARATOR = new Comparator() {
    public int compare(final Object o1, final Object o2) {
      if(o1.equals(o2)) {
        return 0;
      }
      final List m1Params = ((AssertMethod)o1).getUniqueParams();
      final List m2Params = ((AssertMethod)o2).getUniqueParams();
      final int m1Size = m1Params.size();
      final int m2Size = m2Params.size();
      if(m1Size < m2Size) {
        return -1;
      } else if(m1Size > m2Size) {
        return 1;
      } else {
        //Need total order, can't return 0.
        //Check params and sort by Object name,
        final Iterator m1Iterator = m1Params.iterator();
        final Iterator m2Iterator = m2Params.iterator();
        while(m1Iterator.hasNext()) {
          final String m1Type = ((StringPair)m1Iterator.next()).getStringOne();
          final String m2Type = ((StringPair)m2Iterator.next()).getStringOne();
          if(m1Type.equals("boolean") && !m2Type.equals("boolean")) {
            return 1;
          } else if(!m1Type.equals("boolean") && m2Type.equals("boolean")) {
            return -1;
          } else {
            final int stringCmp = m1Type.compareTo(m2Type);
            if(stringCmp != 0) {
              return stringCmp;
            }
          }
        }
        internalError("This should never happen: "
                      + " m1Params: " + m1Params
                      + " m2Params: " + m2Params
                      );
        return 0;
      }
    }
  };

  /**
     @param type String that represents a java type
     
     @return <tt>true</tt> if <tt>type</tt> represents a java primative

     @pre (type != null)
  **/
  public static boolean isPrimative(final String type) {
    return (type.equals("boolean")
            || type.equals("byte")
            || type.equals("char")
            || type.equals("short")
            || type.equals("int")
            || type.equals("long")
            || type.equals("float")
            || type.equals("double"));
  }

  /**
     @return true if clazz extends Object and has no implemented interfaces.
     This signals a case where optimizations can be done by not generated
     assertion methods.
  **/
  public static boolean extendsObject(final AssertClass clazz) {
    final String superclass = clazz.getSuperclass();
    return (superclass == null || superclass.equals("java.lang.Object")) &&
      clazz.getInterfaces().isEmpty();
  }


  /**
     Used to represent no class.  This is used for caching.  If this class is
     encountered it, then one can be assured that it's already been determined
     that a class has been checked for and not found.
  **/
  public static final NONE NO_CLASS = new NONE();
  /**
   * Represent no class. 
   */
  private static class NONE {
    public void noMethod() {} //used for no super method
  }

  /**
   * Used in caching to represent that no super method can be found.
   */
  public static final Method NO_METHOD;
  static {
    Method noMethod = null;
    try {
      noMethod = NONE.class.getDeclaredMethod("noMethod", new Class[0]);
    } catch(final NoSuchMethodException nsme) {
      internalError("Cannot find method NONE.noMethod");
    } catch(final SecurityException se) {
      internalError("Cannot access method NONE.noMethod");
    }
    NO_METHOD = noMethod;
  }

  /**
   * Used for locking methods.
   *
   * @see #lockMethod(String, Object)
   */
  public static final class MethodLock {
    /**
     * @pre (null != signature)
     * @pre (null != thread)
     */
    public MethodLock(final String signature,
                      final Object instance,
                      final Thread thread) {
      _signature = signature;
      _instance = instance;
      _thread = thread;
    }

    public int hashCode() {
      return _signature.hashCode();
    }

    public boolean equals(final Object o) {
      if(o instanceof MethodLock) {
        final MethodLock other = (MethodLock)o;
        return _signature.equals(other._signature)
          && _thread.equals(other._thread)
          && (_instance != null ? _instance.equals(other._instance) : _instance == other._instance);
      } else {
        return false;
      }
    }
    
    private final String _signature;
    private final Object _instance;
    private final Thread _thread;
  }
}
