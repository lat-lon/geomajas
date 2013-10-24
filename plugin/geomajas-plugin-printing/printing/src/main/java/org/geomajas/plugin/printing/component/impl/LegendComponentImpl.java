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

import org.geomajas.plugin.printing.component.dto.LegendComponentInfo;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Inclusion of legend in printed document.
 * 
 * @author Jan De Moerloose
 */
@Component()
@Scope(value = "prototype")
public class LegendComponentImpl extends AbstractLegendComponentImpl<LegendComponentInfo> {

	public LegendComponentImpl() {
		this("Legend");
	}

	public LegendComponentImpl(String title) {
		super(title);
	}

	public void fromDto(LegendComponentInfo legendInfo) {
		super.fromDto(legendInfo);
		setApplicationId(legendInfo.getApplicationId());
		setMapId(legendInfo.getMapId());
		setFont(converterService.toInternal(legendInfo.getFont()));
		setTitle(legendInfo.getTitle());
	}

}