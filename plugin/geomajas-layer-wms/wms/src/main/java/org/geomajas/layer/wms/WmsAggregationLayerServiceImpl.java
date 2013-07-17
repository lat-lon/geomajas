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
		if (layers == null) {
			throw new GeomajasException();
		}
		if (layers.size() > 1) {
			List<WmsLayer> currentWmsLayerStreak = new ArrayList<WmsLayer>();
			String currentServiceUrl = null;
			for (Layer<?> rasterLayer : layers) {
				if (rasterLayer instanceof WmsLayer) {
					WmsLayer wmsLayer = (WmsLayer) rasterLayer;
					String baseWmsUrl = wmsLayer.getBaseWmsUrl();
					if (currentServiceUrl != null && baseWmsUrlsAreDifferent(currentServiceUrl,baseWmsUrl)) {
						throw new GeomajasException(); // TODO exc code
					}
					currentServiceUrl = baseWmsUrl;
					currentWmsLayerStreak.add(wmsLayer);
				} else {
					throw new GeomajasException(); // TODO exc code
				}
			}
			return new AggregatedWmsLayer(currentWmsLayerStreak, painter);
		} else if (layers.size() == 1) {
			return layers.get(0);
		}
		throw new GeomajasException(); // TODO exc code
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