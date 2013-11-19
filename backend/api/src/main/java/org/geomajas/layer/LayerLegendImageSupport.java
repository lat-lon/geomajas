/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2013 Geosparc nv, http://www.geosparc.com/, Belgium.
 *
 * The program is available in open source according to the GNU Affero
 * General Public License. All contributions in this program are covered
 * by the Geomajas Contributors License Agreement. For full licensing
 * details, see LICENSE.txt in the project root.
 */

package org.geomajas.layer;


/**
 * 
 * Interface for supporting legend images.
 * 
 * @author Dirk Stenger
 * @author Jeronimo Wanhoff
 *
 */

public interface LayerLegendImageSupport {

	/**
	 * Get the width of the legend image.
	 * 
	 * @return the image width
	 */
	int getLegendImageWidth();

	/**
	 * Get the height of the legend image.
	 * 
	 * @return the image height
	 */
	int getLegendImageHeight();

	/**
	 * Get the URL pointing to the legend image.
	 * 
	 * @return a string representing the image URL
	 */
	String getLegendImageUrl();

	/**
	 * Get the path of the default static legend image
	 * 
	 * @return the relative path to the static legend image. Never <code>null</code>
	 */
	String getStaticLegendImagePath();
}
