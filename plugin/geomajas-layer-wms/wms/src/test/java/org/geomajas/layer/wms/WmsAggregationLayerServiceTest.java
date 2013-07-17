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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geomajas.configuration.RasterLayerInfo;
import org.geomajas.global.GeomajasException;
import org.geomajas.layer.Layer;
import org.geomajas.layer.LayerException;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Tests for {@link WmsAggregationLayerServiceImpl}.
 * 
 * @author Alexander Erben
 * @author Lyn Goltz
 */
public class WmsAggregationLayerServiceTest {

	private static final String BS_NAME = "bs1";

	private static final String LAYER_ID = "id";

	private static final String DATA_SOURCE_NAME = "dataSource";

	private WmsAggregationLayerServiceImpl service;

	@Before
	public void setUp() {
		service = new WmsAggregationLayerServiceImpl();
	}

	@Test
	public void testAggregateSingletonLayerShouldReturnUnchanged() throws GeomajasException {
		List<Layer<?>> layerList = createSingletonWmsLayerList();
		Layer<?> aggregated = service.aggregate(layerList);
		assertEquals(layerList.get(0).getId(), aggregated.getId());
	}

	@Test
	public void testAggregateSameBaseUrlShouldContainOneAggregatedWmsLayer() throws GeomajasException {
		List<Layer<?>> layerList = createConfiguredWmsLayerListSameBaseUrls();
		Layer<?> aggregated = service.aggregate(layerList);
		assertEquals(AggregatedWmsLayer.class, aggregated.getClass());
	}

	@Test(expected = GeomajasException.class)
	public void testAggregateAllDifferentBaseUrlShouldFail() throws GeomajasException {
		List<Layer<?>> layerList = createConfiguredWmsLayerListDifferentBaseUrls();
		service.aggregate(layerList);
	}

	@Test(expected = GeomajasException.class)
	public void testAggregateNullListShouldFail() throws GeomajasException {
		service.aggregate(null);
	}

	@Test(expected = GeomajasException.class)
	public void testAggregateEmptyListShouldFail() throws GeomajasException {
		List<Layer<?>> list = Collections.emptyList();
		service.aggregate(list);
	}

	@Test(expected = GeomajasException.class)
	public void testAggregateMixedLayerTypeShouldFail() throws GeomajasException {
		List<Layer<?>> list = createConfiguredWmsLayerNonWmsLayerWithin();
		service.aggregate(list);
	}

	private Layer<?> createConfiguredWmsLayer() throws LayerException {
		return createConfiguredWmsLayer(BS_NAME, DATA_SOURCE_NAME, LAYER_ID);
	}

	private Layer<?> createConfiguredWmsLayer(String baseServiceUrl, String dsName, String id) throws LayerException {
		WmsLayer layer = new WmsLayer();
		layer.setBaseWmsUrl(baseServiceUrl);
		RasterLayerInfo rasterLayerInfo = new RasterLayerInfo();
		rasterLayerInfo.setDataSourceName(dsName);
		layer.setLayerInfo(rasterLayerInfo);
		layer.setId(id);
		return layer;
	}

	private List<Layer<?>> createConfiguredWmsLayerListDifferentBaseUrls() throws LayerException {
		List<Layer<?>> layerList = new ArrayList<Layer<?>>();
		layerList.add(createConfiguredWmsLayer("bs1", "ds1", "id1"));
		layerList.add(createConfiguredWmsLayer("bs2", "ds2", "id2"));
		layerList.add(createConfiguredWmsLayer("bs3", "ds3", "id3"));
		return layerList;
	}

	private List<Layer<?>> createConfiguredWmsLayerListSameBaseUrls() throws LayerException {
		List<Layer<?>> layerList = new ArrayList<Layer<?>>();
		String baseServiceUrl = "bs4";
		layerList.add(createConfiguredWmsLayer(baseServiceUrl, "ds1", "id1"));
		layerList.add(createConfiguredWmsLayer(baseServiceUrl, "ds2", "id2"));
		layerList.add(createConfiguredWmsLayer(baseServiceUrl, "ds3", "id3"));
		return layerList;
	}

	private List<Layer<?>> createConfiguredWmsLayerNonWmsLayerWithin() throws LayerException {
		List<Layer<?>> layerList = new ArrayList<Layer<?>>();
		layerList.add(createConfiguredWmsLayer("bs1", "ds1", "id1"));
		layerList.add(createConfiguredWmsLayer("bs1", "ds2", "id2"));
		layerList.add(createDummyRasterLayer("id3"));
		layerList.add(createConfiguredWmsLayer("bs3", "ds3", "id4"));
		layerList.add(createConfiguredWmsLayer("bs3", "ds4", "id5"));
		return layerList;
	}

	private Layer<?> createDummyRasterLayer(String id) {
		return new TestRasterLayer(id);
	}

	private List<Layer<?>> createSingletonWmsLayerList() throws LayerException {
		Layer<?> layer = createConfiguredWmsLayer();
		List<Layer<?>> layerList = new ArrayList<Layer<?>>();
		layerList.add(layer);
		return layerList;
	}

	class TestRasterLayer implements Layer<RasterLayerInfo> {

		private String id;

		TestRasterLayer(String id) {
			this.id = id;
		}

		@Override
		@Deprecated
		public CoordinateReferenceSystem getCrs() {
			return null;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public RasterLayerInfo getLayerInfo() {
			return null;
		}

	}

}
