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

import java.util.List;
import java.util.Map;

import org.geomajas.configuration.NamedStyleInfo;
import org.geomajas.global.GeomajasException;
import org.geomajas.layer.tile.RasterTile;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Service which allows accessing data from a map layer. </p> A map layer is a representation of the whole map in a
 * single layer.
 * 
 * @author Oliver May
 */
public interface MapLayerService extends LayerService {

	/**
	 * Get the map tiles for the given bounds.
	 * 
	 * @param layerIds
	 * @param vectorLayerFilters
	 * @param vectorLayerStyleInfo
	 * @param crs
	 * @param bounds
	 * @param scale
	 * @return
	 * @throws GeomajasException
	 */
	List<RasterTile> getTiles(List<String> layerIds, Map<String, Filter> vectorLayerFilters,
			Map<String, NamedStyleInfo> vectorLayerStyleInfo, CoordinateReferenceSystem crs, Envelope bounds,
			double scale) throws GeomajasException;

}
