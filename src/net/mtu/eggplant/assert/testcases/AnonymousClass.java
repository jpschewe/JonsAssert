/*
  This file is licensed through the GNU public License.  Please read it.
  Basically you can modify this code as you wish, but you need to distribute
  the source code for all applications that use this code.

  I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
*/
package net.mtu.eggplant.assert.test;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
   test parsing of anonomous classes.
**/
public class AnonymousClass {
  private ActionListener _al;
  
  public AnonymousClass() {
     _al = new ActionListener() {
         /**
            @pre (ae != null)
         **/
        public void actionPerformed(ActionEvent ae) {
          int j=0;
        }
      };
  }

  public void pass() {
    _al.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "test"));
  }

  public void fail() {
    _al.actionPerformed(null);
  }

}
