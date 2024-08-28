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
package com.ericsson.oss.ap.erbs.test.cases.validators;

import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setTestStepBegin;
import static se.ericsson.jcat.fw.ng.JcatNGTestBase.assertEquals;
import static se.ericsson.jcat.fw.ng.JcatNGTestBase.assertFalse;
import static se.ericsson.jcat.fw.ng.JcatNGTestBase.assertNotNull;
import static se.ericsson.jcat.fw.ng.JcatNGTestBase.assertTrue;
import static se.ericsson.jcat.fw.ng.JcatNGTestBase.fail;

import java.io.File;

import javax.inject.Inject;

import com.ericsson.oss.ap.core.getters.APDirectoryResolver;
import com.ericsson.oss.ap.core.getters.APHostResolver;
import com.ericsson.oss.ap.core.getters.SMRSResolver;
import com.ericsson.oss.ap.core.operators.cm.CmCliOperator;
import com.ericsson.oss.ap.core.operators.cm.model.ManagedObjectDto;
import com.ericsson.oss.ap.core.operators.file.CommonFileOperator;
import com.ericsson.oss.ap.erbs.test.model.APNodeTestInfo;
import com.ericsson.oss.services.ap.common.Constants;

/**
 * Validates all activites in the order workflow are successfully completed on
 * order integration success.
 * 
 */
public class OrderWorkflowValidator implements WorkflowValidator {

    @Inject
    private CmCliOperator cmOperator;

    @Inject
    private CommonFileOperator fileOperator;

    @Inject
    private APHostResolver hostResolver;

    @Inject
    private SMRSResolver smrsResolver;

    @Inject
    private APDirectoryResolver directoryResovler;

    @Override
    public void execute(final APNodeTestInfo nodeInfo) {
        final String nodeName = nodeInfo.getName();
        final boolean isSecurityEnabled = isSecurityEnabled(nodeInfo);

        setTestStepBegin("Verify order executed successfully for node " + nodeName);
        verifyNodeAdded(nodeName);
        verifySiteBasicFile(nodeName);
        verifySiteEquipmentFile(nodeName);
        verifyRbsSummaryFile(nodeInfo, isSecurityEnabled);
        verifySiteInstallFile(nodeName, isSecurityEnabled);

        if (isSecurityEnabled) {
            verifyIscfFile(nodeName);
        }
    }

    private boolean isSecurityEnabled(final APNodeTestInfo nodeInfo) {
        final String securityFdn = nodeInfo.getApNodeFdn() + "," + nodeInfo.getNodeType() + "Security=1";
        return !(cmOperator.findMoByFdn(hostResolver.getApacheHost(), securityFdn) == null);
    }

    private void verifyNodeAdded(final String nodeName) {
        setTestStepBegin("Verify node " + nodeName + " is successfully created");
        final String networkElementFdn = "NetworkElement=" + nodeName;
        verifyMoExists(networkElementFdn);

        final String connInfoFdn = networkElementFdn + ",CppConnectivityInformation=1";
        verifyMoExists(connInfoFdn);

        final String meContextFdn = "MeContext=" + nodeName;
        verifyMoExists(meContextFdn);
    }

    private void verifyMoExists(final String fdn) {
        final ManagedObjectDto networkElementMo = cmOperator.findMoByFdn(hostResolver.getApacheHost(), fdn);
        assertTrue("MO " + fdn + " does not exist", networkElementMo != null);
    }

    private void verifySiteBasicFile(final String nodeName) {
        final String siteBasicFilePath = smrsResolver.getAbsoluteSiteBasicFilePath(nodeName);
        verifyFileExists(siteBasicFilePath);
    }

    private void verifySiteEquipmentFile(final String nodeName) {
        final String siteEquipmentPath = smrsResolver.getAbsoluteSiteEquipmentFilePath(nodeName);
        verifyFileExists(siteEquipmentPath);
    }

    private void verifyIscfFile(final String nodeName) {
        final String iscfFilePath = smrsResolver.getAbsoluteIscfFilePath(nodeName);
        verifyFileExists(iscfFilePath);
    }

    private void verifyRbsSummaryFile(final APNodeTestInfo nodeInfo, final boolean isSecurityEnabled) {
        final String rbsSummaryFilePath = smrsResolver.getAbsoluteRbsSummaryFilePath(nodeInfo.getName());
        verifyFileExists(rbsSummaryFilePath);

        setTestStepBegin("Verify RBSSummary file data.");
        final DocumentReader docReader = new DocumentReader(readFile(rbsSummaryFilePath));

        assertEquals(smrsResolver.getRelativeSiteBasicFilePath(nodeInfo.getName()),
                docReader.getElementAttributeValue("ConfigurationFiles", "siteBasicFilePath"));

        assertEquals(smrsResolver.getRelativeSiteEquipmentFilePath(nodeInfo.getName()),
                docReader.getElementAttributeValue("ConfigurationFiles", "siteEquipmentFilePath"));

        final String upgradePackageName = nodeInfo.getUpgradePackageName();
        if (upgradePackageName != null) {
            assertNotNull(docReader.getElementAttributeValue("ConfigurationFiles", "upgradePackageFilePath"));
        }

        if (isSecurityEnabled) {
            assertEquals(smrsResolver.getRelativeIscfFilePath(nodeInfo.getName()),
                    docReader.getElementAttributeValue("ConfigurationFiles", "initialSecurityConfigurationFilePath"));
        }
    }

    private void verifySiteInstallFile(final String nodeName, final boolean isSecurityEnabled) {
        String siteInstallFilePath;
        if (Constants.ENV_LOCAL) {
            siteInstallFilePath = directoryResovler.getGeneratedDirectory() + File.separator + nodeName + File.separator + "SiteInstall.xml";
        } else {
            siteInstallFilePath = "/ericsson/tor/data/autoprovisioning/artifacts/generated/" + nodeName + "/SiteInstall.xml";
        }

        verifyFileExists(siteInstallFilePath);

        setTestStepBegin("Verify SiteInstall file data.");

        final DocumentReader docReader = new DocumentReader(readFile(siteInstallFilePath));

        assertFalse(docReader.getElementAttributeValue("SmrsData", "address").isEmpty()); // dont know SMRS ip address so just check value is not empty
        assertEquals(smrsResolver.getRelativeRbsSummaryFilePath(nodeName), docReader.getElementAttributeValue("SmrsData", "summaryFilePath"));

        if (isSecurityEnabled) {
            assertFalse(docReader.getElementAttributeValue("InstallationData", "rbsIntegrationCode").isEmpty());

        }
    }

    private void verifyFileExists(final String filePath) {
        setTestStepBegin("Verify file exists, " + filePath);
        assertTrue("File does not exist on SMRS, " + filePath, fileOperator.fileExists(filePath));
    }

    private String readFile(final String filePath) {
        final String file = fileOperator.readFile(filePath);
        if (file == null) {
            fail("Error reading file " + filePath);
        }
        return file;
    }
}
