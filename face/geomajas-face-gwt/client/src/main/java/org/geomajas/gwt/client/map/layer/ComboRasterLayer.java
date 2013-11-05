package org.geomajas.gwt.client.map.layer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geomajas.configuration.client.ClientRasterLayerInfo;
import org.geomajas.gwt.client.gfx.PainterVisitor;
import org.geomajas.gwt.client.map.cache.tile.RasterTile;
import org.geomajas.gwt.client.map.cache.tile.TileFunction;
import org.geomajas.gwt.client.map.event.LayerStyleChangeEvent;
import org.geomajas.gwt.client.map.store.DefaultRasterLayerStore;
import org.geomajas.gwt.client.spatial.Bbox;

import com.smartgwt.client.util.SC;

/**
 * Client side combined {@link RasterLayer}
 * 
 * @author Alexander Erben
 *
 */
public class ComboRasterLayer extends RasterLayer {

	private List<Layer<?>> layers;

	private DefaultRasterLayerStore store;


	public ComboRasterLayer(List<Layer<?>> layers) {
		super(layers.get(0).getMapModel(), (ClientRasterLayerInfo) layers.get(0).getLayerInfo());
		configureOpacity(layers);
		this.layers = new ArrayList<Layer<?>>(layers);
		this.store = new DefaultRasterLayerStore(this);
	}

	private void configureOpacity(List<Layer<?>> layers) {
		for (Layer<?> layer : layers) {
			if (layer instanceof RasterLayer) {
				RasterLayer rLayer = (RasterLayer) layer;
				double rasterLayerOpacity = rLayer.getOpacity();
				if (getOpacity() > rasterLayerOpacity) setOpacity(rasterLayerOpacity);
			}
		}
	}

	public List<Layer<?>> getLayers() {
		return Collections.unmodifiableList(layers);
	}

	@Override
	public void updateStyle() {
		handlerManager.fireEvent(new LayerStyleChangeEvent(this));
	}

	@Override
	public void accept(final PainterVisitor visitor, final Object group, final Bbox bounds, boolean recursive) {
		visitor.visit(this, group);
		// When visible, take care of fetching through an applyAndSync:
		TileFunction<RasterTile> onDelete = new TileFunction<RasterTile>() {

			public void execute(RasterTile tile) {
				visitor.remove(tile, group);
			}
		};
		TileFunction<RasterTile> onUpdate = new TileFunction<RasterTile>() {

			// Updating a tile, re-rendering it:
			public void execute(RasterTile tile) {
				tile.accept(visitor, group, bounds, true);
			}
		};
		store.applyAndSync(bounds, onDelete, onUpdate);
	}
	
	@Override
	public boolean isShowing() {
		return true;
	}
	
	public List<String> getServerLayerIds() {
		List<String> layerIds = new ArrayList<String>();
		for (Layer layer : getLayers()) {
			layerIds.add(layer.getServerLayerId());
		}
		return layerIds;
	}

}
