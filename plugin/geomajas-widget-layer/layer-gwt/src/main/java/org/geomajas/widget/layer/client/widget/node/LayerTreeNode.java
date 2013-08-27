package org.geomajas.widget.layer.client.widget.node;

import org.geomajas.widget.layer.client.widget.RefreshableTree;

import com.smartgwt.client.widgets.tree.TreeNode;

public abstract class LayerTreeNode extends TreeNode {

	protected static final String ICON_BASE = "[ISOMORPHIC]/geomajas/widget/layertree/";

	protected final RefreshableTree tree;

	public LayerTreeNode(RefreshableTree tree, String label) {
		super(label);
		this.tree = tree;
	}

	public abstract boolean isVisible();

	public abstract void updateIcon();

}