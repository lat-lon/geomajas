package org.geomajas.layer;

import java.util.List;

import org.geomajas.global.GeomajasException;
import org.geomajas.layer.tile.RasterTile;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Services that retrieves combined Tiles from multiple layers.
 * 
 * @author Alexander Erben
 * @author Lyn Goltz
 */
public interface AggregationLayerService {
	
	List<Layer<?>> aggregate(List<Layer<?>> layers)
			throws GeomajasException;

}