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

import org.geomajas.annotation.Api;

/**
 * DTO object for LegendComponent.
 * 
 * @author Jan De Moerloose
 * @see org.geomajas.plugin.printing.component.LegendComponent
 * @since 2.0.0
 */
@Api(allMethods = true)
public class LegendComponentInfo extends AbstractLegendComponentInfo {

	private static final long serialVersionUID = 200L;

	/**
	 * Default constructor.
	 */
	public LegendComponentInfo() {
		super();
		getLayoutConstraint().setAlignmentX(LayoutConstraintInfo.RIGHT);
		getLayoutConstraint().setAlignmentY(LayoutConstraintInfo.BOTTOM);
		getLayoutConstraint().setFlowDirection(LayoutConstraintInfo.FLOW_Y);
		getLayoutConstraint().setMarginX(20);
		getLayoutConstraint().setMarginY(20);
	}

}