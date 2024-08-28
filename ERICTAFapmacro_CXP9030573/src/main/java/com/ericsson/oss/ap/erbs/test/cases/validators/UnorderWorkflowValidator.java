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

import com.ericsson.oss.ap.erbs.test.model.APNodeTestInfo;

/**
 * Validates all activites in the unorder workflow, and verifies that all
 * artifacts are successfully removed.
 * 
 */
public class UnorderWorkflowValidator extends OrderWorkflowRollbackValidator {
    
    @Override
    public void execute(final APNodeTestInfo nodeInfo) {
        final String nodeName = nodeInfo.getName();
        
        setTestStepBegin("Verify unorder executed successfully for node " + nodeName);
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
}
