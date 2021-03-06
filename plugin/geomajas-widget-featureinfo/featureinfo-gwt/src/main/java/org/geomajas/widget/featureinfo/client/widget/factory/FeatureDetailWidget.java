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
package org.geomajas.widget.featureinfo.client.widget.factory;

import org.geomajas.gwt.client.map.feature.Feature;

import com.smartgwt.client.widgets.Canvas;

/**
 * A canvas with one extra method to set a new feature, so featuredetailwidgets can be reused.
 * @author Kristof Heirwegh
 *
 */
public abstract class FeatureDetailWidget extends Canvas {

	public abstract void setFeature(Feature feature, int maxHeight);
}
