/*
 * Copyright (c) 2000-2002
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
 * Send appreciate comments/suggestions on the code to jpschewe@mtu.net
 */
package net.mtu.eggplant.dbc.test;

/**
 * Test handeling of an exception in a condition.  Pre and post conditions are
 * allowed to throw exceptions, as long as they're declared by the method
 * they're checking.
 * 
 * @version $Revision: 1.1 $
 */
public class ExceptionInCondition {
  
  /**
   * @throws ClassNotFoundException on a database error
   */
  public boolean isExists()
    throws ClassNotFoundException {
    Class.forName("net.mtu.eggplant.dbc.test.ExceptionInCondition");
    return false;
  }

  /**
   * @pre (!isExists())
   * @post (!isExists())
   */
  public void create() 
    throws ClassNotFoundException {
    isExists();
  }
  
}
