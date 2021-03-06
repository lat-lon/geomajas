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

package org.geomajas.widget.layer.client.widget;

import java.util.ArrayList;
import java.util.List;

import org.geomajas.gwt.client.i18n.I18nProvider;
import org.geomajas.gwt.client.map.MapModel;
import org.geomajas.gwt.client.map.event.LayerDeselectedEvent;
import org.geomajas.gwt.client.map.event.LayerSelectedEvent;
import org.geomajas.gwt.client.map.event.LayerSelectionHandler;
import org.geomajas.gwt.client.map.event.MapModelChangedEvent;
import org.geomajas.gwt.client.map.event.MapModelChangedHandler;
import org.geomajas.gwt.client.map.layer.AbstractLayer;
import org.geomajas.gwt.client.map.layer.Layer;
import org.geomajas.gwt.client.util.Log;
import org.geomajas.gwt.client.widget.MapWidget;
import org.geomajas.gwt.client.widget.MapWidget.RenderGroup;
import org.geomajas.gwt.client.widget.MapWidget.RenderStatus;
import org.geomajas.widget.layer.client.widget.node.LayerTreeBranchNode;
import org.geomajas.widget.layer.client.widget.node.LayerTreeLeafNode;
import org.geomajas.widget.layer.client.widget.node.LayerTreeNode;
import org.geomajas.widget.layer.configuration.client.ClientAbstractNodeInfo;
import org.geomajas.widget.layer.configuration.client.ClientLayerTreeInfo;

import com.google.gwt.user.client.Element;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.EventHandler;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.HTMLFlow;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeNode;
import com.smartgwt.client.widgets.tree.events.FolderClickEvent;
import com.smartgwt.client.widgets.tree.events.FolderClickHandler;
import com.smartgwt.client.widgets.tree.events.LeafClickEvent;
import com.smartgwt.client.widgets.tree.events.LeafClickHandler;

/**
 * The LayerTree shows a tree resembling the available layers for the map. Several actions can be executed on the layers
 * (make them invisible, ...).
 * 
 * TODO This is a copy from LayerTree, should make original properties protected, of add get/setters
 * 
 * @author Kristof Heirwegh
 * @author Frank Wynants
 * @author Pieter De Graef
 */
public abstract class LayerTreeBase extends Canvas implements LeafClickHandler, FolderClickHandler,
		LayerSelectionHandler {

	protected static final String IMG_TAGNAME = "IMG";

	protected static final int LAYERTREEBUTTON_SIZE = 22;

	protected static final int DEFAULT_ICONSIZE = 18;

	protected final HTMLFlow htmlSelectedLayer = new HTMLFlow(I18nProvider.getLayerTree().activeLayer(
			I18nProvider.getLayerTree().none()));

	protected LayerTreeLeafNode selectedLayerTreeNode;

	protected LayerTreeBranchNode selectedLayerBranchNode;
	
	protected TreeGrid treeGrid;

	protected RefreshableTree tree;

	protected MapModel mapModel;

	protected boolean initialized;

	private MapWidget mapWidget;

	// -------------------------------------------------------------------------
	// Constructor:
	// -------------------------------------------------------------------------

	/**
	 * Initialize the LayerTree, using a MapWidget as base reference. It will display the map's layers, as configured in
	 * the XML configuration, and select/deselect the layer as the user clicks on them in the tree.
	 * 
	 * @param mapWidget
	 *            map widget this layer tree is connected to
	 * @since 1.0.0
	 */
	public LayerTreeBase(final MapWidget mapWidget) {
		super();
		setHeight100();
		this.mapWidget = mapWidget;
		mapModel = mapWidget.getMapModel();
		htmlSelectedLayer.setWidth100();
		treeGrid = createTreeGrid();
		treeGrid.setSelectionType(SelectionStyle.SINGLE);
		treeGrid.setShowRoot(false);

		// Wait for the MapModel to be loaded
		mapModel.addMapModelChangedHandler(new MapModelChangedHandler() {

			public void onMapModelChanged(MapModelChangedEvent event) {
				if (!initialized) {
					initialize();
				}
				initialized = true;
			}
		});
		mapModel.addLayerSelectionHandler(this);
	}

	// -------------------------------------------------------------------------
	// LayerSelectionHandler implementation:
	// -------------------------------------------------------------------------

	/**
	 * When a layer deselection event comes in, the LayerTree must also deselect the correct node in the tree, update
	 * the selected layer text, and update all buttons icons.
	 * 
	 * @param event
	 *            event
	 */
	@Override
	public void onDeselectLayer(LayerDeselectedEvent event) {
		ListGridRecord selected = treeGrid.getSelectedRecord();
		if (selected != null) {
			treeGrid.deselectRecord(selected);
		}
		selectedLayerTreeNode = null;
		showNoLayerSelectedNote();
	}

	/**
	 * When a layer selection event comes in, the LayerTree must also select the correct node in the tree, update the
	 * selected layer text, and update all buttons icons.
	 * 
	 * @param event
	 *            event
	 */
	@Override
	public void onSelectLayer(LayerSelectedEvent event) {
		for (TreeNode node : tree.getAllNodes()) {
			if (node.getName().equals(event.getLayer().getLabel())) {
				selectedLayerTreeNode = (LayerTreeLeafNode) node;
				treeGrid.selectRecord(selectedLayerTreeNode);
				updateActiveLayerNote(selectedLayerTreeNode.getLayer().getLabel());

				// Canvas[] toolStripMembers = toolStrip.getMembers();
				// updateButtonIconsAndStates(toolStripMembers);
			}
		}
	}

	// -------------------------------------------------------------------------
	// LeafClickHandler, FolderClickHandler, CellClickHandler
	// -------------------------------------------------------------------------

	/**
	 * When the user clicks on a folder nothing gets selected.
	 * 
	 * @param event
	 *            event
	 */
	@Override
	public void onFolderClick(FolderClickEvent event) {
		try {
			Element e = EventHandler.getNativeMouseTarget();
			TreeNode treeNode = event.getFolder();
			if (IMG_TAGNAME.equals(e.getTagName())) {
				onIconClick(treeNode);
			} else {
				if (treeNode instanceof LayerTreeLeafNode) {
					mapModel.selectLayer(((LayerTreeLeafNode) treeNode).getLayer());
					mapModel.setSelectedLayersOfCategory(null);
				} else if (treeNode instanceof LayerTreeBranchNode) {
					mapModel.selectLayer(null);
					if (sameFolderNodeIsClicked(treeNode)) {
						treeGrid.deselectRecord(treeNode);
						mapModel.setSelectedLayersOfCategory(null);
						showNoCategorySelectedNote();
						selectedLayerBranchNode = null;
					} else {
						List<Layer<?>> childLayers = new ArrayList<Layer<?>>();
						collectChildLayers((LayerTreeBranchNode) treeNode, childLayers);
						mapModel.setSelectedLayersOfCategory(childLayers);
						updateActiveCategoryNote(treeNode.getName());
						selectedLayerBranchNode = (LayerTreeBranchNode) treeNode;
					}
				}
			}
		} catch (Exception e) { // NOSONAR
			Log.logError(e.getMessage());
			// some other unusable element
		}
	}

	private boolean sameFolderNodeIsClicked(TreeNode treeNode) {
		return selectedLayerBranchNode != null && selectedLayerBranchNode == treeNode;
	}

	private void collectChildLayers(LayerTreeBranchNode branchNode, List<Layer<?>> childLayers) {
		List<LayerTreeNode> childLayerNodes = branchNode.getChildLayers();
		for (LayerTreeNode childLayer : childLayerNodes) {
			if (childLayer instanceof LayerTreeLeafNode) {
				Layer<?> layer = ((LayerTreeLeafNode) childLayer).getLayer();
				childLayers.add(layer);
			} else if (childLayer instanceof LayerTreeBranchNode) {
				collectChildLayers((LayerTreeBranchNode) childLayer, childLayers);
			}
		}
	}

	/**
	 * When the user clicks on a leaf the headertext of the treetable is changed to the selected leaf and the toolbar
	 * buttons are updated to represent the correct state of the buttons.
	 * 
	 * @param event
	 *            event
	 */
	@Override
	public void onLeafClick(LeafClickEvent event) {
		TreeNode leaf = event.getLeaf();
		handleVisibilityAndSelection(leaf);
	}

	protected void handleVisibilityAndSelection(TreeNode leaf) {
		try {
			Element e = EventHandler.getNativeMouseTarget();
			if (IMG_TAGNAME.equals(e.getTagName())) {
				onIconClick(leaf);
			}
			LayerTreeLeafNode layerTreeNode = (LayerTreeLeafNode) leaf;
			if (selectedLayerTreeNode == layerTreeNode) {
				mapModel.deselectLayer(layerTreeNode.getLayer());
			} else {
				mapModel.selectLayer(layerTreeNode.getLayer());
			}
			mapModel.setSelectedLayersOfCategory(null);
		} catch (Exception e) { // NOSONAR
			Log.logError(e.getMessage());
			// some other unusable element
		}
	}

	private void onIconClick(TreeNode node) {
		if (node instanceof LayerTreeLeafNode) {
			onLeafNodeClick(node);
		} else if (node instanceof LayerTreeBranchNode) {
			onBranchNodeClick(node);
		}
		
	}

	private void onLeafNodeClick(TreeNode node) {
		LayerTreeLeafNode leafNode = (LayerTreeLeafNode) node;
		leafNode.getLayer().setVisible(!leafNode.getLayer().isVisible());
		leafNode.updateIcon();
		updateParents(leafNode);
	}

	private void onBranchNodeClick(TreeNode node) {
		LayerTreeBranchNode branchNode = (LayerTreeBranchNode) node;
		boolean newStatus = !branchNode.isVisible();
		updateChildLayers(branchNode, newStatus);
		branchNode.updateIcon();
		updateParents(branchNode);
		mapWidget.render(mapModel, RenderGroup.WORLD, RenderStatus.ALL);
	}

	private void updateParents(LayerTreeNode leafNode) {
		TreeNode[] parents = tree.getParents(leafNode);
		for (TreeNode treeNode : parents) {
			if (treeNode instanceof LayerTreeBranchNode) {
				LayerTreeBranchNode branchNode = (LayerTreeBranchNode) treeNode;
				branchNode.updateIcon();
			}
		}
	}

	private void updateChildLayers(LayerTreeBranchNode branchTreeNode, boolean newStatus) {
		List<LayerTreeNode> childNodes = branchTreeNode.getChildLayers();
        LayerTreeLeafNode lastLayer = null;
		for (LayerTreeNode childNode : childNodes) {
			if (childNode instanceof LayerTreeLeafNode) {
				LayerTreeLeafNode n = (LayerTreeLeafNode) childNode;
				n.getLayer().setVisibleNoEvent(newStatus);
				n.updateIcon();
                lastLayer = n;
			} else if (childNode instanceof LayerTreeBranchNode) {
				LayerTreeBranchNode childBranchTreeNode = (LayerTreeBranchNode) childNode;
				updateChildLayers(childBranchTreeNode, newStatus);
			}
		}
        //Executing setVisible of the last layer to fire an event which updates the legend.
        if (lastLayer != null) {
            AbstractLayer<?> layer = lastLayer.getLayer();
            layer.setVisible(!layer.isVisible());
            layer.setVisible(newStatus);
        }
	}

	// -------------------------------------------------------------------------
	// Getters:
	// -------------------------------------------------------------------------

	/**
	 * Get the currently selected tree node.
	 * 
	 * @return selected node
	 */
	public LayerTreeLeafNode getSelectedLayerTreeNode() {
		return selectedLayerTreeNode;
	}

	// -------------------------------------------------------------------------
	// Private methods:
	// -------------------------------------------------------------------------

	protected void initialize() {
		buildTree();
		VLayout vLayout = new VLayout();
		vLayout.setSize("100%", "100%");
		htmlSelectedLayer.setBackgroundColor("#cccccc");
		htmlSelectedLayer.setAlign(Alignment.CENTER);
		vLayout.addMember(htmlSelectedLayer);
		vLayout.addMember(treeGrid);
		treeGrid.markForRedraw();
		LayerTreeBase.this.addChild(vLayout);
		LayerTreeBase.this.markForRedraw();
	}

	/**
	 * Builds up the tree showing the layers.
	 */
	protected void buildTree() {
		treeGrid.setWidth100();
		treeGrid.setHeight100();
		treeGrid.setShowHeader(false);
		treeGrid.setOverflow(Overflow.AUTO);
		tree = new RefreshableTree(this);
		final TreeNode nodeRoot = new TreeNode("ROOT");
		tree.setRoot(nodeRoot); // invisible ROOT node (ROOT node is required)

		ClientLayerTreeInfo layerTreeInfo = (ClientLayerTreeInfo) mapModel.getMapInfo().getWidgetInfo(
				ClientLayerTreeInfo.IDENTIFIER);
		tree.setRoot(nodeRoot); // invisible ROOT node (ROOT node is required)
		if (layerTreeInfo != null) {
			for (ClientAbstractNodeInfo node : layerTreeInfo.getTreeNode().getTreeNodes()) {
				processNode(node, nodeRoot, false);
			}
		}

		treeGrid.setData(tree);
		treeGrid.addLeafClickHandler(this);
		treeGrid.addFolderClickHandler(this);
		tree.openFolder(nodeRoot);
		syncNodeState(false);
	}

	/**
	 * Processes a treeNode (add it to the TreeGrid).
	 * 
	 * @param treeNode
	 *            The treeNode to process
	 * @param nodeRoot
	 *            The root node to which the treeNode has to be added
	 * @param refresh
	 *            True if the tree is refreshed (causing it to keep its expanded state)
	 * @return the list of processed (and added) nodes
	 */
	protected abstract List<LayerTreeNode> processNode(final ClientAbstractNodeInfo treeNode, final TreeNode nodeRoot,
			final boolean refresh);

	protected abstract void syncNodeState(boolean layersOnly);

	/**
	 * Creation of tree grid is decoupled to allow you to make a custom tree grid. (SmartGWT uses some design patterns
	 * which only give you the ability to customize certain aspects in a subclass)
	 * 
	 * @return tree grid
	 */
	protected TreeGrid createTreeGrid() {
		return new TreeGrid();
	}
	
	protected void showNoLayerSelectedNote() {
		updateActiveLayerNote(I18nProvider.getLayerTree().none());
	}

	protected void updateActiveLayerNote(String activeLayer) {
		htmlSelectedLayer.setContents(I18nProvider.getLayerTree().activeLayer(activeLayer));
	}

	protected void showNoCategorySelectedNote() {
		updateActiveCategoryNote(I18nProvider.getLayerTree().none());
	}

	protected void updateActiveCategoryNote(String activeCategory) {
		htmlSelectedLayer.setContents(I18nProvider.getLayerTree().activeCategory(activeCategory));
	}

}