package org.geomajas.layer.wms;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import org.geomajas.configuration.Parameter;
import org.geomajas.configuration.RasterLayerInfo;
import org.geomajas.geometry.Bbox;
import org.geomajas.geometry.CrsTransform;
import org.geomajas.global.GeomajasException;
import org.geomajas.layer.tile.RasterTile;
import org.geomajas.layer.tile.TileCode;
import org.geomajas.plugin.caching.service.CacheManagerService;
import org.geomajas.security.SecurityContext;
import org.geomajas.service.DispatcherUrlService;
import org.geomajas.service.DtoConverterService;
import org.geomajas.service.GeoService;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

@Component
public class WmsLayerPainter {

	@Autowired
	private GeoService geoService;

	@Autowired
	private DtoConverterService converterService;

	@Autowired(required = false)
	private DispatcherUrlService dispatcherUrlService;

	@Autowired(required = false)
	private CacheManagerService cacheManagerService;

	@Autowired
	private SecurityContext securityContext;

	/**
	 * Paints the specified bounds optimized for the specified scale in pixel/unit.
	 * 
	 * @param targetCrs
	 *            Coordinate reference system used for bounds
	 * @param bounds
	 *            bounds to request images for
	 * @param scale
	 *            scale or zoom level (unit?)
	 * @return a list of raster images that covers the bounds
	 * @throws GeomajasException
	 *             oops
	 */
	public List<RasterTile> paint(WmsLayer layer, CoordinateReferenceSystem targetCrs, Envelope bounds, double scale)
			throws GeomajasException {
		CoordinateReferenceSystem crs = layer.getCrs();
		boolean needTransform = !crs.equals(targetCrs);

		PainterParameters painterParameters = calclatePainterParams(targetCrs, bounds, scale, crs, needTransform);

		Bbox bbox = layer.getLayerInfo().getMaxExtent();
		Envelope clippedLayerBounds = clipBounds(bbox, painterParameters.layerBounds);
		if (clippedLayerBounds.isNull()) {
			return new ArrayList<RasterTile>(0);
		}

		// Grid is in layer coordinate space!
		Resolution bestResolution = WmsLayerUtils.getResolutionForScale(layer.getResolutions(), layer.getLayerInfo(),
				painterParameters.layerScale);
		RasterGrid grid = getRasterGrid(bbox, clippedLayerBounds, bestResolution.getTileWidth(),
				bestResolution.getTileHeight());

		// We calculate the first tile's screen box with this assumption
		List<RasterTile> result = new ArrayList<RasterTile>();
		for (int i = grid.getXmin(); i < grid.getXmax(); i++) {
			for (int j = grid.getYmin(); j < grid.getYmax(); j++) {
				RasterTile image = createRasterTile(layer, scale, painterParameters.layerToMap, needTransform,
						bestResolution, grid, i, j);
				result.add(image);
			}
		}

		return result;
	}

	public RasterTile paint(AggregatedWmsLayer layer, CoordinateReferenceSystem targetCrs, Envelope bounds, double scale)
			throws GeomajasException {
		CoordinateReferenceSystem crs = layer.getCrs();
		boolean needTransform = !crs.equals(targetCrs);

		PainterParameters painterParameters = calclatePainterParams(targetCrs, bounds, scale, crs, needTransform);

		Bbox bbox = layer.getLayerInfo().getMaxExtent();
		Envelope clippedLayerBounds = clipBounds(bbox, painterParameters.layerBounds);
		if (clippedLayerBounds.isNull()) {
			return null;
		}

		// return createRasterTile(layer, scale, painterParameters.layerToMap, needTransform, bestResolution, grid, 0,
		// 0);
		return null;
	}

	private PainterParameters calclatePainterParams(CoordinateReferenceSystem targetCrs, Envelope bounds, double scale,
			CoordinateReferenceSystem crs, boolean needTransform) throws GeomajasException {
		PainterParameters painterParameters;
		if (needTransform) {
			painterParameters = calculateTransformedPainterParams(targetCrs, bounds, scale, crs);
		} else {
			painterParameters = new PainterParameters(bounds, scale, null);
		}
		return painterParameters;
	}

	private PainterParameters calculateTransformedPainterParams(CoordinateReferenceSystem targetCrs, Envelope bounds,
			double scale, CoordinateReferenceSystem crs) throws GeomajasException {
		PainterParameters painterParameters;
		// We don't necessarily need to split into same CRS and different
		// CRS cases, the latter implementation uses
		// identity transform if crs's are equal for map and layer but might
		// introduce bugs in rounding and/or
		// conversions.
		CrsTransform layerToMap = geoService.getCrsTransform(crs, targetCrs);
		Envelope layerBounds = calulateLayerBounds(targetCrs, bounds, crs);
		double layerScale = calculateScale(bounds, scale, layerBounds);
		painterParameters = new PainterParameters(layerBounds, layerScale, layerToMap);
		return painterParameters;
	}

	private double calculateScale(Envelope bounds, double scale, Envelope layerBounds) {
		return bounds.getWidth() * scale / layerBounds.getWidth();
	}

	private RasterTile createRasterTile(WmsLayer layer, double scale, CrsTransform layerToMap, boolean needTransform,
			Resolution bestResolution, RasterGrid grid, int xPosition, int yPosition) throws GeomajasException {
		double x = grid.getLowerLeft().x + (xPosition - grid.getXmin()) * grid.getTileWidth();
		double y = grid.getLowerLeft().y + (yPosition - grid.getYmin()) * grid.getTileHeight();
		// layer coordinates
		Bbox worldBox;
		Bbox layerBox;
		if (needTransform) {
			layerBox = new Bbox(x, y, grid.getTileWidth(), grid.getTileHeight());
			// Transforming back to map coordinates will only result in
			// a proper grid if the transformation
			// is nearly affine
			worldBox = geoService.transform(layerBox, layerToMap);
		} else {
			worldBox = new Bbox(x, y, grid.getTileWidth(), grid.getTileHeight());
			layerBox = worldBox;
		}
		// Rounding to avoid white space between raster tiles lower-left
		// becomes upper-left in inverted y-space
		Bbox screenBox = new Bbox(Math.round(scale * worldBox.getX()), -Math.round(scale * worldBox.getMaxY()),
				Math.round(scale * worldBox.getMaxX()) - Math.round(scale * worldBox.getX()), Math.round(scale
						* worldBox.getMaxY())
						- Math.round(scale * worldBox.getY()));

		RasterTile image = new RasterTile(screenBox, layer.getId() + "." + bestResolution.getLevel() + "." + xPosition
				+ "," + yPosition);

		image.setCode(new TileCode(bestResolution.getLevel(), xPosition, yPosition));
		String url = buildGetMapUrl(layer, bestResolution.getTileWidthPx(), bestResolution.getTileHeightPx(), layerBox);
		image.setUrl(url);
		return image;
	}

	private Envelope calulateLayerBounds(CoordinateReferenceSystem targetCrs, Envelope bounds,
			CoordinateReferenceSystem crs) throws GeomajasException {
		Envelope layerBounds;
		CrsTransform mapToLayer = geoService.getCrsTransform(targetCrs, crs);

		// Translate the map coordinates to layer coordinates, assumes
		// equal x-y orientation
		layerBounds = geoService.transform(bounds, mapToLayer);
		return layerBounds;
	}

	RasterGrid getRasterGrid(Bbox bbox, Envelope bounds, double width, double height) {
		int ymin = (int) Math.floor((bounds.getMinY() - bbox.getY()) / height);
		int ymax = (int) Math.ceil((bounds.getMaxY() - bbox.getY()) / height);
		int xmin = (int) Math.floor((bounds.getMinX() - bbox.getX()) / width);
		int xmax = (int) Math.ceil((bounds.getMaxX() - bbox.getX()) / width);

		Coordinate lowerLeft = new Coordinate(bbox.getX() + xmin * width, bbox.getY() + ymin * height);
		return new RasterGrid(lowerLeft, xmin, ymin, xmax, ymax, width, height);
	}
	
	private Envelope clipBounds(Bbox rasterInfoMaxExtent, Envelope bounds) {
		Envelope maxExtent = converterService.toInternal(rasterInfoMaxExtent);
		return bounds.intersection(maxExtent);
	}

	String buildGetMapUrl(WmsLayer layer, int width, int height, Bbox box) throws GeomajasException {
		StringBuilder url = formatBaseUrl(layer, width, height, box);
		url.append("&request=GetMap");
		String token = securityContext.getToken();
		if (null != token) {
			url.append("&userToken=");
			url.append(token);
		}
		return url.toString();
	}

	private String getWmsTargetUrl(WmsLayer layer) {
		String id = layer.getId();
		if (layer.isUseProxy() || null != layer.getAuthentication() || layer.isUseCache()) {
			if (null != dispatcherUrlService) {
				String url = dispatcherUrlService.getDispatcherUrl();
				if (!url.endsWith("/")) {
					url += "/";
				}
				return url + "wms/" + id + "/";
			} else {
				return "./d/wms/" + id + "/";
			}
		} else {
			return layer.getBaseWmsUrl();
		}
	}

	/**
	 * Build the base part of the url (doesn't change for getMap or getFeatureInfo requests).
	 * 
	 * @param targetUrl
	 *            base url
	 * @param width
	 *            image width
	 * @param height
	 *            image height
	 * @param box
	 *            bounding box
	 * @return base WMS url
	 * @throws GeomajasException
	 *             missing parameter
	 */
	StringBuilder formatBaseUrl(WmsLayer layer, int width, int height, Bbox box) throws GeomajasException {
		try {
			String targetUrl = getWmsTargetUrl(layer);
			StringBuilder url = new StringBuilder(targetUrl);
			int pos = url.lastIndexOf("?");
			if (pos > 0) {
				url.append("&SERVICE=WMS");
			} else {
				url.append("?SERVICE=WMS");
			}
			String layers = layer.getId();
			RasterLayerInfo layerInfo = layer.getLayerInfo();
			String format = layer.getFormat();
			if (layerInfo.getDataSourceName() != null) {
				layers = layerInfo.getDataSourceName();
			}
			url.append("&layers=");
			url.append(URLEncoder.encode(layers, "UTF8"));
			url.append("&WIDTH=");
			url.append(Integer.toString(width));
			url.append("&HEIGHT=");
			url.append(Integer.toString(height));
			DecimalFormat decimalFormat = new DecimalFormat(); // create new as
																// this is not
																// thread safe
			decimalFormat.setDecimalSeparatorAlwaysShown(false);
			decimalFormat.setGroupingUsed(false);
			decimalFormat.setMinimumFractionDigits(0);
			decimalFormat.setMaximumFractionDigits(100);
			DecimalFormatSymbols symbols = new DecimalFormatSymbols();
			symbols.setDecimalSeparator('.');
			decimalFormat.setDecimalFormatSymbols(symbols);

			url.append("&bbox=");
			url.append(decimalFormat.format(box.getX()));
			url.append(",");
			url.append(decimalFormat.format(box.getY()));
			url.append(",");
			url.append(decimalFormat.format(box.getMaxX()));
			url.append(",");
			url.append(decimalFormat.format(box.getMaxY()));
			url.append("&format=");
			url.append(format);
			url.append("&version=");
			String version = layer.getVersion();
			url.append(version);
			if ("1.3.0".equals(version)) {
				url.append("&crs=");
			} else {
				url.append("&srs=");
			}
			url.append(URLEncoder.encode(layerInfo.getCrs(), "UTF8"));
			url.append("&styles=");
			url.append(layer.getStyles());
			List<Parameter> parameters = layer.getParameters();
			if (null != parameters) {
				for (Parameter p : parameters) {
					url.append("&");
					url.append(URLEncoder.encode(p.getName(), "UTF8"));
					url.append("=");
					url.append(URLEncoder.encode(p.getValue(), "UTF8"));
				}
			}
			return url;
		} catch (UnsupportedEncodingException uee) {
			throw new IllegalStateException("Cannot find UTF8 encoding?", uee);
		}
	}

	private class PainterParameters {

		Envelope layerBounds;

		double layerScale;

		CrsTransform layerToMap;

		PainterParameters(Envelope layerBounds, double layerScale, CrsTransform layerToMap) {
			this.layerScale = layerScale;
			this.layerToMap = layerToMap;
			this.layerBounds = layerBounds;
		}

	}

}
