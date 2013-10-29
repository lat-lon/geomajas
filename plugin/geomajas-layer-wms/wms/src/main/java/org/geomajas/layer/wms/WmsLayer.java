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
package org.geomajas.layer.wms;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;

import org.geomajas.annotation.Api;
import org.geomajas.configuration.Parameter;
import org.geomajas.configuration.RasterLayerInfo;
import org.geomajas.configuration.client.ScaleInfo;
import org.geomajas.geometry.Bbox;
import org.geomajas.geometry.Crs;
import org.geomajas.global.ExceptionCode;
import org.geomajas.global.GeomajasException;
import org.geomajas.layer.LayerException;
import org.geomajas.layer.LayerLegendImageSupport;
import org.geomajas.layer.RasterLayer;
import org.geomajas.layer.common.proxy.LayerAuthentication;
import org.geomajas.layer.common.proxy.LayerAuthenticationMethod;
import org.geomajas.layer.common.proxy.LayerHttpService;
import org.geomajas.layer.feature.Attribute;
import org.geomajas.layer.feature.Feature;
import org.geomajas.layer.feature.attribute.StringAttribute;
import org.geomajas.layer.tile.RasterTile;
import org.geomajas.layer.wms.feature.NumberOfFeaturesInEnvelopeRetriever;
import org.geomajas.plugin.caching.service.CacheManagerService;
import org.geomajas.security.SecurityContext;
import org.geomajas.service.DispatcherUrlService;
import org.geomajas.service.DtoConverterService;
import org.geomajas.service.GeoService;
import org.geotools.GML;
import org.geotools.GML.Version;
import org.geotools.data.ows.HTTPClient;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.SimpleHttpClient;
import org.geotools.data.ows.StyleImpl;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.WebMapServer;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.ows.ServiceException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/**
 * <p>
 * Layer model for accessing raster data from WMS servers. It has support for most WMS versions: up to 1.3.0. When using
 * this layer, note that the following fields are required:
 * <ul>
 * <li><b>baseWmsUrl</b>: The base URL to the WMS server.</li>
 * <li><b>format</b>: The format for the returned images.</li>
 * <li><b>version</b>: The version of WMS to use.</li>
 * <li><b>styles</b>: The styles to use when rendering the WMS images.</li>
 * <li><b>useProxy</b>: Set to true to use a proxy for rendering the WMS, hiding the URL from the client. This
 * automatically happens when setting the authentication object.</li>
 * </ul>
 * There always is the option of adding additional parameters to the WMS GetMap requests, by filling the
 * <code>parameters</code> list. Such parameters could include optional WMS GetMap parameters, such as "transparency",
 * but also "user" and "password".
 * </p>
 * <p>
 * This layer also supports BASIC and DIGEST authentication. To use this functionality, set the
 * <code>authentication</code> field.
 * </p>
 * 
 * @author Jan De Moerloose
 * @author Pieter De Graef
 * @author Oliver May
 * @author Joachim Van der Auwera
 * @since 1.7.1
 */
@Api
public class WmsLayer implements RasterLayer, LayerLegendImageSupport, LayerFeatureInfoSupport,
		LayerFeatureInfoAsHtmlSupport, LayerFeatureInfoAsGmlSupport {

	private static final String GFI_UNAVAILABLE_MSG = "GetFeatureInfo-support not available on this layer";

	private static final int DEFAULT_CAPABILITIES_CONNECTION_TIMEOUT_MS = 10000;

	private static final int EMPTY_LEGEND_WIDTH = -1;

	private static final int EMPTY_LEGEND_HEIGHT = -1;

	private static final String EMPTY_LEGENDURL = "";

	private static final boolean IS_GML_REQUEST = false;

	private static final boolean IS_HTML_REQUEST = true;

	private final Logger log = LoggerFactory.getLogger(WmsLayer.class);

	private final List<Resolution> resolutions = new ArrayList<Resolution>();

	// @NotNull this seems to cause problems, it is tested in @PostConstruct
	// anyway
	private String baseWmsUrl;

	private String format = "image/png";

	private String version = "1.1.1";

	private String styles = EMPTY_LEGENDURL;

	private List<Parameter> parameters;

	private boolean enableFeatureInfoSupport;

	private boolean enableFeatureInfoSupportAsHtml;

	private RasterLayerInfo layerInfo;

	private Crs crs;

	private String id;

	/**
	 * @deprecated use layerAuthentication
	 */
	@Deprecated
	private WmsAuthentication authentication;

	private LayerAuthentication layerAuthentication;

	private boolean useProxy;

	private boolean useCache;

	@Autowired
	private GeoService geoService;

	@Autowired
	private LayerHttpService httpService;

	@Autowired
	private DtoConverterService converterService;

	@Autowired(required = false)
	private DispatcherUrlService dispatcherUrlService;

	@Autowired(required = false)
	private CacheManagerService cacheManagerService;

	@Autowired
	private SecurityContext securityContext;

	@Autowired
	private WmsLayerPainter painter;

	@Autowired(required = false)
	private NumberOfFeaturesInEnvelopeRetriever numberOfFeaturesInEnvelopeRetriever;

	private boolean enableFeatureInfoSupportAsGml;

	private String legendImageUrl;

	private int legendImageHeight;

	private int legendImageWidth;

	/**
	 * Return the layers identifier.
	 * 
	 * @return layer id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the layer identifier.
	 * 
	 * @param id
	 *            layer id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Return the layer info object.
	 * 
	 * @since 1.7.1
	 */
	public RasterLayerInfo getLayerInfo() {
		return layerInfo;
	}

	/**
	 * Get coordinate reference system for this layer.
	 * 
	 * @return Coordinate reference system for this layer.
	 * @deprecated use {@link org.geomajas.layer.LayerService#getCrs(org.geomajas.layer.Layer)}
	 */
	@Deprecated
	public CoordinateReferenceSystem getCrs() {
		return crs;
	}

	@PostConstruct
	protected void postConstruct() throws GeomajasException {
		if (null == baseWmsUrl) {
			throw new GeomajasException(ExceptionCode.PARAMETER_MISSING, "baseWmsUrl");
		}

		crs = geoService.getCrs2(getLayerInfo().getCrs());

		// calculate resolutions
		List<ScaleInfo> scales = layerInfo.getZoomLevels();
		if (scales != null) {
			List<Double> r = new ArrayList<Double>();
			for (ScaleInfo scale : scales) {
				r.add(1. / scale.getPixelPerUnit());
			}

			// sort in decreasing order !!!
			Collections.sort(r);
			Collections.reverse(r);

			int level = 0;
			for (double resolution : r) {
				resolutions
						.add(new Resolution(resolution, level++, layerInfo.getTileWidth(), layerInfo.getTileHeight()));
			}
		}
		retrieveAndSetLegendImageParameters(baseWmsUrl);
	}

	/**
	 * Set the layer configuration.
	 * 
	 * @param layerInfo
	 *            layer information
	 * @throws LayerException
	 *             oops
	 * @since 1.7.1
	 */
	@Api
	public void setLayerInfo(RasterLayerInfo layerInfo) throws LayerException {
		this.layerInfo = layerInfo;
	}

	/** {@inheritDoc}. */
	public List<Feature> getFeaturesByLocation(Coordinate layerCoordinate, double layerScale, int pixelTolerance)
			throws LayerException {
		if (!isEnableFeatureInfoSupport()) {
			return Collections.emptyList();
		}
		List<Feature> features = new ArrayList<Feature>();
		Resolution bestResolution = WmsLayerUtils.getResolutionForScale(resolutions, layerInfo, layerScale);
		Bbox bbox = getLayerInfo().getMaxExtent();

		RasterGrid grid = painter.getRasterGrid(bbox, new Envelope(layerCoordinate), bestResolution.getTileWidth(),
				bestResolution.getTileHeight());
		int x = (int) (((layerCoordinate.x - grid.getLowerLeft().x) * bestResolution.getTileWidthPx()) / grid
				.getTileWidth());
		int y = (int) (bestResolution.getTileHeightPx() - (((layerCoordinate.y - grid.getLowerLeft().y) * bestResolution
				.getTileHeightPx()) / grid.getTileHeight()));

		Bbox layerBox = new Bbox(grid.getLowerLeft().x, grid.getLowerLeft().y, grid.getTileWidth(),
				grid.getTileHeight());

		InputStream stream = null;
		try {
			String url = buildGetFeatureInfoRequestUrl(layerCoordinate, layerScale, IS_GML_REQUEST);
			log.debug("getFeaturesByLocation: {} {} {} {}", new Object[] { layerCoordinate, layerScale, pixelTolerance,
					url });
			GML gml = new GML(Version.GML3);

			stream = httpService.getStream(url, getLayerAuthentication(), getId());
			FeatureCollection<?, SimpleFeature> collection = gml.decodeFeatureCollection(stream);
			FeatureIterator<SimpleFeature> it = collection.features();

			while (it.hasNext()) {
				features.add(toDto(it.next()));
			}
		} catch (LayerException le) {
			throw le; // NOSONAR
		} catch (Exception e) { // NOSONAR
			throw new LayerException(e, ExceptionCode.UNEXPECTED_PROBLEM);
		} finally {
			if (null != stream) {
				try {
					stream.close();
				} catch (IOException ioe) {
					// ignore, closing anyway
				}
			}
		}

		return features;
	}

	@Override
	public List<Feature> getFeatureInfoAsGml(Coordinate coordinate, double layerScale, int pixelTolerance)
			throws LayerException {
		if (!isEnableFeatureInfoAsGmlSupport()) {
			return Collections.emptyList();
		}
		return getFeaturesByLocation(coordinate, layerScale, pixelTolerance);
	}

	@Override
	public String getFeatureInfoAsHtml(Coordinate coordinate, double layerScale, int pixelTolerance)
			throws LayerException {
		if (!isEnableFeatureInfoAsHtmlSupport()) {
			return GFI_UNAVAILABLE_MSG;
		}
		InputStream stream = null;
		String url;
		try {
			url = buildGetFeatureInfoRequestUrl(coordinate, layerScale, IS_HTML_REQUEST);

			log.debug("getFeaturesByLocation: {} {} {} {}",
					new Object[] { coordinate, layerScale, pixelTolerance, url });
			stream = httpService.getStream(url, getLayerAuthentication(), getId());
		} catch (Exception e) {
			throw new LayerException(e, ExceptionCode.UNEXPECTED_PROBLEM);
		}
		return getStringFromInputStream(stream);
	}

	public boolean isAtLeastOneFeatureInEnvelope(Envelope bbox) {
		if (numberOfFeaturesInEnvelopeRetriever == null)
			return true;
		return numberOfFeaturesInEnvelopeRetriever.isAtLeastOneFeatureInEnvelope(bbox);
	}

	private static String getStringFromInputStream(InputStream is) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is, "utf-8"));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();

	}

	protected String buildGetFeatureInfoRequestUrl(Coordinate layerCoordinate, double layerScale, boolean isHtmlRequest)
			throws GeomajasException {
		Resolution bestResolution = WmsLayerUtils.getResolutionForScale(resolutions, layerInfo, layerScale);
		Bbox bbox = getLayerInfo().getMaxExtent();
		RasterGrid grid = painter.getRasterGrid(bbox, new Envelope(layerCoordinate), bestResolution.getTileWidth(),
				bestResolution.getTileHeight());
		int x = (int) (((layerCoordinate.x - grid.getLowerLeft().x) * bestResolution.getTileWidthPx()) / grid
				.getTileWidth());
		int y = (int) (bestResolution.getTileHeightPx() - (((layerCoordinate.y - grid.getLowerLeft().y) * bestResolution
				.getTileHeightPx()) / grid.getTileHeight()));
		Bbox layerBox = new Bbox(grid.getLowerLeft().x, grid.getLowerLeft().y, grid.getTileWidth(),
				grid.getTileHeight());
		String url = formatGetFeatureInfoUrl(bestResolution.getTileWidthPx(), bestResolution.getTileHeightPx(),
				layerBox, x, y, isHtmlRequest);
		return url;
	}

	private Feature toDto(SimpleFeature feature) {
		if (feature == null) {
			return null;
		}
		Feature dto = new Feature(feature.getID());

		HashMap<String, Attribute> attributes = new HashMap<String, Attribute>();

		for (AttributeDescriptor desc : feature.getType().getAttributeDescriptors()) {
			Object obj = feature.getAttribute(desc.getName());
			if (null != obj) {
				attributes.put(desc.getLocalName(), new StringAttribute(obj.toString()));
			}
		}
		dto.setAttributes(attributes);
		dto.setId(feature.getID());

		dto.setUpdatable(false);
		dto.setDeletable(false);
		return dto;

	}

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
	public List<RasterTile> paint(CoordinateReferenceSystem targetCrs, Envelope bounds, double scale)
			throws GeomajasException {
		return painter.paint(createWmsParams(), resolutions, this, targetCrs, bounds, scale);
	}

	@Override
	public String getLegendImageUrl() {
		return legendImageUrl;
	}

	private String formatGetFeatureInfoUrl(int width, int height, Bbox box, int x, int y, boolean isHtmlRequest)
			throws GeomajasException {
		// Always use direct url
		try {
			StringBuilder url = painter.formatBaseUrl(createWmsParams(), this, width, height, box);
			String layers = getId();
			if (layerInfo.getDataSourceName() != null) {
				layers = layerInfo.getDataSourceName();
			}
			url.append("&QUERY_LAYERS=");
			url.append(URLEncoder.encode(layers, "UTF8"));
			url.append("&request=GetFeatureInfo");
			url.append("&X=");
			url.append(Integer.toString(x));
			url.append("&Y=");
			url.append(Integer.toString(y));
			url.append("&INFO_FORMAT=");
			url.append(retrieveFormatString(isHtmlRequest));
			log.debug("Requesting GetFeatureInfo with URL {}", url);
			return url.toString();
		} catch (UnsupportedEncodingException uee) {
			throw new IllegalStateException("Cannot find UTF8 encoding?", uee);
		}
	}

	WmsParams createWmsParams() {
		return new WmsParams(format, useProxy, authentication, useCache, baseWmsUrl, styles, parameters, version);
	}

	private String retrieveFormatString(boolean isHtmlRequest) {
		if (isHtmlRequest) {
			return ("text/html");
		} else {
			return ("application/vnd.ogc.gml");
		}
	}

	private String formatGetCapabilitiesUrl(String targetUrl) {
		StringBuilder url = new StringBuilder(targetUrl);
		int pos = url.lastIndexOf("?");
		if (pos > 0) {
			url.append("&SERVICE=WMS");
		} else {
			url.append("?SERVICE=WMS");
		}
		url.append("&version=");
		url.append(version);
		url.append("&REQUEST=");
		url.append("getCapabilities");
		return url.toString();
	}

	private void retrieveAndSetLegendImageParameters(String baseWmsUrl) {
		// set default values
		setDefaultLegendImageValues();
		String capabilitiesUrl = formatGetCapabilitiesUrl(baseWmsUrl);
		try {
			HTTPClient httpClient = createConfiguredWmsHttpClient();
			WebMapServer wms = new WebMapServer(new URL(capabilitiesUrl), httpClient);
			WMSCapabilities capabilities = wms.getCapabilities();
			String searchedLayerId = getId();
			if (layerInfo.getDataSourceName() != null) {
				searchedLayerId = layerInfo.getDataSourceName();
			}
			String searchedStyleName = styles;
			parseNodeLayer(searchedStyleName, searchedLayerId, capabilities.getLayer());
		} catch (IOException e) {
			log.warn(e.getMessage());
		} catch (ServiceException e) {
			log.warn("Could not parse capabilities from {}", capabilitiesUrl);
		}
	}

	private HTTPClient createConfiguredWmsHttpClient() {
		HTTPClient httpClient = new SimpleHttpClient();
		httpClient.setConnectTimeout(DEFAULT_CAPABILITIES_CONNECTION_TIMEOUT_MS);
		if (layerAuthentication != null) {
			httpClient.setUser(layerAuthentication.getUser());
			httpClient.setPassword(layerAuthentication.getPassword());
		}
		return httpClient;
	}

	private void setDefaultLegendImageValues() {
		legendImageUrl = EMPTY_LEGENDURL;
		legendImageHeight = EMPTY_LEGEND_HEIGHT;
		legendImageWidth = EMPTY_LEGEND_WIDTH;
	}

	private boolean parseNodeLayer(String searchedStyleName, String searchedLayerId, Layer layer) {
		// check if the layer itself matches the given id
		boolean isThisLayerMatched = parseLeafLayer(searchedStyleName, searchedLayerId, layer);
		if (isThisLayerMatched) {
			return true; // the layer is matched. stop search.
		}

		List<Layer> childLayers = layer.getLayerChildren();
		for (Layer childLayer : childLayers) {
			boolean isChildMatched = false;
			if (layer.getChildren().length > 0) {
				isChildMatched = parseNodeLayer(searchedStyleName, searchedLayerId, childLayer);
			} else {
				isChildMatched = parseLeafLayer(searchedStyleName, searchedLayerId, childLayer);
			}
			if (isChildMatched) {
				return true; // A child layer matched. stop search.
			}
		}
		// neither the layer itself nor its childs matched. return to parent layer.
		return false;
	}

	private boolean parseLeafLayer(String searchedStyleName, String searchedLayerId, Layer layer) {
		if (searchedLayerId.equals(layer.getName())) {
			legendImageUrl = retrieveStyleForLayer(searchedStyleName, layer);
			// this layer matched. stop search.
			return true;
		}
		return false; // this leaf did not match.
	}

	private String retrieveStyleForLayer(String searchedStyleName, Layer layer) {
		List<StyleImpl> layerStyles = layer.getStyles();
		if (layerStyles != null && !layerStyles.isEmpty()) {
			String retrievedLegendUrl = searchStyle(searchedStyleName, layerStyles);
			if (!EMPTY_LEGENDURL.equals(retrievedLegendUrl)) {
				return retrievedLegendUrl;
			}
			return retrieveFirstStyle(layerStyles);
		} else {
			return EMPTY_LEGENDURL;
		}
	}

	private String retrieveFirstStyle(List<StyleImpl> layerStyles) {
		String legendUrl = EMPTY_LEGENDURL;
		StyleImpl style = layerStyles.get(0);
		List<?> legendURLs = style.getLegendURLs();
		if (legendURLs != null && !legendURLs.isEmpty()) {
			legendUrl = legendURLs.get(0).toString();
			retrieveAndSetHeightAndWidth(legendUrl);
		}
		return legendUrl;
	}

	private String searchStyle(String styleName, List<StyleImpl> layerStyles) {
		String legendUrl = EMPTY_LEGENDURL;
		for (StyleImpl style : layerStyles) {
			List<?> legendUrls = style.getLegendURLs();
			if (styleName.equals(style.getName()) && legendUrls != null && !legendUrls.isEmpty()) {
				legendUrl = legendUrls.get(0).toString();
				retrieveAndSetHeightAndWidth(legendUrl);
			}
		}
		return legendUrl;
	}

	private void retrieveAndSetHeightAndWidth(String legendUrl) {
		try {
			InputStream stream = httpService.getStream(legendUrl, null, null);
			BufferedImage img = ImageIO.read(stream);
			if (img != null) {
				setLegendImageHeight(img.getHeight());
				setLegendImageWidth(img.getWidth());
			}
		} catch (IOException e) {
			log.debug("Could not retrieve legend image height and size. Reason: ", e);
		}
	}

	public String getBaseWmsUrl() {
		return baseWmsUrl;
	}

	/**
	 * Set the base URL to the WMS server.
	 * 
	 * @param baseWmsUrl
	 *            base URL for the WMS server
	 * @since 1.7.1
	 */
	@Api
	public void setBaseWmsUrl(String baseWmsUrl) {
		this.baseWmsUrl = baseWmsUrl;
	}

	public String getFormat() {
		return format;
	}

	/**
	 * Set file format to request.
	 * 
	 * @param format
	 *            file format. For allowed values, check your WMS server.
	 * @since 1.7.1
	 */
	@Api
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * Set WMS version to use.
	 * 
	 * @param version
	 *            WMS version. For allowed values, check your WMS server.
	 * @since 1.7.1
	 */
	@Api
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Set the styles.
	 * 
	 * @param styles
	 *            styles. For allowed values, check your WMS server.
	 * @since 1.7.1
	 */
	@Api
	public void setStyles(String styles) {
		this.styles = styles;
	}

	/**
	 * Set additional parameters to include in all WMS <code>getMap</code> requests.
	 * 
	 * @param parameters
	 *            parameters. For possible keys and values, check your WMS server.
	 * @since 1.7.1
	 */
	@Api
	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}

	/**
	 * Get additional parameters to include in all WMS <code>getMap</code> requests.
	 * 
	 * @return parameters. For possible keys and values, check your WMS server.
	 */
	public List<Parameter> getParameters() {
		return parameters;
	}

	/**
	 * Get the authentication object.
	 * 
	 * @return authentication object
	 * @deprecated use getLayerAuthentication
	 */
	@Deprecated
	public WmsAuthentication getAuthentication() {
		return authentication;
	}

	/**
	 * <p>
	 * Set the authentication object. This configuration object provides support for basic and digest HTTP
	 * authentication on the WMS server. If no HTTP authentication is required, leave this empty.
	 * </p>
	 * <p>
	 * Note that there is still the option of adding a user name and password as HTTP parameters, as some WMS server
	 * support. To do that, just add {@link #parameters}.
	 * </p>
	 * 
	 * @param authentication
	 *            authentication object
	 * @since 1.8.0
	 * @deprecated use setLayerAuthentication()
	 */
	@Api
	@Deprecated
	public void setAuthentication(WmsAuthentication authentication) {
		this.authentication = authentication;
	}

	/**
	 * Get the layerAuthentication object.
	 * 
	 * @return layerAuthentication object
	 */
	public LayerAuthentication getLayerAuthentication() {
		// convert authentication to layerAuthentication so we only use one
		// TODO Remove when removing deprecated authentication field.
		if (layerAuthentication == null && authentication != null) {
			layerAuthentication = new LayerAuthentication();
			layerAuthentication.setAuthenticationMethod(LayerAuthenticationMethod.valueOf(authentication
					.getAuthenticationMethod().name()));
			layerAuthentication.setPassword(authentication.getPassword());
			layerAuthentication.setPasswordKey(authentication.getPasswordKey());
			layerAuthentication.setRealm(authentication.getRealm());
			layerAuthentication.setUser(authentication.getUser());
			layerAuthentication.setUserKey(authentication.getUserKey());
		}
		// TODO Remove when removing deprecated authentication field.
		return layerAuthentication;
	}

	/**
	 * <p>
	 * Set the authentication object. This configuration object provides support for basic and digest HTTP
	 * authentication on the WMS server. If no HTTP authentication is required, leave this empty.
	 * </p>
	 * <p>
	 * Note that there is still the option of adding a user name and password as HTTP parameters, as some WMS server
	 * support. To do that, just add {@link #parameters}.
	 * </p>
	 * 
	 * @param layerAuthentication
	 *            layerAuthentication object
	 * @since 1.11.0
	 */
	@Api
	public void setLayerAuthentication(LayerAuthentication layerAuthentication) {
		this.layerAuthentication = layerAuthentication;
	}

	/**
	 * Set whether the WMS request should use a proxy. This is automatically done when the authentication object is set.
	 * When the WMS request is proxied, the credentials and WMS base address are hidden from the client.
	 * 
	 * @param useProxy
	 *            true when request needs to use the proxy
	 * @since 1.8.0
	 */
	@Api
	public void setUseProxy(boolean useProxy) {
		this.useProxy = useProxy;
	}

	/**
	 * Set whether the WMS tiles should be cached for later use. This implies that the WMS tiles will be proxied.
	 * 
	 * @param useCache
	 *            true when request needs to be cached
	 * @since 1.9.0
	 */
	@Api
	public void setUseCache(boolean useCache) {
		if (null == cacheManagerService && useCache) {
			log.warn("The caching plugin needs to be available to cache WMS requests. Not setting useCache.");
		} else {
			this.useCache = useCache;
		}
	}

	/**
	 * Set whether the WMS tiles should be cached for later use. This implies that the WMS tiles will be proxied.
	 * 
	 * @return true when request needs to be cached
	 * @since 1.9.0
	 */
	@Api
	public boolean isUseCache() {
		return useCache;
	}

	/**
	 * Set whether the WMS layer should support feature info support. This allows to retrieve feature info from a raster
	 * layer. This only makes sense if the WMS layer is based on some kind of feature store like a database.
	 * 
	 * @param enableFeatureInfoSupport
	 *            whether feature info support is enabled for this layer
	 * @since 1.9.0
	 */
	@Api
	public void setEnableFeatureInfoSupport(boolean enableFeatureInfoSupport) {
		this.enableFeatureInfoSupport = enableFeatureInfoSupport;
	}

	/**
	 * Get whether the WMS layer should support feature info support.
	 * 
	 * @return the enableFeatureInfoSupport true if feature info support is enabled
	 * 
	 * @deprecated use {@link #isEnableFeatureInfoAsGmlSupport()} instead
	 */
	@Deprecated
	public boolean isEnableFeatureInfoSupport() {
		return enableFeatureInfoSupport;
	}

	/**
	 * Get whether the WMS layer should support feature info HTML support.
	 * 
	 * @return the enableFeatureInfoSupportAsHtml true if feature info html support is enabled
	 * 
	 * @since 1.11.0
	 */
	@Api
	public boolean isEnableFeatureInfoAsHtmlSupport() {
		return enableFeatureInfoSupportAsHtml;
	}

	public void setEnableFeatureInfoAsHtmlSupport(boolean enableFeatureInfoSupportAsHtml) {
		this.enableFeatureInfoSupportAsHtml = enableFeatureInfoSupportAsHtml;
	}

	/**
	 * Get whether the WMS layer should support feature info GML support.
	 * 
	 * @return the enableFeatureInfoSupportAsHtml true if feature info gml support is enabled
	 * 
	 * @since 1.11.0
	 */
	@Api
	public boolean isEnableFeatureInfoAsGmlSupport() {
		return enableFeatureInfoSupportAsHtml;
	}

	public void setEnableFeatureInfoAsGmlSupport(boolean enableFeatureInfoSupportAsGml) {
		this.setEnableFeatureInfoSupportAsGml(enableFeatureInfoSupportAsGml);
	}

	protected LayerHttpService getHttpService() {
		return httpService;
	}

	protected void setHttpService(LayerHttpService httpService) {
		this.httpService = httpService;
	}

	/**
	 * Clear cache manager service. Provided only for testing.
	 */
	void clearCacheManagerService() {
		this.cacheManagerService = null;
	}

	public boolean isEnableFeatureInfoSupportAsGml() {
		return enableFeatureInfoSupportAsGml;
	}

	public void setEnableFeatureInfoSupportAsGml(boolean enableFeatureInfoSupportAsGml) {
		this.enableFeatureInfoSupportAsGml = enableFeatureInfoSupportAsGml;
	}

	public int getLegendImageHeight() {
		return legendImageHeight;
	}

	public void setLegendImageHeight(int legendImageHeight) {
		this.legendImageHeight = legendImageHeight;
	}

	public int getLegendImageWidth() {
		return legendImageWidth;
	}

	public void setLegendImageWidth(int legendImageWidth) {
		this.legendImageWidth = legendImageWidth;
	}

	public List<Resolution> getResolutions() {
		return resolutions;
	}

}