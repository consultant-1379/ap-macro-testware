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
package com.ericsson.oss.ap.erbs.test.cases;

import javax.inject.Inject;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.oss.ap.core.getters.APHostResolver;
import com.ericsson.oss.ap.core.operators.UserManagementOperator;
import com.ericsson.oss.ap.core.operators.cm.CmCliOperator;
import com.ericsson.oss.ap.core.operators.cm.model.ManagedObjectDto;
import com.ericsson.oss.ap.core.test.helper.ImportProjectHelper;
import com.ericsson.oss.ap.core.test.helper.NodeStatusHelper;
import com.ericsson.oss.ap.core.test.model.CommandResult;
import com.ericsson.oss.ap.erbs.test.model.APNodeTestInfo;
import com.ericsson.oss.ap.erbs.test.model.APProjectTestInfo;
import com.ericsson.oss.services.commonCLI.operator.ApCmEditorRestOperator;

/**
 * Verifies AP commands cannot be executed when node is not in a valid state as
 * defined by the state machine.
 * 
 * @since 1.5.3
 * 
 */
public class APInvalidNodeState extends TorTestCaseHelper {

    @Inject
    private ApCmEditorRestOperator apCmEditorRestOperator;

    @Inject
    private CmCliOperator cmOperator;

    @Inject
    private APHostResolver hostResolver;

    @Inject
    private NodeStatusHelper nodeStatusHelper;

    @Inject
    private ImportProjectHelper importProjectHelper;

    private static APProjectTestInfo projectTestInfo;

    private final static String IMPORT_DATA_PROVIDER = "apInvalidNodeState"; //project with single node

    @Inject
    private UserManagementOperator createUserOperator;

    @BeforeSuite(alwaysRun = true)
    public void preTAFSetup() {
        createUserOperator.createTAFUserInNonLocalEnv();
    }

    @BeforeClass(alwaysRun = true)
    public void setup() {
        projectTestInfo = importProjectHelper.importProject(IMPORT_DATA_PROVIDER);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (projectTestInfo != null) {
            final String projectFdn = "Project=" + projectTestInfo.getName();
            cmOperator.deleteMo(hostResolver.getApacheHost(), projectFdn);
        }
    }

    @Context(context = Context.CLI)
    @DataDriven(name = "ap_invalid_node_state")
    @Test(groups = { "Acceptance", "GAT" })
    public void testCommandFailsForInvalidNodeState(@Input("description") final String description, @Input("command") final String command, @Input("invalidState") final String invalidState,
            @Input("errorMessage") final String errorMessage) {
        setTestInfo(description);

        final APNodeTestInfo nodeTestInfo = projectTestInfo.getNodes().iterator().next();

        final String updatedCommand = command.replace("<nodename>", nodeTestInfo.getName());

        setTestStep("Setting invalid state " + invalidState + " for node " + nodeTestInfo.getName());
        setInvalidNodeStatePriorToExection(invalidState, nodeTestInfo);

        final ManagedObjectDto nodeStatusPriorToExecution = nodeStatusHelper.getNodeStatus(nodeTestInfo.getApNodeFdn());

        setTestStep("Executing command " + updatedCommand);
        final CommandResult commandResult = apCmEditorRestOperator.executeCommand(hostResolver.getApacheHost(), updatedCommand);

        setTestStep("Verifying command failed due to invalid node state");
        assertFalse(commandResult.isSuccessful());
        assertEquals(errorMessage, commandResult.getStatusMessage());

        setTestStep("Verifying node status unchanged after execution");
        final ManagedObjectDto nodeStatusAfterExecution = nodeStatusHelper.getNodeStatus(nodeTestInfo.getApNodeFdn());
        verifyNodeStatusUnchangeAfterExecution(nodeStatusPriorToExecution, nodeStatusAfterExecution);
    }

    private void setInvalidNodeStatePriorToExection(final String state, final APNodeTestInfo nodeTestInfo) {
        nodeStatusHelper.setState(nodeTestInfo.getApNodeFdn(), state);
    }

    private void verifyNodeStatusUnchangeAfterExecution(final ManagedObjectDto nodeStatusPriorToExecution, final ManagedObjectDto nodeStatusAfterExecution) {
        assertEquals(nodeStatusPriorToExecution.getAttribute("state"), nodeStatusAfterExecution.getAttribute("state"));
        assertEquals(nodeStatusPriorToExecution.getAttribute("phase"), nodeStatusAfterExecution.getAttribute("phase"));
    }
}