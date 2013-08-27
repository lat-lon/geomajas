package org.geomajas.widget.layer.client.widget.node;

import java.util.ArrayList;

import org.geomajas.configuration.client.ClientVectorLayerInfo;
import org.geomajas.gwt.client.map.layer.Layer;
import org.geomajas.gwt.client.map.layer.RasterLayer;
import org.geomajas.gwt.client.map.layer.VectorLayer;
import org.geomajas.sld.FeatureTypeStyleInfo;
import org.geomajas.sld.RuleInfo;
import org.geomajas.sld.UserStyleInfo;
import org.geomajas.widget.layer.client.util.LayerIconUtil;
import org.geomajas.widget.layer.client.widget.CombinedLayertree;
import org.geomajas.widget.layer.client.widget.RefreshableTree;

/**
 * Node with legend for LayerNode.
 */
public class LayerTreeLegendNode extends LayerTreeTreeNode {

	private CombinedLayertree layerTree;

	public LayerTreeLegendNode(CombinedLayertree layerTree, RefreshableTree tree, Layer<?> layer) {
		super(tree, layer);
		this.layerTree = layerTree;
	}

	public void init() {
		if (getLayer() instanceof VectorLayer) {
			VectorLayer vl = (VectorLayer) getLayer();
			ArrayList<LayerTreeLegendItemNode> nodeList = new ArrayList<LayerTreeLegendItemNode>();
			layerTree.addLegendIcon(vl, nodeList);
			ClientVectorLayerInfo layerInfo = vl.getLayerInfo();

			// For vector layer; loop over the style definitions:
			UserStyleInfo userStyle = layerInfo.getNamedStyleInfo().getUserStyle();
			FeatureTypeStyleInfo info = userStyle.getFeatureTypeStyleList().get(0);
			for (int i = 0; i < info.getRuleList().size(); i++) {
				RuleInfo rule = info.getRuleList().get(i);
				// use title if present, name if not
				String title = (rule.getTitle() != null ? rule.getTitle() : rule.getName());
				// fall back to style name
				if (title == null) {
					title = layerInfo.getNamedStyleInfo().getName();
				}

				LayerTreeLegendItemNode tn = new LayerTreeLegendItemNode(this, vl, i, title);
				nodeList.add(tn);
				tree.add(tn, this);
			}

		} else if (getLayer() instanceof RasterLayer) {
			RasterLayer rl = (RasterLayer) getLayer();
			LayerTreeLegendItemNode tn = new LayerTreeLegendItemNode(this, rl, LayerIconUtil.getSmallLayerIconUrl(rl));
			tree.add(tn, this);
		}
	}

}