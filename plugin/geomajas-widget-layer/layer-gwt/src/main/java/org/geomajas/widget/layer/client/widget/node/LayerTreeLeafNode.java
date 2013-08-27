package org.geomajas.widget.layer.client.widget.node;

import org.geomajas.gwt.client.map.layer.AbstractLayer;
import org.geomajas.gwt.client.map.layer.Layer;
import org.geomajas.gwt.client.map.layer.VectorLayer;
import org.geomajas.widget.layer.client.widget.RefreshableTree;

/**
 * A node inside the LayerTree.
 * 
 * @author Frank Wynants
 * @author Pieter De Graef
 */
public abstract class LayerTreeLeafNode extends LayerTreeNode {

	protected static final String ICON_HIDE = ICON_BASE + "layer-hide.png";

	protected static final String ICON_SHOW = ICON_BASE + "layer-show";

	protected static final String ICON_SHOW_OUT_OF_RANGE = "-outofrange";

	protected static final String ICON_SHOW_LABELED = "-labeled";

	protected static final String ICON_SHOW_FILTERED = "-filtered";

	protected static final String ICON_SHOW_END = ".png";

	protected final AbstractLayer<?> layer;

	/**
	 * Constructor creates a TreeNode with layer.getLabel as label.
	 * 
	 * @param tree
	 *            tree for node
	 * @param layer
	 *            The layer object
	 */
	public LayerTreeLeafNode(RefreshableTree tree, Layer<?> layer) {
		super(tree, layer.getLabel());
		this.layer = (AbstractLayer<?>) layer;
		updateIcon(false);
	}
	
	@Override
	public boolean isVisible() {
		return layer.isShowing();
	}

	@Override
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