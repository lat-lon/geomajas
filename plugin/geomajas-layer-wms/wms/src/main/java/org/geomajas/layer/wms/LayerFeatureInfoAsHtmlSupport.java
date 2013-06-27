package org.geomajas.layer.wms;
import org.geomajas.layer.LayerException;

import com.vividsolutions.jts.geom.Coordinate;


/**
 * Extension for any type of layer that supports retrieving feature info as HTML.
 * @author Alexander Erben
 *
 */
public interface LayerFeatureInfoAsHtmlSupport {
	
	/**
	 * Issue a GFI request against the backend WMS service and retrieve the response as HTML
	 * 
	 * @param coordinate coordinate used to search for features in the layer coordinate space
	 * @param layerScale the scale of the layer
	 * @param pixelTolerance pixel tolerance for searching
	 * @return HTML response of the GFI Request
	 * @throws LayerException
	 */
	String getFeatureInfoAsHtml(Coordinate coordinate, double layerScale, int pixelTolerance) throws LayerException;

	/**
	 * Return whether the layer should support GetFeatureInfo support as HTML.
	 *
	 * @return the enableFeatureInfoSupport true if feature info HTML support is enabled
	 */
	boolean isEnableFeatureInfoAsHtmlSupport();
}
