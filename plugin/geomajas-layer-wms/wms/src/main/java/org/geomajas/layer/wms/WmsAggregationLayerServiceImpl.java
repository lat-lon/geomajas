package org.geomajas.layer.wms;

import java.util.ArrayList;
import java.util.List;

import org.geomajas.global.GeomajasException;
import org.geomajas.layer.AggregationLayerService;
import org.geomajas.layer.Layer;
import org.geomajas.service.pipeline.PipelineService;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("rawtypes")
public class WmsAggregationLayerServiceImpl implements AggregationLayerService {

	@Autowired
	private PipelineService pipelineService;

	@Autowired
	private WmsLayerPainter painter;

	@Override
	public Layer<?> aggregate(List<Layer<?>> layers) throws GeomajasException {
		if (layers.size() > 1) {
			String currentBaseUrl = "";
			boolean isFirst = true;
			List<WmsLayer> currentWmsLayerStreak = new ArrayList<WmsLayer>();

			for (Layer<?> rasterLayer : layers) {
				if (rasterLayer instanceof WmsLayer) {
					WmsLayer wmsLayer = (WmsLayer) rasterLayer;
					if (!isFirst && baseWmsUrlsAreDifferent(currentBaseUrl, wmsLayer.getBaseWmsUrl())) {
						return endStreak(currentWmsLayerStreak);
					}
					isFirst = false;
					currentWmsLayerStreak.add(wmsLayer);
					currentBaseUrl = wmsLayer.getBaseWmsUrl();
				} else {
					return endStreak(currentWmsLayerStreak);
				}
			}
		} else if (layers.size() == 1)
			return layers.get(0);
		return null;
	}

	private Layer<?> endStreak(List<WmsLayer> currentWmsLayerStreak) {
		if (currentWmsLayerStreak.size() == 1) {
			return currentWmsLayerStreak.get(0);
		} else {
			return aggregateWmsLayers(currentWmsLayerStreak);
		}
	}

	private Layer<?> aggregateWmsLayers(List<WmsLayer> currentWmsLayerStreak) {
		StringBuilder dataSources = new StringBuilder();
		boolean isFirst = true;
		for (WmsLayer layer : currentWmsLayerStreak) {
			if (!isFirst) {
				dataSources.append(",");
				isFirst = false;
			}
			dataSources.append(layer.getLayerInfo().getDataSourceName());
		}
		WmsLayer result = currentWmsLayerStreak.get(0);
		result.getLayerInfo().setDataSourceName(dataSources.toString());
		return result;
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