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
package org.geomajas.gwt.client.map.layer;

import org.geomajas.gwt.client.map.MapModel;
import org.geomajas.gwt.client.map.layer.viewport.SmartGwtViewport;
import org.geomajas.plugin.wmsclient.client.layer.WmsLayerImpl;
import org.geomajas.plugin.wmsclient.client.layer.config.WmsLayerConfiguration;
import org.geomajas.plugin.wmsclient.client.layer.config.WmsTileConfiguration;

/**
 * SmartGWT implementation of the client WMS layer. This is an extension of the GWT2 wms layer adding support for the
 * SmartGWT map.
 * 
 * @author Oliver May
 */
public class ClientWmsLayer extends WmsLayerImpl {

    /**
     * Create a new Client WMS layer.
     * 
     * @param title
     *            the title
     * @param wmsConfig
     *            the wms configuration
     * @param tileConfig
     *            the tile configuration
     */
    public ClientWmsLayer( String title, WmsLayerConfiguration wmsConfig, WmsTileConfiguration tileConfig ) {
        super( title, wmsConfig, tileConfig );
    }

    /**
     * Set the map model on this layer.
     * 
     * @param mapModel
     *            the mapModel.
     */
    public void setMapModel( MapModel mapModel ) {
        SmartGwtViewport viewPort = new SmartGwtViewport();
        viewPort.initialize( mapModel.getMapInfo(), null );
        setViewPort( viewPort );
    }

}