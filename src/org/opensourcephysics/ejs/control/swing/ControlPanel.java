/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control.swing;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.opensourcephysics.ejs.control.value.Value;

/**
 * A configurable panel. It has no internal value, nor can trigger
 * any action.
 */
public class ControlPanel extends ControlContainer {
  protected JPanel panel;
  private java.awt.LayoutManager myLayout = null;
  private java.awt.Rectangle myBorder = null;

  // ------------------------------------------------
  // Visual component
  // ------------------------------------------------

  /**
   * Constructor ControlPanel
   * @param _visual
   */
  public ControlPanel(Object _visual) {
    super(_visual);
  }

  @Override
protected java.awt.Component createVisual(Object _visual) {
    if(_visual instanceof JPanel) {
      panel = (JPanel) _visual;
    } else {
      panel = new JPanel();
    }
    return panel;
  }

  // ------------------------------------------------
  // Properties
  // ------------------------------------------------
  static private java.util.ArrayList<String> infoList = null;

  @Override
public java.util.ArrayList<String> getPropertyList() {
    if(infoList==null) {
      infoList = new java.util.ArrayList<String>();
      infoList.add("layout"); //$NON-NLS-1$
      infoList.add("border"); //$NON-NLS-1$
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  @Override
public String getPropertyInfo(String _property) {
    if(_property.equals("layout")) { //$NON-NLS-1$
      return "Layout|Object";        //$NON-NLS-1$
    }
    if(_property.equals("border")) { //$NON-NLS-1$
      return "Margins|Object";       //$NON-NLS-1$
    }
    return super.getPropertyInfo(_property);
  }

  // ------------------------------------------------
  // Set and Get the values of the properties
  // ------------------------------------------------
  @Override
public void setValue(int _index, Value _value) {
    switch(_index) {
       case 0 : // layout
         if(_value.getObject() instanceof java.awt.LayoutManager) {
           java.awt.LayoutManager layout = (java.awt.LayoutManager) _value.getObject();
           if(layout!=myLayout) {
             getContainer().setLayout(myLayout = layout);
             panel.validate();
           }
         }
         break;
       case 1 : // border
         if(_value.getObject() instanceof java.awt.Rectangle) {
           java.awt.Rectangle rect = (java.awt.Rectangle) _value.getObject();
           if(rect!=myBorder) {
             panel.setBorder(new EmptyBorder(rect.x, rect.y, rect.width, rect.height));
             myBorder = rect;
           }
         }
         break;
       default :
         super.setValue(_index-2, _value);
         break;
    }
  }

  @Override
public void setDefaultValue(int _index) {
    switch(_index) {
       case 0 :
         getContainer().setLayout(myLayout = new java.awt.BorderLayout());
         panel.validate();
         break;
       case 1 :
         panel.setBorder(null);
         myBorder = null;
         break;
       default :
         super.setDefaultValue(_index-2);
         break;
    }
  }

  @Override
public Value getValue(int _index) {
    switch(_index) {
       case 0 :
       case 1 :
         return null;
       default :
         return super.getValue(_index-2);
    }
  }

} // End of class

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
