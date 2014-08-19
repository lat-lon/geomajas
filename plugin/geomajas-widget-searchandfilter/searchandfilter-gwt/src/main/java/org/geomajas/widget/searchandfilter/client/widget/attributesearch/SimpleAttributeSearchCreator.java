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
package org.geomajas.widget.searchandfilter.client.widget.attributesearch;

import org.geomajas.gwt.client.widget.MapWidget;
import org.geomajas.widget.searchandfilter.client.SearchAndFilterMessages;
import org.geomajas.widget.searchandfilter.client.widget.search.DockableWindowSearchWidget;
import org.geomajas.widget.searchandfilter.client.widget.search.SearchWidget;
import org.geomajas.widget.searchandfilter.client.widget.search.SearchWidgetCreator;

import com.google.gwt.core.client.GWT;

/**
 * Simplified combined search creator.
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class SimpleAttributeSearchCreator implements SearchWidgetCreator {

    public static final String IDENTIFIER = "SimpleAttributeSearch";

    private final SearchAndFilterMessages messages = GWT.create( SearchAndFilterMessages.class );

    public String getSearchWidgetId() {
        return IDENTIFIER;
    }

    public String getSearchWidgetName() {
        return messages.attributeSearchWidgetTitle();
    }

    public SearchWidget createInstance( MapWidget mapWidget ) {
        return new DockableWindowSearchWidget( IDENTIFIER, getSearchWidgetName(),
                                               new SimpleAttributeSearchPanel( mapWidget ) );
    }

}