package org.geomajas.gwt.client.map.layer;

import org.geomajas.gwt.client.gfx.Paintable;
import org.geomajas.gwt.client.gfx.PainterVisitor;
import org.geomajas.gwt.client.map.MapModel;
import org.geomajas.gwt.client.map.cache.tile.MapTile;
import org.geomajas.gwt.client.map.cache.tile.TileFunction;
import org.geomajas.gwt.client.map.store.DefaultMapLayerStore;
import org.geomajas.gwt.client.spatial.Bbox;

public class MapLayer implements Paintable {

	private MapModel mapModel;
	private DefaultMapLayerStore store;

	public MapLayer(MapModel mapModel) {
		this.mapModel = mapModel;
		this.store = new DefaultMapLayerStore(this);
	}
	
	public MapModel getMapModel() {
		return mapModel;
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
