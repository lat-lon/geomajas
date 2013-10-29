package org.geomajas.layer.wms.feature;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.vividsolutions.jts.geom.Envelope;

/**
 * @author <a href="mailto:erben@lat-lon.de">Alexander Erben</a>
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
@Ignore
public class NumberOfFeaturesInEnvelopeFromWfsRetrieverIT {

	private static final String WFS_URL = "http://178.63.99.239:8080/xplansyn-wfs/services?request=GetFeature&version=1.1.0&typename=xplansyn:BP_Plan&resulttype=hits&service=WFS";

	private static final Envelope BBOX_WITH_0_FEATURES = new Envelope(240396, 5649843, 5805770, 5879026);

	private static final Envelope BBOX_WITH_175_FEATURES = new Envelope(240396, 5649843, 5705770, 5879026);

	private static final Envelope BBOX_WITH_253_FEATURES = new Envelope(233396, 486577, 5649843, 5879026);

	private static NumberOfFeaturesInEnvelopeFromWfsRetriever numberOfFeaturesInEnvelopeRetriever;

	@BeforeClass
	public static void initNumberOfFeaturesRetriever() {
		numberOfFeaturesInEnvelopeRetriever = new NumberOfFeaturesInEnvelopeFromWfsRetriever();
		numberOfFeaturesInEnvelopeRetriever.setFeatureCollectionRetriever(new FeatureCollectionRetriever());
		numberOfFeaturesInEnvelopeRetriever.setWfsRequestUrlForBboxFeatureHits(WFS_URL);
	}

	@Test
	public void testIsAtLeastOneFeatureInEnvelopeWith0Features() throws Exception {
		boolean isAtLeastOneFeatureInEnvelope = numberOfFeaturesInEnvelopeRetriever
				.isAtLeastOneFeatureInEnvelope(BBOX_WITH_0_FEATURES);
		assertThat(isAtLeastOneFeatureInEnvelope, is(false));
	}

	@Test
	public void testIsAtLeastOneFeatureInEnvelopeWith175Features() throws Exception {
		boolean isAtLeastOneFeatureInEnvelope = numberOfFeaturesInEnvelopeRetriever
				.isAtLeastOneFeatureInEnvelope(BBOX_WITH_175_FEATURES);
		assertThat(isAtLeastOneFeatureInEnvelope, is(true));
	}

	@Test
	public void testIsAtLeastOneFeatureInEnvelopeWith253Features() throws Exception {
		boolean isAtLeastOneFeatureInEnvelope = numberOfFeaturesInEnvelopeRetriever
				.isAtLeastOneFeatureInEnvelope(BBOX_WITH_253_FEATURES);
		assertThat(isAtLeastOneFeatureInEnvelope, is(true));
	}

	@Test
	public void testCountFeaturesInEnvelopeWith0Features() throws Exception {
		int numberOfFeatures = numberOfFeaturesInEnvelopeRetriever.countFeaturesInEnvelope(BBOX_WITH_0_FEATURES);
		assertThat(numberOfFeatures, is(0));
	}

	@Test
	public void testCountFeaturesInEnvelopeWith175Features() throws Exception {
		int numberOfFeatures = numberOfFeaturesInEnvelopeRetriever.countFeaturesInEnvelope(BBOX_WITH_175_FEATURES);
		assertThat(numberOfFeatures, is(175));
	}

	@Test
	public void testCountFeaturesInEnvelopeWith253Features() throws Exception {
		int numberOfFeatures = numberOfFeaturesInEnvelopeRetriever.countFeaturesInEnvelope(BBOX_WITH_253_FEATURES);
		assertThat(numberOfFeatures, is(253));
	}

}