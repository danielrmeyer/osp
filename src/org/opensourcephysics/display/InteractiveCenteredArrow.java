/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;

public class InteractiveCenteredArrow extends BoundedShape {
  Point2D.Double[] hotSpots = new Point2D.Double[2]; // shadows superclass field
  BasicStroke stroke = new BasicStroke(2);
  Shape head;
  static int HEAD = 1;

  /**
   * Constructor InteractiveCenteredArrow
   * @param x
   * @param y
   * @param w
   * @param h
   */
  public InteractiveCenteredArrow(double x, double y, double w, double h) {
    super(new Line2D.Double(-w/2, -h/2, w/2, h/2), x, y);
    theta = (w==0) ? 0 : Math.atan2(h, w);
    head = getHead(theta);
    setPixelSized(false);
    setRotateDrag(true);
    hideBounds = true;
    width = w;
    height = h;
    for(int i = 0, n = hotSpots.length; i<n; i++) {
      hotSpots[i] = new Point2D.Double();
    }
  }

  /**
   * Sets the stroke for rendering fat arrows.
   * @param width double
   */
  public void setStrokeWidth(double width) {
    stroke = new BasicStroke((float) width);
  }

  /**
   * Determines if the shape is enabled and if the given pixel coordinates are within the shape.
   *
   * @param panel DrawingPanel
   * @param xpix int
   * @param ypix int
   * @return boolean
   */
  @Override
public boolean isInside(DrawingPanel panel, int xpix, int ypix) {
    hotspot = NONE;
    if(!enabled) {
      return false;
    }
    if(pixelBounds.contains(xpix, ypix)&&!selected) {
      return true;
    }
    if(selected) {
      hotspot = getHotSpotIndex(xpix, ypix, hotSpots);
      return true;
    }
    return false;
  }

  /**
   * Rotates the arrow without changing its length.
   *
   * @param theta
   */
  @Override
public void setTheta(double theta) {
    double len = Math.sqrt(width*width+height*height)/2.0;
    double dx = len*Math.cos(theta);
    double dy = len*Math.sin(theta);
    shape = new Line2D.Double(this.x-dx, this.y-dy, this.x+dx, this.y+dy);
    width = 2*dx;
    height = 2*dy;
    this.theta = theta;
    head = getHead(theta);
  }

  /**
   * Sets the x and y coordinates using hot spots.
   *
   * @param y
   */
  @Override
void setHotSpotXY(double x, double y) {
    if(hideBounds) {
      setXY(x, y);
      return;
    }
    if(xyDrag&&selected&&(hotspot==CENTER)) {
      setXY(x, y);
    } else if(rotateDrag&&selected&&(hotspot==HEAD)) {
      double r = -toPixels.getScaleY()/toPixels.getScaleX();
      double dx = x-this.x;
      double dy = y-this.y;
      shape = new Line2D.Double(this.x-dx, this.y-dy, this.x+dx, this.y+dy);
      width = 2*dx;
      height = 2*dy;
      theta = (width==0) ? theta : Math.atan2(r*height, width);
      head = getHead(theta);
    }
  }

  /**
 * Sets the origin, width (horizontal) and height (vertical) components of this arrow.
 *
 * @param width double
 * @param height double
 */
  public void setWidthHeight(double width, double height) {
    double w2 = width/2, h2 = height/2;
    shape = new Line2D.Double(x-w2, y-h2, x+w2, y+h2);
    this.width = width;
    this.height = height;
    theta = (width==0) ? theta : Math.atan2(height, width);
    head = getHead(theta);
  }

  /**
   * Draws the arrow.
   *
   * @param panel  the world in which the arrow is viewed
   * @param g  the graphics context upon which to draw
   */
  @Override
public void draw(DrawingPanel panel, Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    getPixelPt(panel);
    pixelBounds = computePixelBounds(pixelPt);
    Shape temp;
    if(pixelSized) {
      // translate the shape to correct pixel coordinates
    	trIC.setTransform(1, 0, 0, -1, -x+pixelPt.x, y+pixelPt.y);
    	temp = trIC.createTransformedShape(shape);
    } else {
      temp = toPixels.createTransformedShape(shape);
    }
    g2.setPaint(edgeColor);
    Stroke oldStroke = g2.getStroke();
    g2.setStroke(stroke);
    g2.draw(temp);
    hotSpots[CENTER].setLocation(pixelPt);
    pixelPt.setLocation(x+width/2, y+height/2);
    toPixels.transform(pixelPt, pixelPt);
    hotSpots[BOTTOM].setLocation(pixelPt);
    trIC.setToTranslation(pixelPt.x, pixelPt.y);
    temp = trIC.createTransformedShape(head);
    g2.fill(temp);
    g2.draw(temp);
    g2.setStroke(oldStroke);
    if(!selected||hideBounds) {
      return;
    }
    g2.setPaint(boundsColor);
    if(xyDrag) {
      g2.fillRect((int) hotSpots[CENTER].getX()-delta, (int) hotSpots[CENTER].getY()-delta, d2, d2);
    }
    if(rotateDrag) {
      g2.fillOval((int) hotSpots[HEAD].getX()-delta, (int) hotSpots[HEAD].getY()-delta, d2, d2);
    }
    g2.setPaint(Color.BLACK);
  }

  private AffineTransform trIC = new AffineTransform();
  private Rectangle2D.Double rect = new Rectangle2D.Double();
  
	private Shape computePixelBounds(Point2D.Double pt) {
		double dx = toPixels.getScaleX() * width;
		double dy = toPixels.getScaleY() * height;
		double len = Math.sqrt(dx * dx + dy * dy) + delta;
		rect.setFrame(pt.x - len / 2, pt.y - delta, len, d2);
		trIC.setToRotation(-theta, pt.x, pt.y);
		return trIC.createTransformedShape(rect);
	}

	/**
	 * Gets the cursor depending on the current hot spot.
	 *
	 * @return Cursor
	 */
	@Override
	public java.awt.Cursor getPreferredCursor() {
		int c = Cursor.HAND_CURSOR;
		if (xyDrag && (hotspot == CENTER)) {
			c = Cursor.MOVE_CURSOR;
		} else if (rotateDrag && (hotspot == HEAD)) {
			// hand
		} else if (selected) {
			c = Cursor.CROSSHAIR_CURSOR;
		}
		return Cursor.getPredefinedCursor(c);		
	}

  private Shape getHead(double theta) {
    float size = 4+2*stroke.getLineWidth();
    GeneralPath path = new GeneralPath();
    path.moveTo(0, 0);
    path.lineTo((-size), (-size/2));
    path.lineTo((-size), (+size/2));
    path.closePath();
    if (theta == 0)
    	return path;
    trIC.setToRotation(-theta);
    return trIC.createTransformedShape(path);
  }

  /**
  * Gets the XML object loader for this class.
  * @return ObjectLoader
  */
  public static XML.ObjectLoader getLoader() {
    return new InteractiveCenteredArrowLoader();
  }

  /**
  * A class to save and load InteractiveArrow in an XMLControl.
  */
  protected static class InteractiveCenteredArrowLoader extends XMLLoader {
    @Override
	public void saveObject(XMLControl control, Object obj) {
      InteractiveCenteredArrow arrow = (InteractiveCenteredArrow) obj;
      control.setValue("x", arrow.x);                      //$NON-NLS-1$
      control.setValue("y", arrow.y);                      //$NON-NLS-1$
      control.setValue("width", arrow.width);              //$NON-NLS-1$
      control.setValue("height", arrow.height);            //$NON-NLS-1$
      control.setValue("is enabled", arrow.isEnabled());   //$NON-NLS-1$
      control.setValue("is measured", arrow.isMeasured()); //$NON-NLS-1$
      control.setValue("color", arrow.color);              //$NON-NLS-1$
    }

    @Override
	public Object createObject(XMLControl control) {
      return new InteractiveCenteredArrow(0, 0, 0, 0);
    }

    @Override
	public Object loadObject(XMLControl control, Object obj) {
      InteractiveCenteredArrow arrow = (InteractiveCenteredArrow) obj;
      double x = control.getDouble("x");      //$NON-NLS-1$
      double y = control.getDouble("y");      //$NON-NLS-1$
      double w = control.getDouble("width");  //$NON-NLS-1$
      double h = control.getDouble("height"); //$NON-NLS-1$
      arrow.enabled = control.getBoolean("is enabled");        //$NON-NLS-1$
      arrow.enableMeasure = control.getBoolean("is measured"); //$NON-NLS-1$
      arrow.color = (Color) control.getObject("color");        //$NON-NLS-1$
      arrow.setXY(x, y);
      arrow.setWidthHeight(w, h);
      return obj;
    }

  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2024  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
