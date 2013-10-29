package org.geomajas.layer.wms.feature;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Allows to retrieve the number of features inside a bbox.
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public interface NumberOfFeaturesInEnvelopeRetriever {

	static final int UNKNOWN_FEATURECOUNT = -1;

	/**
	 * Checks if at least one feature intersects the given bbox.
	 * 
	 * @param bbox
	 *            never <code>null</code>
	 * @return true if at least one features is inside the passed bbox or an error occurred, false if no feature is
	 *         inside.
	 */
	boolean isAtLeastOneFeatureInEnvelope(Envelope bbox);

	/**
	 * Counts the number of features inside the bbox.
	 * 
	 * @param bbox
	 *            never <code>null</code>
	 * @return the number of features inside the bbox, UNPARSEABLE_FEATURECOUNT (-1) if count fails or could not be
	 *         detected.
	 */
	int countFeaturesInEnvelope(Envelope bbox);

}