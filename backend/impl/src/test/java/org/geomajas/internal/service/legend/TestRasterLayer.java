package org.geomajas.internal.service.legend;

import java.util.ArrayList;
import java.util.List;

import org.geomajas.configuration.RasterLayerInfo;
import org.geomajas.global.GeomajasException;
import org.geomajas.layer.LayerLegendImageSupport;
import org.geomajas.layer.RasterLayer;
import org.geomajas.layer.tile.RasterTile;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

public class TestRasterLayer implements RasterLayer, LayerLegendImageSupport {

	private String id;

	private RasterLayerInfo layerInfo;

	private CoordinateReferenceSystem crs;

	public RasterLayerInfo getLayerInfo() {
		return layerInfo;
	}

	public void setLayerInfo(RasterLayerInfo layerInfo) {
		this.layerInfo = layerInfo;
	}

	public CoordinateReferenceSystem getCrs() {
		return crs;
	}

	public List<RasterTile> paint(CoordinateReferenceSystem boundsCrs, Envelope bounds, double scale)
			throws GeomajasException {
		return new ArrayList<RasterTile>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLegendImageUrl() {
		return null;
	}

	public int getLegendImageWidth() {
		return -1;
	}

	public int getLegendImageHeight() {
		return -1;
	}

	@Override
	public String getStaticLegendImagePath() {
		// TODO Auto-generated method stub
		return null;
	}

}
