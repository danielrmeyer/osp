/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

/*
 * The org.opensourcephysics.media package defines the Open Source Physics
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
package org.opensourcephysics.media.gif;
import java.io.IOException;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.media.core.VideoFileFilter;
import org.opensourcephysics.media.core.Video;
import org.opensourcephysics.media.core.VideoRecorder;
import org.opensourcephysics.media.core.VideoType;

/**
 * This implements the VideoType interface with a buffered image type.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class GifVideoType implements VideoType {

	protected static VideoFileFilter gifFilter = new VideoFileFilter("gif", new String[] { "gif" }); //$NON-NLS-1$ //$NON-NLS-2$

	@Override
	public Video getVideo(String name, String basePath, XMLControl control) {
		Video video;
		try {
			video = new GifVideo(XML.getResolvedPath(name, basePath));
			video.setProperty("video_type", this); //$NON-NLS-1$
		} catch (IOException ex) {
			OSPLog.fine(ex.getMessage());
			video = null;
		}
		return video;
	}

	/**
	 * Gets a gif video recorder.
	 *
	 * @return the video recorder
	 */
	@Override
	public VideoRecorder getRecorder() {
		return new GifVideoRecorder();
	}

	/**
	 * Reports whether this type can record videos
	 *
	 * @return true if this can record videos
	 */
	@Override
	public boolean canRecord() {
		return true;
	}

	/**
	 * Gets the name and/or description of this type.
	 *
	 * @return a description
	 */
	@Override
	public String getDescription() {
		return gifFilter.getDescription();
	}

	/**
	 * Gets the name and/or description of this type.
	 *
	 * @return a description
	 */
	@Override
	public String getDefaultExtension() {
		return gifFilter.getDefaultExtension();
	}

	/**
	 * Gets the file filter for this type.
	 *
	 * @return a file filter
	 */
	@Override
	public VideoFileFilter[] getFileFilters() {
		return new VideoFileFilter[] { gifFilter };
	}

	/**
	 * Gets the default file filter for this type. May return null.
	 *
	 * @return the default file filter
	 */
	@Override
	public VideoFileFilter getDefaultFileFilter() {
		return gifFilter;
	}

	/**
	 * Return true if the specified video is this type.
	 *
	 * @param video the video
	 * @return true if the video is this type
	 */
	@Override
	public boolean isType(Video video) {
		return video instanceof GifVideo;
	}

	@Override
	public String getTypeName() {
		return TYPE_GIF;
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
