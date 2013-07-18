package org.geomajas.layer;

import java.util.List;

import org.geomajas.global.GeomajasException;

/**
 * Services that retrieves a combined {@link Layer} from multiple layers.
 * 
 * @author Alexander Erben
 * @author Lyn Goltz
 */
public interface AggregationLayerService {
	
	/**
	 * Aggregate a list of layers into one aggregated layer. The given list of layers is aggregated as far as possible.
	 * If the service encounters an unsupported layer
	 * 
	 * @param layers the layers to aggregate. Never {@link null} and never empty.
	 * 
	 * @return An aggregated {@link Layer} instance.
	 * @throws GeomajasException
	 */
	Layer<?> aggregate(List<Layer<?>> layers)
			throws GeomajasException;
	
	/**
	 * Decide whether a list of layers can be aggregated by this service.
	 * 
	 * @param layers the layers to judge. May be null or empty.
	 * 
	 * @return boolean true if the list of layers can be aggregated by this service, false if not.
	 * @throws GeomajasException
	 */
	boolean canHandle(List<Layer<?>> layers);

}