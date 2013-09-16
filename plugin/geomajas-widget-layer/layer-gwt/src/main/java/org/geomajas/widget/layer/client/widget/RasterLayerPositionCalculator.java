package org.geomajas.widget.layer.client.widget;

import java.util.List;

/**
 * The <code>RasterLayerPositionCalculator</code> calculates the new position of raster layers in the layer list of the
 * map model.
 * 
 * @author Dirk Stenger
 * @author Lyn Goltz
 * 
 */
public class RasterLayerPositionCalculator {

	/**
	 * Calculate the new position of raster layers in the layer list of the map model.
	 * 
	 * @param currentPositionInLayerGrid
	 *            the new position of the raster layer in the list of the <code>DragAndDropLayerList</code>
	 * @param positionsOfNonRasterLayers
	 *            contains the positions of non raster layers in the layer list of the map model, never
	 *            <code>null</code>.
	 * @return new position of the raster layer in the layer list of the map model.
	 * 
	 * @throws <code>NullPointerException</code> if postitionsOfNonRasterLayers is <code>null</code>.
	 */
	public int calculatePosition(int currentPositionInLayerGrid, List<Integer> positionsOfNonRasterLayers) {
		int newPositionInMapModel = currentPositionInLayerGrid;
		for (int position : positionsOfNonRasterLayers) {
			if (newPositionInMapModel >= position) {
				newPositionInMapModel++;
			}
		}
		return newPositionInMapModel;
	}
}
