package org.geomajas.widget.layer.client.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geomajas.gwt.client.map.layer.RasterLayer;
import org.geomajas.gwt.client.widget.MapWidget;

import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.DropCompleteEvent;
import com.smartgwt.client.widgets.events.DropCompleteHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;

/**
 * A raster layer list widget with drag and drop functionality and direct connection to the map (reordering of drawing
 * order).
 * 
 * @author Dirk Stenger
 */
public class DragAndDropLayerList extends Canvas {

    private final MapWidget mapWidget;

    /**
     * Creates a drag and drop list grid of all raster layers with direct connection to the map (reordering of drawing
     * order).
     * 
     * @param mapWidget
     */
    public DragAndDropLayerList( final MapWidget mapWidget ) {
        super();
        this.mapWidget = mapWidget;

        ListGrid list = createList();
        this.addChild( list );
    }

    private ListGrid createList() {
        ListGrid listGrid = createListGrid();
        fillList( listGrid );
        return listGrid;
    }

    private ListGrid createListGrid() {
        ListGrid listGrid = new ListGrid();
        listGrid.setWidth( 300 );
        listGrid.setHeight( 500 );
        listGrid.setShowHeader( false );
        listGrid.setShowAllRecords( true );
        listGrid.setFields( new ListGridField( "label" ) );
        listGrid.setSelectionType( SelectionStyle.SINGLE );
        listGrid.setCanReorderRecords( true );
        createReorderingOfMapFunctionality( listGrid );
        return listGrid;
    }

    private void createReorderingOfMapFunctionality( final ListGrid listGrid ) {
        listGrid.addDropCompleteHandler( new DropCompleteHandler() {
            @Override
            public void onDropComplete( DropCompleteEvent event ) {
                Record record = event.getTransferredRecords()[0];
                RasterLayer layer = getTransferredLayer( record );
                int position = getTransferredPosition( listGrid, record );
                mapWidget.getMapModel().moveRasterLayer( layer, position );
            }

            private RasterLayer getTransferredLayer( Record record ) {
                String layerId = record.getAttribute( "id" );
                RasterLayer layer = mapWidget.getMapModel().getRasterLayer( layerId );
                return layer;
            }

            private int getTransferredPosition( final ListGrid listGrid, Record record ) {
                int recordIndex = listGrid.getRecordIndex( record );
                int position = listGrid.getTotalRows() - recordIndex - 1;
                return position;
            }
        } );
    }

    private void fillList( ListGrid listGrid ) {
        ListGridRecord[] layerList = createLayerList();
        listGrid.setData( layerList );
    }

    private ListGridRecord[] createLayerList() {
        List<ListGridRecord> layerList = new ArrayList<ListGridRecord>();
        addLayersToList( layerList );
        Collections.reverse( layerList );
        return (ListGridRecord[]) layerList.toArray();
    }

    private void addLayersToList( List<ListGridRecord> layerList ) {
        List<RasterLayer> layers = mapWidget.getMapModel().getRasterLayers();
        for ( RasterLayer layer : layers ) {
            ListGridRecord record = createListGridRecord( layer );
            layerList.add( record );
        }
    }

    private ListGridRecord createListGridRecord( RasterLayer layer ) {
        ListGridRecord record = new ListGridRecord();
        record.setAttribute( "label", layer.getLabel() );
        record.setAttribute( "id", layer.getId() );
        return record;
    }

}
