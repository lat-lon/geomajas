package org.geomajas.gwt.client.action.toolbar;

import org.geomajas.gwt.client.action.ToolbarAction;
import org.geomajas.gwt.client.i18n.I18nProvider;
import org.geomajas.gwt.client.util.WidgetLayout;
import org.geomajas.gwt.client.widget.MapWidget;

import com.smartgwt.client.widgets.events.ClickEvent;

/**
 * Refresh the entired map.
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class MapRefreshAction extends ToolbarAction {

	private MapWidget mapWidget;

	public MapRefreshAction(MapWidget mapWidget) {
		super(WidgetLayout.iconRedraw, I18nProvider.getToolbar().refreshMapTitle(), I18nProvider.getToolbar()
				.refreshMapTooltip());
		this.mapWidget = mapWidget;
	}

	@Override
	public void onClick(ClickEvent event) {
		mapWidget.getMapModel().refresh();
	}

}