package org.geomajas.widget.layer.client.widget;

import org.geomajas.gwt.client.widget.KeepInScreenWindow;
import org.geomajas.widget.layer.client.LayerMessages;

import com.google.gwt.core.client.GWT;

/**
 * <p>
 * The <code>DragAndDropLayerWindow</code> is a window that shows a list of raster layers which can be reordered.
 * </p>
 * 
 * @author Dirk Stenger
 */
public class DragAndDropLayerListWindow extends KeepInScreenWindow {

    private static final LayerMessages MESSAGES = GWT.create( LayerMessages.class );

    /**
     * Constructs a DragAndDropLayerWindow allowing to reorder raster layers.
     * 
     * @param layerList
     */
    public DragAndDropLayerListWindow( DragAndDropLayerList layerList ) {
        buildWidget( layerList );
    }

    private void buildWidget( DragAndDropLayerList layerList ) {
        setTitle( MESSAGES.dragAndDropLayerListWindowTitle() );
        setAutoSize( true );
        centerInPage();
        addItem( layerList );
        show();
    }

}
