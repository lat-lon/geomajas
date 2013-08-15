package org.geomajas.command.dto;

import java.util.ArrayList;
import java.util.List;

import org.geomajas.command.CommandRequest;
import org.geomajas.geometry.Bbox;


public class GetMapTilesRequest implements CommandRequest {

	private static final long serialVersionUID = 1140L;

	private String crs;

	private Bbox bbox;

	private double scale;

	private List<String> visibleLayers = new ArrayList<String>();
	
	/**
	 * Command name for this request.
	 *
	 * @since 1.13.0
	 **/
	public static final String COMMAND = "command.render.GetMapTiles";

	
	public String getCrs() {
		return crs;
	}

	
	public void setCrs(String crs) {
		this.crs = crs;
	}

	
	public Bbox getBbox() {
		return bbox;
	}

	
	public void setBbox(Bbox bbox) {
		this.bbox = bbox;
	}

	
	public double getScale() {
		return scale;
	}

	
	public void setScale(double scale) {
		this.scale = scale;
	}


	public void setVisibleLayers(List<String> visibleLayers) {
		this.visibleLayers = visibleLayers;
	}


	public List<String> getVisibleLayers() {
		return visibleLayers;
	}
	
	
	
}
