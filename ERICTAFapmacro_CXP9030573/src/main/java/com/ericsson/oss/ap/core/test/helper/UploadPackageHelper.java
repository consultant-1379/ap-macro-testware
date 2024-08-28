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
 * @author eshemeh
 * @since 1.5.2
 */
public class UploadPackageHelper {

    @Inject
    private SHMServiceRestOperator shmOperator;

    private final static Logger LOGGER = Logger.getLogger(UploadPackageHelper.class);

    /**
     * Loads the upgrade package for the node.
     * 
     * @param upgradePackageName
     *            the upgrade package name
     */
    public void loadUpgradePackage(final String upgradePackageName) {
        if (upgradePackageName != null && !upgradePackageName.trim().equals("")) {
            try {
                final InputStream is = this.getClass().getResourceAsStream("/upgrade_packages/" + upgradePackageName + ".zip");
                final byte[] upgradeFileContents = IOUtils.toByteArray(is);
                shmOperator.uploadUpgradePackage(upgradePackageName + ".zip", upgradeFileContents);
                LOGGER.info("Uploaded upgrade package " + upgradePackageName);
            } catch (final Exception e) {
                LOGGER.error("Failed to upload package " + upgradePackageName, e);
            }
        }
    }
}
