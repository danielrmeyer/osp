/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control.value;

/**
 * A <code>StringValue</code> is a <code>Value</code> object that
 * holds a String value.
 * <p>
 * @see     Value
 */
public class StringValue extends Value {
  public String value;

  /**
   * Constructor StringValue
   * @param _val
   */
  public StringValue(String _val) {
	  super(TYPE_STRING);
    value = _val;
  }

  @Override
public boolean getBoolean() {
    return value.equals("true"); //$NON-NLS-1$
  }

  @Override
public int getInteger() {
    return(int) Math.round(getDouble());
  }

  @Override
public double getDouble() {
    try {
      return Double.valueOf(value).doubleValue();
    } catch(NumberFormatException exc) {
      return 0.0;
    }
  }

  @Override
public String getString() {
    return value;
  }

  @Override
public Object getObject() {
    return null;
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
