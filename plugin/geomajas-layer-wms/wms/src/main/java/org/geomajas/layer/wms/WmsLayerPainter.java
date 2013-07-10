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
import org.geomajas.global.ExceptionCode;
import org.geomajas.global.GeomajasException;
import org.geomajas.layer.RasterLayer;
import org.geomajas.layer.tile.RasterTile;
import org.geomajas.layer.tile.TileCode;
import org.geomajas.plugin.caching.service.CacheManagerService;
import org.geomajas.security.SecurityContext;
import org.geomajas.service.DispatcherUrlService;
import org.geomajas.service.DtoConverterService;
import org.geomajas.service.GeoService;
import org.opengis.geometry.MismatchedDimensionException;
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
		Envelope layerBounds = bounds;
		double layerScale = scale;
		CrsTransform layerToMap = null;
		boolean needTransform = !crs.equals(targetCrs);
		Bbox bbox = layer.getLayerInfo().getMaxExtent();

		try {
			// We don't necessarily need to split into same CRS and different
			// CRS cases, the latter implementation uses
			// identity transform if crs's are equal for map and layer but might
			// introduce bugs in rounding and/or
			// conversions.
			if (needTransform) {
				layerToMap = geoService.getCrsTransform(crs, targetCrs);
				CrsTransform mapToLayer = geoService.getCrsTransform(targetCrs, crs);

				// Translate the map coordinates to layer coordinates, assumes
				// equal x-y orientation
				layerBounds = geoService.transform(bounds, mapToLayer);
				layerScale = bounds.getWidth() * scale / layerBounds.getWidth();
			}
		} catch (MismatchedDimensionException e) {
			throw new GeomajasException(e, ExceptionCode.RENDER_DIMENSION_MISMATCH);
		}
		
		layerBounds = clipBounds(bbox, layerBounds);
		if (layerBounds.isNull()) {
			return new ArrayList<RasterTile>(0);
		}

		// Grid is in layer coordinate space!
		Resolution bestResolution = getResolutionForScale(layer,layerScale);
		RasterGrid grid = getRasterGrid(bbox,layerBounds, bestResolution.getTileWidth(), bestResolution.getTileHeight());

		// We calculate the first tile's screen box with this assumption
		List<RasterTile> result = new ArrayList<RasterTile>();
		for (int i = grid.getXmin(); i < grid.getXmax(); i++) {
			for (int j = grid.getYmin(); j < grid.getYmax(); j++) {
				double x = grid.getLowerLeft().x + (i - grid.getXmin()) * grid.getTileWidth();
				double y = grid.getLowerLeft().y + (j - grid.getYmin()) * grid.getTileHeight();
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

				RasterTile image = new RasterTile(screenBox, layer.getId() + "." + bestResolution.getLevel() + "." + i
						+ "," + j);

				image.setCode(new TileCode(bestResolution.getLevel(), i, j));
				String url = formatUrl(layer, bestResolution.getTileWidthPx(), bestResolution.getTileHeightPx(), layerBox);
				image.setUrl(url);
				result.add(image);
			}
		}

		return result;
	}

	RasterGrid getRasterGrid(Bbox bbox, Envelope bounds, double width, double height) {
		int ymin = (int) Math.floor((bounds.getMinY() - bbox.getY()) / height);
		int ymax = (int) Math.ceil((bounds.getMaxY() - bbox.getY()) / height);
		int xmin = (int) Math.floor((bounds.getMinX() - bbox.getX()) / width);
		int xmax = (int) Math.ceil((bounds.getMaxX() - bbox.getX()) / width);

		Coordinate lowerLeft = new Coordinate(bbox.getX() + xmin * width, bbox.getY() + ymin * height);
		return new RasterGrid(lowerLeft, xmin, ymin, xmax, ymax, width, height);
	}

	private Envelope clipBounds(Bbox rasterInfoMaxExtent ,Envelope bounds) {
		Envelope maxExtent = converterService.toInternal(rasterInfoMaxExtent);
		return bounds.intersection(maxExtent);
	}

	private String formatUrl(WmsLayer layer,int width, int height, Bbox box) throws GeomajasException {
		StringBuilder url = formatBaseUrl(layer, width, height, box);
		url.append("&request=GetMap");
		String token = securityContext.getToken();
		if (null != token) {
			url.append("&userToken=");
			url.append(token);
		}
		return url.toString();
	}

	Resolution getResolutionForScale(WmsLayer layer, double scale) {
		List<Resolution> resolutions = layer.getResolutions();
		if (null == resolutions || resolutions.size() == 0) {
			return calculateBestQuadTreeResolution(layer,scale);
		} else {
			double screenResolution = 1.0 / scale;
			if (screenResolution >= resolutions.get(0).getResolution()) {
				return resolutions.get(0);
			} else if (screenResolution <= resolutions.get(resolutions.size() - 1).getResolution()) {
				return resolutions.get(resolutions.size() - 1);
			} else {
				for (int i = 0; i < resolutions.size() - 1; i++) {
					Resolution upper = resolutions.get(i);
					Resolution lower = resolutions.get(i + 1);
					if (screenResolution <= upper.getResolution() && screenResolution >= lower.getResolution()) {
						if ((upper.getResolution() - screenResolution) > 2 * (screenResolution - lower.getResolution())) {
							return lower;
						} else {
							return upper;
						}
					}
				}
			}
		}
		// should not occur !!!!
		return resolutions.get(resolutions.size() - 1);
	}

	private Resolution calculateBestQuadTreeResolution(WmsLayer layer, double scale) {
		RasterLayerInfo layerInfo = layer.getLayerInfo();
		double screenResolution = 1.0 / scale;
		// based on quad tree created by subdividing the maximum extent
		Bbox bbox = layerInfo.getMaxExtent();
		double maxWidth = bbox.getWidth();
		double maxHeight = bbox.getHeight();

		int tileWidth = layerInfo.getTileWidth();
		int tileHeight = layerInfo.getTileHeight();

		Resolution upper = new Resolution(Math.max(maxWidth / tileWidth, maxHeight / tileHeight), 0, tileWidth,
				tileHeight);
		if (screenResolution >= upper.getResolution()) {
			return upper;
		} else {
			int level = 0;
			Resolution lower = upper; // set value to avoid possible NPE
			while (screenResolution < upper.getResolution()) {
				lower = upper;
				level++;
				double width = maxWidth / Math.pow(2, level);
				double height = maxHeight / Math.pow(2, level);
				upper = new Resolution(Math.max(width / tileWidth, height / tileHeight), level, tileWidth, tileHeight);
			}
			if ((screenResolution - upper.getResolution()) > 2 * (lower.getResolution() - screenResolution)) {
				return lower;
			} else {
				return upper;
			}
		}
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

}
