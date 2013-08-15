package org.geomajas.layer.wms;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Grid definition for a WMS layer. It is used internally in the WMS layer.
 * 
 * @author Jan De Moerloose
 * @author Pieter De Graef
 */
class RasterGrid {

	private final Coordinate lowerLeft;

	private final int xmin;

	private final int ymin;

	private final int xmax;

	private final int ymax;

	private final double tileWidth;

	private final double tileHeight;

	RasterGrid(Coordinate lowerLeft, int xmin, int ymin, int xmax, int ymax, double tileWidth, double tileHeight) {
		super();
		this.lowerLeft = lowerLeft;
		this.xmin = xmin;
		this.ymin = ymin;
		this.xmax = xmax;
		this.ymax = ymax;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
	}

	public Coordinate getLowerLeft() {
		return lowerLeft;
	}

	public double getTileHeight() {
		return tileHeight;
	}

	public double getTileWidth() {
		return tileWidth;
	}

	public int getXmax() {
		return xmax;
	}

	public int getXmin() {
		return xmin;
	}

	public int getYmax() {
		return ymax;
	}

	public int getYmin() {
		return ymin;
	}
}