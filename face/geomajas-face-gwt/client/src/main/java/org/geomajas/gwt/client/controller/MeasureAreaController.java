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
import org.geomajas.gwt.client.spatial.geometry.operation.InsertCoordinateOperation;
import org.geomajas.gwt.client.util.DistanceFormat;
import org.geomajas.gwt.client.widget.MapWidget;

/**
 * <p>
 * Controller that measures areas on the map, by clicking points. The actual areas are displayed in a label at the top
 * left of the map.
 * </p>
 * 
 * @author Alexander Erben
 */
public class MeasureAreaController extends MeasureController {

	public MeasureAreaController(MapWidget mapWidget) {
		super(mapWidget);
	}

	@Override
	protected Geometry createCurrentGeometry(Coordinate worldPosition) {
		Geometry originalLocation = (Geometry) geometryToCalculate.getOriginalLocation();
		InsertCoordinateOperation op = new InsertCoordinateOperation(originalLocation.getNumPoints(), worldPosition);
		return op.execute(originalLocation);
	}

	@Override
	protected Geometry createInitialGeometry(Coordinate worldPosition) {
		return getFactory().createLinearRing(new Coordinate[] { worldPosition });
	}

	@Override
	protected float calculateTotalMeasurementResult() {
		return (float) ((Geometry) geometryToCalculate.getOriginalLocation()).getArea();
	}

	@Override
	protected float calculateLatestMeasurementResult() {
		return (float) ((Geometry) geometryToDraw.getOriginalLocation()).getArea();
	}

	@Override
	protected String createLabelContent(double totalArea, double currentArea) {
		String total;
		if (totalArea < 0) {
			total = I18nProvider.getMenu().invalidGeometryToMeasure();
		}
		else {
			total = DistanceFormat.asMapArea(mapWidget, totalArea);
		}
		String current = DistanceFormat.asMapArea(mapWidget, currentArea);
		String dist = I18nProvider.getMenu().getMeasureAreaString(total, current);
		return "<div><b>" + I18nProvider.getMenu().area() + "</b>:</div><div style='margin-top:5px;'>" + dist
				+ "</div>";
	}

	@Override
	protected boolean isGeometryValid(Geometry geometry) {
		if (geometry.getNumPoints() < 4)
			return true;
		return super.isGeometryValid(geometry);
	}

}