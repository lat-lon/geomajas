package org.geomajas.layer.wms.feature;

import static org.geomajas.layer.wms.feature.NumberOfFeaturesInEnvelopeRetriever.UNKNOWN_FEATURECOUNT;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.ClientProtocolException;
import org.geomajas.configuration.RasterLayerInfo;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.vividsolutions.jts.geom.Envelope;

/**
 * @author <a href="mailto:erben@lat-lon.de">Alexander Erben</a>
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class NumberOfFeaturesInEnvelopeFromWfsRetrieverTest {

	private static final String DATASOURCE_NAME = "BP_Plan";

	private static final Envelope BBOX_FAILURE = new Envelope();

	private static final Envelope BBOX_CONTAINING_0_FEATURES = new Envelope(1, 2, 1, 2);

	private static final Envelope BBOX_CONTAINING_253_FEATURES = new Envelope(1, 2, 6, 7);

	private static RasterLayerInfo rasterLayerInfo;

	private NumberOfFeaturesInEnvelopeFromWfsRetriever numberOfFeaturesRetriever;

	@BeforeClass
	public static void initRasterLayerInfo() {
		rasterLayerInfo = mock(RasterLayerInfo.class);
		Mockito.when(rasterLayerInfo.getDataSourceName()).thenReturn(DATASOURCE_NAME);
	}

	@Before
	public void initNumberOfFeaturesRetriever() throws ClientProtocolException, IOException {
		InputStream featuresCollectionStreamWith253Features = NumberOfFeaturesInEnvelopeFromWfsRetrieverTest.class
				.getResourceAsStream("featureResponse_253.xml");
		InputStream featuresCollectionStreamWith0Features = NumberOfFeaturesInEnvelopeFromWfsRetrieverTest.class
				.getResourceAsStream("featureResponse_0.xml");
		numberOfFeaturesRetriever = new NumberOfFeaturesInEnvelopeFromWfsRetriever();

		FeatureCollectionRetriever featureCollectionRetriever = mock(FeatureCollectionRetriever.class);
		when(
				featureCollectionRetriever.retrieveGetFeatureResponseAsInputStream(anyString(),
						eq(BBOX_CONTAINING_0_FEATURES))).thenReturn(featuresCollectionStreamWith0Features);
		when(
				featureCollectionRetriever.retrieveGetFeatureResponseAsInputStream(anyString(),
						eq(BBOX_CONTAINING_253_FEATURES))).thenReturn(featuresCollectionStreamWith253Features);

		when(featureCollectionRetriever.retrieveGetFeatureResponseAsInputStream(anyString(), eq(BBOX_FAILURE)))
				.thenThrow(new IOException());
		numberOfFeaturesRetriever.setFeatureCollectionRetriever(featureCollectionRetriever);
		numberOfFeaturesRetriever.setWfsRequestUrlForBboxFeatureHits("WhatEver");
	}

	@Test
	public void testIsAtLeastOneFeatureInEnvelopeFromCollectionWithFailure() throws Exception {
		boolean isAtLeastOneFeatureInEnvelope = numberOfFeaturesRetriever.isAtLeastOneFeatureInEnvelope(
				rasterLayerInfo, BBOX_FAILURE);
		assertThat(isAtLeastOneFeatureInEnvelope, is(true));
	}

	@Test
	public void testIsAtLeastOneFeatureInEnvelopeFromCollectionWith0Features() throws Exception {
		boolean isAtLeastOneFeatureInEnvelope = numberOfFeaturesRetriever.isAtLeastOneFeatureInEnvelope(
				rasterLayerInfo, BBOX_CONTAINING_0_FEATURES);
		assertThat(isAtLeastOneFeatureInEnvelope, is(false));
	}

	@Test
	public void testIsAtLeastOneFeatureInEnvelopeFromCollectionWith253Features() throws Exception {
		boolean isAtLeastOneFeatureInEnvelope = numberOfFeaturesRetriever.isAtLeastOneFeatureInEnvelope(
				rasterLayerInfo, BBOX_CONTAINING_253_FEATURES);
		assertThat(isAtLeastOneFeatureInEnvelope, is(true));
	}

	@Test
	public void testCountFeaturesInEnvelopeFromCollectionWithFailure() throws Exception {
		int countFeaturesInEnvelope = numberOfFeaturesRetriever.countFeaturesInEnvelope(rasterLayerInfo, BBOX_FAILURE);
		assertThat(countFeaturesInEnvelope, is(UNKNOWN_FEATURECOUNT));
	}

	@Test
	public void testCountFeaturesInEnvelopeFromCollectionWith0Features() throws Exception {
		int countFeaturesInEnvelope = numberOfFeaturesRetriever.countFeaturesInEnvelope(rasterLayerInfo,
				BBOX_CONTAINING_0_FEATURES);
		assertThat(countFeaturesInEnvelope, is(0));
	}

	@Test
	public void testCountFeaturesInEnvelopeFromCollectionWith253Features() throws Exception {
		int countFeaturesInEnvelope = numberOfFeaturesRetriever.countFeaturesInEnvelope(rasterLayerInfo,
				BBOX_CONTAINING_253_FEATURES);
		assertThat(countFeaturesInEnvelope, is(253));
	}

}