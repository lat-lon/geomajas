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
import org.geomajas.gwt.client.gfx.paintable.GfxGeometry;
import org.geomajas.gwt.client.i18n.I18nProvider;
import org.geomajas.gwt.client.spatial.geometry.Geometry;
import org.geomajas.gwt.client.spatial.geometry.GeometryFactory;
import org.geomajas.gwt.client.spatial.geometry.operation.InsertCoordinateOperation;
import org.geomajas.gwt.client.util.DistanceFormat;
import org.geomajas.gwt.client.widget.MapWidget;
import org.geomajas.gwt.client.widget.MapWidget.RenderGroup;
import org.geomajas.gwt.client.widget.MapWidget.RenderStatus;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.menu.Menu;
import com.vividsolutions.jts.geom.LinearRing;

/**
 * <p>
 * Controller that measures areas on the map, by clicking points. The actual areas are displayed in a label at the top
 * left of the map.
 * </p>
 * 
 * @author Alexander Erben
 */
public class MeasureAreaController extends AbstractSnappingController {

	private GfxGeometry distanceLine;

	private GfxGeometry lineSegment;

	private AreaLabel label;

	private GeometryFactory factory;

	private double tempArea;

	public MeasureAreaController(MapWidget mapWidget) {
		super(mapWidget);
	}

	/** Create the context menu for this controller. */
	public void onActivate() {
		// TODO implement
	}

	/** Clean everything up. */
	public void onDeactivate() {
		// TODO implement
	}

	/** Set a new point on the area-line. */
	public void onMouseUp(MouseUpEvent event) {
		if (event.getNativeButton() != NativeEvent.BUTTON_RIGHT) {
			Coordinate coordinate = getWorldPosition(event);
			if (distanceLine.getOriginalLocation() == null) {
				distanceLine.setGeometry(getFactory().createLinearRing(new Coordinate[] { coordinate }));
				mapWidget.registerWorldPaintable(distanceLine);
				mapWidget.registerWorldPaintable(lineSegment);
				label = new AreaLabel();
				label.setArea(0);
				label.animateMove(mapWidget.getWidth() - 130, 10);
			} else {
				Geometry geometry = (Geometry) distanceLine.getOriginalLocation();
				InsertCoordinateOperation op = new InsertCoordinateOperation(geometry.getNumPoints(), coordinate);
				geometry = op.execute(geometry);
				distanceLine.setGeometry(geometry);
				tempArea = generateTempArea();
				label.setArea(tempArea);
			}
			mapWidget.render(mapWidget.getMapModel(), RenderGroup.VECTOR, RenderStatus.UPDATE);
		}
	}

	private double generateTempArea() {
		// close the linear ring
//		Geometry geometry = (Geometry) distanceLine.getOriginalLocation();
//		Geometry clonedGeometry = (Geometry) geometry.clone();
//		Coordinate[] coords = clonedGeometry.getCoordinates();
//		Coordinate last = coords[coords.length - 1];
//		InsertCoordinateOperation op = new InsertCoordinateOperation(geometry.getNumPoints(), last);
//		Geometry newGeometry = op.execute(clonedGeometry);
//		LinearRing ring = (LinearRing) newGeometry;
//		String message = "polygon not valid!";
//		if (ring.isValid()) {
//			Double area = new Double (ring.getArea());
//			message = area.toString();
//		}
//		SC.say(message);
//		return 0;
		Geometry ring = (Geometry) distanceLine.getOriginalLocation();
		return ring.getArea();
	}

	/** Update the drawing while moving the mouse. */
	public void onMouseMove(MouseMoveEvent event) {
		if (isMeasuring() && distanceLine.getOriginalLocation() != null) {
			Geometry geometry = (Geometry) distanceLine.getOriginalLocation();
			Coordinate coordinate1 = geometry.getCoordinates()[distanceLine.getGeometry().getNumPoints() - 1];
			Coordinate coordinate2 = getWorldPosition(event);
			lineSegment.setGeometry(getFactory().createLineString(new Coordinate[] { coordinate1, coordinate2 }));
			mapWidget.render(mapWidget.getMapModel(), RenderGroup.VECTOR, RenderStatus.UPDATE);
		}
	}

	/** Stop the measuring, and remove all graphics from the map. */
	public void onDoubleClick(DoubleClickEvent event) {
		tempArea = 0;
		mapWidget.unregisterWorldPaintable(distanceLine);
		mapWidget.unregisterWorldPaintable(lineSegment);
		distanceLine.setGeometry(null);
		lineSegment.setGeometry(null);
		if (label != null) {
			label.destroy();
		}
	}

	// -------------------------------------------------------------------------
	// Private methods:
	// -------------------------------------------------------------------------

	private boolean isMeasuring() {
		return distanceLine.getGeometry() != null;
	}

	/**
	 * The factory can only be used after the MapModel has initialized, that is why this getter exists...
	 * 
	 * @return geometry factory
	 */
	private GeometryFactory getFactory() {
		if (factory == null) {
			factory = mapWidget.getMapModel().getGeometryFactory();
		}
		return factory;
	}

	// -------------------------------------------------------------------------
	// Private classes:
	// -------------------------------------------------------------------------

	/**
	 * The label that shows the areas.
	 * 
	 * @author Pieter De Graef
	 */
	private class AreaLabel extends Label {

		public AreaLabel() {
			setParentElement(mapWidget);
			setValign(VerticalAlignment.TOP);
			setShowEdges(true);
			setWidth(120);
			setPadding(3);
			setLeft(mapWidget.getWidth() - 130);
			setTop(-80);
			setBackgroundColor("#FFFFFF");
			setAnimateTime(500);
		}

		public void setArea(double totalArea) {
			String total = DistanceFormat.asMapLength(mapWidget, totalArea);
			String dist = I18nProvider.getMenu().getMeasureDistanceString(total, ""); // TODO change i18n template
			setContents("<div><b>" + I18nProvider.getMenu().distance() + "</b>:</div><div style='margin-top:5px;'>"
					+ dist + "</div>");
		}
	}

}
