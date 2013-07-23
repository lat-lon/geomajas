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

package org.geomajas.gwt.client.controller;

import org.geomajas.geometry.Coordinate;
import org.geomajas.gwt.client.i18n.I18nProvider;
import org.geomajas.gwt.client.spatial.geometry.Geometry;
import org.geomajas.gwt.client.util.DistanceFormat;
import org.geomajas.gwt.client.widget.MapWidget;

/**
 * <p>
 * Controller that measures distances on the map, by clicking points. The actual distances are displayed in a label at
 * the top left of the map.
 * </p>
 * 
 * @author Pieter De Graef
 */
public class MeasureDistanceController extends MeasureController {

	public MeasureDistanceController(MapWidget mapWidget) {
		super(mapWidget);
	}

	@Override
	protected Geometry createInitialGeometry(Coordinate worldPosition) {
		return getFactory().createLineString(new Coordinate[] { worldPosition });
	}

	@Override
	protected Geometry createCurrentGeometry(Coordinate worldPosition) {
		Geometry geometry = (Geometry) geometryToCalculate.getOriginalLocation();
		Coordinate coordinate1 = geometry.getCoordinates()[geometryToCalculate.getGeometry().getNumPoints() - 1];
		return getFactory().createLineString(new Coordinate[] { coordinate1, worldPosition });
	}

	@Override
	protected float calculateTotalMeasurementResult() {
		return (float) ((Geometry) geometryToCalculate.getOriginalLocation()).getLength();
	}

	@Override
	protected float calculateLatestMeasurementResult() {
		return (float) ((Geometry) geometryToDraw.getOriginalLocation()).getLength();
	}

	@Override
	protected String createLabelContent(double totalResult, double currentResult) {
		String total = DistanceFormat.asMapLength(mapWidget, totalResult);
		String r = DistanceFormat.asMapLength(mapWidget, currentResult);
		String dist = I18nProvider.getMenu().getMeasureDistanceString(total, r);
		return "<div><b>" + I18nProvider.getMenu().distance() + "</b>:</div><div style='margin-top:5px;'>" + dist
				+ "</div>";
	}

}