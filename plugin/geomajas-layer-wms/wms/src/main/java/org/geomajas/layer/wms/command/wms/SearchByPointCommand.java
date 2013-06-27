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
package org.geomajas.layer.wms.command.wms;

import java.util.List;
import java.util.Map.Entry;

import org.geomajas.annotation.Api;
import org.geomajas.command.Command;
import org.geomajas.geometry.Bbox;
import org.geomajas.geometry.Crs;
import org.geomajas.global.ExceptionCode;
import org.geomajas.global.GeomajasException;
import org.geomajas.layer.Layer;
import org.geomajas.layer.LayerException;
import org.geomajas.layer.LayerService;
import org.geomajas.layer.feature.Feature;
import org.geomajas.layer.wms.LayerFeatureInfoAsGmlSupport;
import org.geomajas.layer.wms.LayerFeatureInfoAsHtmlSupport;
import org.geomajas.layer.wms.LayerFeatureInfoSupport;
import org.geomajas.layer.wms.command.dto.SearchByPointRequest;
import org.geomajas.layer.wms.command.dto.SearchByPointResponse;
import org.geomajas.security.SecurityContext;
import org.geomajas.service.ConfigurationService;
import org.geomajas.service.GeoService;
import org.opengis.geometry.MismatchedDimensionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * <p>
 * Execute a search for features by location/point. You can also specify a tolerance (in pixels) for the search. This
 * tolerance may or may not be supported (it is not for WMS GetFeatureInfo).
 * </p>
 * <p>
 * It is required that at least one layer ID is given to search in. If multiple layers are given, an extra parameter
 * comes into play: searchType. This searchType specifies whether to search features in all given layers, to start
 * searching from the first layer, and stop once features are found (can be in the first layer, or in the second, ...).
 * </p>
 * <p>
 * It will go over all given layers (provided they're layers that support feature info by implementing
 * {@link LayerFeatureInfoSupport}), and fetch the features, using the location and the query type. The resulting list
 * of features is added to the command result so it can be send back to the client.
 * </p>
 * <p>
 * This class is based on the {@link org.geomajas.command.feature.SearchByLocationCommand} class.
 * </p>
 * 
 * @author Oliver May
 * @since 1.8.0
 */
@Api
@Component()
public class SearchByPointCommand implements Command<SearchByPointRequest, SearchByPointResponse> {

	private static final String TEXT_HTML = "text/html";

	private final Logger log = LoggerFactory.getLogger(SearchByPointCommand.class);

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private GeoService geoService;

	@Autowired
	@Qualifier("layer.LayerService")
	private LayerService layerService;

	@Autowired
	private SecurityContext securityContext;

	public void execute(SearchByPointRequest request, SearchByPointResponse response) throws Exception {
		if (null == request.getLayerMapping()) {
			throw new GeomajasException(ExceptionCode.PARAMETER_MISSING, "serverLayerMapping");
		}
		String crsCode = request.getCrs();
		if (null == crsCode) {
			throw new GeomajasException(ExceptionCode.PARAMETER_MISSING, "crs");
		}
		org.geomajas.geometry.Coordinate requestLocation = request.getLocation();
		if (null == requestLocation) {
			throw new GeomajasException(ExceptionCode.PARAMETER_MISSING, "location");
		}
		Bbox mapBounds = request.getMapBounds();
		if (null == mapBounds) {
			throw new GeomajasException(ExceptionCode.PARAMETER_MISSING, "mapBounds");
		}

		Coordinate coordinate = new Coordinate(requestLocation.getX(), requestLocation.getY());
		Crs crs = geoService.getCrs2(request.getCrs());
		boolean searchFirstLayerOnly;
		switch (request.getSearchType()) {
			case SearchByPointRequest.SEARCH_FIRST_LAYER:
				searchFirstLayerOnly = true;
				break;
			case SearchByPointRequest.SEARCH_ALL_LAYERS:
				searchFirstLayerOnly = false;
				break;
			default:
				throw new IllegalArgumentException("Invalid value for searchType");
		}

		log.debug("search by location {}", coordinate);

		if (request.getLayerMapping().size() > 0) {
			for (Entry<String, String> entry : request.getLayerMapping().entrySet()) {
				String serverLayerId = entry.getValue();
				String clientLayerId = entry.getKey();

				if (securityContext.isLayerVisible(serverLayerId)) {

					Layer<?> layer = configurationService.getLayer(serverLayerId);
					boolean match;
					if (TEXT_HTML.equals(request.getFeatureInfoFormat())) {
						match = addFeatureInfoHtmlLayerIfSupported(request, response, mapBounds, coordinate, crs,
								clientLayerId, layer);
					} else {
						match = addFeatureInfoGmlLayerIfSupported(request, response, mapBounds, coordinate, crs,
								clientLayerId, layer);
					}
					if (match && searchFirstLayerOnly) {
						break;
					}
				}
			}
		}
	}

	private boolean addFeatureInfoHtmlLayerIfSupported(SearchByPointRequest request, SearchByPointResponse response,
			Bbox mapBounds, Coordinate coordinate, Crs crs, String clientLayerId, Layer<?> layer)
			throws LayerException, GeomajasException {
		if (layer instanceof LayerFeatureInfoAsHtmlSupport
				&& ((LayerFeatureInfoAsHtmlSupport) layer).isEnableFeatureInfoAsHtmlSupport()) {
			Crs layerCrs = layerService.getCrs(layer);
			double layerScale = calculateLayerScale(crs, layerCrs, mapBounds, request.getScale());
			Coordinate layerCoordinate = geoService.transform(coordinate, crs, layerCrs);
			String html = ((LayerFeatureInfoAsHtmlSupport) layer).getFeatureInfoAsHtml(layerCoordinate, layerScale,
					request.getPixelTolerance());
			if (html != null && !"".equals(html)) {
				response.addLayer(clientLayerId, html);
				return true;
			}
		}
		return false;
	}

	private boolean addFeatureInfoGmlLayerIfSupported(SearchByPointRequest request, SearchByPointResponse response,
			Bbox mapBounds, Coordinate coordinate, Crs crs, String clientLayerId, Layer<?> layer)
			throws LayerException, GeomajasException {
		if (checkIfGmlFeatureInfoIsSupported(layer)) {
			Crs layerCrs = layerService.getCrs(layer);
			double layerScale = calculateLayerScale(crs, layerCrs, mapBounds, request.getScale());
			Coordinate layerCoordinate = geoService.transform(coordinate, crs, layerCrs);
			List<Feature> features = retrieveFeaturesAsGml(request, layer, layerScale, layerCoordinate);
			if (features != null && features.size() > 0) {
				response.addLayer(clientLayerId, features);
				return true;
			}
		}
		return false;
	}

	private List<Feature> retrieveFeaturesAsGml(SearchByPointRequest request, Layer<?> layer, double layerScale,
			Coordinate layerCoordinate) throws LayerException {
		if (layer instanceof LayerFeatureInfoSupport) {
			return ((LayerFeatureInfoSupport) layer).getFeaturesByLocation(layerCoordinate, layerScale,
					request.getPixelTolerance());
		} else {
			return ((LayerFeatureInfoAsGmlSupport) layer).getFeatureInfoAsGml(layerCoordinate, layerScale,
					request.getPixelTolerance());
		}
	}

	private boolean checkIfGmlFeatureInfoIsSupported(Layer<?> layer) {
		// Check if the underlying layer implements one of the matching interfaces and has GFI enabled for at least one
		// of them
		boolean result = false;
		if (layer instanceof LayerFeatureInfoAsGmlSupport) {
			result = ((LayerFeatureInfoAsGmlSupport) layer).isEnableFeatureInfoAsGmlSupport();
		}
		if (layer instanceof LayerFeatureInfoSupport) {
			result = result || ((LayerFeatureInfoSupport) layer).isEnableFeatureInfoSupport();
		}
		return result;
	}

	public SearchByPointResponse getEmptyCommandResponse() {
		return new SearchByPointResponse();
	}

	private double calculateLayerScale(Crs mapCrs, Crs layerCrs, Bbox mapBounds, double mapScale)
			throws GeomajasException {
		double layerScale = mapScale;

		try {
			// We don't necessarily need to split into same CRS and different CRS cases, the latter implementation uses
			// identity transform if CRSs are equal for map and layer but might introduce bugs in rounding and/or
			// conversions.
			if (!mapCrs.equals(layerCrs)) {

				// Translate the map coordinates to layer coordinates, assumes equal x-y orientation
				Bbox layerBounds = geoService.transform(mapBounds, mapCrs, layerCrs);
				layerScale = mapBounds.getWidth() * mapScale / layerBounds.getWidth();
			}
		} catch (MismatchedDimensionException e) {
			throw new GeomajasException(e, ExceptionCode.RENDER_DIMENSION_MISMATCH);
		}
		return layerScale;
	}
}
