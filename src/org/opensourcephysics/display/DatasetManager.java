/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;
import org.opensourcephysics.numerics.SuryonoParser;
import org.opensourcephysics.tools.FunctionTool;
import org.opensourcephysics.tools.ToolsRes;

/**
 *
 * DatasetManager maintains a list of datasets. Datasets are added automatically
 * to this DatasetCollection by calling a method in this DatasetManager with a
 * dataset index greater than the maximum value for the dataset index that has
 * been used previously. For example the statements:
 * <code> DatasetManager datasetManager = new DatasetManager();
 *  datasetManager.append(0,3,4);
 *  datasetManager.append(1,5,6);</code> appends the point (3,4) to the 0th
 * dataset (and creates this dataset automatically) and appends the point (5,6)
 * to the 1-st dataset (and also creates this dataset automatically).
 *
 * @version 1.1
 * @author Joshua Gould
 * @author Wolfgang Christian
 * @created February 17, 2002
 *
 */
public class DatasetManager extends DataTable.DataModel implements Measurable, LogMeasurable, Data {
	
	final public Model model;
	
	public class Model extends DataTable.OSPTableModel {

		@Override
		public boolean isFoundOrdered() {
			return (dsFound == null || dsFound.model.isFoundOrdered());
		}

		@Override
		public int getStride() {
			return stride;
		}

		@Override
		public int getRowCount() {
			return DatasetManager.this.getRowCount();
		}

		@Override
		public int getColumnCount() {
			return DatasetManager.this.getColumnCount();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return Double.valueOf(DatasetManager.this.getValueAt(rowIndex, columnIndex));
		}
		
		@Override
		public Class<Double> getColumnClass(int columnIndex) {
			return Double.class;
		}
		
		@Override
    public String getColumnName(int column) {
    	return DatasetManager.this.getColumnName(column);
    }

	}


	ArrayList<Dataset> datasets = new ArrayList<Dataset>();
	boolean connected; // default values for new datasets
	boolean sorted;
	int markerShape;
	private int stride = 1;
	
	boolean linked = false; // whether x data in datasets is linked. If set to true, then x data for
							// datasets > 0 will not be shown in a table view.
	String xColumnName = "x", yColumnName = "y"; // default names for new datasets //$NON-NLS-1$ //$NON-NLS-2$
	ArrayList<String> constantNames = new ArrayList<String>();
	Map<String, Double> constantValues = new TreeMap<String, Double>();
	Map<String, String> constantExpressions = new TreeMap<String, String>();
	Map<String, String> constantDescriptions = new TreeMap<String, String>();
	String name = ""; //$NON-NLS-1$
	int datasetID = hashCode();
	public Dataset dsFound;

	/**
	 * used by TableTrackView
	 */
	private Dataset frameDataset;
	
//	/**
//	 * used in sending order to DataTool from TableTrackView
//	 */
//	private int[] jobColumnOrder;
//
//	/**
//	 * A one-time read, by the DataTool job loader. (unimplemented)
//	 * 
//	 * @return
//	 */
//	public int[] getjobColumnOrder() {
//		 // unimplemented
//		int[] order = jobColumnOrder;
//		jobColumnOrder = null;
//		return order;
//	}
	
	/**
	 *
	 * DatasetManager constructor.
	 *
	 */
	public DatasetManager() {
		this(false, false, false, Dataset.SQUARE);
	}

	/**
	 *
	 * DatasetManager constructor.
	 *
	 * @param linked
	 *
	 */
	public DatasetManager(boolean linked) {
		this(false, false, linked, Dataset.SQUARE);
	}

	/**
	 *
	 * DatasetManager constructor specifying whether points are connected and
	 * sorted.
	 * 
	 * @param _connected Description of Parameter
	 *
	 * @param _sorted    Description of Parameter
	 *
	 */
	public DatasetManager(boolean _connected, boolean _sorted) {
		this(_connected, _sorted, false, Dataset.SQUARE);
	}

	/**
	 *
	 * DatasetManager constructor specifying whether points are connected,
	 *
	 * sorted, and the marker shape.
	 *
	 *
	 *
	 * @param _connected   Description of Parameter
	 *
	 * @param _sorted      Description of Parameter
	 *
	 * @param _linked
	 *
	 * @param _markerShape Description of Parameter
	 *
	 */
	public DatasetManager(boolean _connected, boolean _sorted, boolean _linked, int _markerShape) {
		connected = _connected;
		sorted = _sorted;
		markerShape = _markerShape;
		linked = _linked;
		model = new Model();
	}

	/**
	 * Sets the linked flag. X data for datasets > 0 will not be shown in a table
	 * view.
	 *
	 * @param _linked The new value
	 */
	public void setXPointsLinked(boolean _linked) {
		linked = _linked;
		for (int i = 1; i < datasets.size(); i++) {
			Dataset dataset = datasets.get(i);
			dataset.setXColumnVisible(!linked);
		}
	}

	/**
	 * Gets the linked flag.
	 *
	 * @return true if linked
	 */
	public boolean isXPointsLinked() {
		return linked;
	}

	/**
	 *
	 * Sets the sorted flag. Data is sorted by increasing x.
	 *
	 * @param datasetIndex The new sorted value
	 * @param _sorted      <code>true<\code> to sort
	 */
	public void setSorted(int datasetIndex, boolean _sorted) {
		checkDatasetIndex(datasetIndex);
		Dataset dataset = datasets.get(datasetIndex);
		dataset.setSorted(_sorted);
	}

	/**
	 *
	 * Sets the sorted flag for all datasets.
	 *
	 * @param _sorted
	 *
	 */
	public void setSorted(boolean _sorted) {
		sorted = _sorted; // sorted for future datasets
		for (int i = 0; i < datasets.size(); i++) {
			(datasets.get(i)).setSorted(_sorted);
		}
	}

	/**
	 * Sets the data connected flag. Points are connected by straight lines.
	 *
	 * @param datasetIndex The new connected value
	 * @param _connected   <code>true<\code> if points are connected
	 */
	public void setConnected(int datasetIndex, boolean _connected) {
		checkDatasetIndex(datasetIndex);
		Dataset dataset = datasets.get(datasetIndex);
		dataset.setConnected(_connected);
	}

	/**
	 * Sets the connected flag for all datasets.
	 *
	 * @param _connected true if connected; false otherwise
	 */
	public void setConnected(boolean _connected) {
		connected = _connected; // sorted for future datasets
		for (int i = 0; i < datasets.size(); i++) {
			(datasets.get(i)).setConnected(_connected);
		}
	}

	/**
	 *
	 * Sets the stride for the given dataset.
	 *
	 * @param datasetIndex The new markerColor value
	 * @param stride
	 */
	public void setStride(int datasetIndex, int stride) {
		checkDatasetIndex(datasetIndex);
		Dataset dataset = datasets.get(datasetIndex);
		dataset.setStride(stride);
	}

	/**
	 * Sets the stride for all datasets.
	 * 
	 * @param _stride
	 */
	public void setStride(int _stride) {
		stride = _stride; // default stride for future datasets
		// set the stride for current datasets
		for (int i = 0; i < datasets.size(); i++) {
			(datasets.get(i)).setStride(stride);
		}
	}

	/**
	 * Sets the data point marker color.
	 *
	 * @param datasetIndex
	 * @param _markerColor
	 */
	public void setMarkerColor(int datasetIndex, Color _markerColor) {
		checkDatasetIndex(datasetIndex);
		Dataset dataset = datasets.get(datasetIndex);
		dataset.setMarkerColor(_markerColor);
	}

	/**
	 * Sets the data point marker's fill and edge color.
	 *
	 * @param datasetIndex
	 * @param fillColor
	 * @param edgeColor
	 */
	public void setMarkerColor(int datasetIndex, Color fillColor, Color edgeColor) {
		checkDatasetIndex(datasetIndex);
		Dataset dataset = datasets.get(datasetIndex);
		dataset.setMarkerColor(fillColor, edgeColor);
	}

	/**
	 * Sets the data point marker shape. Shapes are: NO_MARKER, CIRCLE, SQUARE,
	 * AREA, PIXEL, BAR, POST
	 *
	 * @param datasetIndex
	 * @param _markerShape
	 */
	public void setMarkerShape(int datasetIndex, int _markerShape) {
		checkDatasetIndex(datasetIndex);
		Dataset dataset = datasets.get(datasetIndex);
		dataset.setMarkerShape(_markerShape);
	}

	/**
	 * Sets a custom marker shape.
	 *
	 * @param datasetIndex int
	 * @param marker       Shape
	 */
	public void setCustomMarker(int datasetIndex, Shape marker) {
		checkDatasetIndex(datasetIndex);
		Dataset dataset = datasets.get(datasetIndex);
		dataset.setCustomMarker(marker);
	}

	/**
	 * Sets the visibility of the x column in a table view.
	 *
	 * @param datasetIndex
	 * @param visible
	 */
	public void setXColumnVisible(int datasetIndex, boolean visible) {
		checkDatasetIndex(datasetIndex);
		Dataset dataset = datasets.get(datasetIndex);
		dataset.setXColumnVisible(visible);
	}

	/**
	 * Sets the visibility of the y column in a table view.
	 *
	 * @param datasetIndex
	 * @param visible
	 */
	public void setYColumnVisible(int datasetIndex, boolean visible) {
		checkDatasetIndex(datasetIndex);
		Dataset dataset = datasets.get(datasetIndex);
		dataset.setYColumnVisible(visible);
	}

	/**
	 * Sets the half-width of the data point marker.
	 *
	 * @param datasetIndex
	 * @param _markerSize  in pixels
	 */
	public void setMarkerSize(int datasetIndex, int _markerSize) {
		checkDatasetIndex(datasetIndex);
		Dataset dataset = datasets.get(datasetIndex);
		dataset.setMarkerSize(_markerSize);
	}

	/**
	 * Sets the color of the lines connecting data points.
	 *
	 * @param datasetIndex
	 * @param _lineColor
	 */
	public void setLineColor(int datasetIndex, Color _lineColor) {
		checkDatasetIndex(datasetIndex);
		Dataset dataset = datasets.get(datasetIndex);
		dataset.setLineColor(_lineColor);
	}

	/**
	 * Line colors for Data interface.
	 * 
	 * @return color array
	 */
	@Override
	public java.awt.Color[] getLineColors() {
		return null;
	}

	/**
	 * Fill colors for Data interface.
	 * 
	 * @return color array
	 */
	@Override
	public java.awt.Color[] getFillColors() {
		return null;
	}

	/**
	 * Sets the column names when rendering this dataset in a JTable.
	 *
	 * @param datasetIndex
	 * @param xColumnName
	 * @param yColumnName
	 * @param datsetName
	 */
	public void setXYColumnNames(int datasetIndex, String xColumnName, String yColumnName, String datsetName) {
		checkDatasetIndex(datasetIndex);
		Dataset dataset = datasets.get(datasetIndex);
		dataset.setXYColumnNames(xColumnName, yColumnName, datsetName);
	}

	/**
	 * Sets the column names when rendering this dataset in a JTable.
	 *
	 * @param datasetIndex
	 * @param xColumnName
	 * @param yColumnName
	 */
	public void setXYColumnNames(int datasetIndex, String xColumnName, String yColumnName) {
		checkDatasetIndex(datasetIndex);
		Dataset dataset = datasets.get(datasetIndex);
		dataset.setXYColumnNames(xColumnName, yColumnName);
		if ("frame".equals(yColumnName))
			frameDataset = dataset;
	}

	/**
	 * Gets the valid measure flag. The measure is valid if the min and max values
	 * have been set for at least one dataset.
	 *
	 * @return <code>true<\code> if measure is valid
	 */
	@Override
	public boolean isMeasured() {
		for (int i = 0; i < datasets.size(); i++) {
			Dataset d = datasets.get(i);
			if (d.isMeasured()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Sets the ID number of this Data.
	 *
	 * @param id the ID number
	 */
	@Override
	public void setID(int id) {
		datasetID = id;
	}

	/**
	 * Returns a unique identifier for this Data.
	 *
	 * @return the ID number
	 */
	@Override
	public int getID() {
		return datasetID;
	}

	/**
	 * Gets the x world coordinate for the left hand side of the panel.
	 *
	 * @return xmin
	 */
	@Override
	public double getXMin() {
		double xmin = Double.MAX_VALUE;
		for (int i = 0; i < datasets.size(); i++) {
			Dataset d = datasets.get(i);
			if (d.isMeasured()) {
				xmin = Math.min(xmin, d.getXMin());
			}
		}
		return xmin;
	}

	@Override
	public double getXMinLogscale() {
		double xmin = Double.MAX_VALUE;
		for (int i = 0; i < datasets.size(); i++) {
			Dataset d = datasets.get(i);
			if (d.isMeasured()) {
				xmin = Math.min(xmin, d.getXMinLogscale());
			}
		}
		return Math.max(Float.MIN_VALUE, xmin);
	}

	/**
	 * Gets the x world coordinate for the right hand side of the panel.
	 *
	 * @return xmax
	 */
	@Override
	public double getXMax() {
		double xmax = -Double.MAX_VALUE;
		for (int i = 0; i < datasets.size(); i++) {
			Dataset d = datasets.get(i);
			if (d.isMeasured()) {
				xmax = Math.max(xmax, d.getXMax());
			}
		}
		return xmax;
	}

	@Override
	public double getXMaxLogscale() {
		double xmax = -Double.MAX_VALUE;
		for (int i = 0; i < datasets.size(); i++) {
			Dataset d = datasets.get(i);
			if (d.isMeasured()) {
				xmax = Math.max(xmax, d.getXMaxLogscale());
			}
		}
		return Math.max(Float.MIN_VALUE, xmax);
	}

	/**
	 * Gets y world coordinate for the bottom of the panel.
	 *
	 * @return ymin
	 */
	@Override
	public double getYMin() {
		double ymin = Double.MAX_VALUE;
		for (int i = 0; i < datasets.size(); i++) {
			Dataset d = datasets.get(i);
			if (d.isMeasured()) {
				ymin = Math.min(ymin, d.getYMin());
			}
		}
		return ymin;
	}

	@Override
	public double getYMinLogscale() {
		double ymin = Double.MAX_VALUE;
		for (int i = 0; i < datasets.size(); i++) {
			Dataset d = datasets.get(i);
			if (d.isMeasured()) {
				ymin = Math.min(ymin, d.getYMinLogscale());
			}
		}
		return Math.max(Float.MIN_VALUE, ymin);
	}

	/**
	 * Gets y world coordinate for the top of the panel.
	 *
	 * @return ymax
	 */
	@Override
	public double getYMax() {
		double ymax = -Double.MAX_VALUE;
		for (int i = 0; i < datasets.size(); i++) {
			Dataset d = datasets.get(i);
			if (d.isMeasured()) {
				ymax = Math.max(ymax, d.getYMax());
			}
		}
		return ymax;
	}

	@Override
	public double getYMaxLogscale() {
		double ymax = -Double.MAX_VALUE;
		for (int i = 0; i < datasets.size(); i++) {
			Dataset d = datasets.get(i);
			if (d.isMeasured()) {
				ymax = Math.max(ymax, d.getYMaxLogscale());
			}
		}
		return Math.max(Float.MIN_VALUE, ymax);
	}

	/**
	 * Gets a copy of the xpoints array.
	 *
	 * @param datasetIndex Description of Parameter
	 * @return xpoints[]
	 */
	public double[] getXPoints(int datasetIndex) {
		checkDatasetIndex(datasetIndex);
		Dataset dataset = datasets.get(datasetIndex);
		return dataset.getXPoints();
	}

	/**
	 * Gets a copy of the ypoints array.
	 *
	 * @param datasetIndex Description of Parameter
	 * @return ypoints[]
	 */
	public double[] getYPoints(int datasetIndex) {
		checkDatasetIndex(datasetIndex);
		Dataset dataset = datasets.get(datasetIndex);
		return dataset.getYPoints();
	}

	/**
	 * Gets the sorted flag.
	 *
	 * @param datasetIndex Description of Parameter
	 * @return <code>true<\code> if the data is sorted
	 */
	public boolean isSorted(int datasetIndex) {
		checkDatasetIndex(datasetIndex);
		Dataset dataset = datasets.get(datasetIndex);
		return dataset.isSorted();
	}

	/**
	 * Gets the data connected flag.
	 *
	 * @param datasetIndex Description of Parameter
	 * @return <code>true<\code> if points are connected
	 */
	public boolean isConnected(int datasetIndex) {
		checkDatasetIndex(datasetIndex);
		Dataset dataset = datasets.get(datasetIndex);
		return dataset.isConnected();
	}

	/**
	 * Gets the number of columns for rendering in a JTable.
	 *
	 * @return the count
	 */
	@Override
	public int getColumnCount() {
		int columnCount = 0;
		for (int i = 0; i < datasets.size(); i++) {
			Dataset d = datasets.get(i);
			if (d == null)
				continue; // added by DB to prevent occasional null pointer exception here
			columnCount += d.getColumnCount();
		}
		return columnCount;
	}

	/**
	 * Gets the number of rows for rendering in a JTable.
	 *
	 * @return the count
	 */
	@Override
	public int getRowCount() {
		int rowCount = 0;
		for (int i = 0; i < datasets.size(); i++) {
			Dataset d = datasets.get(i);
			rowCount = Math.max(rowCount, d.getRowCount());
		}
		return rowCount;
	}

	/**
	 * Gets the name of this data.
	 *
	 * @return name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of this data.
	 *
	 * @param name
	 */
	public void setName(String name) {
		if (name != null) {
			this.name = TeXParser.parseTeX(name);
		}
	}

	/**
	 * Gets the name of the column for rendering in a JTable
	 *
	 * @param tableColumnIndex
	 * @return the name
	 */
	@Override
	public String getColumnName(int tableColumnIndex) {
		if (datasets.size() == 0) {
			return null;
		}
		Dataset ds = find(tableColumnIndex);
		return (ds == null ? null : ds.getColumnName(ds.foundColumn));
	}

//	/**
//	 * Gets an x or y value for rendering in a JTable.
//	 *
//	 * @param rowIndex
//	 * @param tableColumnIndex
//	 * @return the datum
//	 */
//	@Override
//	public Object getValueAt(int rowIndex, int tableColumnIndex) {
//		if (datasets.size() == 0 || tableColumnIndex < 0) {
//			return null;
//		}
//		Dataset ds = find(tableColumnIndex);
//		return (ds == null || rowIndex >= ds.getRowCount() ? null : ds.getValueAt(rowIndex, ds.foundColumn));
//	}

	@Override
	public double getValueAt(int row, int col) {
		if (datasets.size() == 0 || col < 0) {
			return Double.NaN;
		}
		Dataset ds = find(col);
		return (ds == null || row >= ds.getRowCount() ? Double.NaN : ds.getValueAt(row, ds.foundColumn));
	}

	private Dataset find(int icol) {
		if (icol >= 0)
			for (int i = 0, ncol = 0, n = datasets.size(); i < n; i++) {
				Dataset ds = datasets.get(i);
				int nVis = ds.getColumnCount();
				if (ncol + nVis > icol) {
					ds.foundColumn = icol - ncol;
					return dsFound = ds;
				}
				ncol += nVis;
			}
		return dsFound = null;
	}

	/**
	 * Appends an (x,y) datum to the Dataset with the given index.
	 *
	 * @param x
	 * @param y
	 * @param datasetIndex Description of Parameter
	 */
	public void append(int datasetIndex, double x, double y) {
		checkDatasetIndex(datasetIndex);
		datasets.get(datasetIndex).append(x, y);
	}

	/**
	 * Appends a data point and its uncertainty to the Dataset.
	 * 
	 * (not used)
	 *
	 * @param datasetIndex
	 * @param x
	 * @param y
	 * @param delx
	 * @param dely
	 *
	 */
	public void append(int datasetIndex, double x, double y, double delx, double dely) {
		checkDatasetIndex(datasetIndex);
		Dataset dataset = datasets.get(datasetIndex);
		dataset.append(x, y, delx, dely);
	}

	/**
	 * Appends (x,y) arrays to the Dataset.
	 *
	 * @param xpoints
	 * @param ypoints
	 * @param datasetIndex Description of Parameter
	 */
	public void append(int datasetIndex, double[] xpoints, double[] ypoints) {
		checkDatasetIndex(datasetIndex);
		Dataset dataset = datasets.get(datasetIndex);
		dataset.append(xpoints, ypoints);
	}

	/**
	 * Appends arrays of data points and uncertainties to the Dataset.
	 *
	 * @param datasetIndex
	 * @param xpoints
	 * @param ypoints
	 * @param delx
	 * @param dely
	 */
	public void append(int datasetIndex, double[] xpoints, double[] ypoints, double[] delx, double[] dely) {
		checkDatasetIndex(datasetIndex);
		Dataset dataset = datasets.get(datasetIndex);
		dataset.append(xpoints, ypoints, delx, dely);
	}

	/**
	 * Draws this Dataset in the drawing panel.
	 *
	 * @param drawingPanel
	 * @param g
	 */
	@Override
	public void draw(DrawingPanel drawingPanel, Graphics g) {
		for (int i = 0; i < datasets.size(); i++) {
			(datasets.get(i)).draw(drawingPanel, g);
		}
	}

	/**
	 * Clears all data from Dataset with the given datasetIndex.
	 *
	 * @param datasetIndex Description of Parameter
	 */
	public void clear(int datasetIndex) {
		checkDatasetIndex(datasetIndex);
		Dataset dataset = datasets.get(datasetIndex);
		dataset.clear();
	}

	/**
	 * Clears all data from all Datasets.
	 *
	 * Dataset properties are preserved because only the data is cleared.
	 */
	public void clear() {
		for (int i = 0; i < datasets.size(); i++) {
			datasets.get(i).clear();
		}
	}

	/**
	 * Removes all Datasets from the manager.
	 *
	 * New datasets will be created with default properties as needed.
	 */
	public void removeDatasets() {
		clear();
		datasets.clear();
		// for DataTable
		model.fireTableChanged(new TableModelEvent(model, 0, Integer.MAX_VALUE, -1, TableModelEvent.DELETE));
	}

	/**
	 * Gets a dataset with the given index.
	 *
	 * @param datasetIndex
	 * @return the index
	 *
	 */
	public Dataset getDataset(int datasetIndex) {
		checkDatasetIndex(datasetIndex);
		return datasets.get(datasetIndex);
	}

	/**
	 * Gets a shallow clone of the dataset list. Implements Data.
	 * 
	 * @return cloned list
	 */
	@Override
	final public ArrayList<Dataset> getDatasets() {
		return new ArrayList<Dataset>(datasets);
	}

	final public ArrayList<Dataset> getDatasetsRaw() {
		return datasets;
	}

	/**
	 * Some objects (eg, a Group) do not contain data, but a list of Data objects
	 * that do. This method is used by Data displaying tools to create as many pages
	 * as needed.
	 * 
	 * @return a list of Data objects, or null if this object contains data
	 */
	@Override
	public java.util.List<Data> getDataList() {
		return null;
	}

	/**
	 * The column names to be used in the data display tool
	 * 
	 * @return string array
	 */
	@Override
	public String[] getColumnNames() {
		int n = datasets.size();
		String[] names = new String[n];
		for (int i = 0; i < n; i++) {
			if (datasets.get(i) != null) {
				names[i] = datasets.get(i).getName();
			}
		}
		return names;
	}

	/**
	 * Gets a 2D array of data. Implements Data.
	 *
	 * @return double[][]
	 */
	@Override
	public double[][] getData2D() {
		if (isXPointsLinked()) {
			int count = datasets.size();
			int index = 0;
			for (Dataset next : datasets) {
				index = Math.max(index, next.getIndex());
			}
			double[][] data = new double[count + 1][index];
			Dataset src = datasets.get(0);
			double[] d = src.getXPointsRaw();
			int n = src.getIndex();
			System.arraycopy(d, 0, data[0], 0, n);
			for (int i = 0; i < count; i++) {
				src = datasets.get(i);
				d = (src.isShifted() ? src.getYPoints() : src.getYPointsRaw());
				System.arraycopy(d, 0, data[i + 1], 0, n);
			}
			return data;
		}
		return null;
	}

	/**
	 * Gets a 3D array of data. Implements Data.
	 *
	 * @return double[][][]
	 */
	@Override
	public double[][][] getData3D() {
		return null;
	}

	/**
	 * Adds a dataset. Method added by Doug Brown 2007-1-15.
	 *
	 * @param dataset the Dataset to add
	 * @return the index of the added dataset
	 */
	public int addDataset(Dataset dataset) {
		if (linked && !datasets.isEmpty()) {
			dataset.setXColumnVisible(false);
		}
		int n = datasets.size();
		datasets.add(dataset);
		if ("frame".equals(dataset.getYColumnName())) { //$NON-NLS-1$
			frameDataset = dataset;
		}

		// for DataTable
		model.fireTableChanged(new TableModelEvent(model, 0, Integer.MAX_VALUE, n, TableModelEvent.INSERT));
		return n;
	}

	/**
	 * Removes the dataset at the specified index. Method added by Doug Brown
	 * 1/15/2007.
	 *
	 * @param index the index
	 * @return the removed dataset, or null if none removed
	 */
	public Dataset removeDataset(int index) {
		if ((index < 0) || (index > datasets.size() - 1)) {
			return null;
		}
		Dataset d = datasets.remove(index);
		// for DataTable
		model.fireTableChanged(new TableModelEvent(model, 0, Integer.MAX_VALUE, index, TableModelEvent.DELETE));
		return d;
	}

	/**
	 * Returns the index of the first dataset with the specified y column name.
	 * Method added by Doug Brown 1/15/2007.
	 *
	 * @param yColumnName the y column name
	 * @return the index, or -1 if none found
	 */
	public int getDatasetIndex(String yColumnName) {
		for (int i = 0; i < datasets.size(); i++) {
			if (datasets.get(i).getYColumnName().equals(yColumnName)) {
				return i;
			}
		}
		return -1;
	}

//	public String[] getConstantNames() {
//		return constantNames.toArray(new String[constantNames.size()]);
//	}

	public ArrayList<String> getConstantNames() {
		return constantNames;
	}

	/**
	 * Returns the value of a constant. Added by Doug Brown 3/24/2011.
	 *
	 * @param name the name of the constant
	 * @return Double value of the constant, or null if not defined
	 */
	public Double getConstantValue(String name) {
		return constantValues.get(name);
	}

	/**
	 * Returns the expression of a constant. Added by Doug Brown 3/24/2011.
	 *
	 * @param name the name of the constant
	 * @return the expression, or null if not defined
	 */
	public String getConstantExpression(String name) {
		return constantExpressions.get(name);
	}

	/**
	 * Returns the description of a constant. Added by Doug Brown 11/23/14.
	 *
	 * @param name the name of the constant
	 * @return the description, or null if not defined
	 */
	public String getConstantDescription(String name) {
		return constantDescriptions.get(name);
	}

	/**
	 * Sets the value of a constant. Added by Doug Brown 3/24/2011. modified
	 * 11/23/14.
	 *
	 * @param name       the name of the constant
	 * @param val        the value of the constant
	 * @param expression the expression that defines the value
	 */
	public void setConstant(String name, double val, String expression) {
		if (!constantNames.contains(name))
			constantNames.add(name);
		constantValues.put(name, val);
		constantExpressions.put(name, expression);
	}

	/**
	 * Sets the value of a constant. Added by Doug Brown 11/23/14.
	 *
	 * @param name       the name of the constant
	 * @param val        the value of the constant
	 * @param expression the expression that defines the value
	 * @param desc       the description of the constant (may be null)
	 */
	public void setConstant(String name, double val, String expression, String desc) {
		if (!constantNames.contains(name))
			constantNames.add(name);
		constantValues.put(name, val);
		constantExpressions.put(name, expression);
		constantDescriptions.put(name, desc);
	}

	/**
	 * Clears a constant. Added by Doug Brown 3/24/2011, modified 11/23/14.
	 *
	 * @param name the name of the constant
	 */
	public void clearConstant(String name) {
		constantNames.remove(name);
		constantValues.remove(name);
		constantExpressions.remove(name);
		constantDescriptions.remove(name);
	}

	/**
	 * Create a string representation of the data.
	 * 
	 * @return a String of data
	 */
	@Override
	public String toString() {
		if (datasets.size() == 0) {
			return "No data in datasets."; //$NON-NLS-1$
		}
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < datasets.size(); i++) {
			b.append("Dataset "); //$NON-NLS-1$
			b.append(i);
			b.append('\n');
			b.append(datasets.get(i).toString());
		}
		return b.toString();
	}

	/**
	 *
	 * Sets the column names for all datasets when rendering this dataset in a
	 * JTable.
	 *
	 *
	 *
	 * @param _xColumnName
	 *
	 * @param _yColumnName
	 *
	 */
	public void setXYColumnNames(String _xColumnName, String _yColumnName) {
		xColumnName = _xColumnName; // default names for future datasets
		yColumnName = _yColumnName; // default names for future datasets
		// set the column names for current datasets
		for (int i = 0, size = datasets.size(); i < size; i++) {
			(datasets.get(i)).setXYColumnNames(_xColumnName, _yColumnName);
		}
	}

	/**
	 * Ensures capacity
	 *
	 * @param datasetIndex
	 */
	protected void checkDatasetIndex(int datasetIndex) {
		while (datasetIndex >= datasets.size()) {
			Dataset d = new Dataset(DisplayColors.getMarkerColor(datasetIndex),
					DisplayColors.getLineColor(datasetIndex), connected); // use specified colors
			if (linked && (datasets.size() > 0)) {
				d.setXColumnVisible(false); // hide all x points in new datasets (except the 0th dataset)
			}
			d.setSorted(sorted);
			d.setXYColumnNames(xColumnName, yColumnName);
			d.setMarkerShape(markerShape);
			datasets.add(d);
		}
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
			DatasetManager dsm = (DatasetManager) obj;
			control.setValue("connected", dsm.connected); //$NON-NLS-1$
			control.setValue("sorted", dsm.sorted); //$NON-NLS-1$
			control.setValue("maker_shape", dsm.markerShape); //$NON-NLS-1$
			control.setValue("stride", dsm.stride); //$NON-NLS-1$
			control.setValue("linked", dsm.linked); //$NON-NLS-1$
			control.setValue("x_column_name", dsm.xColumnName); //$NON-NLS-1$
			control.setValue("y_column_name", dsm.yColumnName); //$NON-NLS-1$
			control.setValue("data_name", dsm.name); //$NON-NLS-1$
			control.setValue("datasets", dsm.datasets); //$NON-NLS-1$
			control.setValue("id", dsm.datasetID); //$NON-NLS-1$
//			if (dsm.jobColumnOrder != null)
//				control.setValue("column_order", dsm.jobColumnOrder);
		}

		@Override
		public Object createObject(XMLControl control) {
			return new DatasetManager();
		}

		@Override
		public Object loadObject(XMLControl control, Object obj) {
			DatasetManager dsm = (DatasetManager) obj;
			dsm.connected = control.getBoolean("connected"); //$NON-NLS-1$
			dsm.sorted = control.getBoolean("sorted"); //$NON-NLS-1$
			dsm.markerShape = control.getInt("maker_shape"); //$NON-NLS-1$
			dsm.stride = control.getInt("stride"); //$NON-NLS-1$
			dsm.linked = control.getBoolean("linked"); //$NON-NLS-1$
			dsm.xColumnName = control.getString("x_column_name"); //$NON-NLS-1$
			dsm.yColumnName = control.getString("y_column_name"); //$NON-NLS-1$
//			dsm.jobColumnOrder = (int[]) control.getObject("column_order");
			dsm.setName(control.getString("data_name")); //$NON-NLS-1$
			if (control.getPropertyNamesRaw().contains("id")) { //$NON-NLS-1$
				dsm.setID(control.getInt("id")); //$NON-NLS-1$
			}
			dsm.removeDatasets();
			Collection<?> datasets = Collection.class.cast(control.getObject("datasets")); //$NON-NLS-1$
			if (datasets != null) {
				Iterator<?> it = datasets.iterator();
				while (it.hasNext()) {
					dsm.datasets.add((Dataset) it.next());
				}
			}
			return obj;
		}

	}

	public Dataset getFrameDataset() {
		return frameDataset;
	}

	/**
	 * Returns true if name is a duplicate of an existing dataset.
	 *
	 * @param d    the dataset
	 * @param name the proposed name for the dataset
	 * @return true if duplicate
	 */
	public boolean isDuplicateName(Dataset d, String name) {
		if (datasets.isEmpty()) {
			return false;
		}
		if (getDataset(0).getXColumnName().equals(name)) {
			return true;
		}
		name = TeXParser.removeSubscripting(name);
		for (int i = 0, n = datasets.size(); i < n; i++) {
			Dataset next = datasets.get(i);
			if (next != d && TeXParser.removeSubscripting(next.getYColumnName()).equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a column name that is provided by the user and is unique to this tab,
	 * contains no spaces, and is not reserved by the OSP parser.
	 *
	 * @param d        the dataset
	 * @param proposed the proposed name to be offered to the user, or null
	 * @return unique name
	 */
	public String getUniqueYColumnName(Component c, Dataset d, String proposed) {
		proposed = GUIUtils.showInputDialog(c, ToolsRes.getString("DataToolTab.Dialog.NameColumn.Message"), //$NON-NLS-1$
				ToolsRes.getString("DataToolTab.Dialog.NameColumn.Title"), //$NON-NLS-1$
				JOptionPane.QUESTION_MESSAGE, uniquifyColumnName(d, proposed));
		if (proposed == null || (proposed = proposed.trim()).length() == 0) {
			return null;
		}
		// remove all spaces
		proposed = proposed.replaceAll(" ", ""); //$NON-NLS-1$ //$NON-NLS-2$
		boolean containsOperators = FunctionTool.arrayContains(FunctionTool.parserOperators, proposed);
		// check for duplicate or reserved names
//		if (askUser || containsOperators) {
		int tries = 0, maxTries = 3;
		while (tries < maxTries) {
			tries++;
			if (isDuplicateName(d, proposed)) {
				Object response = GUIUtils.showInputDialog(c, "\"" + proposed + "\" " + //$NON-NLS-1$ //$NON-NLS-2$
						ToolsRes.getString("DataFunctionPanel.Dialog.DuplicateName.Message"), //$NON-NLS-1$
						ToolsRes.getString("DataFunctionPanel.Dialog.DuplicateName.Title"), //$NON-NLS-1$
						JOptionPane.WARNING_MESSAGE, proposed);
				proposed = (response == null) ? null : response.toString();
			}
			if ((proposed == null) || proposed.equals("")) { //$NON-NLS-1$
				return null;
			}
			if (FunctionTool.isReservedName(proposed)) {
				Object response = GUIUtils.showInputDialog(c, "\"" + proposed + "\" " + //$NON-NLS-1$ //$NON-NLS-2$
						ToolsRes.getString("DataToolTab.Dialog.ReservedName.Message"), //$NON-NLS-1$
						ToolsRes.getString("DataToolTab.Dialog.ReservedName.Title"), //$NON-NLS-1$
						JOptionPane.WARNING_MESSAGE, proposed);
				proposed = (response == null) ? null : response.toString();
			}
			if ((proposed == null) || proposed.equals("")) { //$NON-NLS-1$
				return null;
			}
			containsOperators = FunctionTool.arrayContains(FunctionTool.parserOperators, proposed);
			if (containsOperators) {
				Object response = GUIUtils.showInputDialog(c, ToolsRes.getString("DataToolTab.Dialog.OperatorInName.Message"), //$NON-NLS-1$
						ToolsRes.getString("DataToolTab.Dialog.OperatorInName.Title"), //$NON-NLS-1$
						JOptionPane.WARNING_MESSAGE, proposed);
				proposed = (response == null) ? null : response.toString();
			}
			if ((proposed == null) || proposed.equals("")) { //$NON-NLS-1$
				return null;
			}
		}
//		}
		return (containsOperators ? null : uniquifyColumnName(d, proposed));
	}

	public String uniquifyColumnName(Dataset d, String name) {
		if (name == null)
			return null;
		int i = 0;
		// trap for names that are numbers
		if (!Double.isNaN(SuryonoParser.getNumber(name))) {
			name = ToolsRes.getString("DataToolTab.NewColumn.Name"); //$NON-NLS-1$
		}
		// remove existing number subscripts, if any, from duplicate names
		boolean subscriptRemoved = false;
		if (isDuplicateName(d, name)) {
			String subscript = TeXParser.getSubscript(name);
			double di; // check for integer subscript
			if ((di = SuryonoParser.getNumber(subscript)) == (int) di) {
				name = TeXParser.removeSubscript(name);
				subscriptRemoved = true;
			}
		}
		while (subscriptRemoved || isDuplicateName(d, name) || FunctionTool.isReservedName(name)) {
			name = TeXParser.addSubscript(name, "" + ++i);
			subscriptRemoved = false;
		}
		return name;
	}

	public double get(String var, int row, int col) {
		return getDataset(getDatasetIndex(var)).getValueAt(row, col);
	}

//	public void setJobColumnOrder(int[] modelColumnOrder) {
//		jobColumnOrder = modelColumnOrder;
//	}

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
