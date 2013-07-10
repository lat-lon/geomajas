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
 * Test for {@link WmsAggregationLayerServiceImpl}.
 * 
 * @author Alexander Erben
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
	public void testCreateAggregatedLayerWithSingleWmsLayerShouldHaveCorrectDataSource() throws GeomajasException {
		List<RasterLayer> layerList = createSingletonWmsLayerList();
		RasterLayer aggregatedLayer = service.createAggregatedLayer(layerList);
		assertEquals(DATA_SOURCE_NAME, aggregatedLayer.getLayerInfo().getDataSourceName());
	}

	@Test
	public void testCreateAggregatedLayerWithSingleWmsLayerShouldHaveCorrectId() throws GeomajasException {
		List<RasterLayer> layerList = createSingletonWmsLayerList();
		RasterLayer aggregatedLayer = service.createAggregatedLayer(layerList);
		assertEquals(LAYER_ID, aggregatedLayer.getId());
	}
	
	@Test
	public void testCreateAggregatedLayerWithMultipleWmsLayerShouldHaveCorrectDataSource() throws GeomajasException {
		List<RasterLayer> layerList = createConfiguredWmsLayerList();
		RasterLayer aggregatedLayer = service.createAggregatedLayer(layerList);
		assertEquals("ds1,ds2,ds3", aggregatedLayer.getLayerInfo().getDataSourceName());
	}

	@Test
	public void testCreateAggregatedLayerWithMultipleWmsLayerShouldHaveCorrectId() throws GeomajasException {
		List<RasterLayer> layerList = createConfiguredWmsLayerList();
		RasterLayer aggregatedLayer = service.createAggregatedLayer(layerList);
		assertEquals("id1id2id3", aggregatedLayer.getId());
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
