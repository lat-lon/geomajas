package org.geomajas.layer.wms;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geomajas.configuration.RasterLayerInfo;
import org.geomajas.global.GeomajasException;
import org.geomajas.layer.LayerException;
import org.junit.Test;

/**
 * Tests for {@link AggregatedWmsLayer}.
 * 
 * @author Alexander Erben
 * @author Lyn Goltz
 */
public class AggregatedWmsLayerTest {

	private static final String LAYER_ID = "id";

	private static final String DATA_SOURCE_NAME = "dataSource";

	@Test
	public void testCreateAggregatedLayerWithSingleWmsLayerShouldHaveCorrectDataSource() throws GeomajasException {
		List<WmsLayer> layerList = createSingletonWmsLayerList();
		AggregatedWmsLayer aggregatedLayer = new AggregatedWmsLayer(layerList);
		assertEquals(DATA_SOURCE_NAME, aggregatedLayer.getLayerInfo().getDataSourceName());
	}

	@Test
	public void testCreateAggregatedLayerWithSingleWmsLayerShouldHaveCorrectId() throws GeomajasException {
		List<WmsLayer> layerList = createSingletonWmsLayerList();
		AggregatedWmsLayer aggregatedLayer = new AggregatedWmsLayer(layerList);
		assertEquals(LAYER_ID, aggregatedLayer.getId());
	}

	@Test
	public void testCreateAggregatedLayerWithMultipleWmsLayerShouldHaveCorrectDataSource() throws GeomajasException {
		List<WmsLayer> layerList = createConfiguredWmsLayerList();
		AggregatedWmsLayer aggregatedLayer = new AggregatedWmsLayer(layerList);
		assertEquals("ds1,ds2,ds3", aggregatedLayer.getLayerInfo().getDataSourceName());
	}

	@Test
	public void testCreateAggregatedLayerWithMultipleWmsLayerShouldHaveCorrectId() throws GeomajasException {
		List<WmsLayer> layerList = createConfiguredWmsLayerList();
		AggregatedWmsLayer aggregatedLayer = new AggregatedWmsLayer(layerList);
		assertEquals("id1id2id3", aggregatedLayer.getId());
	}

	private WmsLayer createConfiguredWmsLayer() throws LayerException {
		return createConfiguredWmsLayer(DATA_SOURCE_NAME, LAYER_ID);
	}

	private WmsLayer createConfiguredWmsLayer(String dsName, String id) throws LayerException {
		WmsLayer layer = new WmsLayer();
		RasterLayerInfo rasterLayerInfo = new RasterLayerInfo();
		rasterLayerInfo.setDataSourceName(dsName);
		layer.setLayerInfo(rasterLayerInfo);
		layer.setId(id);
		return layer;
	}

	private List<WmsLayer> createConfiguredWmsLayerList() throws LayerException {
		List<WmsLayer> layerList = new ArrayList<WmsLayer>();
		layerList.add(createConfiguredWmsLayer("ds1", "id1"));
		layerList.add(createConfiguredWmsLayer("ds2", "id2"));
		layerList.add(createConfiguredWmsLayer("ds3", "id3"));
		return layerList;
	}

	private List<WmsLayer> createSingletonWmsLayerList() throws LayerException {
		WmsLayer layer = createConfiguredWmsLayer();
		List<WmsLayer> layerList = Collections.singletonList(layer);
		return layerList;
	}

}
