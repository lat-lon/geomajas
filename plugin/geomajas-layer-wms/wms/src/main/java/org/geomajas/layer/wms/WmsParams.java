package org.geomajas.layer.wms;

import java.util.List;

import org.geomajas.configuration.Parameter;
import org.geomajas.layer.common.proxy.LayerAuthentication;

public class WmsParams {

	private final String format;

	private final boolean useProxy;

	private final WmsAuthentication authentication;

	private final boolean useCache;

	private final String baseWmsUrl;

	private final String styles;

	private final List<Parameter> parameters;

	private final String version;

	public WmsParams(String format, boolean useProxy, WmsAuthentication authentication, boolean useCache,
			String baseWmsUrl, String styles, List<Parameter> parameters, String version) {
		this.format = format;
		this.useProxy = useProxy;
		this.authentication = authentication;
		this.useCache = useCache;
		this.baseWmsUrl = baseWmsUrl;
		this.styles = styles;
		this.parameters = parameters;
		this.version = version;
	}

	public String getFormat() {
		return format;
	}

	public boolean isUseProxy() {
		return useProxy;
	}

	public WmsAuthentication getAuthentication() {
		return authentication;
	}

	public boolean isUseCache() {
		return useCache;
	}

	public String getBaseWmsUrl() {
		return baseWmsUrl;
	}

	public String getStyles() {
		return styles;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public String getVersion() {
		return version;
	}

}
