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
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geomajas.configuration.RasterLayerInfo;
import org.geomajas.global.GeomajasException;
import org.geomajas.layer.Layer;
import org.geomajas.layer.LayerException;
import org.geomajas.layer.RasterLayer;
import org.geomajas.layer.tile.RasterTile;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

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
		List<Layer<?>> aggregated = service.aggregate(layerList);
		assertEquals(layerList.get(0).getId(), aggregated.get(0).getId());
	}

	@Test
	public void testAggregateSameBaseUrlShouldContainOneAggregatedWmsLayer() throws GeomajasException {
		List<Layer<?>> layerList = createConfiguredWmsLayerListSameBaseUrls();
		List<Layer<?>> aggregated = service.aggregate(layerList);
		assertEquals(1, aggregated.size());
		Layer<?> firstLayer = aggregated.get(0);
		assertEquals(AggregatedWmsLayer.class, firstLayer.getClass());
	}

	@Test
	public void testAggregateAllDifferentBaseUrlShouldReturnUnchanged() throws GeomajasException {
		List<Layer<?>> layerList = createConfiguredWmsLayerListDifferentBaseUrls();
		List<Layer<?>> aggregated = service.aggregate(layerList);
		assertEquals(layerList.size(), aggregated.size());
		assertEquals(layerList.get(0).getId(), aggregated.get(0).getId());
		assertEquals(layerList.get(1).getId(), aggregated.get(1).getId());
		assertEquals(layerList.get(2).getId(), aggregated.get(2).getId());
	}

	@Test
	public void testAggregateMixedBaseUrlShouldReturnAggregatedOnlySameBaseUrlMidStreakScenario()
			throws GeomajasException {
		List<Layer<?>> layerList = createConfiguredWmsLayerMixedBaseUrlsMidStreak();
		List<Layer<?>> aggregated = service.aggregate(layerList);
		assertEquals(3, aggregated.size());

		Layer<?> firstLayer = aggregated.get(0);
		Layer<?> secondLayer = aggregated.get(1);
		Layer<?> thirdLayer = aggregated.get(2);

		assertThat(firstLayer, isWmsLayerWithId("id1"));
		assertThat(secondLayer, isAggregatedWmsLayerWithId("id2id3"));
		assertThat(thirdLayer, isWmsLayerWithId("id4"));
	}

	@Test
	public void testAggregateMixedBaseUrlShouldReturnAggregatedOnlySameBaseUrlStartStreakScenario()
			throws GeomajasException {
		List<Layer<?>> layerList = createConfiguredWmsLayerMixedBaseUrlsStartStreak();
		List<Layer<?>> aggregated = service.aggregate(layerList);
		assertEquals(3, aggregated.size());

		Layer<?> firstLayer = aggregated.get(0);
		Layer<?> secondLayer = aggregated.get(1);
		Layer<?> thirdLayer = aggregated.get(2);

		assertThat(firstLayer, isAggregatedWmsLayerWithId("id1id2"));
		assertThat(secondLayer, isWmsLayerWithId("id3"));
		assertThat(thirdLayer, isWmsLayerWithId("id4"));
	}

	@Test
	public void testAggregateMixedBaseUrlShouldReturnAggregatedOnlySameBaseUrlEndStreakScenario()
			throws GeomajasException {
		List<Layer<?>> layerList = createConfiguredWmsLayerMixedBaseUrlsEndStreak();
		List<Layer<?>> aggregated = service.aggregate(layerList);
		assertEquals(3, aggregated.size());

		Layer<?> firstLayer = aggregated.get(0);
		Layer<?> secondLayer = aggregated.get(1);
		Layer<?> thirdLayer = aggregated.get(2);

		assertThat(firstLayer, isWmsLayerWithId("id1"));
		assertThat(secondLayer, isWmsLayerWithId("id2"));
		assertThat(thirdLayer, isAggregatedWmsLayerWithId("id3id4"));
	}

	@Test
	public void testAggregateMixedBaseUrlShouldReturnNotAggregatedNoStreak() throws GeomajasException {
		List<Layer<?>> layerList = createConfiguredWmsLayerMixedBaseUrlsNoStreak();
		List<Layer<?>> aggregated = service.aggregate(layerList);
		assertEquals(4, aggregated.size());

		Layer<?> firstLayer = aggregated.get(0);
		Layer<?> secondLayer = aggregated.get(1);
		Layer<?> thirdLayer = aggregated.get(2);
		Layer<?> fourthLayer = aggregated.get(3);

		assertThat(firstLayer, isWmsLayerWithId("id1"));
		assertThat(secondLayer, isWmsLayerWithId("id2"));
		assertThat(thirdLayer, isWmsLayerWithId("id3"));
		assertThat(fourthLayer, isWmsLayerWithId("id4"));
	}

	@Test
	public void testAggregateMixedBaseUrlShouldReturnAggregatedDoubleStreak() throws GeomajasException {
		List<Layer<?>> layerList = createConfiguredWmsLayerMixedBaseUrlsDoubleStreak();
		List<Layer<?>> aggregated = service.aggregate(layerList);
		assertEquals(3, aggregated.size());

		Layer<?> firstLayer = aggregated.get(0);
		Layer<?> secondLayer = aggregated.get(1);
		Layer<?> thirdLayer = aggregated.get(2);

		assertThat(firstLayer, isAggregatedWmsLayerWithId("id1id2"));
		assertThat(secondLayer, isWmsLayerWithId("id3"));
		assertThat(thirdLayer, isAggregatedWmsLayerWithId("id4id5"));
	}

	@Test
	public void testAggregateWithNonWmsLayerShouldLeaveOutNonWmsLayer() throws GeomajasException {
		List<Layer<?>> layerList = createConfiguredWmsLayerNonWmsLayerWithin();
		List<Layer<?>> aggregated = service.aggregate(layerList);
		assertEquals(3, aggregated.size());

		Layer<?> firstLayer = aggregated.get(0);
		Layer<?> secondLayer = aggregated.get(1);
		Layer<?> thirdLayer = aggregated.get(2);

		assertThat(firstLayer, isAggregatedWmsLayerWithId("id1id2"));
		assertThat(secondLayer, isTestLayerWithId("id3"));
		assertThat(thirdLayer, isAggregatedWmsLayerWithId("id4id5"));
	}

	@Test
	public void testAggregateWithNonWmsLayerWithinSameUrlShouldLeaveOutNonWmsLayer() throws GeomajasException {
		List<Layer<?>> layerList = createConfiguredWmsLayerNonWmsLayerWithinSameBaseUrl();
		List<Layer<?>> aggregated = service.aggregate(layerList);
		assertEquals(3, aggregated.size());

		Layer<?> firstLayer = aggregated.get(0);
		Layer<?> secondLayer = aggregated.get(1);
		Layer<?> thirdLayer = aggregated.get(2);

		assertThat(firstLayer, isWmsLayerWithId("id1"));
		assertThat(secondLayer, isTestLayerWithId("id2"));
		assertThat(thirdLayer, isWmsLayerWithId("id3"));
	}

	private Matcher<Layer<?>> isTestLayerWithId(String expectedId) {
		Class<?> expectedClass = TestRasterLayer.class;
		return isRasterLayerWithId(expectedId, expectedClass);
	}

	private Matcher<Layer<?>> isAggregatedWmsLayerWithId(String expectedId) {
		Class<?> expectedClass = AggregatedWmsLayer.class;
		return isRasterLayerWithId(expectedId, expectedClass);
	}

	private Matcher<Layer<?>> isWmsLayerWithId(final String expectedId) {
		Class<?> expectedClass = WmsLayer.class;
		return isRasterLayerWithId(expectedId, expectedClass);
	}

	private Matcher<Layer<?>> isRasterLayerWithId(final String expectedId, final Class<?> expectedClass) {
		return new BaseMatcher<Layer<?>>() {

			@Override
			public boolean matches(Object item) {
				Layer<?> rasterLayer = (Layer<?>) item;
				if (rasterLayer.getClass().equals(expectedClass)) {
					return expectedId.equals(rasterLayer.getId());
				}
				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("RasterLayer should be a WmsLayer with id " + expectedId);
			}
		};
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

	private List<Layer<?>> createConfiguredWmsLayerMixedBaseUrlsMidStreak() throws LayerException {
		List<Layer<?>> layerList = new ArrayList<Layer<?>>();
		layerList.add(createConfiguredWmsLayer("bs1", "ds1", "id1"));
		layerList.add(createConfiguredWmsLayer("bs2", "ds2", "id2"));
		layerList.add(createConfiguredWmsLayer("bs2", "ds3", "id3"));
		layerList.add(createConfiguredWmsLayer("bs3", "ds4", "id4"));
		return layerList;
	}

	private List<Layer<?>> createConfiguredWmsLayerMixedBaseUrlsStartStreak() throws LayerException {
		List<Layer<?>> layerList = new ArrayList<Layer<?>>();
		layerList.add(createConfiguredWmsLayer("bs1", "ds1", "id1"));
		layerList.add(createConfiguredWmsLayer("bs1", "ds2", "id2"));
		layerList.add(createConfiguredWmsLayer("bs2", "ds3", "id3"));
		layerList.add(createConfiguredWmsLayer("bs3", "ds4", "id4"));
		return layerList;
	}

	private List<Layer<?>> createConfiguredWmsLayerMixedBaseUrlsEndStreak() throws LayerException {
		List<Layer<?>> layerList = new ArrayList<Layer<?>>();
		layerList.add(createConfiguredWmsLayer("bs1", "ds1", "id1"));
		layerList.add(createConfiguredWmsLayer("bs2", "ds2", "id2"));
		layerList.add(createConfiguredWmsLayer("bs3", "ds3", "id3"));
		layerList.add(createConfiguredWmsLayer("bs3", "ds4", "id4"));
		return layerList;
	}

	private List<Layer<?>> createConfiguredWmsLayerMixedBaseUrlsNoStreak() throws LayerException {
		List<Layer<?>> layerList = new ArrayList<Layer<?>>();
		layerList.add(createConfiguredWmsLayer("bs1", "ds1", "id1"));
		layerList.add(createConfiguredWmsLayer("bs2", "ds2", "id2"));
		layerList.add(createConfiguredWmsLayer("bs1", "ds3", "id3"));
		layerList.add(createConfiguredWmsLayer("bs2", "ds4", "id4"));
		return layerList;
	}

	private List<Layer<?>> createConfiguredWmsLayerMixedBaseUrlsDoubleStreak() throws LayerException {
		List<Layer<?>> layerList = new ArrayList<Layer<?>>();
		layerList.add(createConfiguredWmsLayer("bs1", "ds1", "id1"));
		layerList.add(createConfiguredWmsLayer("bs1", "ds2", "id2"));
		layerList.add(createConfiguredWmsLayer("bl2", "ds3", "id3"));
		layerList.add(createConfiguredWmsLayer("bs3", "ds3", "id4"));
		layerList.add(createConfiguredWmsLayer("bs3", "ds4", "id5"));
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

	private List<Layer<?>> createConfiguredWmsLayerNonWmsLayerWithinSameBaseUrl() throws LayerException {
		List<Layer<?>> layerList = new ArrayList<Layer<?>>();
		layerList.add(createConfiguredWmsLayer("bs1", "ds1", "id1"));
		layerList.add(createDummyRasterLayer("id2"));
		layerList.add(createConfiguredWmsLayer("bs1", "ds3", "id3"));
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
			// TODO Auto-generated method stub
			return null;
		}

	}

}
