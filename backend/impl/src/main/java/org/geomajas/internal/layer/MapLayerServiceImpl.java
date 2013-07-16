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
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.geomajas.configuration.NamedStyleInfo;
import org.geomajas.geometry.Bbox;
import org.geomajas.global.ExceptionCode;
import org.geomajas.global.GeomajasException;
import org.geomajas.layer.AggregationLayerService;
import org.geomajas.layer.Layer;
import org.geomajas.layer.MapLayerService;
import org.geomajas.layer.RasterLayer;
import org.geomajas.layer.RasterLayerService;
import org.geomajas.layer.VectorLayerService;
import org.geomajas.layer.tile.RasterTile;
import org.geomajas.layer.tile.TileCode;
import org.geomajas.security.GeomajasSecurityException;
import org.geomajas.security.SecurityContext;
import org.geomajas.service.CacheService;
import org.geomajas.service.ConfigurationService;
import org.geomajas.service.pipeline.PipelineCode;
import org.geomajas.service.pipeline.PipelineContext;
import org.geomajas.service.pipeline.PipelineService;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private final Logger log = LoggerFactory.getLogger(MapLayerServiceImpl.class);

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private RasterLayerService rasterLayerService;

	@Autowired
	private VectorLayerService vectorLayerService;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private SecurityContext securityContext;

	@Autowired
	private PipelineService pipelineService;

	@Autowired(required = false)
	private AggregationLayerService aggregationService;

	@Override
	public List<RasterTile> getTiles(List<String> layerIds, Map<String, Filter> vectorLayerFilters,
			Map<String, NamedStyleInfo> vectorLayerStyleInfo, CoordinateReferenceSystem crs, Envelope bounds,
			double scale) throws GeomajasException {
		List<Layer<?>> layers = collectLayersFromIds(layerIds);
		List<RasterTile> response = new ArrayList<RasterTile>();
		if (aggregationService != null) {
			Layer layer = aggregateRasterLayers(layers);
			if (layer != null) {
				log.debug("getTiles start on layer {}", layerIds);
				long ts = System.currentTimeMillis();
				PipelineContext context = pipelineService.createContext();
				context.put(PipelineCode.LAYER_KEY, layer);
				context.put(PipelineCode.CRS_KEY, crs);
				context.put(PipelineCode.BOUNDS_KEY, bounds);
				context.put(PipelineCode.SCALE_KEY, scale);
				pipelineService.execute(PipelineCode.PIPELINE_GET_RASTER_TILES, layer.getId(), context, response);
				log.debug("getTiles done on layer {}, time {}s", layer.getId(),
						(System.currentTimeMillis() - ts) / 1000.0);
			}
		}
		return response;
	}

	private RasterLayer getRasterLayer(String layerId) throws GeomajasException {
		if (!securityContext.isLayerVisible(layerId)) {
			throw new GeomajasSecurityException(ExceptionCode.LAYER_NOT_VISIBLE, layerId, securityContext.getUserId());
		}
		RasterLayer layer = configurationService.getRasterLayer(layerId);
		if (null == layer) {
			throw new GeomajasException(ExceptionCode.RASTER_LAYER_NOT_FOUND, layerId);
		}
		return layer;
	}

	// @Override
	// public List<RasterTile> getTiles(List<String> layerIds, Map<String, Filter> vectorLayerFilters,
	// Map<String, NamedStyleInfo> vectorLayerStyleInfo, CoordinateReferenceSystem crs, Envelope bounds,
	// double scale) throws GeomajasException {
	//
	// List<Layer<?>> layers = collectLayersFromIds(layerIds);
	// if (aggregationService != null) {
	// layers = aggregateRasterLayers(layers);
	// }
	//
	// // Save the List<MapLayer> configuration in the cache for each rastertile (using cacheService)
	// // cacheService.put(MapLayerServiceImpl.class.toString(), "some unique identifier, uuid", layers /*
	// // * or a meta object
	// // * if more
	// // * information is
	// // * needed
	// // */);
	//
	// // next step is to set the url in the rastertile to a spring mvc controller where the image can be retrieved
	// // based on the given uuid.
	//
	// return generateTilesFromLayers(layers, crs, bounds, scale);
	// }

	private List<RasterTile> generateTilesFromLayers(List<Layer<?>> layers, CoordinateReferenceSystem crs,
			Envelope bounds, double scale) throws GeomajasException {
		List<BufferedImage> wholeImages = new ArrayList<BufferedImage>();
		for (Layer<?> layer : layers) {
			if (layer instanceof RasterLayer) {
				RasterLayer rasterLayer = (RasterLayer) layer;
				List<RasterTile> tiles = rasterLayerService.getTiles(rasterLayer.getId(), crs, bounds, scale);
				try {
					wholeImages.add(createWholeImageFromTiles(rasterLayer, tiles));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		BufferedImage aggregatedImage = new BufferedImage(wholeImages.get(0).getWidth(),
				wholeImages.get(0).getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics aggregatedGraphics = aggregatedImage.getGraphics();
		for (BufferedImage image : wholeImages) {
			aggregatedGraphics.drawImage(image, 0, 0, null);
		}
		File imgFile = new File("/tmp/geomajas/tile" + layers.get(0).getId());
		try {
			ImageIO.write(aggregatedImage, "png", imgFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	private BufferedImage createWholeImageFromTiles(RasterLayer rasterLayer, List<RasterTile> tiles)
			throws MalformedURLException, IOException {
		Collections.sort(tiles, new Comparator<RasterTile>() {

			@Override
			public int compare(RasterTile first, RasterTile second) {
				TileCode firstCode = first.getCode();
				TileCode secondCode = second.getCode();
				if ((firstCode.getY() == secondCode.getY())) {
					if ((firstCode.getX() < secondCode.getX()))
						return -1;
					if ((firstCode.getX() > secondCode.getX()))
						return 1;
					return 0;
				} else {
					if ((firstCode.getY() < secondCode.getY()))
						return -1;
					if ((firstCode.getY() > secondCode.getY()))
						return 1;
					return 0;
				}
			}
		});

		int rasterAxisSize = calculateRowSize(tiles);
		int tileWidth = rasterLayer.getLayerInfo().getTileWidth();
		int tileHeight = rasterLayer.getLayerInfo().getTileHeight();
		System.out.println(rasterAxisSize);
		BufferedImage wholeImage = new BufferedImage(tileWidth * rasterAxisSize, tileHeight * rasterAxisSize,
				BufferedImage.TYPE_INT_ARGB);
		Graphics wholeImageGraphics = wholeImage.getGraphics();
		for (RasterTile tile : tiles) {
			BufferedImage tileImage = ImageIO.read(new URL(tile.getUrl()));
			wholeImageGraphics.drawImage(tileImage, (int) tile.getCode().getX() * tileWidth, (int) tile.getCode()
					.getY() * tileHeight, null);
		}
		return wholeImage;
	}

	private int calculateRowSize(List<RasterTile> tiles) {
		int tileLevel = tiles.get(0).getCode().getTileLevel();
		return new Double(Math.pow(2, tileLevel)).intValue();
	}

	private List<Layer<?>> collectLayersFromIds(List<String> layerIds) {
		List<Layer<?>> layers = new ArrayList<Layer<?>>();
		for (String layerId : layerIds) {
			layers.add(configurationService.getLayer(layerId));
		}
		return layers;
	}

	private Layer<?> aggregateRasterLayers(List<Layer<?>> layers) throws GeomajasException {
		return aggregationService.aggregate(layers);
	}

}
