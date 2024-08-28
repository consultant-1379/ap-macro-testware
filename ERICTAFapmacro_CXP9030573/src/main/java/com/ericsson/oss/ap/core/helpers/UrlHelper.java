/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.ap.core.helpers;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.Ports;

/**
 * This class provides a helper method for building a URL.
 * 
 * @author ebenmoo
 * @since 1.2.4
 */
public class UrlHelper {
	
    public String buildUrl(final Host theHost, final String theUri) {
        final StringBuilder urlBuffer = new StringBuilder("http");

        boolean useSSL = true;
        int port = 0;

        String portValue = theHost.getPort().get(Ports.HTTPS);
        if (portValue == null) {
            useSSL = false;
            portValue = theHost.getPort().get(Ports.HTTP);
        }

        if (portValue != null) {
            port = Integer.parseInt(portValue);
        }

        if (useSSL) {
            urlBuffer.append('s');
        }
        urlBuffer.append("://");
        urlBuffer.append(theHost.getIp());
        if (port > 0) {
            urlBuffer.append(':').append(port);
        }

        if (theUri != null) {
            urlBuffer.append(theUri);
        }

        return urlBuffer.toString();
    }

}
