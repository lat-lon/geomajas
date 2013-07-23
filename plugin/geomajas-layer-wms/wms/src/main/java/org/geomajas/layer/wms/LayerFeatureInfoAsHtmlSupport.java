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

package org.geomajas.layer.wms;
import org.geomajas.layer.LayerException;

import com.vividsolutions.jts.geom.Coordinate;


/**
 * Extension for any type of layer that supports retrieving feature info as HTML.
 * @author Alexander Erben
 *
 */
public interface LayerFeatureInfoAsHtmlSupport {
	
	/**
	 * Issue a GFI request against the backend WMS service and retrieve the response as HTML.
	 * 
	 * @param coordinate coordinate used to search for features in the layer coordinate space
	 * @param layerScale the scale of the layer
	 * @param pixelTolerance pixel tolerance for searching
	 * @return HTML response of the GFI Request
	 * @throws LayerException
	 */
	String getFeatureInfoAsHtml(Coordinate coordinate, double layerScale, int pixelTolerance) throws LayerException;

	/**
	 * Return whether the layer should support GetFeatureInfo support as HTML.
	 *
	 * @return the enableFeatureInfoSupport true if feature info HTML support is enabled
	 */
	boolean isEnableFeatureInfoAsHtmlSupport();
}
