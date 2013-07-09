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
package org.geomajas.internal.layer;

import java.util.List;




/**
 * A single layer representing all layers on the map.
 * 
 * @author Oliver May
 * @param <T> @link {@link org.geomajas.layer.tile.RasterTile} of @link {@link org.geomajas.layer.tile.VectorTile}
 *
 */
public class MapLayer<T> {
	
	private List<T> tiles;

	public void setTiles(List<T> tiles) {
		this.tiles = tiles;
	}

	public List<T> getTiles() {
		return tiles;
	}
	
	
}
