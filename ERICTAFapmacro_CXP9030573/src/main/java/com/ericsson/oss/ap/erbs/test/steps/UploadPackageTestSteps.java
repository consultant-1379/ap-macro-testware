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
package com.ericsson.oss.ap.erbs.test.steps;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.oss.ap.core.operators.shm.SHMServiceRestOperator;
import com.ericsson.oss.ap.core.test.helper.UploadPackageHelper;
import com.ericsson.oss.ap.erbs.test.model.APNodeTestInfo;
import com.ericsson.oss.ap.erbs.test.model.APProjectTestInfo;

/**
 * Upgrade any software package defined in every APNodeTestInfo in a project.
 * 
 * @author eeibky
 * @since 1.6.4
 */
public class UploadPackageTestSteps {

    public static final String TEST_STEP_UPGRADE_PACKAGE = "upgradePackage";

    @Inject
    private SHMServiceRestOperator shmOperator;

    @Inject
    private TestContext context;

    private final static Logger LOGGER = Logger.getLogger(UploadPackageHelper.class);

    private final Collection<String> uploadedPackages = new ArrayList<String>();

    /**
     * Loads the upgrade package for the node.
     * <p>
     * Requires the following test context attributes to be set
     * <ul>
     * <li><code>TEST_CONTEXT_KEY_PROJECTINFO</code></li> - the
     * <code>APProjectTestInfo</code> for an imported project
     * </ul>
     * </p>
     */
    @TestStep(id = TEST_STEP_UPGRADE_PACKAGE)
    public void loadUpgradePackage() {
        final APProjectTestInfo projectInfo = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_PROJECTINFO);

        for (final APNodeTestInfo node : projectInfo.getNodes()) {
            final String upgradePackageName = node.getUpgradePackageName();
            addUpgradePackage(upgradePackageName);
        }
    }

    private void addUpgradePackage(final String upgradePackageName) {
        if (upgradePackageNameIsValid(upgradePackageName)) {
            try {
                final InputStream is = this.getClass().getResourceAsStream("/upgrade_packages/" + upgradePackageName + ".zip");
                final byte[] upgradeFileContents = IOUtils.toByteArray(is);
                shmOperator.uploadUpgradePackage(upgradePackageName + ".zip", upgradeFileContents);
                uploadedPackages.add(upgradePackageName);
                LOGGER.info("Uploaded upgrade package " + upgradePackageName);
            } catch (final Exception e) {
                LOGGER.error("Failed to upload package " + upgradePackageName, e);
            }
        }
    }

    private boolean upgradePackageNameIsValid(final String upgradePackageName) {
        return StringUtils.isNotBlank(upgradePackageName) && !(uploadedPackages.contains(upgradePackageName));
    }
}
