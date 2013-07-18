package org.geomajas.layer.wms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geomajas.configuration.RasterLayerInfo;
import org.geomajas.global.GeomajasException;
import org.geomajas.layer.RasterLayer;
import org.geomajas.layer.tile.RasterTile;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

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

	public AggregatedWmsLayer(List<WmsLayer> wmsLayers, WmsLayerPainter painter) throws GeomajasException {
		if (wmsLayers == null || wmsLayers.isEmpty()) {
			throw new GeomajasException(); // TODO code
		}
		this.wmsLayers = wmsLayers;
		this.painter = painter;
		this.id = generateId();
		this.rasterLayerInfo = createRasterLayerInfo(wmsLayers);
	}

	private RasterLayerInfo createRasterLayerInfo(List<WmsLayer> layers) {
		RasterLayerInfo layerInfo = layers.get(0).getLayerInfo();
		RasterLayerInfo cloned = new RasterLayerInfo();
		cloned.setDataSourceName(generateDatasourceString(layers));
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
		return rasterLayerInfo;
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

	private String generateDatasourceString(List<WmsLayer> wmsLayers) {
		StringBuilder builder = new StringBuilder();
		boolean isFirst = true;
		List<String> alreadyUsedDataSources = new ArrayList<String>();
		for (WmsLayer layer : wmsLayers) {
			String dataSourceName = layer.getLayerInfo().getDataSourceName();
			if (!alreadyUsedDataSources.contains(dataSourceName)) {
				if (!isFirst) {
					builder.append(",");
				}
				isFirst = false;
				builder.append(dataSourceName);
				alreadyUsedDataSources.add(dataSourceName);
			}
		}
		return builder.toString();
	}

	public List<WmsLayer> getWmsLayers() {
		return wmsLayers;
	}

}
