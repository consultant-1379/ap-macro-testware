/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.ap.core.test.model;

/**
 * Test data for APUploadTestInfo.
 * 
 * @since 1.7.8
 */
public class APUploadTestInfo extends GenericAPTestInfo {

    public String getDescription() {
        return this.getAttribute("description");
    }

    public String getArtifactType() {
        return this.getAttribute("artifactType");
    }

    public String getFilename() {
        return this.getAttribute("filename");
    }

    public byte[] getArtifactContent() {
        return this.getAttribute("artifactByteContent");
    }

    public String getResult() {
        return this.getAttribute("result");
    }

    public String getErrorMessage() {
        return this.getAttribute("errorMessage");
    }
}