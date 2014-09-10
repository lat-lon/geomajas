package org.geomajas.layer.wms.feature;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

/**
 * 
 * Retrieves a feature collection from WFS
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class FeatureCollectionRetriever {

    private final Logger log = LoggerFactory.getLogger( FeatureCollectionRetriever.class );

    /**
     * @param baseWfsUrl
     *            the url of the WFS including VERSIOn, REQUEST, SERVICE, TYPENAME and RESULTTYPE parameter, RESULTTYPE
     *            should be hits, never <code>null</code>
     * @param bbox
     *            never <code>null</code>
     * @return the response of the GetFeature request, never <code>null</code>. The {@link InputStream} must be closed
     *         fron the invoking method.
     * @throws IOException
     * @throws ClientProtocolException
     */
    public InputStream retrieveGetFeatureResponseAsInputStream( String baseWfsUrl, Envelope bbox )
                            throws IOException, ClientProtocolException {
        String requestUrlFromEnvelope = buildRequestUrlFromEnvelope( baseWfsUrl, bbox );
        log.debug( "GetFeature request: " + requestUrlFromEnvelope );
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet( requestUrlFromEnvelope );
        HttpResponse response = client.execute( request );
        return response.getEntity().getContent();
    }

    private String buildRequestUrlFromEnvelope( String baseWfsUrl, Envelope bbox ) {
        StringBuilder requestUrlBuilder = new StringBuilder( baseWfsUrl );
        requestUrlBuilder.append( "&bbox=" );
        requestUrlBuilder.append( bbox.getMinX() ).append( "," ).append( bbox.getMinY() ).append( "," );
        requestUrlBuilder.append( bbox.getMaxX() ).append( "," ).append( bbox.getMaxY() ).append( "," );
        requestUrlBuilder.append( "EPSG:25833" );
        return requestUrlBuilder.toString();
    }

}