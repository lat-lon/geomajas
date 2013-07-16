package org.geomajas.layer;

import java.util.List;

import org.geomajas.global.GeomajasException;

/**
 * Services that retrieves combined Tiles from multiple layers.
 * 
 * @author Alexander Erben
 * @author Lyn Goltz
 */
public interface AggregationLayerService {
	
	Layer<?> aggregate(List<Layer<?>> layers)
			throws GeomajasException;

}