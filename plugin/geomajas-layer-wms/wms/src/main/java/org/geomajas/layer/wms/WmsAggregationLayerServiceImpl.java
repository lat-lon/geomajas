package org.geomajas.layer.wms;

import java.util.ArrayList;
import java.util.List;

import org.geomajas.global.GeomajasException;
import org.geomajas.layer.AggregationLayerService;
import org.geomajas.layer.RasterLayer;
import org.geomajas.layer.tile.RasterTile;
import org.geomajas.service.pipeline.PipelineCode;
import org.geomajas.service.pipeline.PipelineContext;
import org.geomajas.service.pipeline.PipelineService;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("rawtypes")
public class WmsAggregationLayerServiceImpl implements AggregationLayerService {

	@Autowired
	private PipelineService pipelineService;

	private RasterLayer getAggregatedLayer(List<RasterLayer> rasterLayers) {
		List<WmsLayer> wmsLayers = new ArrayList<WmsLayer>();
		for (RasterLayer layer : rasterLayers) {
			if (layer instanceof WmsLayer) {
				wmsLayers.add((WmsLayer) layer);
			}
		}
		return new AggregatedWmsLayer(wmsLayers);
	}

	@Override
	public RasterTile getAggregatedLayerTile(List<RasterLayer> rasterLayers) {
		RasterLayer layer = getAggregatedLayer(rasterLayers);
		try {
			List<RasterTile> tiles = getTiles(layer);
			if (tiles != null && !tiles.isEmpty()) {
				return tiles.get(0);
			} 
		} catch (GeomajasException e) {
			// TODO logging
		}
		return null;
	}

	public List<RasterTile> getTiles(RasterLayer layer) throws GeomajasException {
		PipelineContext context = pipelineService.createContext();
		String layerId = layer.getId();
		context.put(PipelineCode.LAYER_ID_KEY, layerId);
		context.put(PipelineCode.LAYER_KEY, layer);
		List<RasterTile> response = new ArrayList<RasterTile>();
		pipelineService.execute(PipelineCode.PIPELINE_GET_RASTER_TILES, layerId, context, response);
		return response;
	}
}
