package org.geomajas.layer.wms;

import java.util.ArrayList;
import java.util.List;

import org.geomajas.global.GeomajasException;
import org.geomajas.layer.AggregationLayerService;
import org.geomajas.layer.Layer;
import org.geomajas.layer.RasterLayer;
import org.geomajas.service.pipeline.PipelineService;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("rawtypes")
public class WmsAggregationLayerServiceImpl implements AggregationLayerService {

	@Autowired
	private PipelineService pipelineService;

	@Autowired
	private WmsLayerPainter painter;
	
	@Override
	public List<Layer<?>> aggregate(List<Layer<?>> layers) throws GeomajasException {
		if (layers.size() > 1) {
			String currentBaseUrl = "";
			List<Layer<?>> resultingLayers = new ArrayList<Layer<?>>();
			List<WmsLayer> currentWmsLayerStreak = new ArrayList<WmsLayer>();

			for (Layer<?> rasterLayer : layers) {
				if (rasterLayer instanceof WmsLayer) {
					WmsLayer wmsLayer = (WmsLayer) rasterLayer;
					if (baseWmsUrlsAreDifferent(currentBaseUrl, wmsLayer.getBaseWmsUrl())) {
						endStreak(resultingLayers, currentWmsLayerStreak);
					}
					currentWmsLayerStreak.add(wmsLayer);
					currentBaseUrl = wmsLayer.getBaseWmsUrl();
				} else {
					endStreak(resultingLayers, currentWmsLayerStreak);
					resultingLayers.add(rasterLayer);
					currentBaseUrl = "";
				}
			}
			endStreak(resultingLayers, currentWmsLayerStreak);
			return resultingLayers;
		} else {
			return layers;
		}
	}

	private void endStreak(List<Layer<?>> wmsLayerList, List<WmsLayer> currentWmsLayerStreak) {
		if (currentWmsLayerStreak.size() == 1) {
			wmsLayerList.addAll(currentWmsLayerStreak);
			currentWmsLayerStreak.clear();
		} else if (currentWmsLayerStreak.size() > 1) {
			wmsLayerList.add(new AggregatedWmsLayer(currentWmsLayerStreak, painter));
			currentWmsLayerStreak.clear();
		}
	}

	protected RasterLayer createAggregatedLayer(List<RasterLayer> rasterLayers) throws GeomajasException {
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
		return new AggregatedWmsLayer(wmsLayers, painter);
	}

	private String checkBaseWmsUrl(String baseWmsUrl, WmsLayer wmsLayer) throws GeomajasException {
		String currentBaseWmsUrl = wmsLayer.getBaseWmsUrl();
		if (baseWmsUrl != null) {
			if (baseWmsUrlsAreDifferent(baseWmsUrl, currentBaseWmsUrl)) {
				throw new GeomajasException(/* TODO: code */);
			}
		} else {
			baseWmsUrl = currentBaseWmsUrl;
		}
		return baseWmsUrl;
	}

	private boolean baseWmsUrlsAreDifferent(String baseWmsUrl, String currentWaseWmsUrl) {
		return !baseWmsUrl.equalsIgnoreCase(currentWaseWmsUrl);
	}

	public PipelineService getPipelineService() {
		return pipelineService;
	}

	public void setPipelineService(PipelineService pipelineService) {
		this.pipelineService = pipelineService;
	}

}