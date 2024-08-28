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
package com.ericsson.oss.ap.core.test.helper;

import java.io.InputStream;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.ericsson.oss.ap.core.operators.shm.SHMServiceRestOperator;

/**
 * @since 1.9.4
 */
public class LicenseFileUploader {

    @Inject
    private SHMServiceRestOperator shmOperator;

    private final static Logger LOGGER = Logger.getLogger(LicenseFileUploader.class);

    /**
     * Loads the license file for the node.
     *
     * @param licenseFileName
     *            the license file name
     */
    public void upload(final String licenseFileName) {
        try {
            final InputStream is = this.getClass().getResourceAsStream("/license_files/" + licenseFileName);
            final byte[] licenseFileContents = IOUtils.toByteArray(is);
            shmOperator.uploadLicense(licenseFileName, licenseFileContents);
            LOGGER.info("Uploaded license file " + licenseFileName);
        } catch (final Exception e) {
            LOGGER.error("Failed to upload license file " + licenseFileName, e);
        }
    }
}
