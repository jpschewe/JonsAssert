/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code jpschewe@eggplant.mtu.net
*/
package org.tcfreenet.schewe.assert.test;

import org.tcfreenet.schewe.assert.AssertTools;
import org.tcfreenet.schewe.assert.AssertClass;
import org.tcfreenet.schewe.assert.AssertMethod;

import org.tcfreenet.schewe.utils.StringPair;

import java.util.LinkedList;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
   Test cases for AssertTools.
**/
public class AssertToolsTest extends TestCase {

  public static void main(String args[]) {
//     TestSuite suite = new TestSuite();
//     suite.addTest(new AssertToolsTest("testUniqueParams2"));
    TestRunner.run(suite());
    
    System.exit(0);
  }
  
  public AssertToolsTest(String name) {
    super(name);
  }

  public static TestSuite suite() {
    TestSuite suite = new TestSuite(AssertToolsTest.class);
    return suite;
  }

  /**
     Test setUniqueParams.

     @see AssertTools#setUniqueParams(AssertClass)
  **/
  public void testUniqueParams0() {
    final String className = "testClass";
    final AssertClass assertClass = new AssertClass(className,
                                                    "foo",
                                                    false,
                                                    null,
                                                    false,
                                                    null,
                                                    Collections.EMPTY_LIST,
                                                    Collections.EMPTY_MAP,
                                                    Collections.EMPTY_SET);
    final List params0 = new LinkedList();
    final List uparams0 = new LinkedList();
    final AssertMethod c0 = new AssertMethod(assertClass,
                                             className,
                                             Collections.EMPTY_LIST,
                                             Collections.EMPTY_LIST,
                                             params0,
                                             null,
                                             Collections.EMPTY_SET);
    assertClass.addMethod(c0);
    
    final List params1 = new LinkedList();
    params1.add(new StringPair("int", "param0"));
    final List uparams1 = new LinkedList();
    uparams1.add(new StringPair("int", "param0"));
    uparams1.add(new StringPair("boolean", "_JPS_dummy0"));
    final AssertMethod c1 = new AssertMethod(assertClass,
                                             className,
                                             Collections.EMPTY_LIST,
                                             Collections.EMPTY_LIST,
                                             params1,
                                             null,
                                             Collections.EMPTY_SET);
    assertClass.addMethod(c1);
    
    final List params2 = new LinkedList();
    params2.add(new StringPair("int", "param0"));
    params2.add(new StringPair("Object", "param1"));
    final List uparams2 = new LinkedList();
    uparams2.add(new StringPair("int", "param0"));
    uparams2.add(new StringPair("Object", "param1"));
    final AssertMethod c2 = new AssertMethod(assertClass,
                                             className,
                                             Collections.EMPTY_LIST,
                                             Collections.EMPTY_LIST,
                                             params2,
                                             null,
                                             Collections.EMPTY_SET);
    assertClass.addMethod(c2);
 
    AssertTools.setUniqueParams(assertClass);

    assertEquals("a0", uparams0, c0.getUniqueParams());
    assertEquals("a1", uparams1, c1.getUniqueParams());
    assertEquals("a2", uparams2, c2.getUniqueParams());
    
  }

  public void testUniqueParams1() {
    final String className = "testClass";
    final AssertClass assertClass = new AssertClass(className,
                                                    "foo",
                                                    false,
                                                    null,
                                                    false,
                                                    null,
                                                    Collections.EMPTY_LIST,
                                                    Collections.EMPTY_MAP,
                                                    Collections.EMPTY_SET);
    final List params0 = new LinkedList();
    params0.add(new StringPair("int", "param0"));
    final List uparams0 = new LinkedList();
    uparams0.add(new StringPair("int", "param0"));
    final AssertMethod c0 = new AssertMethod(assertClass,
                                             className,
                                             Collections.EMPTY_LIST,
                                             Collections.EMPTY_LIST,
                                             params0,
                                             null,
                                             Collections.EMPTY_SET);
    assertClass.addMethod(c0);
    
    final List params1 = new LinkedList();
    params1.add(new StringPair("int", "param0"));
    params1.add(new StringPair("Object", "param1"));
    params1.add(new StringPair("boolean", "param2"));
    final List uparams1 = new LinkedList();
    uparams1.add(new StringPair("int", "param0"));
    uparams1.add(new StringPair("Object", "param1"));
    uparams1.add(new StringPair("boolean", "param2"));
    final AssertMethod c1 = new AssertMethod(assertClass,
                                             className,
                                             Collections.EMPTY_LIST,
                                             Collections.EMPTY_LIST,
                                             params1,
                                             null,
                                             Collections.EMPTY_SET);
    assertClass.addMethod(c1);
    
    final List params2 = new LinkedList();
    params2.add(new StringPair("int", "param0"));
    params2.add(new StringPair("boolean", "param1"));
    final List uparams2 = new LinkedList();
    uparams2.add(new StringPair("int", "param0"));
    uparams2.add(new StringPair("boolean", "param1"));
    final AssertMethod c2 = new AssertMethod(assertClass,
                                             className,
                                             Collections.EMPTY_LIST,
                                             Collections.EMPTY_LIST,
                                             params2,
                                             null,
                                             Collections.EMPTY_SET);
    assertClass.addMethod(c2);
 
    AssertTools.setUniqueParams(assertClass);

    assertEquals("a0", uparams0, c0.getUniqueParams());
    assertEquals("a1", uparams1, c1.getUniqueParams());
    assertEquals("a2", uparams2, c2.getUniqueParams());

  }

  public void testUniqueParams2() {
    final String className = "testClass";
    final AssertClass assertClass = new AssertClass(className,
                                                    "foo",
                                                    false,
                                                    null,
                                                    false,
                                                    null,
                                                    Collections.EMPTY_LIST,
                                                    Collections.EMPTY_MAP,
                                                    Collections.EMPTY_SET);
    final List params0 = new LinkedList();
    params0.add(new StringPair("int", "param0"));
    final List uparams0 = new LinkedList();
    uparams0.add(new StringPair("int", "param0"));
    final AssertMethod c0 = new AssertMethod(assertClass,
                                             className,
                                             Collections.EMPTY_LIST,
                                             Collections.EMPTY_LIST,
                                             params0,
                                             null,
                                             Collections.EMPTY_SET);
    assertClass.addMethod(c0);
    
    final List params1 = new LinkedList();
    params1.add(new StringPair("int", "param0"));
    params1.add(new StringPair("Object", "param1"));
    params1.add(new StringPair("boolean", "param2"));
    final List uparams1 = new LinkedList();
    uparams1.add(new StringPair("int", "param0"));
    uparams1.add(new StringPair("Object", "param1"));
    uparams1.add(new StringPair("boolean", "param2"));
    final AssertMethod c1 = new AssertMethod(assertClass,
                                             className,
                                             Collections.EMPTY_LIST,
                                             Collections.EMPTY_LIST,
                                             params1,
                                             null,
                                             Collections.EMPTY_SET);
    assertClass.addMethod(c1);
    
    final List params2 = new LinkedList();
    params2.add(new StringPair("int", "param0"));
    params2.add(new StringPair("boolean", "param1"));
    final List uparams2 = new LinkedList();
    uparams2.add(new StringPair("int", "param0"));
    uparams2.add(new StringPair("boolean", "param1"));
    final AssertMethod c2 = new AssertMethod(assertClass,
                                             className,
                                             Collections.EMPTY_LIST,
                                             Collections.EMPTY_LIST,
                                             params2,
                                             null,
                                             Collections.EMPTY_SET);
    assertClass.addMethod(c2);

    final List params3 = new LinkedList();
    final List uparams3 = new LinkedList();
    uparams3.add(new StringPair("boolean", "_JPS_dummy0"));
    uparams3.add(new StringPair("boolean", "_JPS_dummy1"));
    final AssertMethod c3 = new AssertMethod(assertClass,
                                             className,
                                             Collections.EMPTY_LIST,
                                             Collections.EMPTY_LIST,
                                             params3,
                                             null,
                                             Collections.EMPTY_SET);
    assertClass.addMethod(c3);

    final List params4 = new LinkedList();
    params4.add(new StringPair("Object", "param0"));
    final List uparams4 = new LinkedList();
    uparams4.add(new StringPair("Object", "param0"));
    final AssertMethod c4 = new AssertMethod(assertClass,
                                             className,
                                             Collections.EMPTY_LIST,
                                             Collections.EMPTY_LIST,
                                             params4,
                                             null,
                                             Collections.EMPTY_SET);
    assertClass.addMethod(c4);
    
    final List params5 = new LinkedList();
    params5.add(new StringPair("boolean", "param0"));
    final List uparams5 = new LinkedList();
    uparams5.add(new StringPair("boolean", "param0"));
    final AssertMethod c5 = new AssertMethod(assertClass,
                                             className,
                                             Collections.EMPTY_LIST,
                                             Collections.EMPTY_LIST,
                                             params5,
                                             null,
                                             Collections.EMPTY_SET);
    assertClass.addMethod(c5);
    
    AssertTools.setUniqueParams(assertClass);

    assertEquals("a0", uparams0, c0.getUniqueParams());
    assertEquals("a1", uparams1, c1.getUniqueParams());
    assertEquals("a2", uparams2, c2.getUniqueParams());
    assertEquals("a3", uparams3, c3.getUniqueParams());
    assertEquals("a4", uparams4, c4.getUniqueParams());
    assertEquals("a5", uparams5, c5.getUniqueParams());
    
  }
  
}
