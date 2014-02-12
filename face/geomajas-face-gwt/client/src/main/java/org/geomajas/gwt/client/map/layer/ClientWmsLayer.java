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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geomajas.configuration.client.ClientMapInfo;
import org.geomajas.geometry.Coordinate;
import org.geomajas.geometry.Geometry;
import org.geomajas.geometry.Matrix;
import org.geomajas.gwt.client.map.MapModel;
import org.geomajas.gwt.client.map.MapView;
import org.geomajas.gwt.client.map.RenderSpace;
import org.geomajas.gwt.client.spatial.Bbox;
import org.geomajas.gwt.client.util.Log;
import org.geomajas.plugin.wmsclient.client.layer.WmsLayerImpl;
import org.geomajas.plugin.wmsclient.client.layer.config.WmsLayerConfiguration;
import org.geomajas.plugin.wmsclient.client.layer.config.WmsTileConfiguration;
import org.geomajas.puregwt.client.map.MapEventBus;
import org.geomajas.puregwt.client.map.ViewPort;
import org.geomajas.puregwt.client.map.ZoomStrategy;

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
        SmartGwtViewport viewPort = new SmartGwtViewport( mapModel );
        viewPort.initialize( mapModel.getMapInfo(), null );
        setViewPort( viewPort );
    }

    /**
     * SmartGwt implementation of the GWT2 viewport. This is intended for internal use in the client WMS layer, as it
     * does not implement all ViewPort methods. It can however be extended to fully support everything.
     */
    private class SmartGwtViewport implements ViewPort {

        private final MapModel mapModel;

        private final List<Double> scales = new ArrayList<Double>();

        private ZoomStrategy zoomStrategy;

        public SmartGwtViewport( MapModel mapModel ) {
            this.mapModel = mapModel;

            // Calculate fixed scales based on the resolutions.
            // FIXME: what to do when no fixed resolutions exist.
            if ( mapModel.getMapView().getResolutions() == null || mapModel.getMapView().getResolutions().size() < 1 ) {
                RuntimeException e = new RuntimeException( "Error while adding Client WMS layer, "
                                                           + "the map should define a list of resolutions." );
                Log.logError( "Error while adding Client WMS layer.", e );
                throw e;
            }
            for ( Double resolution : mapModel.getMapView().getResolutions() ) {
                scales.add( 1 / resolution );
            }
            Collections.sort( scales );
        }

        @Override
        public org.geomajas.geometry.Bbox getMaximumBounds() {
            return mapModel.getMapView().getMaxBounds().toDtoBbox();
        }

        @Override
        public int getMapWidth() {
            return mapModel.getMapView().getWidth();
        }

        @Override
        public int getMapHeight() {
            return mapModel.getMapView().getWidth();
        }

        @Override
        public String getCrs() {
            return mapModel.getCrs();
        }

        @Override
        public Coordinate getPosition() {
            return mapModel.getMapView().getPanOrigin();
        }

        @Override
        public double getScale() {
            return mapModel.getMapView().getCurrentScale();
        }

        @Override
        public org.geomajas.geometry.Bbox getBounds() {
            return mapModel.getMapView().getBounds().toDtoBbox();
        }

        @Override
        public void applyPosition( Coordinate coordinate ) {
            mapModel.getMapView().setCenterPosition( coordinate );
        }

        @Override
        public void applyScale( double v ) {
            throw new UnsupportedOperationException();
            // applyScale(v, ZoomOption.FREE);
        }

        private MapView.ZoomOption convertZoomOption( org.geomajas.puregwt.client.map.ZoomStrategy.ZoomOption zoomOption ) {
            if ( zoomOption == null )
                return MapView.ZoomOption.LEVEL_CLOSEST;
            switch ( zoomOption ) {
            // case FREE:
            // return MapView.ZoomOption.EXACT;
            case LEVEL_FIT:
                return MapView.ZoomOption.LEVEL_FIT;
            case LEVEL_CLOSEST:
            default:
                return MapView.ZoomOption.LEVEL_CLOSEST;
            }
        }

        @Override
        public void applyBounds( org.geomajas.geometry.Bbox bbox ) {
            applyBounds( bbox, null );
        }

        @Override
        public void applyBounds( org.geomajas.geometry.Bbox bounds,
                                 org.geomajas.puregwt.client.map.ZoomStrategy.ZoomOption zoomOption ) {
            mapModel.getMapView().applyBounds( new Bbox( bounds ), convertZoomOption( zoomOption ) );
        }

        @Override
        public void initialize( ClientMapInfo mapInfo, MapEventBus eventBus ) {
            setZoomStrategy( new FreeForAllZoomStrategy( mapInfo, null ) );
        }

        @Override
        public void setMapSize( int mapWidth, int mapHeight ) {
            // TODO Auto-generated method stub

        }

        @Override
        public ZoomStrategy getZoomStrategy() {
            return zoomStrategy;
        }

        @Override
        public void setZoomStrategy( ZoomStrategy zoomStrategy ) {
            this.zoomStrategy = zoomStrategy;

        }

        @Override
        public void dragToPosition( Coordinate coordinate ) {
            // TODO Auto-generated method stub

        }

        @Override
        public void applyScale( double scale, Coordinate rescalePoint ) {
            // TODO Auto-generated method stub

        }

        @Override
        public void dragToScale( double scale ) {
            // TODO Auto-generated method stub

        }

        @Override
        public void dragToScale( double scale, Coordinate rescalePoint ) {
            // TODO Auto-generated method stub

        }

        @Override
        public Coordinate transform( Coordinate coordinate, RenderSpace from, RenderSpace to ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Geometry transform( Geometry geometry, RenderSpace from, RenderSpace to ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.geomajas.geometry.Bbox transform( org.geomajas.geometry.Bbox bbox, RenderSpace from, RenderSpace to ) {
            throw new UnsupportedOperationException();
        }

        public Matrix getTransformationMatrix( RenderSpace from, RenderSpace to ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Matrix getTranslationMatrix( RenderSpace from, RenderSpace to ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isInitialized() {
            throw new UnsupportedOperationException();
        }

    }

    // @Override
    // public double getMinimumScale() {
    // if (fixedScales.size() == 0) {
    // return 0;
    // }
    // return fixedScales.get(0);
    // }
    //
    // @Override
    // public double getMaximumScale() {
    // if (fixedScales.size() == 0) {
    // return Double.MAX_VALUE;
    // }
    // return fixedScales.get(fixedScales.size() - 1);
    // }
    //
    // @Override
    // public int getFixedScaleCount() {
    // return fixedScales.size();
    // }
    //
    // @Override
    // public double getFixedScale(int index) {
    // if (index < 0) {
    // throw new IllegalArgumentException("Scale index cannot be found.");
    // }
    // if (index >= fixedScales.size()) {
    // throw new IllegalArgumentException("Scale index cannot be found.");
    // }
    // return fixedScales.get(index);
    // }
    //
    // @Override
    // public int getFixedScaleIndex(double scale) {
    // double minimumScale = getMinimumScale();
    // if (scale <= minimumScale) {
    // return 0;
    // }
    // double maximumScale = getMaximumScale();
    // if (scale >= maximumScale) {
    // return fixedScales.size() - 1;
    // }
    //
    // for (int i = 0; i < fixedScales.size(); i++) {
    // double lower = fixedScales.get(i);
    // double upper = fixedScales.get(i + 1);
    // if (scale <= upper && scale > lower) {
    // if (Math.abs(upper - scale) >= Math.abs(lower - scale)) {
    // return i;
    // } else {
    // return i + 1;
    // }
    // }
    // }
    // return 0;
    // }
    // @Override
    // public void applyScale( double v, ZoomOption zoomOption ) {
    // mapModel.getMapView().setCurrentScale( v, convertZoomOption( zoomOption ) );
    // }
    //
    // @Override
    // public View getView() {
    // return new View( getPosition(), getScale() );
    // }
    //
    // @Override
    // public void registerAnimation( NavigationAnimation navigationAnimation ) {
    // throw new UnsupportedOperationException();
    // }
    //
    // @Override
    // public void applyView( View view ) {
    // throw new UnsupportedOperationException();
    // }
    //
    // @Override
    // public void applyView( View view, ZoomOption zoomOption ) {
    // throw new UnsupportedOperationException();
    // }
    //
    // @Override
    // public ViewPortTransformationService getTransformationService() {
    // throw new UnsupportedOperationException();
    // }
    //
    // @Override
    // public double toScale( double v ) {
    // throw new UnsupportedOperationException();
    // }
    //
    // @Override
    // public org.geomajas.geometry.Bbox asBounds( View view ) {
    // throw new UnsupportedOperationException();
    // }
    //
    // @Override
    // public View asView( org.geomajas.geometry.Bbox bbox, ZoomOption zoomOption ) {
    // throw new UnsupportedOperationException();
    // }

}
