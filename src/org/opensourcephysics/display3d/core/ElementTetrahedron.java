/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.core;
import org.opensourcephysics.controls.XMLControl;

/**
 * <p>Title: ElementTetrahedron</p>
 * <p>Description: A 3D Tetrahedron. The tetrahedron can be incomplete or double,
 * if height!=sizeZ.</p>
 * @author Francisco Esquembre
 * @version December 2008
 */
public interface ElementTetrahedron extends Element {
  /**
   * Sets the truncation height for this tetrahedron.
   * The standard height of a tetrahedron is set using setSizeZ().
   * This method helps create truncated tetrahedrons by setting the
   * truncation height at a value smaller than the Z size.
   * Negative, zero, or Double.NaN values set the tetrahedron to a standard
   * (complete) one. Values greater than the Z size are ignored.
   * @param height double
   */
  public void setTruncationHeight(double height);

  /**
   * Gets the truncation height for this tetrahedron.
   * @return double The truncation height (Double.NaN if the tetrahedron is complete.)
   * @see #setTruncationHeight()
   */
  public double getTruncationHeight();

  /**
   * Whether the tetrahedron should be closed at its bottom.
   * @param close the desired value
   */
  public void setClosedBottom(boolean close);

  /**
   * Whether the tetrahedron is closed at its bottom.
   * @return the value
   */
  public boolean isClosedBottom();

  /**
   * Whether an incomplete tetrahedron element should be closed at its top.
   * @param closed the desired value
   */
  public void setClosedTop(boolean close);

  /**
   * Whether the tetrahedron is closed at its top.
   * @return the value
   */
  public boolean isClosedTop();

  // ----------------------------------------------------
  // XML loader
  // ----------------------------------------------------
  static abstract class Loader extends Element.Loader {
    @Override
	public void saveObject(XMLControl control, Object obj) {
      super.saveObject(control, obj);
      ElementTetrahedron element = (ElementTetrahedron) obj;
      if(Double.isNaN(element.getTruncationHeight())) {
        control.setValue("truncation height", -1.0);                          //$NON-NLS-1$
      } else {
        control.setValue("truncation height", element.getTruncationHeight()); //$NON-NLS-1$
      }
      control.setValue("closed top", element.isClosedTop());       //$NON-NLS-1$
      control.setValue("closed bottom", element.isClosedBottom()); //$NON-NLS-1$
    }

    @Override
	public Object loadObject(XMLControl control, Object obj) {
      super.loadObject(control, obj);
      ElementTetrahedron element = (ElementTetrahedron) obj;
      element.setTruncationHeight(control.getDouble("truncation height")); //$NON-NLS-1$
      element.setClosedTop(control.getBoolean("closed top"));              //$NON-NLS-1$
      element.setClosedBottom(control.getBoolean("closed bottom"));        //$NON-NLS-1$
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
