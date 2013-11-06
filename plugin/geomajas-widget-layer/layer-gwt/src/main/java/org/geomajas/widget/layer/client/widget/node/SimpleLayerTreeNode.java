package org.geomajas.widget.layer.client.widget.node;

import org.geomajas.gwt.client.map.layer.Layer;
import org.geomajas.widget.layer.client.widget.RefreshableTree;

/**
 * 
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class SimpleLayerTreeNode extends LayerTreeLeafNode {

	public SimpleLayerTreeNode(RefreshableTree tree, Layer<?> layer) {
		super(tree, layer);
	}

}