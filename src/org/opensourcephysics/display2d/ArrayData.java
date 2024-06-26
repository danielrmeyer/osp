/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display2d;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;

/**
 * ArrayData stores numeric data for 2d visualizations using a 2d array for each component.
 *
 * Data components can represent almost anything. For example, we can store vectors as follows:
 * <br>
 * <pre>
 * <code>data=new double [3][n][m]<\code>
 *
 * <code>data[0] = length[][]  <\code>
 * <code>data[1] = a[][]  <\code>
 * <code>data[2] = b[][]  <\code>
 * <\pre>
 *
 * @author     Wolfgang Christian
 * @created    Jan 2, 2004
 * @version    1.0
 */
public class ArrayData implements GridData {
  protected double[][][] data;
  protected double left, right, bottom, top;
  protected double dx = 0, dy = 0;
  protected boolean cellData = false;
  protected String[] names;

  /**
   * Constructor ArrayData
   *
   * @param nx
   * @param ny
   * @param nsamples
   */
  public ArrayData(int nx, int ny, int nsamples) {
    if((ny<1)||(nx<1)) {
      throw new IllegalArgumentException("Number of dataset rows and columns must be positive. Your row="+ny+"  col="+nx); //$NON-NLS-1$ //$NON-NLS-2$
    }
    if((nsamples<1)) {
      throw new IllegalArgumentException("Number of 2d data components must be positive. Your ncomponents="+nsamples); //$NON-NLS-1$
    }
    data = new double[nsamples][nx][ny]; // x, y, and components
    setScale(0, nx, 0, ny);
    names = new String[nsamples];
    for(int i = 0; i<nsamples; i++) {
      names[i] = "Component_"+i; //$NON-NLS-1$
    }
  }

  /**
   * Sets the name of the component.
   *
   * @param i int the component index
   * @param name String
   */
  @Override
public void setComponentName(int i, String name) {
    names[i] = name;
  }

  /**
   * Gets the name of the component,
   * @param i int the component index
   * @return String the name
   */
  @Override
public String getComponentName(int i) {
    return names[i];
  }

  /**
   * Gets the number of data components.
   *
   * @return int
   */
  @Override
public int getComponentCount() {
    return data.length;
  }

  /**
   *
   * Sets the left, right, bottom, and top of the grid data using a lattice model.
   *
   * @param _left
   * @param _right
   * @param _bottom
   * @param _top
   */
  @Override
public void setScale(double _left, double _right, double _bottom, double _top) {
    cellData = false;
    left = _left;
    right = _right;
    bottom = _bottom;
    top = _top;
    int ix = data[0].length;
    int iy = data[0][0].length;
    dx = 0; // special case if #col==1
    if(ix>1) {
      dx = (right-left)/(ix-1);
    }
    dy = 0; // special ase if #row==1
    if(iy>1) {
      dy = (bottom-top)/(iy-1); // note that dy is usualy negative
    }
    if(dx==0) {
      left -= 0.5;
      right += 0.5;
    }
    if(dy==0) {
      bottom -= 0.5;
      top += 0.5;
    }
  }

  /**
   * Gets the cellData flag.
   *
   * @return true if cell data.
   */
  @Override
public boolean isCellData() {
    return cellData;
  }

  /**
   * Gets the value of the given component at the given location.
   *
   * @param ix  x index
   * @param iy  y index
   * @param component
   * @return the value.
   */
  @Override
public double getValue(int ix, int iy, int component) {
    return data[component][ix][iy];
  }

  /**
   * Sets the value of the given component at the given location.
   *
   * @param ix  x index
   * @param iy  y index
   * @param component
   * @param value
   */
  @Override
public void setValue(int ix, int iy, int component, double value) {
    data[component][ix][iy] = value;
  }

  /**
   * Gets the number of x entries.
   * @return nx
   */
  @Override
public int getNx() {
    return data[0].length;
  }

  /**
   * Gets the number of y entries.
   * @return ny
   */
  @Override
public int getNy() {
    return data[0][0].length;
  }

  /**
   * Sets the left, right, bottom, and top of the grid data using a cell model.
   *
   * Coordinates are centered on each cell and will NOT include the edges.
   *
   * @param _left
   * @param _right
   * @param _bottom
   * @param _top
   */
  @Override
public void setCellScale(double _left, double _right, double _bottom, double _top) {
    cellData = true;
    int nx = data[0].length;
    int ny = data[0][0].length;
    dx = 0; // special case if #col==1
    if(nx>1) {
      dx = (_right-_left)/nx;
    }
    dy = 0; // special ase if #row==1
    if(ny>1) {
      dy = (_bottom-_top)/ny; // note that dy is usualy negative
    }
    left = _left+dx/2;
    right = _right-dx/2;
    bottom = _bottom-dy/2;
    top = _top+dy/2;
  }

  /**
   * Sets the grid such that the centers of the corner cells match the given coordinates.
   *
   * Coordinates are centered on each cell and the bounds are ouside the max and min values.
   *
   * @param xmin
   * @param xmax
   * @param ymin
   * @param ymax
   */
  @Override
public void setCenteredCellScale(double xmin, double xmax, double ymin, double ymax) {
    int nx = data[0].length;
    int ny = data[0][0].length;
    double delta = (nx>1) ? (xmax-xmin)/(nx-1)/2 : 0;
    xmin -= delta;
    xmax += delta;
    delta = (ny>1) ? (ymax-ymin)/(ny-1)/2 : 0;
    ymin -= delta;
    ymax += delta;
    setCellScale(xmin, xmax, ymin, ymax);
  }

  /**
   * Estimates the value of a component at an untabulated point, (x,y).
   *
   * Interpolate uses bilinear interpolation on the grid.  Although the interpolating
   * function is continous across the grid boundaries, the gradient changes discontinuously
   * at the grid-square boundaries.
   *
   * @param x  the untabulated x
   * @param y  the untabulated y
   * @param index
   * @return the interpolated sample
   */
  @Override
public double interpolate(double x, double y, int index) {
    int ix = (int) ((x-left)/dx);
    ix = Math.max(0, ix);
    ix = Math.min(data[0].length-2, ix);
    int iy = -(int) ((top-y)/dy);
    iy = Math.max(0, iy);
    iy = Math.min(data[0][0].length-2, iy);
    double t = (x-left)/dx-ix;
    double u = -(top-y)/dy-iy;
    if(ix<0) {
      return(1-u)*data[index][0][iy]+u*data[index][0][iy+1];
    } else if(iy<0) {
      return(1-t)*data[index][ix][0]+t*data[index][ix+1][0];
    } else {
      return(1-t)*(1-u)*data[index][ix][iy]+t*(1-u)*data[index][ix+1][iy]+t*u*data[index][ix+1][iy+1]+(1-t)*u*data[index][ix][iy+1];
    }
  }

  /**
   * Estimates multiple sample components at an untabulated point, (x,y).
   *
   * Interpolate uses bilinear interpolation on the grid.  Although the interpolating
   * function is continous across the grid boundaries, the gradient changes discontinuously
   * at the grid square boundaries.
   *
   * @param x  untabulated x
   * @param y  untabulated y
   * @param indexes to be interpolated
   * @param values array will contain the interpolated values
   * @return the interpolated array
   */
  @Override
public double[] interpolate(double x, double y, int[] indexes, double[] values) {
    int ix = (int) ((x-left)/dx);
    ix = Math.max(0, ix);
    ix = Math.min(data[0].length-2, ix);
    int iy = -(int) ((top-y)/dy);
    iy = Math.max(0, iy);
    iy = Math.min(data[0][0].length-2, iy);
    // special case if there is only one row or one column
    if((ix<0)&&(iy<0)) {
      for(int i = 0, n = indexes.length; i<n; i++) {
        values[i] = data[indexes[i]][0][0];
      }
      return values;
    } else if(ix<0) {
      double u = -(top-y)/dy-iy;
      for(int i = 0, n = indexes.length; i<n; i++) {
        values[i] = (1-u)*data[indexes[i]][0][iy]+u*data[indexes[i]][0][iy+1];
      }
      return values;
    } else if(iy<0) {
      double t = (x-left)/dx-ix;
      for(int i = 0, n = indexes.length; i<n; i++) {
        values[i] = (1-t)*data[indexes[i]][ix][0]+t*data[indexes[i]][ix+1][0];
      }
      return values;
    }
    double t = (x-left)/dx-ix;
    double u = -(top-y)/dy-iy;
    for(int i = 0, n = indexes.length; i<n; i++) {
      int index = indexes[i];
      values[i] = (1-t)*(1-u)*data[index][ix][iy]+t*(1-u)*data[index][ix+1][iy]+t*u*data[index][ix+1][iy+1]+(1-t)*u*data[index][ix][iy+1];
    }
    return values;
  }

  /**
   * Gets the array containing the data.
   *
   * @return the data
   */
  @Override
public double[][][] getData() {
    return data;
  }

	/**
	 * Gets the minimum and maximum values of the n-th component.
	 *
	 * @param n the component
	 * @return {zmin,zmax}
	 */
	@Override
	public double[] getZRange(int n) {
		return getZRange(n, new double[2]);
	}

	/**
	 * Gets the minimum and maximum values of the n-th component.
	 *
	 * @param n      the component
	 * @param minmax array to fill
	 * @return minmax
	 */
	@Override
	public double[] getZRange(int n, double[] minmax) {
		double zmin = data[n][0][0];
		double zmax = zmin;
		for (int i = 0, mx = data[0].length; i < mx; i++) {
			for (int j = 0, my = data[0][0].length; j < my; j++) {
				double v = data[n][i][j];
				if (v > zmax) {
					zmax = v;
				} else if (v < zmin) {
					zmin = v;
				}
			}
		}
		minmax[0] = zmin;
		minmax[1] = zmax;
		return minmax;
	}

  /**
   * Gets the x value for the first column in the grid.
   * @return  the leftmost x value
   */
  @Override
public final double getLeft() {
    return left;
  }

  /**
   * Gets the x value for the right column in the grid.
   * @return  the rightmost x value
   */
  @Override
public final double getRight() {
    return right;
  }

  /**
   * Gets the y value for the first row of the grid.
   * @return  the topmost y value
   */
  @Override
public final double getTop() {
    return top;
  }

  /**
   * Gets the y value for the last row of the grid.
   * @return the bottommost y value
   */
  @Override
public final double getBottom() {
    return bottom;
  }

  /**
   * Gets the change in x between grid columns.
   * @return the bottommost y value
   */
  @Override
public final double getDx() {
    return dx;
  }

  /**
   * Gets the change in y between grid rows.
   * @return the bottommost y value
   */
  @Override
public final double getDy() {
    return dy;
  }

  /**
   * Gets the x coordinate for the given index.
   *
   * @param i int
   * @return double the x coordinate
   */
  @Override
public double indexToX(int i) {
    return(data==null) ? Double.NaN : left+dx*i;
  }

  /**
   * Gets the y coordinate for the given index.
   *
   * @param i int
   * @return double the y coordinate
   */
  @Override
public double indexToY(int i) {
    return(data==null) ? Double.NaN : top+dy*i;
  }

  /**
   * Gets closest index from the given x  world coordinate.
   *
   * @param x double the coordinate
   * @return int the index
   */
  @Override
public int xToIndex(double x) {
    if(data==null) {
      return 0;
    }
    int nx = getNx();
    double dx = (right-left)/nx;
    int i = (int) ((x-left)/dx);
    if(i<0) {
      return 0;
    }
    if(i>=nx) {
      return nx-1;
    }
    return i;
  }

  /**
   * Gets closest index from the given y  world coordinate.
   *
   * @param y double the coordinate
   * @return int the index
   */
  @Override
public int yToIndex(double y) {
    if(data==null) {
      return 0;
    }
    int ny = getNy();
    double dy = (top-bottom)/ny;
    int i = (int) ((top-y)/dy);
    if(i<0) {
      return 0;
    }
    if(i>=ny) {
      return ny-1;
    }
    return i;
  }

  /**
   * Returns the XML.ObjectLoader for this class.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  /**
   * A class to save and load Dataset data in an XMLControl.
   */
  private static class Loader extends XMLLoader {
    @Override
	public void saveObject(XMLControl control, Object obj) {
      ArrayData gpd = (ArrayData) obj;
      control.setValue("left", gpd.left);             //$NON-NLS-1$
      control.setValue("right", gpd.right);           //$NON-NLS-1$
      control.setValue("bottom", gpd.bottom);         //$NON-NLS-1$
      control.setValue("top", gpd.top);               //$NON-NLS-1$
      control.setValue("dx", gpd.dx);                 //$NON-NLS-1$
      control.setValue("dy", gpd.dy);                 //$NON-NLS-1$
      control.setValue("is cell data", gpd.cellData); //$NON-NLS-1$
      control.setValue("data", gpd.data);             //$NON-NLS-1$
    }

    @Override
	public Object createObject(XMLControl control) {
      return new ArrayData(1, 1, 1);
    }

    @Override
	public Object loadObject(XMLControl control, Object obj) {
      ArrayData gpd = (ArrayData) obj;
      double[][][] data = (double[][][]) control.getObject("data"); //$NON-NLS-1$
      gpd.data = data;
      gpd.left = control.getDouble("left");              //$NON-NLS-1$
      gpd.right = control.getDouble("right");            //$NON-NLS-1$
      gpd.bottom = control.getDouble("bottom");          //$NON-NLS-1$
      gpd.top = control.getDouble("top");                //$NON-NLS-1$
      gpd.dx = control.getDouble("dx");                  //$NON-NLS-1$
      gpd.dy = control.getDouble("dy");                  //$NON-NLS-1$
      gpd.cellData = control.getBoolean("is cell data"); //$NON-NLS-1$
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
