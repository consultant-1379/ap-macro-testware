/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
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
import com.ericsson.cifwk.taf.tools.http.HttpResponse;
import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.cifwk.taf.tools.http.HttpToolBuilder;
import com.ericsson.nms.security.ENMUser;
import com.ericsson.oss.ap.core.operators.UserManagementOperator;
import com.ericsson.oss.services.ap.common.Constants;

/**
 * This class provides a helper method for logging in via SSO.
 * 
 * @author ebenmoo
 * @since 1.4.8
 */
public class LoginHelper {

    private static final String APACHE_LOGIN_URI = "/login";
    private static final String VALID_LOGIN = "0";
    private static final String DEFAULT_USERNAME = UserManagementOperator.DEFAULT_AP_USER;
    private static final String DEFAULT_PASSWORD = UserManagementOperator.DEFAULT_AP_USER_PASSWORD;

    private static ENMUser currentUser = new ENMUser();

    private LoginHelper() {

    }

    /**
     * Returns the current logged in user.
     * 
     * @return
     */
    public static ENMUser getCurrentUser() {
    	 if (currentUser.getUsername() == null) {
             currentUser.setUsername(DEFAULT_USERNAME);
             currentUser.setPassword(DEFAULT_PASSWORD);
         }
         return currentUser;
    }

    /**
     * This method creates the default TOR user and returns a HttpTool with the
     * relevant headers.
     * 
     * @param host
     * @return
     */
    public static HttpTool performDefaultSecureLogin(final Host host) {
        return performSecureLogin(DEFAULT_USERNAME, DEFAULT_PASSWORD, host);
    }

    /**
     * This method creates a TOR user by using the credentials passed into the
     * method and returns a HttpTool with the relevant headers.
     * 
     * @param username
     * @param password
     * @param host
     * @return
     */
    public static HttpTool performSecureLogin(final String username, final String password, final Host host) {
        final HttpTool httpTool = buildHttpTool(host);
        currentUser.setUsername(username);
        currentUser.setPassword(password);

        if (!Constants.ENV_LOCAL) {
            final HttpResponse response = httpTool.request().body("IDToken1", username).body("IDToken2", password).post(APACHE_LOGIN_URI);
            final String authErrorCode = response.getHeaders().get("X-AuthErrorCode");
            if (VALID_LOGIN.equals(authErrorCode)) {
                httpTool.addCookie("TorUserID", username);
            } else {
                throw new SecurityException("Failed to login using credentials supplied: username [" + username + "], password [" + password + "]");
            }
        }
        return httpTool;
    }

    private static HttpTool buildHttpTool(final Host host) {
        return HttpToolBuilder.newBuilder(host).followRedirect(false).useHttpsIfProvided(!Constants.ENV_LOCAL).trustSslCertificates(!Constants.ENV_LOCAL).build();
    }
}
