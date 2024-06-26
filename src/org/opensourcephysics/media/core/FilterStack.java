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

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;



/**
 * This is a Filter that contains and manages a series of Filters.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class FilterStack extends Filter implements PropertyChangeListener {
	// instance fields
	private ArrayList<Filter> filters = new ArrayList<Filter>();
	private Filter postFilter;
	private int indexRemoved = -1;

	/**
	 * Constructs a FilterStack object.
	 */
	public FilterStack() {

		/** empty block */
	}

	/**
	 * Adds a filter to the end of the stack. Multiple filters are applied in the
	 * order they are added.
	 *
	 * @param filter the filter
	 */
	public void addFilter(Filter filter) {
		filters.add(filter);
		filter.stack = this;
		filter.addPropertyChangeListenerSafely(this);
		notifyUpdate(null, filter);
	}

	public void addFilters(Collection<Filter> stack) {
		if (stack != null)
			for (Filter f : stack) {
				addFilter(f);
			}
	}

	public void addFilters(FilterStack stack) {
		if (stack != null)
			for (Filter f : stack.filters) {
				addFilter(f);
			}
	}

	private void notifyUpdate(Filter oldFilter, Filter newFilter) {
		firePropertyChange(PROPERTY_FILTER_IMAGE, null, null); // $NON-NLS-1$
		firePropertyChange(PROPERTY_FILTER_FILTER, oldFilter, newFilter); // $NON-NLS-1$
	}

	/**
	 * Adds a filter at the specified index. Multiple filters are applied in index
	 * order.
	 *
	 * @param filter the filter
	 * @param index  the index
	 */
	public void insertFilter(Filter filter, int index) {
		index = Math.min(index, filters.size());
		index = Math.max(index, 0);
		filters.add(index, filter);
		filter.stack = this;
		filter.addPropertyChangeListener(this);
		notifyUpdate(null, filter);
	}

	/**
	 * Gets the index of the last removed filter, or -1 if none removed.
	 *
	 * @return the stack index.
	 */
	public int lastIndexRemoved() {
		return indexRemoved;
	}

	/**
	 * Sets the post filter. If non-null, the post filter is applied after all other
	 * filters.
	 *
	 * @param filter a filter
	 */
	public void setPostFilter(Filter filter) {
		if (postFilter != null) {
			postFilter.removePropertyChangeListener(this);
		}
		postFilter = filter;
		if (filter != null) {
			filter.addPropertyChangeListener(this);
			notifyUpdate(null, filter);
		}
	}

	/**
	 * Gets the post filter.
	 *
	 * @return the post filter
	 */
	public Filter getPostFilter() {
		return postFilter;
	}

	/**
	 * Gets the first instance of the specified filter class. May return null.
	 *
	 * @param filterClass the filter class
	 * @return the first filter of the specified class, if any
	 */
	public Filter getFilter(Class<?> filterClass) {
		Iterator<Filter> it = filters.iterator();
		while (it.hasNext()) {
			Filter filter = it.next();
			if (filter.getClass() == filterClass) {
				return filter;
			}
		}
		return null;
	}

	/**
	 * Removes the specified filter from the stack.
	 *
	 * @param filter the filter
	 */
	public void removeFilter(Filter filter) {
		indexRemoved = filters.indexOf(filter);
		if (indexRemoved > -1) {
			filters.remove(filter);
			filter.dispose();
			notifyUpdate(filter, null);
		}
		System.gc();
	}

	@Override
	public void dispose() {
		clear();
		super.dispose();
	}
	/**
	 * Clears the filter stack.
	 */
	@Override
	public void clear() {
		for (Filter filter : filters) {
			filter.dispose();
		}
		filters.clear();
		notifyUpdate(null, null);
		System.gc();
	}

	/**
	 * Returns true if this contains no filters.
	 *
	 * @return <code>true</code> if this is empty
	 */
	public boolean isEmpty() {
		return filters.isEmpty() && (postFilter == null);
	}

	/**
	 * Returns a copy of the filters in this filter stack.
	 *
	 * @return a collection of filters
	 */
	public ArrayList<Filter> getFilters() {
		return new ArrayList<Filter>(filters);
	}

	/**
	 * Returns the current filtered image. Called by VideoAdapter or self exclusively.
	 *
	 * @param sourceImage the image to filter
	 * @return the filtered image
	 */
	@Override
	public BufferedImage getFilteredImage(BufferedImage sourceImage) {
		if (!isEnabled()) {
			return sourceImage;
		}
		boolean mustWork = (postFilter != null && postFilter.isEnabled());
		for (int i = 0, n = filters.size(); !mustWork && i < n; i++) {
			mustWork = filters.get(i).isEnabled();
		}
		if (!mustWork) {
			return sourceImage;
		}
		
		for (int i = 0, n = filters.size(); i <= n; i++) {
			Filter filter = (i == n ? postFilter : filters.get(i));
			if (filter != null && filter.isEnabled())
				sourceImage = filter.getFilteredImage(sourceImage);
		}
		
		
		return sourceImage;
	}

	@Override
	protected void setOutputPixels() {
		// n/a
	}

	/**
	 * Implements abstract Filter method.
	 *
	 * @return the inspector
	 */
	@Override
	public InspectorDlg newInspector() {
		return null;
	}

	@Override
	protected InspectorDlg initInspector() {
		return null;
	}
	
	/**
	 * Shows/hides all inspectors.
	 *
	 * @param vis true to show inspectors
	 */
	public void setInspectorsVisible(boolean vis) {
		Collection<Filter> filters = getFilters();
		Iterator<Filter> it = filters.iterator();
		while (it.hasNext()) {
			Filter filter = it.next();
			InspectorDlg inspector = (InspectorDlg) filter.getInspector();
			if (inspector != null) {
				if (!vis) {
					// set the inspector's current visibility flag
					filter.inspectorVisible = inspector.isVisible();
					// make sure all inspectors are hidden
					inspector.setVisible(false);
				} else if (!inspector.isModal()) { // show only non-modal inspectors
					// use the visibility flag to set the visibility of every inspector
					inspector.setVisible(filter.inspectorVisible);
				}
			}
		}
	}

	/**
	 * Refreshes this filter's GUI
	 */
	@Override
	public void refresh() {
		Iterator<Filter> it = getFilters().iterator();
		while (it.hasNext()) {
			it.next().refresh();
		}
	}

	/**
	 * Responds to property change events. FilterStack listens for the following
	 * events: all events from its filters.
	 *
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		switch (e.getPropertyName()) {
		case Video.PROPERTY_VIDEO_FILTERCHANGED:
			firePropertyChange(Video.PROPERTY_VIDEO_FILTERCHANGED, e.getOldValue(), e.getNewValue()); //$NON-NLS-1$
			break;
		default:
			firePropertyChange(PROPERTY_FILTER_IMAGE, null, null); // $NON-NLS-1$
			break;
		}
	}

	@Override
	protected void initializeSubclass() {
		// n/a
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
