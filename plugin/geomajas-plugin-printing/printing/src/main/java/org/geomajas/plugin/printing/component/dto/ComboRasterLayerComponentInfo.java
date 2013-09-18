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
package org.geomajas.plugin.printing.component.dto;

import java.io.Serializable;
import java.util.List;

/**
 * DTO object for ComboRasterLayerComponent.
 * 
 */
public class ComboRasterLayerComponentInfo extends RasterLayerComponentInfo implements Serializable {

	private static final long serialVersionUID = 200L;

	private List<String> layerIds;

	public List<String> getLayerIds() {
		return layerIds;
	}

	public void setLayerIds(List<String> layerIds) {
		this.layerIds = layerIds;
	}
}
