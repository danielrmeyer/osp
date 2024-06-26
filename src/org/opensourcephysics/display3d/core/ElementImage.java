/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.core;
import org.opensourcephysics.controls.XMLControl;

/**
 * <p>Title: ElementImage</p>
 * <p>Description: Draws a circle at its position with the given size.</p>
 * <p>Because an image is essentialy a 2D object, it doesn't behave completely
 * as a 3D object. Thus, its center will be affected by transformations
 * of the element, BUT ITS SIZE WON'T. Moreover, in 3D visualizations, the
 * maximum of sizeX and sizeY is used for its horizontal size. In all other
 * views, the corresponding size is used.</p>
 * <p>Images can be rotated.
 * @author Francisco Esquembre
 * @version March 2005
 */
public interface ElementImage extends Element {
  /**
   * Sets the image file to be displayed
   * @param text the String
   */
  public void setImageFile(String file);

  /**
   * Gets the image displayed
   */
  public String getImageFile();

  /**
   * Sets the image to be displayed
   * @param image java.awt.Image
   */
  public void setImage(java.awt.Image image);

  /**
   * Sets the rotation angle for the image. Default is 0.
   * @param angle the rotation angle
   */
  public void setRotationAngle(double angle);

  /**
   * Gets the rotation angle for the image
   */
  public double getRotationAngle();

  // ----------------------------------------------------
  // XML loader
  // ----------------------------------------------------
  static abstract class Loader extends Element.Loader {
    @Override
	public void saveObject(XMLControl control, Object obj) {
      super.saveObject(control, obj);
      ElementImage element = (ElementImage) obj;
      control.setValue("image file", element.getImageFile());         //$NON-NLS-1$
      control.setValue("rotation angle", element.getRotationAngle()); //$NON-NLS-1$
    }

    @Override
	public Object loadObject(XMLControl control, Object obj) {
      super.loadObject(control, obj);
      ElementImage element = (ElementImage) obj;
      element.setImageFile(control.getString("image file"));         //$NON-NLS-1$
      element.setRotationAngle(control.getDouble("rotation angle")); //$NON-NLS-1$
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
