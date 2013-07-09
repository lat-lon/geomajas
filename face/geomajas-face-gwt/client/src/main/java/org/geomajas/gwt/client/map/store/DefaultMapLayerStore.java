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
package org.geomajas.gwt.client.map.store;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geomajas.command.CommandResponse;
import org.geomajas.command.dto.GetMapTilesRequest;
import org.geomajas.command.dto.GetRasterTilesResponse;
import org.geomajas.gwt.client.command.CommandCallback;
import org.geomajas.gwt.client.command.Deferred;
import org.geomajas.gwt.client.command.GwtCommand;
import org.geomajas.gwt.client.command.GwtCommandDispatcher;
import org.geomajas.gwt.client.map.MapModel;
import org.geomajas.gwt.client.map.MapView;
import org.geomajas.gwt.client.map.MapViewState;
import org.geomajas.gwt.client.map.cache.tile.MapTile;
import org.geomajas.gwt.client.map.cache.tile.TileFunction;
import org.geomajas.gwt.client.map.layer.MapLayer;
import org.geomajas.gwt.client.spatial.Bbox;
import org.geomajas.gwt.client.spatial.Matrix;
import org.geomajas.gwt.client.util.Dom;
import org.geomajas.layer.tile.TileCode;
import org.geotools.map.Layer;

/**
 * A map layer store that keeps tiles in cache while panning, only clearing them when a non-panning move occurs, see
 * {@link org.geomajas.gwt.client.map.MapView}.
 * 
 * @author Jan De Moerloose
 * @author Oliver May
 */
public class DefaultMapLayerStore  {

	private Map<TileCode, MapTile> tiles = new HashMap<TileCode, MapTile>();

	private boolean dirty;

	private MapViewState lastViewState;

	private Bbox tileBounds;

	private Deferred deferred;

	private MapLayer mapLayer;

	public DefaultMapLayerStore(MapLayer mapLayer) {
	this.mapLayer = mapLayer;
	}

	public void applyAndSync(Bbox bounds, TileFunction<MapTile> onDelete, TileFunction<MapTile> onUpdate) {
		MapViewState viewState = mapLayer.getMapModel().getMapView().getViewState();
		boolean panning = lastViewState == null || viewState.isPannableFrom(lastViewState);
		if (!panning || isDirty()) {
			if (deferred != null) {
				deferred.cancel();
			}
			for (MapTile tile : tiles.values()) {
				onDelete.execute(tile);
			}
			tiles.clear();
			tileBounds = null;
			dirty = false;
		}
		lastViewState = mapLayer.getMapModel().getMapView().getViewState();
		if (tileBounds == null || !tileBounds.contains(bounds)) {
			fetchAndUpdateTiles(bounds, onUpdate);
		} else {
			updateTiles(bounds, onUpdate);
		}
	}

	public MapLayer getLayer() {
		return mapLayer;
	}

	public void clear() {
		if (tiles.size() > 0 || deferred != null) {
			dirty = true;
			if (deferred != null) {
				deferred.cancel();
			}
		}
	}

	public boolean isDirty() {
		return dirty;
	}

	public Collection<MapTile> getTiles() {
		return tiles.values();
	}

	private void fetchAndUpdateTiles(Bbox bounds, final TileFunction<MapTile> onUpdate) {
		// fetch a bigger area to avoid server requests while panning
		tileBounds = bounds.scale(3);
		GetMapTilesRequest request = new GetMapTilesRequest();

		request.setBbox(new org.geomajas.geometry.Bbox(tileBounds.getX(), tileBounds.getY(), tileBounds.getWidth(),
				tileBounds.getHeight()));
		request.setCrs(getLayer().getMapModel().getCrs());
		request.setScale(getLayer().getMapModel().getMapView().getCurrentScale());
		for (org.geomajas.gwt.client.map.layer.Layer<?> layer : getLayer().getMapModel().getLayers()) {
			if (layer.isShowing()) {
				request.getVisibleLayers().add(layer.getId());
				//FIXME: add support for vectortiles here
				// We should calculate and add all vectorlayer tile codes here, and add all requests for vectortiles
				// For each vectorlayer we need:
				//				request.setFilter(filter);
				//				request.setPaintLabels(layer.isLabelsShowing() && !labelContent.isLoaded());
				//				request.setStyleInfo(layer.getLayerInfo().getNamedStyleInfo());
				
			}
		}
		
		GwtCommand command = new GwtCommand(GetMapTilesRequest.COMMAND);
		command.setCommandRequest(request);
		RasterCallBack callBack = new RasterCallBack(worldToPan(bounds), onUpdate);
		deferred = GwtCommandDispatcher.getInstance().execute(command, callBack);
	}

	private void updateTiles(Bbox bounds, final TileFunction<MapTile> onUpdate) {
		Bbox panBounds = worldToPan(bounds);
		for (MapTile tile : tiles.values()) {
			if (panBounds.intersects(tile.getBounds())) {
				onUpdate.execute(tile);
			}
		}
	}

	private Bbox worldToPan(Bbox bounds) {
		Matrix t = mapLayer.getMapModel().getMapView().getWorldToPanTransformation();
		return bounds.transform(t);
	}

	private void addTiles(List<org.geomajas.layer.tile.RasterTile> images) {
		Matrix t = mapLayer.getMapModel().getMapView().getWorldToPanTranslation();
		Bbox cacheBounds = null;
		// flag and reference tile to realign the grid when new tiles come in (transformation shift!)
		boolean newTiles = false;
		MapTile referenceTile = null;
		for (org.geomajas.layer.tile.RasterTile image : images) {
			TileCode code = image.getCode().clone();
			if (!tiles.containsKey(code)) {
				Bbox panBounds = new Bbox(image.getBounds());
				panBounds.translate(Math.round(t.getDx()), Math.round(t.getDy()));
				if (cacheBounds == null) {
					cacheBounds = panBounds;
				} else {
					cacheBounds = cacheBounds.union(panBounds);
				}
				MapTile tile = new MapTile(code, panBounds, image.getUrl(), this);
				tiles.put(code, tile);
				newTiles = true;
				referenceTile = tile;
			}
		}
		// This realigns the grid of tiles based on their code
		if (newTiles) {
			for (MapTile tile : tiles.values()) {
				if (!tile.getCode().equals(referenceTile.getCode())) {
					Bbox aligned = new Bbox(referenceTile.getBounds());
					aligned.setX(referenceTile.getBounds().getX()
							+ (tile.getCode().getX() - referenceTile.getCode().getX()) * aligned.getWidth());
					if (tile.getCode().getY() != referenceTile.getCode().getY()) {
						aligned.setY(referenceTile.getBounds().getY() + getOrientedJDiff(referenceTile, tile)
								* aligned.getHeight());
					}
					tile.setBounds(aligned);
				}
			}
		}
		deferred = null;
	}

	/**
	 * Returns the difference in j index, taking orientation of y-axis into account. Some layers (WMS 1.8.0) have
	 * different j-index orientation than screen coordinates (lower-left = (0,0) vs upper-left = (0,0)).
	 * 
	 * @param tile1 tile
	 * @param tile2 tile
	 * @return +/-(j2-j1)
	 */
	private int getOrientedJDiff(MapTile tile1, MapTile tile2) {
		double dy = tile2.getBounds().getY() - tile1.getBounds().getY();
		int dj = tile2.getCode().getY() - tile1.getCode().getY();
		return (dj * dy) > 0 ? dj : -dj;
	}

	/**
	 * Raster layer callback for fetching tiles.
	 */
	private final class RasterCallBack implements CommandCallback {

		private TileFunction<MapTile> callback;

		private Bbox bounds;

		private RasterCallBack(Bbox bounds, TileFunction<MapTile> callback) {
			this.callback = callback;
			this.bounds = bounds;
		}

		public void execute(CommandResponse response) {
			GetRasterTilesResponse r = (GetRasterTilesResponse) response;
			addTiles(r.getRasterData());
			for (MapTile tile : tiles.values()) {
				if (bounds.intersects(tile.getBounds())) {
					callback.execute(tile);
				}
			}
		}

	}
}
