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
import org.geomajas.gwt.client.action.MenuAction;
import org.geomajas.gwt.client.action.menu.ToggleSnappingAction;
import org.geomajas.gwt.client.gfx.paintable.GfxGeometry;
import org.geomajas.gwt.client.gfx.style.ShapeStyle;
import org.geomajas.gwt.client.i18n.I18nProvider;
import org.geomajas.gwt.client.map.layer.Layer;
import org.geomajas.gwt.client.map.layer.VectorLayer;
import org.geomajas.gwt.client.spatial.geometry.Geometry;
import org.geomajas.gwt.client.spatial.geometry.GeometryFactory;
import org.geomajas.gwt.client.spatial.geometry.operation.InsertCoordinateOperation;
import org.geomajas.gwt.client.util.WidgetLayout;
import org.geomajas.gwt.client.widget.MapWidget;
import org.geomajas.gwt.client.widget.MapWidget.RenderGroup;
import org.geomajas.gwt.client.widget.MapWidget.RenderStatus;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.MenuItemIfFunction;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;

/**
 * <p>
 * Abstract controller that measures on the map, by clicking points. The actual measurements are displayed in a label at
 * the top left of the map.
 * </p>
 * 
 * @author Lyn Goltz
 */
public abstract class MeasureController extends AbstractSnappingController {

	private static final ShapeStyle GEOMETRYCALCULATE_STYLE = new ShapeStyle("#FFFFFF", 0, "#FF9900", 1, 2);

	private static final ShapeStyle GEOEMTRYDRAW_STYLE = new ShapeStyle("#FFFFFF", 0, "#FF5500", 1, 2);

	protected GfxGeometry geometryToCalculate;

	protected GfxGeometry geometryToDraw;

	private GeometryFactory factory;

	protected float actualMeasurementResult;

	private Menu menu;

	private MeasureLabel label;

	public MeasureController(MapWidget mapWidget) {
		super(mapWidget);
		geometryToCalculate = new GfxGeometry("measureGeoemtryToCalculate");
		geometryToCalculate.setStyle(GEOMETRYCALCULATE_STYLE);
		geometryToDraw = new GfxGeometry("measureGeometryToDraw");
		geometryToDraw.setStyle(GEOEMTRYDRAW_STYLE);
	}

	/**
	 * Creat// ------------------------------------------------------------------------- // GraphicsController
	 * interface: // -------------------------------------------------------------------------
	 * 
	 * /** Create the context menu for this controller.
	 */
	@Override
	public void onActivate() {
		menu = new Menu();
		menu.addItem(new CancelMeasuringAction(this));
		Layer<?> selectedLayer = mapWidget.getMapModel().getSelectedLayer();
		if (selectedLayer instanceof VectorLayer) {
			menu.addItem(new ToggleSnappingAction((VectorLayer) selectedLayer, this));
		}
		mapWidget.setContextMenu(menu);
	}

	/** Set a new point on the area-line. */
	@Override
	public void onMouseUp(MouseUpEvent event) {
		if (event.getNativeButton() != NativeEvent.BUTTON_RIGHT) {
			Coordinate worldPosition = getWorldPosition(event);
			if (geometryToCalculate.getOriginalLocation() == null) {
				Geometry initialGeometry = createInitialGeometry(worldPosition);
				geometryToCalculate.setGeometry(initialGeometry);
				mapWidget.registerWorldPaintable(geometryToCalculate);
				mapWidget.registerWorldPaintable(geometryToDraw);
				label = new MeasureLabel();
				updateLabel(0, 0);
				label.animateMove(mapWidget.getWidth() - 130, 10);
			} else {
				Geometry geometry = (Geometry) geometryToCalculate.getOriginalLocation();
				InsertCoordinateOperation op = new InsertCoordinateOperation(geometry.getNumPoints(), worldPosition);
				geometry = op.execute(geometry);
				if (isGeometryValid(geometry)) {
					geometryToCalculate.setGeometry(geometry);
					actualMeasurementResult = calculateTotalMeasurementResult();
					updateLabel(actualMeasurementResult, 0);
				} else {
					SC.say(I18nProvider.getMenu().invalidGeometryToMeasure());
				}

			}
			mapWidget.render(mapWidget.getMapModel(), RenderGroup.VECTOR, RenderStatus.UPDATE);
		}
	}

	/** Update the drawing while moving the mouse. */
	@Override
	public void onMouseMove(MouseMoveEvent event) {
		if (isMeasuring() && geometryToCalculate.getOriginalLocation() != null) {
			Coordinate worldPosition = getWorldPosition(event);
			Geometry geometry = createCurrentGeometry(worldPosition);
			geometryToDraw.setGeometry(geometry);
			mapWidget.render(mapWidget.getMapModel(), RenderGroup.VECTOR, RenderStatus.UPDATE);
			updateLabel(actualMeasurementResult, calculateLatestMeasurementResult());
		}
	}

	/** Clean everything up. */
	@Override
	public void onDeactivate() {
		onDoubleClick(null);
		menu.destroy();
		menu = null;
		mapWidget.setContextMenu(null);
		mapWidget.unregisterWorldPaintable(geometryToCalculate);
		mapWidget.unregisterWorldPaintable(geometryToDraw);
	}

	/** Stop the measuring, and remove all graphics from the map. */
	@Override
	public void onDoubleClick(DoubleClickEvent event) {
		actualMeasurementResult = 0;
		mapWidget.unregisterWorldPaintable(geometryToCalculate);
		mapWidget.unregisterWorldPaintable(geometryToDraw);
		geometryToDraw.setGeometry(null);
		geometryToCalculate.setGeometry(null);
		if (label != null) {
			label.destroy();
		}
	}

	protected boolean isGeometryValid(Geometry geometry) {
		return geometry.isValid();
	}

	// -------------------------------------------------------------------------
	// Protected methods:
	// -------------------------------------------------------------------------

	/**
	 * The factory can only be used after the MapModel has initialized, that is why this getter exists...
	 * 
	 * @return geometry factory
	 */
	protected GeometryFactory getFactory() {
		if (factory == null) {
			factory = mapWidget.getMapModel().getGeometryFactory();
		}
		return factory;
	}

	protected boolean isMeasuring() {
		return geometryToCalculate.getGeometry() != null;
	}

	protected abstract Geometry createInitialGeometry(Coordinate worldPosition);

	protected abstract Geometry createCurrentGeometry(Coordinate worldPosition);

	protected abstract float calculateTotalMeasurementResult();

	protected abstract float calculateLatestMeasurementResult();

	protected abstract String createLabelContent(double totalArea, double currentArea);

	private void updateLabel(float total, float latest) {
		label.setMeasurementResult(createLabelContent(total, latest));
	}

	// -------------------------------------------------------------------------
	// Private classes:
	// -------------------------------------------------------------------------
	/**
	 * The label that shows the areas.
	 * 
	 * @author Pieter De Graef
	 */
	private class MeasureLabel extends Label {

		private MeasureLabel() {
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

		private void setMeasurementResult(String contents) {
			setContents(contents);
		}
	}

	/**
	 * Menu item that stop the measuring
	 * 
	 * @author Pieter De Graef
	 */
	private class CancelMeasuringAction extends MenuAction {

		private final MeasureController controller;

		public CancelMeasuringAction(final MeasureController controller) {
			super(I18nProvider.getMenu().cancelMeasuring(), WidgetLayout.iconQuit);
			this.controller = controller;
			setEnableIfCondition(new MenuItemIfFunction() {

				public boolean execute(Canvas target, Menu menu, MenuItem item) {
					return controller.isMeasuring();
				}
			});
		}

		public void onClick(MenuItemClickEvent event) {
			controller.onDoubleClick(null);
		}
	}

}