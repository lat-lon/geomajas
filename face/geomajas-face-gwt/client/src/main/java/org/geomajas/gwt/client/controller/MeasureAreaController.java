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

import org.geomajas.gwt.client.widget.MapWidget;

import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;

/**
 * <p>
 * Controller that measures areas on the map, by clicking points. The actual areas are displayed in a label at
 * the top left of the map.
 * </p>
 * 
 * @author Alexander Erben
 */
public class MeasureAreaController extends AbstractSnappingController {

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
		// TODO implement
	}

	/** Update the drawing while moving the mouse. */
	public void onMouseMove(MouseMoveEvent event) {
		// TODO implement
	}

	/** Stop the measuring, and remove all graphics from the map. */
	public void onDoubleClick(DoubleClickEvent event) {
		// TODO implement
	}

}
