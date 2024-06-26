/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

/*
 * The org.opensourcephysics.media.core package defines the Open Source Physics
 * media framework for working with video and other media.
 *
 * Copyright (c) 2024  Douglas Brown and Wolfgang Christian.
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
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
 * For additional information and documentation on Open Source Physics,
 * please see <http://www.opensourcephysics.org/>.
 */
package org.opensourcephysics.media.core;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.event.SwingPropertyChangeSupport;

import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.display.MessageDrawable;

/**
 * This is a Point2D that implements the Interactive and Trackable interfaces
 * with additional utility methods. Classes that extend TPoint should interpret
 * the stored x and y values as image coordinates. TPoint has an empty draw
 * method.
 *
 * @author Douglas Brown
 * @version 1.0
 */
@SuppressWarnings("serial")
public class TPoint extends Point2D.Double implements Interactive, Trackable {
	protected static boolean coordsVisibleInMouseBox = true;
	protected static XYCoordinateStringBuilder xyStringBuilder = new VidCartesianCoordinateStringBuilder();

	// instance fields
	protected boolean enabled = true;
	protected boolean trackEditTrigger = false;
	protected boolean coordsEditTrigger = false;
	protected boolean stepEditTrigger = false;
	protected boolean isAdjusting = false;
	protected Point screenPt;
	protected Point2D.Double worldPt;
	protected PropertyChangeSupport support;
	protected TPoint attachedTo;
	protected double prevX = java.lang.Double.NaN, prevY = java.lang.Double.NaN;

	/**
	 * Constructs a TPoint with image coordinates (0, 0).
	 */
	public TPoint() {
		this(0, 0);
	}

	/**
	 * Constructs a TPoint with specified image coordinates.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */
	public TPoint(double x, double y) {
		super(x, y);
	}

	/**
	 * Constructs a TPoint with image coordinates specified by a Point2D (commonly
	 * another TPoint).
	 *
	 * @param point the Point2D
	 */
	public TPoint(Point2D.Double point) {
		this(point.x, point.y);
	}

	/**
	 * Empty draw method. This method should be overridden by subclasses.
	 *
	 * @param panel the drawing panel requesting the drawing
	 * @param _g    the graphics context on which to draw
	 */
	@Override
	public void draw(DrawingPanel panel, Graphics _g) {

		/** implemented in subclasses */
	}

	/**
	 * Returns null. This method should be overridden by subclasses.
	 *
	 * @param panel the drawing panel
	 * @param xpix  the x pixel position on the panel
	 * @param ypix  the y pixel position on the panel
	 * @return null
	 */
	@Override
	public Interactive findInteractive(DrawingPanel panel, int xpix, int ypix) {
		return null;
	}

	/**
	 * Sets the x position in imagespace.
	 *
	 * @param x the x position
	 */
	@Override
	public void setX(double x) {
		setXY(x, y);
	}

	/**
	 * Sets the y position in imagespace.
	 *
	 * @param y the y position
	 */
	@Override
	public void setY(double y) {
		setXY(x, y);
	}

	/**
	 * Sets the x and y positions in imagespace.
	 *
	 * @param x the x position
	 * @param y the y position
	 */
	@Override
	public void setXY(double x, double y) {
		setLocation(x, y);
	}

	/**
	 * Overrides Point2D.Double setLocation method.
	 *
	 * @param x the x position
	 * @param y the y position
	 */
	@Override
	public void setLocation(double x, double y) {
		if ((getX() == x) && (getY() == y)) {
			return;
		}
		super.setLocation(x, y);
		if (support != null) { // VectorStep
			support.firePropertyChange("location", null, this); //$NON-NLS-1$
		}
	}

	/**
	 * Gets the frame number this TPoint uses for coordinate system transforms and
	 * other identification. Step-based subclasses can override this method to
	 * report their own frame number.
	 *
	 * @param vidPanel the video panel
	 * @return the frame number
	 */
	public int getFrameNumber(VideoPanel vidPanel) {
		return vidPanel.getFrameNumber();
	}

	protected AffineTransform toScreen;

	/**
	 * Gets the screen position of this TPoint on the specified VideoPanel.
	 *
	 * @param vidPanel the video panel
	 * @return the screen point
	 */
	public Point getScreenPosition(VideoPanel vidPanel) {
		if (screenPt == null) {
			toScreen = new AffineTransform();
			screenPt = new Point();
		}
		toScreen.setTransform(vidPanel.getPixelTransform());
		if (!vidPanel.isDrawingInImageSpace()) {
			int n = getFrameNumber(vidPanel);
			toScreen.concatenate(vidPanel.getCoords().getToWorldTransform(n));
		}
		toScreen.transform(this, screenPt);
		return screenPt;
	}

	/**
	 * Sets the screen position of this TPoint on the specified VideoPanel.
	 *
	 * @param x        the screen x coordinate
	 * @param y        the screen y coordinate
	 * @param vidPanel the video panel
	 */
	public void setScreenPosition(int x, int y, VideoPanel vidPanel) {
		if (screenPt == null) {
			screenPt = new Point();
			toScreen = new AffineTransform();
		}
		if (worldPt == null) {
			worldPt = new Point2D.Double();
		}
		screenPt.setLocation(x, y);
		toScreen.setTransform(vidPanel.getPixelTransform());
		if (!vidPanel.isDrawingInImageSpace()) {
			int n = getFrameNumber(vidPanel);
			toScreen.concatenate(vidPanel.getCoords().getToWorldTransform(n));
		}
		try {
			toScreen.inverseTransform(screenPt, worldPt);
		} catch (NoninvertibleTransformException ex) {
			ex.printStackTrace();
		}
		setXY(worldPt.x, worldPt.y);
	}

	/**
	 * Sets the screen position of this TPoint. This can be overridden by subclasses
	 * to change the behavior based on input event methods that report the state of
	 * keys like shift, control, alt, etc.
	 *
	 * @param x        the screen x coordinate
	 * @param y        the screen y coordinate
	 * @param vidPanel the video panel
	 * @param e        the input event making the request
	 */
	public void setScreenPosition(int x, int y, VideoPanel vidPanel, InputEvent e) {
		setScreenPosition(x, y, vidPanel);
	}

	/**
	 * Gets the world position of this TPoint on the specified VideoPanel.
	 *
	 * @param vidPanel the video panel
	 * @return the world position
	 */
	public Point2D getWorldPosition(VideoPanel vidPanel) {
		AffineTransform at = vidPanel.getCoords().getToWorldTransform(getFrameNumber(vidPanel));
		if (worldPt == null) {
			worldPt = new Point2D.Double();
		}
		return at.transform(this, worldPt);
	}

	/**
	 * Sets the world position of this TPoint on the specified VideoPanel.
	 *
	 * @param x        the world x coordinate
	 * @param y        the world y coordinate
	 * @param vidPanel the video panel
	 */
	public void setWorldPosition(double x, double y, VideoPanel vidPanel) {
		int n = getFrameNumber(vidPanel);
		AffineTransform at = vidPanel.getCoords().getToWorldTransform(n);
		if (worldPt == null) {
			worldPt = new Point2D.Double();
		}
		worldPt.setLocation(x, y);
		try {
			at.inverseTransform(worldPt, worldPt);
		} catch (NoninvertibleTransformException ex) {
			ex.printStackTrace();
		}
		setXY(worldPt.x, worldPt.y);
	}

	/**
	 * Shows the world coordinates of this TPoint in the mouse box of the specified
	 * VideoPanel.
	 *
	 * @param vidPanel the video panel
	 */
	public void showCoordinates(VideoPanel vidPanel) {
		if (coordsVisibleInMouseBox) {
			getWorldPosition(vidPanel);
			XYCoordinateStringBuilder builder = vidPanel.getXYCoordinateStringBuilder(this);
			String s = builder.getCoordinateString(vidPanel, worldPt.x, worldPt.y);
			vidPanel.setMessage(s, MessageDrawable.BOTTOM_LEFT);
		}
	}

	/**
	 * Attaches this TPoint to another so it follows it.
	 *
	 * @param p the point to attach to
	 * @return true if a change has occurred
	 */
	public boolean attachTo(TPoint p) {
		if (p == null || p == this)
			return false;
		if (p == attachedTo && p.x == x && p.y == y)
			return false;
		// detach this from any previous point
		detach();
		// attach by adding property change listener
		attachedTo = p;
		p.addPropertyChangeListener("location", new Follower()); //$NON-NLS-1$
		setXY(p.x, p.y);
		return true;
	}

	/**
	 * Detaches this TPoint.
	 */
	public void detach() {
		if (attachedTo != null) {
			PropertyChangeListener[] listeners = attachedTo.support.getPropertyChangeListeners("location"); //$NON-NLS-1$
			for (PropertyChangeListener next : listeners) {
				if (next instanceof Follower) {
					Follower follower = (Follower) next;
					if (follower.getTarget() == this)
						attachedTo.removePropertyChangeListener("location", next); //$NON-NLS-1$
				}
			}
			attachedTo = null;
		}
	}

	/**
	 * Determines if this point is attached to another.
	 *
	 * @return true if attached
	 */
	public boolean isAttached() {
		return attachedTo != null;
	}

	/**
	 * Sets whether this responds to mouse hits.
	 *
	 * @param enabled <code>true</code> if this responds to mouse hits.
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Gets whether this responds to mouse hits.
	 *
	 * @return <code>true</code> if this responds to mouse hits.
	 */
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets the trackEditTrigger property. A trackEditTrigger triggers undoable
	 * track edits when moved.
	 *
	 * @param edit <code>true</code> to make this a trackEditTrigger.
	 */
	public void setTrackEditTrigger(boolean edit) {
		trackEditTrigger = edit;
	}

	/**
	 * Reports whether this is a trackEditTrigger. A trackEditTrigger triggers
	 * undoable track edits when moved.
	 *
	 * @return <code>true</code> if this is a trackEditTrigger.
	 */
	public boolean isTrackEditTrigger() {
		return trackEditTrigger;
	}

	/**
	 * Sets the coordsEditTrigger property. A coordsEditTrigger triggers undoable
	 * coords edits when moved.
	 *
	 * @param edit <code>true</code> to make this a coordsEditTrigger.
	 */
	public void setCoordsEditTrigger(boolean edit) {
		coordsEditTrigger = edit;
	}

	/**
	 * Reports whether this is a coordsEditTrigger. A coordsEditTrigger triggers
	 * undoable coords edits when moved.
	 *
	 * @return <code>true</code> if this is a coordsEditTrigger.
	 */
	public boolean isCoordsEditTrigger() {
		return coordsEditTrigger;
	}

	/**
	 * Sets the stepEditTrigger property. A stepEditTrigger triggers undoable step
	 * edits when moved.
	 *
	 * @param stepEditTrigger <code>true</code> to make this a stepEditTrigger.
	 */
	public void setStepEditTrigger(boolean stepEditTrigger) {
		this.stepEditTrigger = stepEditTrigger;
	}

	/**
	 * Reports whether this is a stepEditTrigger. A stepEditTrigger triggers
	 * undoable step edits when moved.
	 *
	 * @return <code>true</code> if this is a stepEditTrigger.
	 */
	public boolean isStepEditTrigger() {
		return stepEditTrigger;
	}

	/**
	 * Gets the screen bounds of this object.
	 *
	 * @param vidPanel the video panel
	 * @return the bounding rectangle
	 */
	public Rectangle getBounds(VideoPanel vidPanel) {
		return null;
	}

	/**
	 * Reports whether information is available to set min/max values.
	 *
	 * @return <code>false</code>
	 */
	@Override
	public boolean isMeasured() {
		return false;
	}

	/**
	 * Gets the minimum x needed to draw this object.
	 *
	 * @return minimum x
	 */
	@Override
	public double getXMin() {
		return x;
	}

	/**
	 * Gets the maximum x needed to draw this object.
	 *
	 * @return maximum x
	 */
	@Override
	public double getXMax() {
		return x;
	}

	/**
	 * Gets the minimum y needed to draw this object.
	 *
	 * @return minimum y
	 */
	@Override
	public double getYMin() {
		return y;
	}

	/**
	 * Gets the maximum y needed to draw this object.
	 *
	 * @return maximum y
	 */
	@Override
	public double getYMax() {
		return y;
	}

	/**
	 * Returns the angle measured ccw from the positive x-axis to the line between
	 * this TPoint and the specified coordinates.
	 *
	 * @param x the x coordinate
	 * @param y the x coordinate
	 * @return the angle in radians
	 */
	public double angle(double x, double y) {
		return Math.atan2(y - getY(), x - getX());
	}

	/**
	 * Returns the angle measured ccw from the positive x-axis to a line that goes
	 * from this TPoint to the specified Point2D.
	 *
	 * @param pt the Point2D
	 * @return the angle in radians
	 */
	public double angle(Point2D.Double pt) {
		return Math.atan2(pt.y - y, pt.x - x);
	}

	/**
	 * Returns the sine of the angle measured ccw from the positive x-axis to the
	 * line between this TPoint and the specified coordinates.
	 *
	 * @param x the x coordinate
	 * @param y the x coordinate
	 * @return the sine of the angle
	 */
	public double sin(double x, double y) {
		return (getY() - y) / distance(x, y);
	}

	/**
	 * Returns the sine of the angle measured ccw from the positive x-axis to the
	 * line between this TPoint and the specified Point2D.
	 *
	 * @param pt the Point2D
	 * @return the sine of the angle
	 */
	public double sin(Point2D.Double pt) {
		return (y - pt.y) / distance(pt);
	}

	/**
	 * Returns the cosine of the angle measured ccw from the positive x-axis to the
	 * line between this TPoint and the specified coordinates.
	 *
	 * @param x the x coordinate
	 * @param y the x coordinate
	 * @return the cosine of the angle
	 */
	public double cos(double x, double y) {
		return (x - getX()) / distance(x, y);
	}

	/**
	 * Returns the cosine of the angle measured ccw from the positive x-axis to the
	 * line between this TPoint and the specified Point2D.
	 *
	 * @param pt the Point2D
	 * @return the cosine of the angle
	 */
	public double cos(Point2D.Double pt) {
		return (pt.x - x) / distance(pt);
	}

	/**
	 * Centers this TPoint between the two specified points. Note that this method
	 * does not call setXY.
	 *
	 * @param pt1 the first Point2D
	 * @param pt2 the second Point2D
	 */
	public void center(Point2D.Double pt1, Point2D.Double pt2) {
		double x = (pt1.x + pt2.x) / 2.0;
		double y = (pt1.y + pt2.y) / 2.0;
		setLocation(x, y);
	}

	/**
	 * Translates this TPoint by the specified displacement.
	 *
	 * @param dx the x displacement in imagespace
	 * @param dy the y displacement in imagespace
	 */
	public void translate(double dx, double dy) {
		setXY(x + dx, y + dy);
	}

	/**
	 * Sets the adjusting flag. This is normally set by the mouse handler when this
	 * point is dragged or stops being dragged.
	 *
	 * @param adjusting true if being dragged
	 * @param e a MouseEvent, may be null
	 */
	public void setAdjusting(boolean adjusting, MouseEvent e) {
		if (!isAdjusting && adjusting) {
			prevX = x;
			prevY = y;
		}
		isAdjusting = adjusting;
	}

	/**
	 * Gets the adjusting flag.
	 *
	 * @return true if adjusting
	 */
	public boolean isAdjusting() {
		return isAdjusting;
	}

	/**
	 * Adds a PropertyChangeListener.
	 *
	 * @param listener the object requesting property change notification
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		if (support == null) {
			support = new SwingPropertyChangeSupport(this);
		}
		support.addPropertyChangeListener(listener);
	}

	/**
	 * Adds a PropertyChangeListener for a specified property.
	 *
	 * @param property the name of the property of interest to the listener
	 * @param listener the object requesting property change notification
	 */
	public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
		if (support == null) {
			support = new SwingPropertyChangeSupport(this);
		}
		support.addPropertyChangeListener(property, listener);
	}

	/**
	 * Removes a PropertyChangeListener.
	 *
	 * @param listener the listener requesting removal
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		if (support != null) {
			support.removePropertyChangeListener(listener);
		}
	}

	/**
	 * Removes a PropertyChangeListener for a specified property.
	 *
	 * @param property the name of the property
	 * @param listener the listener to remove
	 */
	public void removePropertyChangeListener(String property, PropertyChangeListener listener) {
		support.removePropertyChangeListener(property, listener);
	}

	/**
	 * Returns a String describing this.
	 *
	 * @return a descriptive string
	 */
	@Override
	public String toString() {
		return "TPoint [" + x + ", " + y + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Compares this to the specified object.
	 *
	 * @param object the object
	 * @return <code>true</code> if this equals the specified object
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if ((obj == null) || (obj.getClass() != this.getClass()))
			return false;
		TPoint p = (TPoint) obj;
		return p.x == x && p.y == y && p.screenPt == screenPt && p.worldPt == worldPt;
	}

	/**
	 * Sets the position of this point on the line between end1 and end2 nearest the
	 * specified screen position.
	 *
	 * @param xScreen  the x screen position
	 * @param yScreen  the y screen position
	 * @param vidPanel the videoPanel drawing this point
	 * @param end1     one end of the line
	 * @param end2     the other end of the line
	 */
	public void setPositionOnLine(int xScreen, int yScreen, VideoPanel vidPanel, TPoint end1, TPoint end2) {
		// get image coordinates of the screen point
		if (screenPt == null) {
			screenPt = new Point();
			toScreen = new AffineTransform();
		}
		if (worldPt == null) {
			worldPt = new Point2D.Double();
		}
		screenPt.setLocation(xScreen, yScreen);
		toScreen.setTransform(vidPanel.getPixelTransform());
		if (!vidPanel.isDrawingInImageSpace()) {
			int n = getFrameNumber(vidPanel);
			toScreen.concatenate(vidPanel.getCoords().getToWorldTransform(n));
		}
		try {
			toScreen.inverseTransform(screenPt, worldPt);
		} catch (NoninvertibleTransformException ex) {
			ex.printStackTrace();
		}
		// set location to nearest point on line between end1 and end2
		double dx = end2.x - end1.x;
		double dy = end2.y - end1.y;
		double u = ((worldPt.x - end1.x) * dx + (worldPt.y - end1.y) * dy) / end1.distanceSq(end2);
		if (java.lang.Double.isNaN(u)) {
			u = 0;
		}
		double xLine = end1.x + u * dx;
		double yLine = end1.y + u * dy;
		setLocation(xLine, yLine);
	}

//______________________________________________ inner class _________________________________________

	private class Follower implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent e) {
			TPoint p = (TPoint) e.getSource();
			setXY(p.x, p.y);
		}

		public TPoint getTarget() {
			return TPoint.this;
		}

	}

}

/*
 * Open Source Physics software is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.
 * 
 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be
 * released under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston MA 02111-1307 USA or view the license online at
 * http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2024 The Open Source Physics project
 * http://www.opensourcephysics.org
 */
