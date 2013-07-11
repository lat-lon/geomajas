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

import static java.util.Collections.singletonList;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.geomajas.configuration.NamedStyleInfo;
import org.geomajas.geometry.Bbox;
import org.geomajas.global.GeomajasException;
import org.geomajas.layer.AggregationLayerService;
import org.geomajas.layer.Layer;
import org.geomajas.layer.MapLayerService;
import org.geomajas.layer.RasterLayer;
import org.geomajas.layer.RasterLayerService;
import org.geomajas.layer.VectorLayerService;
import org.geomajas.layer.tile.RasterTile;
import org.geomajas.layer.tile.TileCode;
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

	@Autowired(required = false)
	private AggregationLayerService aggregationService;

	@Override
	public List<RasterTile> getTiles(List<String> layerIds, Map<String, Filter> vectorLayerFilters,
			Map<String, NamedStyleInfo> vectorLayerStyleInfo, CoordinateReferenceSystem crs, Envelope bounds,
			double scale) throws GeomajasException {

		List<Layer<?>> layers = collectLayersFromIds(layerIds);
		if (aggregationService != null) {
			layers = aggregateRasterLayers(layers);
		}

		// Save the List<MapLayer> configuration in the cache for each rastertile (using cacheService)
		// cacheService.put(MapLayerServiceImpl.class.toString(), "some unique identifier, uuid", layers /*
		// * or a meta object
		// * if more
		// * information is
		// * needed
		// */);

		// next step is to set the url in the rastertile to a spring mvc controller where the image can be retrieved
		// based on the given uuid.

		return generateTilesFromLayers(layers, crs, bounds, scale);
	}

	private List<RasterTile> generateTilesFromLayers(List<Layer<?>> layers, CoordinateReferenceSystem crs,
			Envelope bounds, double scale) throws GeomajasException {
		String id;
		int wholeImageWidth = (int) bounds.getWidth();
		int wholeImageHeight = (int) bounds.getHeight();
		BufferedImage aggregatedImage = new BufferedImage(wholeImageWidth, wholeImageHeight,
				BufferedImage.TYPE_INT_ARGB);
		Graphics aggregatedGraphics = aggregatedImage.getGraphics();
		for (Layer<?> layer : layers) {
			if (layer instanceof RasterLayer) {
				List<RasterTile> tiles = rasterLayerService.getTiles(layer.getId(), crs, bounds, scale);

				try {
					BufferedImage wholeImage = createWholeImageFromTiles(wholeImageWidth, wholeImageHeight, tiles);
					aggregatedGraphics.drawImage(wholeImage, 0, 0, null);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		// TODO generate and return raster tiles
		return null;
		
	}

	private BufferedImage createWholeImageFromTiles(int wholeImageWidth, int wholeImageHeight, List<RasterTile> tiles)
			throws IOException, MalformedURLException {
		BufferedImage wholeImage = new BufferedImage(wholeImageWidth, wholeImageHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics wholeImageGraphics = wholeImage.getGraphics();
		for (RasterTile tile : tiles) {
			BufferedImage tileImage = ImageIO.read(new URL(tile.getUrl()));
			wholeImageGraphics.drawImage(tileImage, (int) tile.getBounds().getX(), (int) tile.getBounds().getY(), null);
		}
		return wholeImage;
	}

	private List<Layer<?>> collectLayersFromIds(List<String> layerIds) {
		List<Layer<?>> layers = new ArrayList<Layer<?>>();
		for (String layerId : layerIds) {
			layers.add(configurationService.getLayer(layerId));
		}
		return layers;
	}

	private List<Layer<?>> aggregateRasterLayers(List<Layer<?>> layers) throws GeomajasException {
		return aggregationService.aggregate(layers);
	}

}
