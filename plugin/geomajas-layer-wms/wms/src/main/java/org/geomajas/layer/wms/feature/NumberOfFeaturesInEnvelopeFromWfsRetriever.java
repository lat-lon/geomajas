package org.geomajas.layer.wms.feature;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Retrieves the number of features from WFS
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class NumberOfFeaturesInEnvelopeFromWfsRetriever implements NumberOfFeaturesInEnvelopeRetriever {

	private static final String NUMBER_OF_FEATURES_ATTRIBUTE_NAME = "numberOfFeatures";

	private final Logger LOG = LoggerFactory.getLogger(NumberOfFeaturesInEnvelopeFromWfsRetriever.class);

	@Autowired
	private String wfsRequestUrlForBboxFeatureHits;

	@Autowired
	private FeatureCollectionRetriever featureCollectionRetriever;

	@Override
	public boolean isAtLeastOneFeatureInEnvelope(Envelope bbox) {
		return countFeaturesInEnvelope(bbox) != 0;
	}

	@Override
	public int countFeaturesInEnvelope(Envelope bbox) {
		InputStream response = null;
		try {
			response = retrieveGetFeatureResponseAsInputStream(bbox);
			return retrieveNumberOfFeatureHitsFromResponseStream(response);
		} catch (Exception e) {
			LOG.error("Number of features from envelope could not be parsed!", e);
		} finally {
			close(response);
		}

		return UNKNOWN_FEATURECOUNT;
	}

	public FeatureCollectionRetriever getFeatureCollectionRetriever() {
		return featureCollectionRetriever;
	}

	public void setFeatureCollectionRetriever(FeatureCollectionRetriever featureCollectionRetriever) {
		this.featureCollectionRetriever = featureCollectionRetriever;
	}

	public String getWfsRequestUrlForBboxFeatureHits() {
		return wfsRequestUrlForBboxFeatureHits;
	}

	public void setWfsRequestUrlForBboxFeatureHits(String wfsRequestUrlForBboxFeatureHits) {
		this.wfsRequestUrlForBboxFeatureHits = wfsRequestUrlForBboxFeatureHits;
	}

	private int retrieveNumberOfFeatureHitsFromResponseStream(InputStream response) throws XMLStreamException,
			FactoryConfigurationError {
		XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(response);
		StAXOMBuilder builder = new StAXOMBuilder(parser);
		OMElement documentElement = builder.getDocumentElement();
		OMAttribute numberOfFeaturesAttribute = documentElement.getAttribute(new QName(
				NUMBER_OF_FEATURES_ATTRIBUTE_NAME));
		if (numberOfFeaturesAttribute != null) {
			String numberOfFeatures = numberOfFeaturesAttribute.getAttributeValue();
			try {
				return Integer.parseInt(numberOfFeatures);
			} catch (NumberFormatException e) {
				LOG.info("Number of features could not be parsed, return -1 ");
			}
		}
		return UNKNOWN_FEATURECOUNT;
	}

	private InputStream retrieveGetFeatureResponseAsInputStream(Envelope bbox) throws ClientProtocolException,
			IOException {
		return getFeatureCollectionRetriever().retrieveGetFeatureResponseAsInputStream(wfsRequestUrlForBboxFeatureHits,
				bbox);
	}

	private void close(InputStream response) {
		if (response != null)
			try {
				response.close();
			} catch (IOException e) {
			}
	}
}