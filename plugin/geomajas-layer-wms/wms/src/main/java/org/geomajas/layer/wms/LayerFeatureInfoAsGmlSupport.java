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

import com.vividsolutions.jts.geom.Coordinate;
import org.geomajas.annotation.FutureApi;
import org.geomajas.annotation.UserImplemented;
import org.geomajas.layer.LayerException;
import org.geomajas.layer.feature.Feature;

import java.util.List;

/**
 * Extension for any type of layer that supports retrieving feature info as GML features.
 *
 * @author Alexander Erben
 */
@FutureApi(allMethods = true)
@UserImplemented
public interface LayerFeatureInfoAsGmlSupport {

	/**
	 * Return the list of features as gml that lay within a given coordinate and buffer for the given view scale.
	 *
	 * @param coordinate coordinate used to search for features in the layer coordinate space
	 * @param layerScale the scale of the layer
	 * @param pixelTolerance pixel tolerance for searching
	 * @return features a list of features
	 * @throws LayerException oops
	 */
	List<Feature> getFeatureInfoAsGml(Coordinate coordinate, double layerScale, int pixelTolerance)
			throws LayerException;
	
	/**
	 * Return whether the layer should support feature info support as gml.
	 *
	 * @return the enableFeatureInfoSupport true if feature info gml support is enabled
	 */
	boolean isEnableFeatureInfoAsGmlSupport();

}
