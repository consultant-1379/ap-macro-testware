/*
 * ------------------------------------------------------------------------------
 *  *******************************************************************************
 *  * COPYRIGHT Ericsson 2014
 *  *
 *  * The copyright to the computer program(s) herein is the property of
 *  * Ericsson Inc. The programs may be used and/or copied only with written
 *  * permission from Ericsson Inc. or in accordance with the terms and
 *  * conditions stipulated in the agreement/contract under which the
 *  * program(s) have been supplied.
 *  *******************************************************************************
 *  *----------------------------------------------------------------------------
 */

package com.ericsson.oss.ap.erbs.test.model;

import com.ericsson.oss.ap.core.test.model.GenericAPTestInfo;

/**
 * Test data for APExportArtifact.
 * 
 * @since 1.2.1
 */
public class APDownloadTestInfo extends GenericAPTestInfo {

    public String getDescription() {
        return this.getAttribute("description");
    }

    public String getCommandOptions() {
        return this.getAttribute("commandOptions");
    }

    public String getArtifactType() {
        return this.getAttribute("artifactType");
    }

    public String getFilename() {
        return this.getAttribute("filename");
    }

    public String getResult() {
        return this.getAttribute("result");
    }

    public String getStatusMessage() {
        return this.getAttribute("statusMessage");
    }

    public String getXmlRootTag() {
        return this.getAttribute("xmlRootTag");
    }

    public String getNodeType() {
        return this.getAttribute("nodeType");
    }

    public String getNodeVersion() {
        return this.getAttribute("nodeVersion");
    }

    public Boolean getOrderIntegration() {
        return Boolean.valueOf(this.getAttributeAsString(("orderIntegration")));
    }
}
