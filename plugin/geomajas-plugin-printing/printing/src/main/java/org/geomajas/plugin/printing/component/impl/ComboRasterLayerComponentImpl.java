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

package org.geomajas.plugin.printing.component.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.geomajas.configuration.NamedStyleInfo;
import org.geomajas.configuration.client.ClientMapInfo;
import org.geomajas.global.GeomajasException;
import org.geomajas.layer.LayerException;
import org.geomajas.layer.MapLayerService;
import org.geomajas.layer.tile.RasterTile;
import org.geomajas.plugin.printing.component.dto.ComboRasterLayerComponentInfo;
import org.geomajas.plugin.printing.component.dto.RasterLayerComponentInfo;
import org.opengis.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Sub component of a map responsible for rendering combo raster layer.
 */
@Component()
@Scope(value = "prototype")
public class ComboRasterLayerComponentImpl extends RasterLayerComponentImpl {

	private List<String> layerIds;

	@Autowired
	private MapLayerService mapLayerService;

	@Override
	protected List<RasterTile> retrieveTiles(ClientMapInfo map) throws GeomajasException, LayerException {
		return mapLayerService.getTiles(layerIds, new HashMap<String, Filter>(), new HashMap<String, NamedStyleInfo>(),
				geoService.getCrs2(map.getCrs()), bbox, rasterScale);
	}

	@Override
	public void fromDto(RasterLayerComponentInfo rasterInfo) {
		if (rasterInfo instanceof RasterLayerComponentInfo) {
			this.layerIds = ((ComboRasterLayerComponentInfo) rasterInfo).getLayerIds();
		} else {
			this.layerIds = Collections.emptyList(); // TODO this case should rather be handled with an exception
		}
		RasterLayerComponentInfo info = new RasterLayerComponentInfo();
		info.setBounds(rasterInfo.getBounds());
		info.setChildren(rasterInfo.getChildren());
		info.setId(rasterInfo.getId());
		info.setLayerId(rasterInfo.getLayerId());
		info.setLayoutConstraint(rasterInfo.getLayoutConstraint());
		info.setSelected(rasterInfo.isSelected());
		info.setStyle(rasterInfo.getStyle());
		info.setTag(rasterInfo.getTag());
		info.setVisible(rasterInfo.isVisible());
		super.fromDto(info);
	}

}