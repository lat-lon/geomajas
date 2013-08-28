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
import org.geomajas.widget.featureinfo.client.FeatureInfoMessages;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.Label;

/**
 * 
 * @author Alexander Erben
 */
public class FeatureInfoHtmlWindow extends KeepInScreenWindow {

	private static final FeatureInfoMessages MESSAGES = GWT.create(FeatureInfoMessages.class);

	public FeatureInfoHtmlWindow(String html) {
		buildWidget(html);
	}

	private void buildWidget(String html) {
		setTitle(MESSAGES.layerHtmlWindowTitleMessage());
		setWidth(400);
		setHeight(400);
		setOverflow(Overflow.AUTO);
		setCanDragResize(true);
		Label label = new Label();
		label.setContents(html);
		label.show();
		addItem(label);
	}

}
