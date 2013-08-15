package org.geomajas.layer.wms;

/**
 * Single resolution definition for a WMS layer. This class is used internally in the WMS layer, and therefore has
 * no public constructors.
 * 
 * @author Jan De Moerloose
 * @author Pieter De Graef
 */
class Resolution {

	private final double resolution;

	private final int level;

	private final int tileWidth;

	private final int tileHeight;

	/**
	 * Constructor that immediately requires all fields.
	 * 
	 * @param resolution
	 *            The actual resolution value. This is the reverse of the scale.
	 * @param level
	 *            The level in the quad tree.
	 * @param tileWidth
	 *            The width of a tile at the given tile level.
	 * @param tileHeight
	 *            The height of a tile at the given tile level.
	 */
	Resolution(double resolution, int level, int tileWidth, int tileHeight) {
		this.resolution = resolution;
		this.level = level;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
	}

	public int getLevel() {
		return level;
	}

	public int getTileHeightPx() {
		return tileHeight;
	}

	public int getTileWidthPx() {
		return tileWidth;
	}

	public double getTileHeight() {
		return tileHeight * resolution;
	}

	public double getTileWidth() {
		return tileWidth * resolution;
	}

	public double getResolution() {
		return resolution;
	}
}