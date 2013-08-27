package org.geomajas.widget.layer.client.widget.node;

import org.geomajas.gwt.client.Geomajas;
import org.geomajas.gwt.client.map.layer.RasterLayer;
import org.geomajas.gwt.client.map.layer.VectorLayer;
import org.geomajas.gwt.client.util.UrlBuilder;

/**
 * Node which displays a legend icon + description.
 */
public class LayerTreeLegendItemNode extends LayerTreeTreeNode {

	private static final String LEGEND_ICONS_PATH = "legendgraphic";

	private static final String LEGEND_ICONS_TYPE = ".png";

	private LayerTreeLegendNode parent;

	private int ruleIndex;

	// rasterlayer
	public LayerTreeLegendItemNode(LayerTreeLegendNode parent, RasterLayer layer, String rasterIconUrl) {
		super(parent.getTree(), parent.getLayer());
		this.parent = parent;
		setTitle(layer.getLabel());
		setName(parent.getAttribute("id") + "_legend");
		if (rasterIconUrl != null) {
			setIcon(rasterIconUrl);
		} else {
			UrlBuilder url = new UrlBuilder(Geomajas.getDispatcherUrl());
			url.addPath(LEGEND_ICONS_PATH);
			url.addPath(layer.getServerLayerId() + LEGEND_ICONS_TYPE);
			setIcon(url.toString());
		}
	}

	// vectorlayer
	public LayerTreeLegendItemNode(LayerTreeLegendNode parent, VectorLayer layer, int ruleIndex, String title) {
		super(parent.getTree(), parent.getLayer());
		this.parent = parent;
		setTitle(title);
		updateStyle(layer);
	}

	public void updateStyle(VectorLayer layer) {
		String name = layer.getLayerInfo().getNamedStyleInfo().getName();
		setName(name + "_" + ruleIndex);
		UrlBuilder url = new UrlBuilder(Geomajas.getDispatcherUrl());
		url.addPath(LEGEND_ICONS_PATH);
		url.addPath(layer.getServerLayerId());
		url.addPath(name);
		url.addPath(ruleIndex + ".png");
		setIcon(url.toString());
	}

	@Override
	public void updateIcon() {
		// leave my icons alone!
	}

	public LayerTreeLegendNode getParent() {
		return parent;
	}

	public void setParent(LayerTreeLegendNode parent) {
		this.parent = parent;
	}
}