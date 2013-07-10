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

	@Override
	public RasterTile getAggregatedLayerTile(List<RasterLayer> rasterLayers) throws GeomajasException {
		RasterLayer layer = getAggregatedLayer(rasterLayers);
		List<RasterTile> tiles = getTiles(layer);
		if (tiles != null && !tiles.isEmpty()) {
			return tiles.get(0);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private List<RasterTile> getTiles(RasterLayer layer) throws GeomajasException {
		PipelineContext context = pipelineService.createContext();
		String layerId = layer.getId();
		context.put(PipelineCode.LAYER_ID_KEY, layerId);
		context.put(PipelineCode.LAYER_KEY, layer);
		List<RasterTile> response = new ArrayList<RasterTile>();
		pipelineService.execute(PipelineCode.PIPELINE_GET_RASTER_TILES, layerId, context, response);
		return response;
	}

	private RasterLayer getAggregatedLayer(List<RasterLayer> rasterLayers) throws GeomajasException {
		List<WmsLayer> wmsLayers = new ArrayList<WmsLayer>();
		String baseWmsUrl = null;
		for (RasterLayer layer : rasterLayers) {
			if (layer instanceof WmsLayer) {
				WmsLayer wmsLayer = (WmsLayer) layer;
				baseWmsUrl = checkBaseWmsUrl(baseWmsUrl, wmsLayer);
				wmsLayers.add(wmsLayer);
			} else {
				throw new GeomajasException(/* TODO: code */);
			}
		}
		return new AggregatedWmsLayer(wmsLayers);
	}

	private String checkBaseWmsUrl(String baseWmsUrl, WmsLayer wmsLayer) throws GeomajasException {
		String currentWaseWmsUrl = wmsLayer.getBaseWmsUrl();
		if (baseWmsUrl != null) {
			if (baseWmsUrlsAreDifferent(baseWmsUrl, currentWaseWmsUrl)) {
				throw new GeomajasException(/* TODO: code */);
			}
		} else {
			baseWmsUrl = currentWaseWmsUrl;
		}
		return baseWmsUrl;
	}

	private boolean baseWmsUrlsAreDifferent(String baseWmsUrl, String currentWaseWmsUrl) {
		return !baseWmsUrl.equalsIgnoreCase(currentWaseWmsUrl);
	}

}