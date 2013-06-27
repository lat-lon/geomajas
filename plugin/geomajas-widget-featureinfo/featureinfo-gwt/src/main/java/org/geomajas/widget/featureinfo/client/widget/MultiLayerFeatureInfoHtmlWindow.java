/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2013 Geosparc nv, http://www.geosparc.com/, Belgium.
 *
 * The program is available in open source according to the GNU Affero
 * General Public License. All contributions in this program are covered
 * by the Geomajas Contributors License Agreement. For full licensing
 * details, see LICENSE.txt in the project root.
 */

package org.geomajas.widget.featureinfo.client.widget;

import java.util.Map;

import org.geomajas.gwt.client.map.layer.Layer;
import org.geomajas.gwt.client.widget.MapWidget;
import org.geomajas.widget.featureinfo.client.FeatureInfoMessages;
import org.geomajas.widget.featureinfo.client.widget.factory.FeatureDetailWidgetFactory;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.widgets.Window;

/**
 * <p>
 * The <code>MultilayerFeatureInfoHtmlWindow</code> is a floating window that shows a list of layers. Selecting one will
 * show a window with the rendered html GFI response.
 * </p>
 * 
 * @author Alexander Erben
 */
public class MultiLayerFeatureInfoHtmlWindow extends DockableWindow {

	private static final FeatureInfoMessages MESSAGES = GWT.create(FeatureInfoMessages.class);

	private final MapWidget mapWidget;
	
	private Map<String,String> htmlMap;

	/**
	 * Construct a MultiLayerFeatureInfoWindow, allowing feature info of multiple features on one location.
	 * 
	 * @param mapWidget the map widget
	 * @param featureMap a Map (Layer, List(Feature)) that contains all the features on this position
	 */
	public MultiLayerFeatureInfoHtmlWindow(MapWidget mapWidget, Map<String,String> htmlMap) {
		super();
		this.mapWidget = mapWidget;
		setHtmlMap(htmlMap);
		buildWidget();

	}
	
	/**
	 * Construct a MultiLayerFeatureInfoWindow with specified featuresListLabels.
	 * Useful when using labels composed of multiple attributes
	 * 
	 * @param mapWidget the map widget
	 * @param featureMap a Map (Layer, List(Feature)) that contains all the features on this position
	 * @param featuresListLabels contains for each layer specified in SLD attributeName
	 * to be used as shown list entry value
	 */
	public MultiLayerFeatureInfoHtmlWindow(MapWidget mapWidget,	Map<String,String> htmlMap, Map<String, String> featuresListLabels) {
		super();
		this.mapWidget = mapWidget;
		//setFeaturesListLabels(featuresListLabels);
		setHtmlMap(htmlMap);
		buildWidget();
	}

	private void buildWidget() {
		setAutoSize(true);
		setTitle(MESSAGES.nearbyFeaturesWindowTitle());
		setCanDragReposition(true);
		setCanDragResize(true);
		setWidth("250px");
		setMinWidth(250);
		setKeepInParentRect(true);

		MultiLayerHtmlList layerList = new MultiLayerHtmlList(mapWidget, new LayerClickHandler() {
			
			@Override
			public void onClick(Layer<?> layer, String html) {
				Window window = FeatureDetailWidgetFactory.createLayerDetailWindow(html, false);
				window.setPageTop(mapWidget.getAbsoluteTop() + 25);
				window.setPageLeft(mapWidget.getAbsoluteLeft() + 25);
				window.setKeepInParentRect(true);
				window.draw();
			}

		});
		layerList.setHtmlMap(htmlMap);
		addItem(layerList);
	}

	public Map<String,String> getHtmlMap() {
		return htmlMap;
	}
	
	public void setHtmlMap(Map<String,String> htmlMap) {
		this.htmlMap = htmlMap;
	}
}
