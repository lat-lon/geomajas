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
package org.geomajas.internal.layer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.geomajas.configuration.NamedStyleInfo;
import org.geomajas.global.GeomajasException;
import org.geomajas.layer.Layer;
import org.geomajas.layer.MapLayerService;
import org.geomajas.layer.RasterLayer;
import org.geomajas.layer.RasterLayerService;
import org.geomajas.layer.VectorLayer;
import org.geomajas.layer.VectorLayerService;
import org.geomajas.layer.tile.RasterTile;
import org.geomajas.service.CacheService;
import org.geomajas.service.ConfigurationService;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Implementation of the map layer service.
 * 
 * @author Oliver May
 *
 */
@Component
public class MapLayerServiceImpl extends LayerServiceImpl implements MapLayerService {

	@Autowired
	private ConfigurationService configurationService;
	
	@Autowired
	private RasterLayerService rasterLayerService;
	
	@Autowired
	private VectorLayerService vectorLayerService;
	
	@Autowired
	private CacheService cacheService;
	
	@Override
	public List<RasterTile> getTiles(List<String> layerIds, Map<String, Filter> vectorLayerFilters,
			Map<String, NamedStyleInfo> vectorLayerStyleInfo, CoordinateReferenceSystem crs, Envelope bounds,
			double scale) throws GeomajasException {
		
		
		List<MapLayer> layers = new ArrayList<MapLayer>();
		
		for (String layerId : layerIds) {
			Layer<?> layer = configurationService.getLayer(layerId);
			if (layer instanceof RasterLayer) {

				MapLayer<RasterTile> rasterLayer = new MapLayer<RasterTile>();
				rasterLayer.getTiles().addAll(rasterLayerService.getTiles(layerId, crs, bounds, scale));
				layers.add(rasterLayer);
			} else if (layer instanceof VectorLayer) {

				//OM: there is no getTiles method in vectorlayer. Two options: calculate tilecode here, or add getvectorlayertiles to the the vectorlayer service.
				// I would suggest adding getVectorTiles to the vectorlayerservice, because it is also required by issue http://jira.geomajas.org/browse/GBE-337.
//				MapLayer<RasterTile> vectorLayer = new MapLayer<RasterTile>();
//				vectorLayer.getTiles().addAll(vectorLayerService.getTiles());
			
			}
		}

		//TODO: We need to create some rastertiles here.
		
		// Save the List<MapLayer> configuration in the cache for each rastertile (using cacheService)
		cacheService.put(MapLayerServiceImpl.class.toString(), "some unique identifier, uuid", layers /* or a meta object if more information is needed */);

		// next step is to set the url in the rastertile to a spring mvc controller where the image can be retrieved based on the given uuid.
		
		return new ArrayList<RasterTile>();
		
		
	}
	
}
