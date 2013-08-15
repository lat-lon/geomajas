package org.geomajas.gwt.client.widget;

import org.geomajas.gwt.client.gfx.painter.RasterLayerPainter;
import org.geomajas.gwt.client.map.layer.ComboRasterLayer;

/**
 * Paints a {@link ComboRasterLayer}
 * 
 * @author Alexander Erben
 *
 */
public class ComboRasterLayerPainter extends RasterLayerPainter {

	public ComboRasterLayerPainter(MapWidget mapWidget) {
		super(mapWidget);
	}
	
	@Override
	public String getPaintableClassName() {
		return ComboRasterLayer.class.getName();
	}

}
