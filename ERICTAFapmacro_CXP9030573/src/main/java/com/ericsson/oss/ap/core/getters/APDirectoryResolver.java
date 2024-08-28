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
package com.ericsson.oss.ap.core.getters;

/**
 * Resolve locations of autoprovisioning directories on the file system.
 * 
 */
public class APDirectoryResolver {

    private static final String ARTIFACTS_DIRECTORY = "/ericsson/tor/data/autoprovisioning/artifacts";

    private static final String RAW_DIRECTORY = "/ericsson/tor/data/autoprovisioning/artifacts/raw";

    private static final String GENERATED_DIRECTORY = "/ericsson/tor/data/autoprovisioning/artifacts/generated";

    private static final String DOWNLOAD_DIRECTORY = "/ericsson/tor/data/autoprovisioning/artifacts/download";

    /**
     * Gets the absolute path to the base directory for all autoprovisioning
     * artifacts
     * 
     * @return the artifacts directory
     */
    public String getArtifactsDirectory() {
        return ARTIFACTS_DIRECTORY;
    }

    /**
     * Gets the absolute path to the directory containing raw artifacts for all
     * nodes.
     * 
     * @return raw artifacts directory
     */
    public String getRawDirectory() {
        return RAW_DIRECTORY;
    }

    /**
     * Gets the absolute path to the directory containing generated artifacts
     * for all nodes.
     * 
     * @return generated artifacts directory
     */
    public String getGeneratedDirectory() {
        return GENERATED_DIRECTORY;
    }

    /**
     * Gets the absolute path to the download staging directory
     * 
     * @return download directory
     */
    public String getDownloadDirectory() {
        return DOWNLOAD_DIRECTORY;
    }
}
