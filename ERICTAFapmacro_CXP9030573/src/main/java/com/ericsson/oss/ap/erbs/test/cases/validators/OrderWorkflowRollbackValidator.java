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
package com.ericsson.oss.ap.erbs.test.cases.validators;

import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setTestStepBegin;
import static se.ericsson.jcat.fw.ng.JcatNGTestBase.assertFalse;
import static se.ericsson.jcat.fw.ng.JcatNGTestBase.assertNull;
import static se.ericsson.jcat.fw.ng.JcatNGTestBase.assertTrue;

import java.io.File;
import java.util.Map;

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
 * Validates any executed activities in the order workflow are successfully
 * rolled back on order integration failure.
 * 
 */
public class OrderWorkflowRollbackValidator implements WorkflowValidator {

    @Inject
    private CmCliOperator cmOperator;

    @Inject
    private CommonFileOperator fileOperator;

    @Inject
    private SMRSResolver smrsResolver;

    @Inject
    private APHostResolver hostResolver;

    @Inject
    private APDirectoryResolver directoryResolver;

    @Override
    public void execute(final APNodeTestInfo nodeInfo) {
        final String nodeName = nodeInfo.getName();

        setTestStepBegin("Verify node " + nodeName + " is successfully rolled back");
        verifyNodeDoesNotExist(nodeName);
        verifySiteBasicDoesNotExist(nodeName);
        verifySiteEquipmentDoesNotExist(nodeName);
        verifyRbsSummaryFileDoesNotExist(nodeName);
        verifySiteInstallDoesNotExist(nodeName);
        if (isSecurityEnabled(nodeInfo)) {
            verifyIscfFileDoesNotExist(nodeName);
            verifySecurityAttributesAreReset(nodeInfo);
        }
    }

    protected void verifyNodeDoesNotExist(final String nodeName) {
        setTestStepBegin("Verify node " + nodeName + " does not exist");
        final String networkElementFdn = "NetworkElement=" + nodeName;
        assertTrue("NetworkElement exists, " + networkElementFdn, cmOperator.findMoByFdn(hostResolver.getApacheHost(), networkElementFdn) == null);
    }

    protected void verifySiteBasicDoesNotExist(final String nodeName) {
        verifyFileDoesNotExist(smrsResolver.getAbsoluteSiteBasicFilePath(nodeName));
    }

    protected void verifySiteEquipmentDoesNotExist(final String nodeName) {
        verifyFileDoesNotExist(smrsResolver.getAbsoluteSiteEquipmentFilePath(nodeName));
    }

    protected void verifyRbsSummaryFileDoesNotExist(final String nodeName) {
        verifyFileDoesNotExist(smrsResolver.getAbsoluteRbsSummaryFilePath(nodeName));
    }

    protected void verifySiteInstallDoesNotExist(final String nodeName) {
        String siteInstallFilePath;
        if (Constants.ENV_LOCAL) {
            siteInstallFilePath = directoryResolver.getGeneratedDirectory() + File.separator + nodeName + File.separator + "SiteInstall.xml";
        } else {
            siteInstallFilePath = "/ericsson/tor/data/autoprovisioning/artifacts/generated/" + nodeName + "/SiteInstall.xml";
        }
        verifyFileDoesNotExist(siteInstallFilePath);
    }

    protected void verifyFileDoesNotExist(final String filePath) {
        setTestStepBegin("Verify file does not exist, " + filePath);
        assertFalse("File exists, " + filePath, fileOperator.fileExists(filePath));
    }

    protected void verifyIscfFileDoesNotExist(final String nodeName) {
        verifyFileDoesNotExist(smrsResolver.getAbsoluteIscfFilePath(nodeName));
    }

    protected void verifySecurityAttributesAreReset(final APNodeTestInfo nodeInfo) {
        setTestStepBegin("Verify Security Attributes Are Reset");
        final String securityFdn = getSecurityFdn(nodeInfo);
        final ManagedObjectDto mo = cmOperator.findMoByFdn(hostResolver.getApacheHost(), securityFdn);
        final Map<String, Object> securityAttributes = mo.getAttributes();
        assertNull(securityAttributes.get("rbsIntegrityCode"));
        assertNull(securityAttributes.get("securityConfigChecksum"));
        assertNull(securityAttributes.get("iscfFileLocation"));
    }

    protected boolean isSecurityEnabled(final APNodeTestInfo nodeInfo) {
        return !(cmOperator.findMoByFdn(hostResolver.getApacheHost(), getSecurityFdn(nodeInfo)) == null);
    }

    private String getSecurityFdn(final APNodeTestInfo nodeInfo) {
        return nodeInfo.getApNodeFdn() + "," + nodeInfo.getNodeType() + "Security=1";
    }
}
