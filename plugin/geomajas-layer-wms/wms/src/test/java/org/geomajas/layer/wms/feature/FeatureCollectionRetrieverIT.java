package org.geomajas.layer.wms.feature;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;

import org.junit.Ignore;
import org.junit.Test;

import com.vividsolutions.jts.geom.Envelope;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
@Ignore
public class FeatureCollectionRetrieverIT {

	private static final String WFS_URL = "http://178.63.99.239:8080/xplansyn-wfs/services?request=GetFeature&version=1.1.0&typename=xplansyn:BP_Plan&resulttype=hits&service=WFS";

	private static final Envelope BBOX = new Envelope(1, 2, 6, 7);

	private FeatureCollectionRetriever collectionRetriever = new FeatureCollectionRetriever();

	@Test
	public void testRetrieveGetFeatureResponseAsInputStream() throws Exception {
		InputStream stream = collectionRetriever.retrieveGetFeatureResponseAsInputStream(WFS_URL, BBOX);
		assertThat(stream, is(notNullValue()));
	}

}