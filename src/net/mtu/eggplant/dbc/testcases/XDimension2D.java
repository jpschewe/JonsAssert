package net.mtu.eggplant.dbc.test;

import java.awt.geom.Dimension2D;

import org.apache.log4j.Logger;

/**
 * Adds a Double implementation of Dimension2D just like the one in
 * {@link java.awt.geom.Point2D}.
 *
 * @version $Revision: 1.1 $
 */
public final class XDimension2D {
  
  private static final Logger LOG = Logger.getLogger(XDimension2D.class);
  
  private XDimension2D() {
    //no instances
  }

  /**
   * A Dimension2D with double precision.
   */
  public static class Double extends Dimension2D {
    public Double() {
      this(0, 0);
    }

    public Double(final double width,
                 final double height) {
      _width = width;
      _height = height;
    }

    private double _height;
    public double getHeight() {
      return _height;
    }

    private double _width;
    public double getWidth() {
      return _width;
    }

    public void setSize(final double width,
                        final double height) {
      _width = width;
      _height = height;
    }
  }  
}
