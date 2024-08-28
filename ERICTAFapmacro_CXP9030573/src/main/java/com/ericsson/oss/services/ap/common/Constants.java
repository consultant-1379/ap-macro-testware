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
package com.ericsson.oss.services.ap.common;

/**
 * Collected constants of general utility.
 * 
 * @author ebenmoo
 * @since 1.7.2
 */
public class Constants {

    public static final boolean ENV_LOCAL = "local".equals(System.getProperty("taf.clusterId", "local"));
    public static final String PROJECT_FDN_PREFIX = "Project=";
    public static final String NODE_FDN_PREFIX = "Node=";

    private Constants() {
    }
}
