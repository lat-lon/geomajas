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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.geomajas.gwt.client.map.MapModel;
import org.geomajas.gwt.client.map.layer.Layer;
import org.geomajas.gwt.client.widget.MapWidget;
import org.geomajas.widget.featureinfo.client.FeatureInfoMessages;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.types.AutoFitWidthApproach;
import com.smartgwt.client.types.Autofit;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;

/**
 * <p>
 * The <code>MultiLayerHtmlList</code> is a class providing a floating window
 * that shows a list of all the layers provided to it. Clicking on a layer in
 * the list, results in a call of the provided
 * {@link org.geomajas.widget.featureinfo.client.widget.LayerClickHandler}.
 * </p>
 * 
 * @author Alexander Erben
 */
public class MultiLayerHtmlList extends ListGrid {

	private static final int MAX_ROWS = 25;

	private static final FeatureInfoMessages MESSAGES = GWT
			.create(FeatureInfoMessages.class);

	/**
	 * external handler, called when clicking on a feature in the list
	 */
	private final LayerClickHandler layerClickHandler;

	private final MapWidget mapWidget;

	private final Map<String, Layer<?>> layerMap;
	
	private final Map<String,String> htmlMap;

	private static final String LABEL = "LABEL";

	private static final String LAYER_ID = "LAYER_ID";

	// -------------------------------------------------------------------------
	// Constructor:
	// -------------------------------------------------------------------------

	/**
	 * Create an instance.
	 * 
	 * @param mapWidget
	 *            map widget
	 * @param featureClickHandler
	 *            handler
	 */
	public MultiLayerHtmlList(final MapWidget mapWidget,
			LayerClickHandler layerClickHandler) {
		super();
		this.mapWidget = mapWidget;
		this.htmlMap = new HashMap<String, String>();
		this.layerMap = new HashMap<String, Layer<?>>();
		this.layerClickHandler = layerClickHandler;
		buildWidget();
	}

	/**
	 * Feed a map of features to the widget, so it can be built.
	 * 
	 * @param featureMap
	 *            feature map
	 */
	public void setHtmlMap(Map<String, String> htmlMap) {
		MapModel mapModel = mapWidget.getMapModel();

		for (Entry<String, String> layerEntry : htmlMap.entrySet()) {
			Layer<?> layer = mapModel.getLayer(layerEntry.getKey());
			if (null != layer) {
				String html = layerEntry.getValue();
				if (html != null && !"".equals(html)) {
					layerMap.put(layer.getId(), layer);
					addLayer(layer, html);
				}
			}
		}
	}

	@Override
	/*
	 * Override getCellCSSText to implement padding-left of ordinary feature
	 * rows
	 */
	protected String getCellCSSText(ListGridRecord record, int rowNum,
			int colNum) {
		// Note: using listGrid.setCellPadding() would also padd group rows
		String newStyle;
		String style = record.getCustomStyle(); /*
												 * returns groupNode if group
												 * row, else e.g. null
												 */

		if (LABEL.equals(getFieldName(colNum))
				&& (null == style || !"groupNode".equalsIgnoreCase(style))) {
			newStyle = "padding-left: 40px;";
		} else { /* groupCell */
			newStyle = "padding-left: 5px;";
		}
		if (null != super.getCellCSSText(record, rowNum, colNum)) {
			newStyle = super.getCellCSSText(record, rowNum, colNum) + newStyle;
			/* add padding after original, the latter specified wins. */
		}
		return newStyle;
	}

	private void addLayer(Layer<?> layer, String html) {
		if (layer == null || html == null || "".equals(html))
			return;
		ListGridRecord record = new ListGridRecord();
		record.setAttribute(LABEL, layer.getLabel());
		record.setAttribute(LAYER_ID, layer.getId());
		addData(record);
	}

	/**
	 * Build the entire widget.
	 */
	private void buildWidget() {
		// setTitle(MESSAGES.nearbyFeaturesListTooltip());
		setShowEmptyMessage(true);
		setWidth100();
		setHeight100();
		setShowHeader(false);
		setShowAllRecords(true);

		// List size calculation
		setDefaultWidth(400);
		setDefaultHeight(300);
		setAutoFitData(Autofit.BOTH);
		setAutoFitMaxRecords(MAX_ROWS);
		setAutoFitFieldWidths(true);
		setAutoFitWidthApproach(AutoFitWidthApproach.BOTH);

		ListGridField labelField = new ListGridField(LABEL);
		ListGridField layerField = new ListGridField(LAYER_ID);

		setFields(labelField, layerField);
		hideFields(layerField);
		addRecordClickHandler(new RecordClickHandler() {

			public void onRecordClick(RecordClickEvent event) {
				String layerId = event.getRecord().getAttribute(LAYER_ID);
				Layer<?> layer = layerMap.get(layerId);
				layerClickHandler.onClick(layer, htmlMap.get(layerId));
			}

		});

		// setShowHover(true);
		// setCanHover(true);
		// setHoverWidth(200);
		// setHoverCustomizer(new HoverCustomizer() {
		//
		// public String hoverHTML(Object value, ListGridRecord record, int
		// rowNum, int colNum) {
		// String fId = record.getAttribute(FEATURE_ID);
		// Feature feat = vectorFeatures.get(fId);
		//
		// StringBuilder tooltip = new StringBuilder();
		//
		// if (feat != null) {
		// for (AbstractAttributeInfo a :
		// feat.getLayer().getLayerInfo().getFeatureInfo().getAttributes()) {
		// if (a instanceof AbstractReadOnlyAttributeInfo &&
		// ((AbstractReadOnlyAttributeInfo) a).isIdentifying()) {
		// tooltip.append("<b>");
		// tooltip.append(((AbstractReadOnlyAttributeInfo) a).getLabel());
		// tooltip.append("</b>: ");
		// tooltip.append(feat.getAttributeValue(a.getName()));
		// tooltip.append("<br/>");
		// }
		// }
		// tooltip.append(MESSAGES.nearbyFeaturesListTooltip());
		// }
		// return tooltip.toString();
		// }
		// });
	}

}
