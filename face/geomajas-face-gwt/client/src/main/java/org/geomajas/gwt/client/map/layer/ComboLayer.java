package org.geomajas.gwt.client.map.layer;

import java.util.List;

import org.geomajas.gwt.client.gfx.PainterVisitor;
import org.geomajas.gwt.client.map.cache.tile.MapTile;
import org.geomajas.gwt.client.map.cache.tile.TileFunction;
import org.geomajas.gwt.client.map.event.LayerStyleChangeEvent;
import org.geomajas.gwt.client.map.store.DefaultComboLayerStore;
import org.geomajas.gwt.client.spatial.Bbox;

public class ComboLayer extends AbstractLayer {

	private List<Layer<?>> layers;

	private DefaultComboLayerStore store;

	public ComboLayer(List<Layer<?>> layers) {
		super(layers.get(0).getMapModel(), layers.get(0).getLayerInfo());
		this.layers = layers;
		this.store = new DefaultComboLayerStore(this);
	}

	public List<Layer<?>> getLayers() {
		return layers;
	}

	@Override
	public void updateStyle() {
		handlerManager.fireEvent(new LayerStyleChangeEvent(this));
	}

	@Override
	public void accept(final PainterVisitor visitor, final Object group, final Bbox bounds, boolean recursive) {
		visitor.visit(this, group);
		// When visible, take care of fetching through an applyAndSync:
		TileFunction<MapTile> onDelete = new TileFunction<MapTile>() {

			public void execute(MapTile tile) {
				visitor.remove(tile, group);
			}
		};
		TileFunction<MapTile> onUpdate = new TileFunction<MapTile>() {

			// Updating a tile, re-rendering it:
			public void execute(MapTile tile) {
				tile.accept(visitor, group, bounds, true);
			}
		};
		store.applyAndSync(bounds, onDelete, onUpdate);
	}

}
