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
import static se.ericsson.jcat.fw.assertion.JcatAssertApi.assertNotEquals;
import static se.ericsson.jcat.fw.assertion.JcatAssertApi.assertNotNull;
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
import com.ericsson.oss.ap.core.test.model.CommandResult;
import com.ericsson.oss.ap.erbs.test.model.APProjectTestInfo;
import com.ericsson.oss.ap.erbs.test.steps.ImportProjectTestSteps;
import com.ericsson.oss.services.ap.common.Constants;

/**
 * Contains the test steps of deleting project(s)
 * 
 * @since 1.9.3
 */
public class DeleteProjectTestSteps {

    public static final String TEST_STEP_DELETE_PROJECT = "deleteProject";
    public static final String TEST_STEP_VERIFY_PROJECT_DELETED = "verifyProjectDeleted";
    public static final String TEST_STEP_DELETE_PROJECT_WITH_BLOCKING_NODE_STATE = "deleteProjectWithInvalidNodeState";
    public static final String TEST_STEP_VERIFY_PROJECT_DELETE_FAILURE = "verifyProjectDeleteFailure";
    public final static String TEST_CONTEXT_KEY_DELETE_RESPONSE = "DeleteResponse";

    private final static String EXPECTED_RESULT = "EXPECTED RESULT: \"%s\" ACTUAL: \"%s\"";
    private final static String EXPECTED_MESSAGE = "EXPECTED MESSAGE: \"%s\" ACTUAL: \"%s\"";
    private static final String PROJECT_FDN_FORMAT = Constants.PROJECT_FDN_PREFIX + "%s";

    @Inject
    private OperatorRegistry<APServiceOperator> operatorRegistry;

    @Inject
    private CmCliOperator cmOperator;

    @Inject
    private CommonFileOperator fileOperator;

    @Inject
    private APHostResolver hostResolver;

    @Inject
    private TestContext context;

    @TestStep(id = TEST_STEP_DELETE_PROJECT)
    public void deleteProject(@Input("expectedResult") final String expectedResult, @Input("expectedProjectDeleteMessage") final String expectedMessage) {
        final APProjectTestInfo projectInfo = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_PROJECTINFO);
        final String projectName = projectInfo.getName();

        setTestStepBegin("Delete project " + projectName);

        setSubTestStep("Do execution of delete project");
        final CommandResult result = doDeleteProject(projectName);
        context.setAttribute(TEST_CONTEXT_KEY_DELETE_RESPONSE, result);

        setSubTestStep("Verify response of delete project");
        final String expectedMessageWithSubstitutions;
        if (expectedMessage.contains("%s")) {
            expectedMessageWithSubstitutions = String.format(expectedMessage, projectName);
        } else {
            expectedMessageWithSubstitutions = expectedMessage;
        }
        verifyCommandResponse(result, isSuccessfulResult(expectedResult), expectedMessageWithSubstitutions);

        setTestStepEnd();
    }

    @TestStep(id = TEST_STEP_VERIFY_PROJECT_DELETED)
    public void verifyProjectDeleted() {
        setTestStepBegin("Verify project deleted.");

        final APProjectTestInfo projectInfo = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_PROJECTINFO);
        final String projectName = projectInfo.getName();
        final String projectFdn = String.format(PROJECT_FDN_FORMAT, projectName);
        final List<ManagedObjectDto> artifacts = cmOperator.getMosByType(hostResolver.getApacheHost(), "ap", "NodeArtifact", projectFdn, "Node,NodeArtifactContainer,NodeArtifact");

        setSubTestStep("Verify project deleted from DPS");
        verifyProjectDeletedFromDPS(projectInfo);

        setSubTestStep("Verify project deleted from DPS");
        verifyArtifactsDeletedFromSFS("project " + projectName, artifacts);

        setTestStepEnd();
    }

    @TestStep(id = TEST_STEP_VERIFY_PROJECT_DELETE_FAILURE)
    public void verifyProjectDeleteFailure() {
        setTestStepBegin("Verify project delete failed to delete");

        final APProjectTestInfo projectInfo = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_PROJECTINFO);
        final CommandResult result = context.getAttribute(TEST_CONTEXT_KEY_DELETE_RESPONSE);

        final String projectName = projectInfo.getName();
        final String projectFdn = String.format(PROJECT_FDN_FORMAT, projectName);

        setSubTestStep("Verifying delete failure result");
        assertEquals("Delete succeeded with " + result.getStatusMessage(), false, result.isSuccessful());

        setSubTestStep("Verifying status message");
        final String resultMessage = result.getStatusMessage();
        final String expectedMessage = "Project nodes are not in the correct state to perform the operation [";
        assertTrue(String.format(EXPECTED_RESULT, expectedMessage, resultMessage), resultMessage.startsWith(expectedMessage));

        final String solutionMessage = result.getSolution();
        final String expectedSolution = "Ensure all of the project nodes are in the correct state before attempting the operation";
        assertEquals(String.format(EXPECTED_RESULT, expectedSolution, solutionMessage), expectedSolution, solutionMessage);

        setSubTestStep("Verify project still exists in DPS");
        verifyProjectExistsInDPS(projectInfo);

        setSubTestStep("Verify project nodes still exists in DPS");
        verifyNodesExistsInDPS(projectFdn);

        setTestStepEnd();
    }

    private CommandResult doDeleteProject(final String projectName) {

        setSubTestStep("Deleting project ... ");
        final APServiceOperator operator = operatorRegistry.provide(APServiceOperator.class);
        final CommandResult result = operator.deleteProject(projectName);

        return result;
    }

    private void verifyCommandResponse(final CommandResult result, final boolean expectedResult, final String expectedResponseMessage) {

        setSubTestStep("Verify command response ...");
        assertEquals(String.format(EXPECTED_RESULT, expectedResult, result.isSuccessful()), expectedResult, result.isSuccessful());
        assertTrue(String.format(EXPECTED_MESSAGE, expectedResponseMessage, result.getStatusMessage()), result.getStatusMessage().startsWith(expectedResponseMessage));
    }

    private void verifyProjectDeletedFromDPS(final APProjectTestInfo projectInfo) {

        final String projectName = projectInfo.getName();
        final String projectFdn = String.format(PROJECT_FDN_FORMAT, projectName);

        setSubTestStep("Verify project does not exist in dps");
        assertNull(String.format("Project %s exists in the database.", projectName), cmOperator.findMoByFdn(hostResolver.getApacheHost(), projectFdn));
    }

    private void verifyProjectExistsInDPS(final APProjectTestInfo projectInfo) {

        final String projectName = projectInfo.getName();
        final String projectFdn = String.format(PROJECT_FDN_FORMAT, projectName);

        setSubTestStep("Verify project exists in dps");
        assertNotNull(String.format("Project %s does not exists in database.", projectName), cmOperator.findMoByFdn(hostResolver.getApacheHost(), projectFdn));
    }

    private void verifyNodesExistsInDPS(final String projectFdn) {
        setSubTestStep("Verify the project nodes exist in dps");

        final List<ManagedObjectDto> nodes = cmOperator.getMosByType(hostResolver.getApacheHost(), "ap", "Node", projectFdn, "Node");

        assertNotEquals(String.format("Project Nodes for project %s do not exist in the database.", projectFdn), 0, nodes.size());
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
