package org.geomajas.widget.layer.client.widget;

import java.util.ArrayList;
import java.util.List;

import org.geomajas.widget.layer.client.widget.node.LayerTreeBranchNode;
import org.geomajas.widget.layer.client.widget.node.LayerTreeLegendNode;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeNode;

/**
 * A SmartGWT Tree with one extra method 'refresh'. This is needed to update icons on the fly in a tree
 * 
 * @author Frank Wynants
 */
public class RefreshableTree extends Tree {

	private final LayerTreeBase treeBase;

	private static final String EXPANDED_ATTR = "isExpanded";

	public RefreshableTree(LayerTreeBase treeBase) {
		this.treeBase = treeBase;
	}

	/**
	 * Refreshes the icons in the tree, this is done by closing and reopening all nodes. A dirty solution but no other
	 * option was found at the time.
	 */
	public void refreshIcons() {
		GWT.log("Refresh node(icon)s");

		List<TreeNode> actuallyOpenNodes = new ArrayList<TreeNode>();
		for (TreeNode node : this.getAllNodes()) {
			if (this.isOpen(node))
				actuallyOpenNodes.add(node);
		}

		this.closeAll();
		treeBase.syncNodeState(true);

		// exclude layers, which are handled by syncNodeState()
		for (TreeNode openNode : actuallyOpenNodes) {
			if (!(openNode instanceof LayerTreeLegendNode)) {
				this.openFolder(openNode);
			}
		}
	}

}