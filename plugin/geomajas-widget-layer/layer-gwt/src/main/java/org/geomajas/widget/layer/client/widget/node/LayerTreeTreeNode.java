package org.geomajas.widget.layer.client.widget.node;

import org.geomajas.gwt.client.map.layer.AbstractLayer;
import org.geomajas.gwt.client.map.layer.Layer;
import org.geomajas.gwt.client.map.layer.VectorLayer;
import org.geomajas.widget.layer.client.widget.RefreshableTree;

import com.smartgwt.client.widgets.tree.TreeNode;

/**
 * A node inside the LayerTree.
 * 
 * @author Frank Wynants
 * @author Pieter De Graef
 */
public class LayerTreeTreeNode extends TreeNode {

	protected static final String ICON_BASE = "[ISOMORPHIC]/geomajas/widget/layertree/";

	protected static final String ICON_HIDE = ICON_BASE + "layer-hide.png";

	protected static final String ICON_SHOW = ICON_BASE + "layer-show";

	protected static final String ICON_SHOW_OUT_OF_RANGE = "-outofrange";

	protected static final String ICON_SHOW_LABELED = "-labeled";

	protected static final String ICON_SHOW_FILTERED = "-filtered";

	protected static final String ICON_SHOW_END = ".png";

	protected final RefreshableTree tree;

	protected final AbstractLayer<?> layer;

	/**
	 * Constructor creates a TreeNode with layer.getLabel as label.
	 * 
	 * @param tree
	 *            tree for node
	 * @param layer
	 *            The layer object
	 */
	public LayerTreeTreeNode(RefreshableTree tree, Layer<?> layer) {
		super(layer.getLabel());
		this.layer = (AbstractLayer<?>) layer;
		this.tree = tree;
		updateIcon(false);
	}

	public void updateIcon() {
		updateIcon(true);
	}

	/**
	 * Causes the node to check its status (visible, showing labels, ...) and to update its icon to match its status.
	 * 
	 * @param refresh
	 *            should tree be refreshed
	 */
	public void updateIcon(boolean refresh) {
		if (getLayer().isVisible()) {
			StringBuffer icon = new StringBuffer(ICON_SHOW);
			if (!getLayer().isShowing()) {
				icon.append(ICON_SHOW_OUT_OF_RANGE);
			}
			if (getLayer().isLabelsVisible()) {
				icon.append(ICON_SHOW_LABELED);
			}
			if (getLayer() instanceof VectorLayer) {
				VectorLayer vl = (VectorLayer) getLayer();
				if (vl.getFilter() != null && vl.getFilter().length() > 0) {
					icon.append(ICON_SHOW_FILTERED);
				}
			}
			icon.append(ICON_SHOW_END);
			setIcon(icon.toString());
		} else {
			setIcon(ICON_HIDE);
		}
		if (refresh) {
			getTree().refreshIcons();
		}
	}

	public AbstractLayer<?> getLayer() {
		return layer;
	}

	public RefreshableTree getTree() {
		return tree;
	}
}