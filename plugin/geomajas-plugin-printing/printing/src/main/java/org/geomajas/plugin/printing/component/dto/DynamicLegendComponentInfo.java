package org.geomajas.plugin.printing.component.dto;

import org.geomajas.plugin.printing.component.LayoutConstraint;

/**
 * DTO object for a dynamic LegendComponent.
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class DynamicLegendComponentInfo extends AbstractLegendComponentInfo {

	private static final long serialVersionUID = 200L;

	public DynamicLegendComponentInfo() {
		super();
		LayoutConstraintInfo legendLayoutConstraint = getLayoutConstraint();
		legendLayoutConstraint.setAlignmentX(LayoutConstraint.LEFT);
		legendLayoutConstraint.setAlignmentY(LayoutConstraint.TOP);
		legendLayoutConstraint.setFlowDirection(LayoutConstraint.FLOW_X);
	}

}