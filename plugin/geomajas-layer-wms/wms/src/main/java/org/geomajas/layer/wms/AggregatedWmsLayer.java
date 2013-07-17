package org.geomajas.layer.wms;

import java.util.Collections;
import java.util.List;

import org.geomajas.configuration.RasterLayerInfo;
import org.geomajas.global.GeomajasException;
import org.geomajas.layer.RasterLayer;
import org.geomajas.layer.tile.RasterTile;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.beans.factory.annotation.Autowired;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Encapsulates multiple {@link WmsLayer}s with a common service, enabling combined requests to the referenced WMS.
 * 
 * @author Alexander Erben
 * @author Lyn Goltz
 */
public class AggregatedWmsLayer implements RasterLayer {

	private List<WmsLayer> wmsLayers;

	private String id;

	private WmsLayerPainter painter;

	private RasterLayerInfo rasterLayerInfo;

	public AggregatedWmsLayer(List<WmsLayer> wmsLayers, WmsLayerPainter painter) {
		if (wmsLayers != null) {
			this.wmsLayers = wmsLayers;
		} else {
			this.wmsLayers = Collections.emptyList();
		}
		id = generateId();
		rasterLayerInfo = createRasterLayerInfo(wmsLayers.get(0).getLayerInfo());
		this.painter = painter;
	}

	private RasterLayerInfo createRasterLayerInfo(RasterLayerInfo layerInfo) {
		RasterLayerInfo cloned = new RasterLayerInfo();
		cloned.setDataSourceName(generateDatasourceString());
		cloned.setTileWidth(layerInfo.getTileWidth());
		cloned.setTileHeight(layerInfo.getTileHeight());
		cloned.setResolutions(layerInfo.getResolutions());
		cloned.setZoomLevels(layerInfo.getZoomLevels());
		cloned.setLayerType(layerInfo.getLayerType());
		cloned.setCrs(layerInfo.getCrs());
		cloned.setMaxExtent(layerInfo.getMaxExtent());
		cloned.setExtraInfo(layerInfo.getExtraInfo());
		return cloned;
	}

	@Override
	public RasterLayerInfo getLayerInfo() {
		if (!wmsLayers.isEmpty()) {
			RasterLayerInfo template = wmsLayers.get(0).getLayerInfo();
			template.setDataSourceName(generateDatasourceString());
			return template;
		} else {
			return null;
		}
	}

	@Override
	@Deprecated
	public CoordinateReferenceSystem getCrs() {
		if (!wmsLayers.isEmpty()) {
			return wmsLayers.get(0).getCrs();
		} else {
			return null;
		}
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public List<RasterTile> paint(CoordinateReferenceSystem boundsCrs, Envelope bounds, double scale)
			throws GeomajasException {
		if (wmsLayers.isEmpty()) {
			return Collections.emptyList();
		}
		WmsLayer firstLayer = wmsLayers.get(0);
		return painter.paint(firstLayer.createWmsParams(), firstLayer.getResolutions(), this, boundsCrs, bounds, scale);
	}

	private String generateId() {
		StringBuilder builder = new StringBuilder();
		for (WmsLayer layer : wmsLayers) {
			builder.append(layer.getId());
		}
		return builder.toString();
	}

	private String generateDatasourceString() {
		StringBuilder builder = new StringBuilder();
		boolean isFirst = true;
		for (WmsLayer layer : wmsLayers) {
			if (!isFirst) {
				builder.append(",");
			}
			isFirst = false;
			builder.append(layer.getLayerInfo().getDataSourceName());
		}
		return builder.toString();
	}

	public List<WmsLayer> getWmsLayers() {
		return wmsLayers;
	}

}
