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

package org.geomajas.widget.featureinfo.client.widget;

import org.geomajas.gwt.client.widget.KeepInScreenWindow;

import com.smartgwt.client.widgets.Canvas;

/**
 * 
 * @author Alexander Erben
 */
public class FeatureInfoHtmlWindow extends KeepInScreenWindow {

	public FeatureInfoHtmlWindow(String html) {
		buildWidget(html);
	}

	private void buildWidget(String html) {
		Canvas canvas = new Canvas();
		canvas.setContents(html);
		canvas.draw();
		canvas.show();
		addChild(canvas);
	}

}
