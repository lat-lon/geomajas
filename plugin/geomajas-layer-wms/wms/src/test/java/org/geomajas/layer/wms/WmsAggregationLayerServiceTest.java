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

package org.geomajas.layer.wms;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geomajas.configuration.RasterLayerInfo;
import org.geomajas.global.GeomajasException;
import org.geomajas.layer.LayerException;
import org.geomajas.layer.RasterLayer;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link WmsAggregationLayerServiceImpl}.
 * 
 * @author Alexander Erben
 * @author Lyn Goltz
 */
public class WmsAggregationLayerServiceTest {

	private static final String LAYER_ID = "id";

	private static final String DATA_SOURCE_NAME = "dataSource";

	private WmsAggregationLayerServiceImpl service;

	@Before
	public void setUp() {
		service = new WmsAggregationLayerServiceImpl();
	}

	@Test
	public void testCreateAggregatedLayerWithSingleWmsLayerShouldHaveCorrectSize() throws GeomajasException {
		List<RasterLayer> layerList = createSingletonWmsLayerList();
		AggregatedWmsLayer aggregatedLayer = (AggregatedWmsLayer) service.createAggregatedLayer(layerList);
		assertEquals(layerList.size(), aggregatedLayer.getWmsLayers().size());
	}
	
	@Test
	public void testCreateAggregatedLayerWithMultipleWmsLayerShouldHaveCorrectSize() throws GeomajasException {
		List<RasterLayer> layerList = createConfiguredWmsLayerList();
		AggregatedWmsLayer aggregatedLayer = (AggregatedWmsLayer) service.createAggregatedLayer(layerList);
		assertEquals(layerList.size(), aggregatedLayer.getWmsLayers().size());
	}

	private RasterLayer createConfiguredWmsLayer() throws LayerException {
		return createConfiguredWmsLayer(DATA_SOURCE_NAME, LAYER_ID);
	}
	
	private RasterLayer createConfiguredWmsLayer(String dsName, String id) throws LayerException {
		WmsLayer layer = new WmsLayer();
		RasterLayerInfo rasterLayerInfo = new RasterLayerInfo();
		rasterLayerInfo.setDataSourceName(dsName);
		layer.setLayerInfo(rasterLayerInfo);
		layer.setId(id);
		return layer;
	}
	
	private List<RasterLayer> createConfiguredWmsLayerList() throws LayerException {
		List<RasterLayer> layerList = new ArrayList<RasterLayer>();
		layerList.add(createConfiguredWmsLayer("ds1","id1"));
		layerList.add(createConfiguredWmsLayer("ds2","id2"));
		layerList.add(createConfiguredWmsLayer("ds3","id3"));
		return layerList;
	}

	private List<RasterLayer> createSingletonWmsLayerList() throws LayerException {
		RasterLayer layer = createConfiguredWmsLayer();
		List<RasterLayer> layerList = Collections.singletonList(layer);
		return layerList;
	}

}
