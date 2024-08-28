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
package com.ericsson.oss.ap.erbs.test.cases;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.oss.ap.core.getters.APDirectoryResolver;
import com.ericsson.oss.ap.core.getters.APHostResolver;
import com.ericsson.oss.ap.core.operators.APServiceOperator;
import com.ericsson.oss.ap.core.operators.UserManagementOperator;
import com.ericsson.oss.ap.core.operators.cm.CmCliOperator;
import com.ericsson.oss.ap.core.operators.cm.model.ManagedObjectDto;
import com.ericsson.oss.ap.core.test.helper.ImportProjectHelper;
import com.ericsson.oss.ap.core.test.helper.NodeStateTransitionTimer;
import com.ericsson.oss.ap.core.test.helper.NodeStatusHelper;
import com.ericsson.oss.ap.core.test.helper.TestDataCleaner;
import com.ericsson.oss.ap.core.test.model.CommandResult;
import com.ericsson.oss.ap.erbs.test.cases.validators.OrderWorkflowValidator;
import com.ericsson.oss.ap.erbs.test.cases.validators.UnorderWorkflowValidator;
import com.ericsson.oss.ap.erbs.test.data.APUnorderTestData;
import com.ericsson.oss.ap.erbs.test.model.APNodeTestInfo;
import com.ericsson.oss.ap.erbs.test.model.APProjectTestInfo;
import com.ericsson.oss.services.ap.common.Constants;

/**
 * Tests the invocation of the AP unorder integrate use case. Although Context.CLI is specified for tests these are actually using the Common CLI
 * through its REST service. Context.Rest will be used for the AP Rest service which already exists but is temporarily not being used.
 * 
 * @since 1.0.x
 */
public class APUnorder extends TorTestCaseHelper {

    @Inject
    private OrderWorkflowValidator orderWorkflowSuccessValidator;

    @Inject
    private UnorderWorkflowValidator unorderWorkflowValidator;

    @Inject
    private CmCliOperator cmOperator;

    @Inject
    private ImportProjectHelper importProjectHelper;

    @Inject
    private APHostResolver hostResolver;

    @Inject
    private NodeStateTransitionTimer nodeStateTransitionTimer;

    @Inject
    private NodeStatusHelper nodeStatusHelper;

    @Inject
    private TestDataCleaner testDataCleaner;

    @Inject
    private UserManagementOperator createUserOperator;

    @Inject
    private OperatorRegistry<APServiceOperator> operatorRegistry;

    private APServiceOperator operator;

    @BeforeSuite(alwaysRun = true)
    public void preTAFSetup() {
        createUserOperator.createTAFUserInNonLocalEnv();
    }

    public APUnorder() {
        //temporarily create the AP data directories as they are not yet created by the server.
        if (Constants.ENV_LOCAL) {
            final APDirectoryResolver directoryResolver = new APDirectoryResolver();
            final File generatedDataDir = new File(directoryResolver.getGeneratedDirectory());
            generatedDataDir.mkdirs();
        }
    }

    /**
     * Tests the execution of an unorder integration workflow. An order integration is performed prior to the unorder execution.
     * 
     * @param zipFileContents
     *            the project zip file contents
     * @param projectInfo
     *            {@link APProjectTestInfo} for importing a project
     * @param orderNodeCount
     *            the number of nodes to order integration for
     * @param expectedUnorderResult
     *            the expected result
     * @param description
     *            description of the test
     */
    @Context(context = Context.CLI)
    @Test(dataProvider = APUnorderTestData.AP_UNORDER_EXECUTION, dataProviderClass = APUnorderTestData.class, groups = { "Acceptance", "GAT",
            "PerformanceKPI" })
    public void unorderIntegration(final byte[] zipFileContents, final APProjectTestInfo projectInfo, final int orderNodeCount,
            final String description, final boolean successfullCommandResult) {

        setTestCase("TORF-6387_Func_1", "Unorder integration");
        setTestInfo(description);
        setTestStep("Prepare and import project with nodes.");

        importProject(zipFileContents, projectInfo);

        operator = operatorRegistry.provide(APServiceOperator.class);
        final List<APNodeTestInfo> nodesToIntegrate = projectInfo.getNodes().subList(0, orderNodeCount);
        final String nodeNames = extractNodeNames(nodesToIntegrate);

        orderIntegration(nodesToIntegrate, nodeNames, orderNodeCount);

        unorderIntegration(nodesToIntegrate, nodeNames, orderNodeCount, successfullCommandResult);
    }

    private void importProject(final byte[] zipFileContents, final APProjectTestInfo projectInfo) {
        final CommandResult importResult = importProjectHelper.importProject(zipFileContents, projectInfo);

        if (!importResult.isSuccessful()) {
            fail("Failed to import project " + projectInfo.getName());
        }

        final String projectFdn = projectInfo.getProjectFdn();
        final ManagedObjectDto projectMo = cmOperator.findMoByFdn(hostResolver.getApacheHost(), projectFdn);

        if (projectMo != null) {
            testDataCleaner.markFdnForCleanUp(projectFdn);
        }
    }

    private void orderIntegration(final List<APNodeTestInfo> nodesToIntegrate, final String nodeNames, final int orderNodeCount) {
        setTestStep("Order Integration for " + orderNodeCount + " node(s)");
        final CommandResult orderResult = operator.orderNode(nodeNames);

        setTestStep("Verify integration ordered successfully");
        assertTrue("Order integration command returned unsuccessful.", orderResult.isSuccessful());

        for (final APNodeTestInfo nodeInfo : nodesToIntegrate) {
            nodeStateTransitionTimer.waitForStateTransitionFromState(nodeInfo.getApNodeFdn(), "ORDER_STARTED");
            orderWorkflowSuccessValidator.execute(nodeInfo);
        }
    }

    private void unorderIntegration(final List<APNodeTestInfo> nodesToIntegrate, final String nodeNames, final int orderNodeCount,
            final boolean successfulCommandResult) {
        setTestStep("Unorder Integration for " + orderNodeCount + " node(s)");

        final CommandResult unorderResult = operator.unorder(nodeNames);

        verifyCommandResult(unorderResult, successfulCommandResult);

        if (successfulCommandResult) {
            validateWorkflow(nodesToIntegrate);
            validateStatus(nodesToIntegrate);
        }
    }

    private void verifyCommandResult(final CommandResult unorderResult, final boolean successfulCommandResult) {
        assertEquals("Order integration command returned unexpected result", successfulCommandResult, unorderResult.isSuccessful());
    }

    private void validateWorkflow(final List<APNodeTestInfo> nodesToIntegrate) {
        for (final APNodeTestInfo nodeInfo : nodesToIntegrate) {
            nodeStateTransitionTimer.waitForStateTransitionFromState(nodeInfo.getApNodeFdn(), "UNORDER_STARTED");
            unorderWorkflowValidator.execute(nodeInfo);
        }
    }

    private void validateStatus(final List<APNodeTestInfo> nodesToIntegrate) {
        for (final APNodeTestInfo nodeInfo : nodesToIntegrate) {
            assertEquals("READY_FOR_ORDER", nodeStatusHelper.getState(nodeInfo.getApNodeFdn()));
        }
    }

    private String extractNodeNames(final List<APNodeTestInfo> nodesToIntegrate) {
        final StringBuilder nodeNames = new StringBuilder();
        int numNodes = nodesToIntegrate.size();
        for (final APNodeTestInfo nodeInfo : nodesToIntegrate) {
            nodeNames.append(nodeInfo.getName());
            if (--numNodes != 0) {
                nodeNames.append(",");
            }
        }
        return nodeNames.toString();
    }
}
