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
package com.ericsson.oss.ap.erbs.test.steps.delete;

import static se.ericsson.jcat.fw.assertion.JcatAssertApi.assertEquals;
import static se.ericsson.jcat.fw.assertion.JcatAssertApi.assertFalse;
import static se.ericsson.jcat.fw.assertion.JcatAssertApi.assertNull;
import static se.ericsson.jcat.fw.assertion.JcatAssertApi.assertTrue;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setSubTestStep;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setTestStepBegin;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setTestStepEnd;

import java.util.List;

import javax.inject.Inject;

import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.oss.ap.core.getters.APHostResolver;
import com.ericsson.oss.ap.core.operators.APServiceOperator;
import com.ericsson.oss.ap.core.operators.cm.CmCliOperator;
import com.ericsson.oss.ap.core.operators.cm.model.ManagedObjectDto;
import com.ericsson.oss.ap.core.operators.file.CommonFileOperator;
import com.ericsson.oss.ap.core.test.helper.NodeStatusHelper;
import com.ericsson.oss.ap.core.test.model.CommandResult;
import com.ericsson.oss.ap.erbs.test.model.APNodeTestInfo;
import com.ericsson.oss.ap.erbs.test.model.APProjectTestInfo;
import com.ericsson.oss.ap.erbs.test.steps.ImportProjectTestSteps;

/**
 * Contains the test steps of deleting project(s) or node(s)
 * 
 * @author exuuguu
 * @since 1.8.2
 */
public class DeleteNodeTestSteps {

    public static final String TEST_STEP_DELETE_NODE = "deleteNode";
    public static final String TEST_STEP_VERIFY_NODE_DELETED = "verifyNodeDeleted";
    public static final String TEST_STEP_EDIT_NODE_STATUS = "editNodeStatus";
    public final static String TEST_CONTEXT_KEY_DELETE_RESPONSE = "DeleteResponse";

    private final static String EXPECTED_RESULT = "EXPECTED RESULT: \"%s\" ACTUAL: \"%s\"";
    private final static String EXPECTED_MESSAGE = "EXPECTED MESSAGE: \"%s\" ACTUAL: \"%s\"";

    @Inject
    private OperatorRegistry<APServiceOperator> operatorRegistry;

    @Inject
    private CmCliOperator cmOperator;

    @Inject
    private CommonFileOperator fileOperator;

    @Inject
    private NodeStatusHelper nodeStatusHelper;

    @Inject
    private APHostResolver hostResolver;

    @Inject
    private TestContext context;

    @TestStep(id = TEST_STEP_DELETE_NODE)
    public void deleteNode(@Input("expectedResult") final String expectedResult, @Input("expectedNodeDeleteMessage") final String expectedMessage) {

        final APProjectTestInfo projectInfo = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_PROJECTINFO);
        se.ericsson.jcat.fw.assertion.JcatAssertApi.assertTrue("No node in project to be deleted.", projectInfo.getNodes().size() > 0);
        final String nodeName = projectInfo.getNodes().get(0).getName();

        setTestStepBegin("Delete node " + nodeName);

        setSubTestStep("Do execution of delete node");
        final CommandResult result = doDeleteNode(nodeName);

        setSubTestStep("Verify response of delete node");
        final String expectedResultMessage = String.format(expectedMessage, nodeName);
        verifyCommandResponse(result, isSuccessfulResult(expectedResult), expectedResultMessage);

        setTestStepEnd();
    }

    @TestStep(id = TEST_STEP_VERIFY_NODE_DELETED)
    public void verifyNodeDeleted() {

        final APProjectTestInfo projectInfo = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_PROJECTINFO);
        final APNodeTestInfo nodeInfo = projectInfo.getNodes().get(0);
        final String nodeName = nodeInfo.getName();
        final String nodeFdn = nodeInfo.getApNodeFdn();
        final List<ManagedObjectDto> artifacts = cmOperator.getMosByType(hostResolver.getApacheHost(), "ap", "NodeArtifact", nodeFdn, "NodeArtifactContainer,NodeArtifact");

        setSubTestStep("Verify node deleted from DPS");
        verifyNodeDeletedFromDPS(nodeFdn);

        setSubTestStep("Verify artifact deleted from SFS");
        verifyArtifactsDeletedFromSFS("node " + nodeName, artifacts);

    }

    @TestStep(id = TEST_STEP_EDIT_NODE_STATUS)
    public void editStatusOfFirstNodeInProject(@Input("state") final String state) {

        final APProjectTestInfo projectInfo = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_PROJECTINFO);
        final String nodeFdn = projectInfo.getNodes().get(0).getApNodeFdn();

        nodeStatusHelper.setState(nodeFdn, state);
    }

    private CommandResult doDeleteNode(final String nodeName) {

        setSubTestStep("Deleting node ... ");
        final APServiceOperator operator = operatorRegistry.provide(APServiceOperator.class);
        final CommandResult result = operator.deleteNode(nodeName);

        return result;
    }

    private void verifyCommandResponse(final CommandResult result, final boolean expectedResult, final String expectedResponseMessage) {

        setSubTestStep("Verify command response ...");
        assertEquals(String.format(EXPECTED_RESULT, expectedResult, result.isSuccessful()), expectedResult, result.isSuccessful());
        assertTrue(String.format(EXPECTED_MESSAGE, expectedResponseMessage, result.getStatusMessage()), result.getStatusMessage().startsWith(expectedResponseMessage));
    }

    private void verifyNodeDeletedFromDPS(final String nodeFdn) {
        setSubTestStep("Verify node does not exist in dps");
        assertNull(String.format("Node %s exist in the database.", nodeFdn), cmOperator.findMoByFdn(hostResolver.getApacheHost(), nodeFdn));
    }

    private void verifyArtifactsDeletedFromSFS(final String moType, final List<ManagedObjectDto> artifacts) {
        setSubTestStep("Verify Artifacts are deleted from SFS for " + moType);

        for (final ManagedObjectDto artifact : artifacts) {
            final String rawLocation = artifact.getAttribute("rawLocation");
            if (rawLocation != null) {
                assertFalse("Artifact not deleted for " + artifact.getFdn(), fileOperator.fileExists(rawLocation));
            }
        }
    }

    private boolean isSuccessfulResult(final String result) {
        return "SUCCESS".equals(result);
    }
}
