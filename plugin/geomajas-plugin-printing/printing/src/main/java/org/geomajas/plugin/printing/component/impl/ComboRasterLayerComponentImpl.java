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
		return mapLayerService.getTiles(layerIds, new HashMap<String, Filter>(),
				new HashMap<String, NamedStyleInfo>(), geoService.getCrs2(map.getCrs()), bbox, rasterScale);
	}

	public void fromDto(ComboRasterLayerComponentInfo comboRasterInfo) {
		this.layerIds = comboRasterInfo.getLayerIds();
		RasterLayerComponentInfo info = new RasterLayerComponentInfo();
		info.setBounds(comboRasterInfo.getBounds());
		info.setChildren(comboRasterInfo.getChildren());
		info.setId(comboRasterInfo.getId());
		info.setLayerId(comboRasterInfo.getLayerId());
		info.setLayoutConstraint(comboRasterInfo.getLayoutConstraint());
		info.setSelected(comboRasterInfo.isSelected());
		info.setStyle(comboRasterInfo.getStyle());
		info.setTag(comboRasterInfo.getTag());
		info.setVisible(comboRasterInfo.isVisible());
		super.fromDto(info);
	}

}