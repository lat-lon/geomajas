package org.geomajas.widget.layer.client.widget.node;

import java.util.ArrayList;
import java.util.List;

import org.geomajas.widget.layer.client.widget.RefreshableTree;

import com.smartgwt.client.widgets.tree.TreeNode;

/**
 * {@link TreeNode} implementation for category nodes
 * 
 * @author <a href="mailto:erben@lat-lon.de">Alexander Erben</a>
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class LayerTreeBranchNode extends LayerTreeNode {

	private static final String FOLDER_HIDE = ICON_BASE + "folder-hide.png";

	private static final String FOLDER_SHOW = ICON_BASE + "folder-show.png";

	private List<LayerTreeNode> childLayers = new ArrayList<LayerTreeNode>();

	public LayerTreeBranchNode(RefreshableTree tree, String treeNodeLabel) {
		super(tree, treeNodeLabel);
		changeIcon(false);
	}

	@Override
	public boolean isVisible() {
		for (TreeNode treeNode : this.childLayers) {
			if (isLayerTreeNodeVisible(treeNode)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void updateIcon() {
		changeIcon(isVisible());
		tree.refreshIcons();
	}

	public void setChildLayers(List<LayerTreeNode> childLayers) {
		if (childLayers != null)
			this.childLayers = childLayers;
		updateIcon();
	}

	public List<LayerTreeNode> getChildLayers() {
		return childLayers;
	}

	private void changeIcon(boolean isVisible) {
		if (isVisible)
			setIcon(FOLDER_SHOW);
		else
			setIcon(FOLDER_HIDE);
	}

	private boolean isLayerTreeNodeVisible(TreeNode treeNode) {
		return treeNode instanceof LayerTreeNode && ((LayerTreeNode) treeNode).isVisible();
	}

}