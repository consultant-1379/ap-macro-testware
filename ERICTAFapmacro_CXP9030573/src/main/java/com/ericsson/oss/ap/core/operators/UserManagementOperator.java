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
package com.ericsson.oss.ap.core.operators;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.nms.security.ENMUser;
import com.ericsson.nms.security.OpenIDMOperatorImpl;
import com.ericsson.oss.services.ap.common.Constants;

/**
 * Operator used to create & delete users for the ENM application, and assign
 * them roles.
 * 
 * @author eshemeh
 * @since 1.6.3
 */
public class UserManagementOperator {

    private final static Logger LOGGER = LoggerFactory.getLogger(UserManagementOperator.class);

    public static final String DEFAULT_AP_USER = "tafapuser498";
    public static final String DEFAULT_AP_USER_PASSWORD = "tt4TBZsTZRaQk3y";
    public static final String ROLE_ADMIN = "ADMINISTRATOR";
    public static final String ROLE_OPERATOR = "OPERATOR";
    public static final String ROLE_SEC_ADMIN = "SECURITY_ADMIN";

    private final static String DEFAULT_ADMIN_USER = "administrator";
    private final static String DEFAULT_ADMIN_USER_PASSWORD = "TestPassw0rd";

    @Inject
    OpenIDMOperatorImpl openIDMOperator;

    /**
     * Creates a user and assigns its roles.
     * 
     * @param user
     *            the user to create
     * @param roles
     *            the roles to assign to this user
     */
    public void createUser(final ENMUser user, final String... roles) {
        try {
            openIDMOperator.connect(DEFAULT_ADMIN_USER, DEFAULT_ADMIN_USER_PASSWORD);
            openIDMOperator.createUser(user);

            for (final String role : roles) {
                openIDMOperator.assignUsersToRole(role, user.getUsername());
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage());
            LOGGER.debug("Error creating user", e);
        }
    }

    /**
     * Deletes a user.
     * 
     * @param user
     *            the user to delete
     */
    public void deleteUser(final ENMUser user) {
        try {
            openIDMOperator.deleteUser(user.getUsername());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage());
            LOGGER.debug("Error deleting user", e);
        }
    }
    
    /**
     * Create generic TAF user
     */
    public void createTAFUserInNonLocalEnv() {
    	final ENMUser defaultAPUser = new ENMUser();
        if (!Constants.ENV_LOCAL) {
            defaultAPUser.setUsername(UserManagementOperator.DEFAULT_AP_USER);
            defaultAPUser.setPassword(UserManagementOperator.DEFAULT_AP_USER_PASSWORD);
            defaultAPUser.setFirstName("ap");
            defaultAPUser.setLastName("user");
            defaultAPUser.setEmail("apuser123@ericsson.com");
            defaultAPUser.setEnabled(true);
            createUser(defaultAPUser, UserManagementOperator.ROLE_ADMIN, UserManagementOperator.ROLE_OPERATOR);
        }
    }
}
