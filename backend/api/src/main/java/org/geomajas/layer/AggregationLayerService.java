package org.geomajas.layer;

import java.util.List;

import org.geomajas.layer.tile.RasterTile;

public interface AggregationLayerService {
	
	RasterTile getAggregatedLayerTile(List<RasterLayer> rasterLayers);
	
}
