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

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.geomajas.annotation.Api;
import org.geomajas.configuration.client.ClientToolInfo;
import org.geomajas.gwt.client.action.ToolbarBaseAction;
import org.geomajas.gwt.client.action.layertree.LayerTreeAction;
import org.geomajas.gwt.client.action.layertree.LayerTreeModalAction;
import org.geomajas.gwt.client.action.layertree.LayerTreeRegistry;
import org.geomajas.gwt.client.map.event.LayerChangedHandler;
import org.geomajas.gwt.client.map.event.LayerFilteredEvent;
import org.geomajas.gwt.client.map.event.LayerFilteredHandler;
import org.geomajas.gwt.client.map.event.LayerLabeledEvent;
import org.geomajas.gwt.client.map.event.LayerShownEvent;
import org.geomajas.gwt.client.map.event.LayerStyleChangeEvent;
import org.geomajas.gwt.client.map.event.LayerStyleChangedHandler;
import org.geomajas.gwt.client.map.layer.Layer;
import org.geomajas.gwt.client.map.layer.VectorLayer;
import org.geomajas.gwt.client.widget.MapWidget;
import org.geomajas.widget.layer.client.LayerMessages;
import org.geomajas.widget.layer.client.widget.node.LayerTreeBranchNode;
import org.geomajas.widget.layer.client.widget.node.LayerTreeLeafNode;
import org.geomajas.widget.layer.client.widget.node.LayerTreeLegendItemNode;
import org.geomajas.widget.layer.client.widget.node.LayerTreeLegendNode;
import org.geomajas.widget.layer.client.widget.node.LayerTreeNode;
import org.geomajas.widget.layer.configuration.client.ClientAbstractNodeInfo;
import org.geomajas.widget.layer.configuration.client.ClientBranchNodeInfo;
import org.geomajas.widget.layer.configuration.client.ClientLayerNodeInfo;
import org.geomajas.widget.layer.configuration.client.ClientLayerTreeInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.SelectionType;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.ImgButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.LayoutSpacer;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeNode;
import com.smartgwt.client.widgets.tree.events.LeafClickEvent;

/**
 * A layertree widget with combined legend per layer.
 * 
 * @author Kristof Heirwegh
 * @since 1.0.0
 */
@Api
public class CombinedLayertree extends LayerTreeBase {

	private static final String SHOW_LAYERINFO_ICON = "[ISOMORPHIC]/geomajas/silk/cog.png";

	private static final LayerMessages MESSAGES = GWT.create(LayerMessages.class);

	private static final String EXPANDED_ATTR = "isExpanded";

	private final MapWidget mapWidget;

	private final List<HandlerRegistration> registrations = new ArrayList<HandlerRegistration>();

	protected LayerTreeLeafNode rollOverLayerTreeNode;

	private HashMap<VectorLayer, List<LayerTreeLegendItemNode>> legendIcons = new HashMap<VectorLayer, List<LayerTreeLegendItemNode>>();

	public CombinedLayertree(final MapWidget mapWidget) {
		super(mapWidget);
		this.mapWidget = mapWidget;
		treeGrid.setShowRollOverCanvas(true);
	}

	public int getIconSize() {
		return treeGrid.getImageSize();
	}

	public void setIconSize(int iconSize) {
		treeGrid.setIconSize(iconSize);
	}

	public void addLegendIcon(VectorLayer key, List<LayerTreeLegendItemNode> value) {
		legendIcons.put(key, value);
	}

	@Override
	protected List<LayerTreeNode> processNode(final ClientAbstractNodeInfo treeNode, final TreeNode nodeRoot,
			final boolean refresh) {
		// Branches
		if (null != treeNode && treeNode instanceof ClientBranchNodeInfo) {
			String treeNodeLabel = ((ClientBranchNodeInfo) treeNode).getLabel();
			final LayerTreeBranchNode node = new LayerTreeBranchNode(tree, treeNodeLabel);
			node.setAttribute(EXPANDED_ATTR, ((ClientBranchNodeInfo) treeNode).isExpanded());
			tree.add(node, nodeRoot);

			// treeNodes
			List<LayerTreeNode> addedChildLayers = new ArrayList<LayerTreeNode>();
			List<ClientAbstractNodeInfo> children = treeNode.getTreeNodes();
			for (ClientAbstractNodeInfo newNode : children) {
				addedChildLayers.addAll(processNode(newNode, node, refresh));
			}
			node.setChildLayers(addedChildLayers);
			return addedChildLayers;
			// Leafs
		} else if (null != treeNode && treeNode instanceof ClientLayerNodeInfo) {
			if (treeNode instanceof ClientLayerNodeInfo) {
				Layer<?> layer = mapModel.getLayer(((ClientLayerNodeInfo) treeNode).getLayerId());
				// Ignore layers that are not available in the map
				if (layer != null) {
					LayerTreeLegendNode ltln = new LayerTreeLegendNode(this, this.tree, layer);
					tree.add(ltln, nodeRoot);
					ltln.init();
					return Collections.<LayerTreeNode> singletonList(ltln);
				}
			}
		}
		return emptyList();
	}

	/**
	 * When a legendItem is selected, select the layer instead.
	 * 
	 * @param event
	 *            event
	 */
	@Override
	public void onLeafClick(LeafClickEvent event) {
		TreeNode leaf = event.getLeaf();
		if (leaf instanceof LayerTreeLegendItemNode) {
			LayerTreeLeafNode layerTreeLegendNode = ((LayerTreeLegendItemNode) leaf).getParent();
			treeGrid.deselectRecord(leaf);
			treeGrid.selectRecord(layerTreeLegendNode);
			handleVisibilityOrSelect(layerTreeLegendNode);
		} else {
			handleVisibilityOrSelect(leaf);
		}
	}

	// ----------------------------------------------------------

	@Override
	protected void syncNodeState(boolean layersOnly) {
		for (TreeNode childnode : tree.getAllNodes(tree.getRoot())) {
			if (childnode instanceof LayerTreeLegendNode) {
				if (((LayerTreeLegendNode) childnode).getLayer().isShowing()) {
					tree.openFolder(childnode);
				} else {
					tree.closeFolder(childnode);
				}
			} else if (!layersOnly && !(childnode instanceof LayerTreeLegendItemNode)) {
				if (childnode.getAttributeAsBoolean(EXPANDED_ATTR)) {
					tree.openFolder(childnode);
				} else {
					tree.closeFolder(childnode);
				}
			}
		}
	}

	@Override
	protected TreeGrid createTreeGrid() {
		return createTreeGridInfoWindowRollover();
	}

	// @Override
	// protected void onIconClick(TreeNode node) {
	// if (node instanceof LayerTreeLegendNode) {
	// super.onIconClick(node);
	// } // else if (node instanceof TreeNode) {
	// // TODO -- show/hide all layers in folder
	// GWT.log("TODO");
	// // }
	// }

	protected TreeGrid createTreeGridInfoWindowRollover() {
		return new TreeGrid() {

			private HLayout rollOverTools;

			private HLayout emptyRollOver;

			@Override
			protected Canvas getRollOverCanvas(Integer rowNum, Integer colNum) {
				if (rollOverTools == null) {
					rollOverTools = new HLayout();
					rollOverTools.setSnapTo("TR");
					rollOverTools.setWidth(25);
					rollOverTools.setHeight(LAYERTREEBUTTON_SIZE);
					emptyRollOver = new HLayout();
					emptyRollOver.setWidth(1);
					emptyRollOver.setHeight(LAYERTREEBUTTON_SIZE);

					ImgButton showInfo = new ImgButton();
					showInfo.setShowDown(false);
					showInfo.setShowRollOver(false);
					showInfo.setLayoutAlign(Alignment.CENTER);
					showInfo.setSrc(SHOW_LAYERINFO_ICON);
					showInfo.setPrompt(MESSAGES.layerTreeWithLegendLayerActionsToolTip());
					showInfo.setHeight(16);
					showInfo.setWidth(16);
					showInfo.addClickHandler(new ClickHandler() {

						public void onClick(ClickEvent event) {
							LayerActions la = new LayerActions(rollOverLayerTreeNode.getLayer());
							la.draw();
						}
					});
					rollOverTools.addMember(showInfo);
				}

				ListGridRecord lgr = this.getRecord(rowNum);
				if (lgr instanceof LayerTreeLegendItemNode) {
					rollOverLayerTreeNode = ((LayerTreeLegendItemNode) lgr).getParent();
				} else if (lgr instanceof LayerTreeLegendNode) {
					rollOverLayerTreeNode = (LayerTreeLeafNode) lgr;
				} else {
					rollOverLayerTreeNode = null;
					rollOverTools.setVisible(false);
					return emptyRollOver;
				}

				rollOverTools.setVisible(true);
				return rollOverTools;
			}
		};
	}

	protected TreeGrid createTreeGridFullRollover() {
		return new TreeGrid() {

			private HLayout rollOverTools;

			private HLayout emptyRollOver;

			private Canvas[] toolButtons = new Canvas[0];

			@Override
			protected Canvas getRollOverCanvas(Integer rowNum, Integer colNum) {
				if (rollOverTools == null) {
					rollOverTools = new HLayout();
					rollOverTools.setSnapTo("TR");
					rollOverTools.setWidth(50);
					rollOverTools.setHeight(LAYERTREEBUTTON_SIZE);
					emptyRollOver = new HLayout();
					emptyRollOver.setWidth(1);
					emptyRollOver.setHeight(LAYERTREEBUTTON_SIZE);

					ClientLayerTreeInfo layerTreeInfo = (ClientLayerTreeInfo) mapModel.getMapInfo().getWidgetInfo(
							ClientLayerTreeInfo.IDENTIFIER);
					if (layerTreeInfo != null) {
						for (ClientToolInfo tool : layerTreeInfo.getTools()) {
							String id = tool.getId();
							IButton button = null;
							ToolbarBaseAction action = LayerTreeRegistry.getToolbarAction(id, mapWidget);
							if (action instanceof LayerTreeAction) {
								button = new LayerTreeButton(CombinedLayertree.this, (LayerTreeAction) action);
							} else if (action instanceof LayerTreeModalAction) {
								button = new LayerTreeModalButton(CombinedLayertree.this, (LayerTreeModalAction) action);
							}
							if (button != null) {
								rollOverTools.addMember(button);
								LayoutSpacer spacer = new LayoutSpacer();
								spacer.setWidth(2);
								rollOverTools.addMember(spacer);
							}
						}
					}
					toolButtons = rollOverTools.getMembers();
				}

				ListGridRecord lgr = this.getRecord(rowNum);
				if (lgr instanceof LayerTreeLegendItemNode) {
					rollOverLayerTreeNode = ((LayerTreeLegendItemNode) lgr).getParent();
				} else if (lgr instanceof LayerTreeLegendNode) {
					rollOverLayerTreeNode = (LayerTreeLeafNode) lgr;
				} else {
					rollOverLayerTreeNode = null;
					rollOverTools.setVisible(false);
					return emptyRollOver;
				}

				rollOverTools.setVisible(true);
				updateButtonIconsAndStates();
				return rollOverTools;
			}

			/**
			 * Updates the icons and the state of the buttons in the toolbar based upon the current layer
			 * 
			 * @param toolStripMembers
			 *            data for the toolbar
			 */
			private void updateButtonIconsAndStates() {
				for (Canvas toolButton : toolButtons) {
					if (toolButton instanceof LayerTreeModalButton) {
						((LayerTreeModalButton) toolButton).update();
					} else if (toolButton instanceof LayerTreeButton) {
						((LayerTreeButton) toolButton).update();
					}
				}
			}
		};
	}

	/**
	 * General definition of an action button for the layer tree.
	 * 
	 * @author Frank Wynants
	 * @author Pieter De Graef
	 */
	private class LayerTreeButton extends IButton {

		private final CombinedLayertree tree;

		private final LayerTreeAction action;

		public LayerTreeButton(final CombinedLayertree tree, final LayerTreeAction action) {
			super();
			this.tree = tree;
			this.action = action;
			setWidth(LAYERTREEBUTTON_SIZE);
			setHeight(LAYERTREEBUTTON_SIZE);
			setIconSize(LAYERTREEBUTTON_SIZE - 8);
			setIcon(action.getIcon());
			setTooltip(action.getTooltip());
			setActionType(SelectionType.BUTTON);
			setShowDisabledIcon(false);
			addClickHandler(new ClickHandler() {

				public void onClick(ClickEvent event) {
					try {
						action.onClick(tree.rollOverLayerTreeNode.getLayer());
						update();
					} catch (Throwable t) {
						GWT.log("LayerTreeButton onClick error", t);
					}
				}
			});
		}

		public void update() {
			LayerTreeLeafNode selected = tree.rollOverLayerTreeNode;
			if (selected != null && action.isEnabled(selected.getLayer())) {
				setDisabled(false);
				setIcon(action.getIcon());
				setTooltip(action.getTooltip());
			} else {
				setDisabled(true);
				GWT.log("LayerTreeButton" + action.getDisabledIcon());
				setIcon(action.getDisabledIcon());
				setTooltip("");
			}
		}
	}

	/**
	 * General definition of a modal button for the layer tree.
	 * 
	 * @author Frank Wynants
	 * @author Pieter De Graef
	 */
	private class LayerTreeModalButton extends IButton {

		private final CombinedLayertree tree;

		private final LayerTreeModalAction modalAction;

		/**
		 * Constructor
		 * 
		 * @param tree
		 *            The currently selected layer
		 * @param modalAction
		 *            The action coupled to this button
		 */
		public LayerTreeModalButton(final CombinedLayertree tree, final LayerTreeModalAction modalAction) {
			super();
			this.tree = tree;
			this.modalAction = modalAction;
			setWidth(LAYERTREEBUTTON_SIZE);
			setHeight(LAYERTREEBUTTON_SIZE);
			setIconSize(LAYERTREEBUTTON_SIZE - 8);
			setIcon(modalAction.getDeselectedIcon());
			setActionType(SelectionType.CHECKBOX);
			setTooltip(modalAction.getDeselectedTooltip());
			setShowDisabledIcon(false);

			this.addClickHandler(new ClickHandler() {

				public void onClick(ClickEvent event) {
					LayerTreeLeafNode selectedLayerNode = tree.rollOverLayerTreeNode;
					if (LayerTreeModalButton.this.isSelected()) {
						modalAction.onSelect(selectedLayerNode.getLayer());
					} else {
						modalAction.onDeselect(selectedLayerNode.getLayer());
					}
					selectedLayerNode.updateIcon();
					update();
				}
			});
		}

		public void update() {
			LayerTreeLeafNode selected = tree.rollOverLayerTreeNode;
			if (selected != null && modalAction.isEnabled(selected.getLayer())) {
				setDisabled(false);
			} else {
				setSelected(false);
				setDisabled(true);
				GWT.log("LayerTreeModalButton" + modalAction.getDisabledIcon());
				setIcon(modalAction.getDisabledIcon());
				setTooltip("");
			}
			if (selected != null && modalAction.isSelected(selected.getLayer())) {
				setIcon(modalAction.getSelectedIcon());
				setTooltip(modalAction.getSelectedTooltip());
				select();
			} else if (selected != null) {
				setIcon(modalAction.getDeselectedIcon());
				setTooltip(modalAction.getDeselectedTooltip());
				deselect();
			}
		}
	}

	// -- part of legend

	@Override
	protected void initialize() {
		super.initialize();
		ClientLayerTreeInfo ltwli = (ClientLayerTreeInfo) mapWidget.getMapModel().getMapInfo()
				.getWidgetInfo(ClientLayerTreeInfo.IDENTIFIER);
		setIconSize(ltwli == null ? DEFAULT_ICONSIZE : ltwli.getIconSize());

		for (Layer<?> layer : mapModel.getLayers()) {
			registrations.add(layer.addLayerChangedHandler(new LayerChangedHandler() {

				public void onLabelChange(LayerLabeledEvent event) {
					GWT.log("Legend: onLabelChange() - " + event.getLayer().getLabel());
					// find the node & update the icon
					for (TreeNode node : tree.getAllNodes()) {
						if (node.getName().equals(event.getLayer().getLabel()) && node instanceof LayerTreeLeafNode) {
							((LayerTreeLeafNode) node).updateIcon();
						}
					}
				}

				public void onVisibleChange(LayerShownEvent event) {
					GWT.log("Legend: onVisibleChange() - " + event.getLayer().getLabel());
					// find the node & update the icon
					for (TreeNode node : tree.getAllNodes()) {
						if (node.getName().equals(event.getLayer().getLabel()) && node instanceof LayerTreeLeafNode) {
							((LayerTreeLeafNode) node).updateIcon();
						}
					}
				}
			}));
			registrations.add(layer.addLayerStyleChangedHandler(new LayerStyleChangedHandler() {

				public void onLayerStyleChange(LayerStyleChangeEvent event) {
					GWT.log("Legend: onLayerStyleChange()");
					Layer<?> layer = event.getLayer();
					if (layer instanceof VectorLayer) {
						for (LayerTreeLegendItemNode node : legendIcons.get(layer)) {
							node.updateStyle((VectorLayer) layer);
						}
					}
				}
			}));

			if (layer instanceof VectorLayer) {
				VectorLayer vl = (VectorLayer) layer;
				registrations.add(vl.addLayerFilteredHandler(new LayerFilteredHandler() {

					public void onFilterChange(LayerFilteredEvent event) {
						GWT.log("Legend: onLayerFilterChange() - " + event.getLayer().getLabel());
						// find the node & update the icon
						for (TreeNode node : tree.getAllNodes()) {
							if (node.getName().equals(event.getLayer().getLabel()) && node instanceof LayerTreeLeafNode) {
								((LayerTreeLeafNode) node).updateIcon();
							}
						}
					}
				}));
			}
		}
	}

	/** Remove all handlers on unload. */
	protected void onUnload() {
		if (registrations != null) {
			for (HandlerRegistration registration : registrations) {
				registration.removeHandler();
			}
		}
		super.onUnload();
	}

}
