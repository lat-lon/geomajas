package org.geomajas.widget.layer.client.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geomajas.gwt.client.map.MapModel;
import org.geomajas.gwt.client.map.event.MapModelChangedEvent;
import org.geomajas.gwt.client.map.event.MapModelChangedHandler;
import org.geomajas.gwt.client.map.layer.Layer;
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
public class DragAndDropLayerList extends Canvas implements MapModelChangedHandler {

	private final MapModel mapModel;

	private boolean isOnMapModelChangedForcedFromThis = false;

	private ListGrid orderedLayerList;
	
	private List<Integer> positionsOfNonRasterLayer = new ArrayList<Integer>();

	/**
	 * Creates a drag and drop list grid of all raster layers with direct connection to the map (reordering of drawing
	 * order).
	 * 
	 * @param mapWidget
	 */
	public DragAndDropLayerList(final MapWidget mapWidget) {
		super();
		this.mapModel = mapWidget.getMapModel();
		this.mapModel.addMapModelChangedHandler(this);
		this.orderedLayerList = createList();
		this.addChild(orderedLayerList);
	}

	@Override
	public void onMapModelChanged(MapModelChangedEvent event) {
		if (!isOnMapModelChangedForcedFromThis) {
			fillList(orderedLayerList);
			orderedLayerList.redraw();
		} else {
			updatePositionsOfNonRasterLayers ();
		}
		isOnMapModelChangedForcedFromThis = false;
	}

	private ListGrid createList() {
		ListGrid listGrid = createListGrid();
		fillList(listGrid);
		return listGrid;
	}

	private ListGrid createListGrid() {
		ListGrid listGrid = new ListGrid();
		listGrid.setWidth(300);
		listGrid.setHeight(500);
		listGrid.setShowHeader(false);
		listGrid.setShowAllRecords(true);
		listGrid.setFields(new ListGridField("label"));
		listGrid.setSelectionType(SelectionStyle.SINGLE);
		listGrid.setCanReorderRecords(true);
		createReorderingOfMapFunctionality(listGrid);
		return listGrid;
	}

	private void createReorderingOfMapFunctionality(final ListGrid listGrid) {
		listGrid.addDropCompleteHandler(new DropCompleteHandler() {

			@Override
			public void onDropComplete(DropCompleteEvent event) {
				Record record = event.getTransferredRecords()[0];
				RasterLayer layer = retrieveTransferredLayer(record);
				int position = calculatePosition(listGrid, record);
				isOnMapModelChangedForcedFromThis = true;
				mapModel.moveRasterLayer(layer, position);
			}

			private RasterLayer retrieveTransferredLayer(Record record) {
				String layerId = record.getAttribute("id");
				return mapModel.getRasterLayer(layerId);
			}

			private int calculatePosition(final ListGrid listGrid, Record record) {
				int recordIndex = listGrid.getRecordIndex(record);
				int reversedRecordIndex = listGrid.getTotalRows() - recordIndex - 1;
				RasterLayerPositionCalculator rasterLayerPositionCalculator = new RasterLayerPositionCalculator();
				return rasterLayerPositionCalculator.calculatePosition(reversedRecordIndex, positionsOfNonRasterLayer);
			}
		});
	}

	private void fillList(ListGrid listGrid) {
		ListGridRecord[] layerList = createLayerList();
		listGrid.setData(layerList);
	}

	private ListGridRecord[] createLayerList() {
		List<ListGridRecord> layerList = new ArrayList<ListGridRecord>();
		addLayersToList(layerList);
		Collections.reverse(layerList);
		return (ListGridRecord[]) layerList.toArray();
	}

	private void addLayersToList(List<ListGridRecord> layerList) {
	    positionsOfNonRasterLayer.clear();
		List<Layer<?>> layers = mapModel.getLayers();
		int currentLayerPosition = 0;
		for (Layer<?> layer : layers) {
		    if (layer instanceof RasterLayer) {
		        ListGridRecord record = createListGridRecord(layer);
		        layerList.add(record);
		    } else {
		        positionsOfNonRasterLayer.add(currentLayerPosition);
		    }
		    currentLayerPosition++;
		}
	}

	private ListGridRecord createListGridRecord(Layer<?> layer) {
		ListGridRecord record = new ListGridRecord();
		record.setAttribute("label", layer.getLabel());
		record.setAttribute("id", layer.getId());
		return record;
	}
	
	private void updatePositionsOfNonRasterLayers () {
		positionsOfNonRasterLayer.clear();
		List<Layer<?>> layers = mapModel.getLayers();
		int currentLayerPosition = 0;
		for (Layer<?> layer : layers) {
		    if (!(layer instanceof RasterLayer)) {
		        positionsOfNonRasterLayer.add(currentLayerPosition);
		    }
		    currentLayerPosition++;
		}
	}

}
