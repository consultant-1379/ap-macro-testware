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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.oss.ap.core.getters.APHostResolver;
import com.ericsson.oss.ap.core.operators.APServiceOperator;
import com.ericsson.oss.ap.core.operators.UserManagementOperator;
import com.ericsson.oss.ap.core.operators.cm.CmCliOperator;
import com.ericsson.oss.ap.core.operators.cm.model.ManagedObjectDto;
import com.ericsson.oss.ap.core.test.helper.ImportProjectHelper;
import com.ericsson.oss.ap.core.test.helper.LicenseFileUploader;
import com.ericsson.oss.ap.core.test.helper.NodeStateTransitionTimer;
import com.ericsson.oss.ap.core.test.helper.NodeStatusHelper;
import com.ericsson.oss.ap.core.test.helper.TestDataCleaner;
import com.ericsson.oss.ap.core.test.helper.UploadPackageHelper;
import com.ericsson.oss.ap.core.test.model.CommandResult;
import com.ericsson.oss.ap.erbs.test.cases.validators.OrderWorkflowRollbackValidator;
import com.ericsson.oss.ap.erbs.test.cases.validators.OrderWorkflowValidator;
import com.ericsson.oss.ap.erbs.test.data.APOrderTestData;
import com.ericsson.oss.ap.erbs.test.data.APOrderTestData.OrderExecutionCsvData;
import com.ericsson.oss.ap.erbs.test.model.APNodeTestInfo;
import com.ericsson.oss.ap.erbs.test.model.APProjectTestInfo;

/**
 * Tests the invocation of the AP order integrate use case. Although Context.CLI is specified for tests these are actually using the Common CLI
 * through its REST service. Context.Rest will be used for the AP Rest service which already exists but is temporarily not being used.
 *
 * @since 1.0.x
 */
public class APOrder extends TorTestCaseHelper {

    @Inject
    private OrderWorkflowValidator orderWorkflowSuccessValidator;

    @Inject
    private OrderWorkflowRollbackValidator orderWorkflowRollbackValidator;

    @Inject
    private ImportProjectHelper importProjectHelper;

    @Inject
    private NodeStateTransitionTimer nodeStateTransitionTimer;

    @Inject
    private NodeStatusHelper nodeStatusHelper;

    @Inject
    private UploadPackageHelper uploadPackageHelper;

    @Inject
    private LicenseFileUploader licenseFileUploader;

    @Inject
    private UserManagementOperator createUserOperator;

    @Inject
    private TestDataCleaner testDataCleaner;

    @Inject
    private OperatorRegistry<APServiceOperator> operatorRegistry;

    @Inject
    private CmCliOperator cmOperator;

    @Inject
    private APHostResolver hostResolver;

    private final static String LICENSE_KEY_FILE_NAME = "erbs2.zip";
    private final static String UPGRADE_PACKAGE_NAME = "CXP102051_1_R4D25";

    private final static String CPP_CONNECTIVITY_VERSION = "1.0.0";
    private final static String ME_CONTEXT_VERSION = "3.0.0";
    private final static String NETWORK_ELEMENT_VERSION = "2.0.0";

    @BeforeSuite(alwaysRun = true)
    public void preTAFSetup() {
        createUserOperator.createTAFUserInNonLocalEnv();
        uploadPackageHelper.loadUpgradePackage(UPGRADE_PACKAGE_NAME);
        licenseFileUploader.upload(LICENSE_KEY_FILE_NAME);
    }

    /**
     * Tests the execution of an order integrate workflow.
     *
     * @param zipFileContents
     *            the project zip file contents
     * @param projectInfo
     *            {@link APProjectTestInfo} for importing a project
     * @param csvData
     *            the object containing the test data retrieved from csv file
     */
    @Context(context = Context.CLI)
    @Test(dataProvider = APOrderTestData.AP_ORDER_EXECUTION, dataProviderClass = APOrderTestData.class, groups = { "Acceptance", "GAT",
            "PerformanceKPI" })
    public void orderIntegration(final byte[] zipFileContents, final APProjectTestInfo projectInfo, final OrderExecutionCsvData csvData) {
        setTestCase("TORF-6389_Func_1", "Order integration");
        setTestInfo(csvData.getDescription());

        final int nodeCount = Integer.parseInt(csvData.getNodeCount());
        final boolean isOrderProject = nodeCount <= 0;
        final List<APNodeTestInfo> nodesToOrder = getNodesToOrder(isOrderProject, projectInfo, nodeCount);

        importProject(zipFileContents, projectInfo);
        final CommandResult result = doOrder(projectInfo, isOrderProject, nodesToOrder);

        verifyOrderResult(result, csvData, nodesToOrder);
    }

    /**
     * Tests the execution of an order integrate successful with valid state transition from ORDER_FAILED.
     *
     * @param zipFileContents
     *            the project zip file contents
     * @param projectInfo
     *            {@link APProjectTestInfo} for importing a project
     * @param csvData
     *            the object containing the test data retrieved from csv file
     */
    @Context(context = Context.CLI)
    @Test(dataProvider = APOrderTestData.AP_ORDER_FROM_ORDER_FAILED_STATE, dataProviderClass = APOrderTestData.class, groups = { "Acceptance", "GAT",
            "PerformanceKPI" })
    public void orderNodeSuccessWithStateInOrderFailed(final byte[] zipFileContents, final APProjectTestInfo projectInfo,
            final OrderExecutionCsvData csvData) {
        setTestCase("TORF-6389_Func_1", "Order integration");
        setTestInfo(csvData.getDescription());

        final int nodeCount = Integer.parseInt(csvData.getNodeCount());
        final boolean isOrderProject = nodeCount <= 0;
        final List<APNodeTestInfo> nodesToOrder = getNodesToOrder(isOrderProject, projectInfo, nodeCount);

        importProject(zipFileContents, projectInfo);
        setInvalidNodeStatePriorToExection("ORDER_FAILED", nodesToOrder.iterator().next());
        final CommandResult result = doOrder(projectInfo, isOrderProject, nodesToOrder);

        verifyOrderResult(result, csvData, nodesToOrder);
    }

    /**
     * Tests of order integrate failed in validation
     *
     * @param zipFileContents
     *            the project zip file contents
     * @param projectInfo
     *            {@link APProjectTestInfo} for importing a project
     * @param csvData
     *            the object containing the test data retrieved from csv file
     */
    @Context(context = Context.CLI)
    @Test(dataProvider = APOrderTestData.AP_ORDER_VALIDATION, dataProviderClass = APOrderTestData.class, groups = { "Acceptance", "GAT",
            "PerformanceKPI" })
    public void orderFailedInValidation(final byte[] zipFileContents, final APProjectTestInfo projectInfo, final OrderExecutionCsvData csvData) {
        setTestCase("TORF-11290_Func_1", "Order Validation");
        setTestInfo(csvData.getDescription());

        final int nodeCount = Integer.parseInt(csvData.getNodeCount());
        final boolean isOrderProject = nodeCount <= 0;
        final List<APNodeTestInfo> nodesToOrder = getNodesToOrder(isOrderProject, projectInfo, nodeCount);

        importProject(zipFileContents, projectInfo);
        createDuplicatedMOsForUniquenessValidation(nodesToOrder, csvData.getImportDataProvider());
        final CommandResult result = doOrder(projectInfo, isOrderProject, nodesToOrder);

        verifyValidationResult(result, csvData, nodesToOrder);
    }

    private CommandResult doOrder(final APProjectTestInfo projectInfo, final boolean isOrderProject, final List<APNodeTestInfo> nodesToOrder) {
        final String projectName = projectInfo.getName();

        setTestStep("Order Integration for project " + projectName + " (" + nodesToOrder.size() + " node(s))");

        final APServiceOperator operator = operatorRegistry.provide(APServiceOperator.class);
        final CommandResult result = isOrderProject ? operator.orderProject(projectName) : orderNode(operator, nodesToOrder);

        if (result.isSuccessful()) {
            testDataCleaner.markOrderedNodesForCleanup(nodesToOrder);
        }

        return result;
    }

    private List<APNodeTestInfo> getNodesToOrder(final boolean isOrderProject, final APProjectTestInfo projectInfo, final int nodeCount) {
        return isOrderProject ? projectInfo.getNodes() : projectInfo.getNodes().subList(0, nodeCount);
    }

    private void verifyOrderResult(final CommandResult result, final OrderExecutionCsvData csvData, final List<APNodeTestInfo> nodesToOrder) {
        verifyCommandResult(csvData.isSuccessfulCommandResult(), result);
        validateWorkflow(nodesToOrder, csvData.isSuccessfulWorkflowResult());
        verifyStatus(nodesToOrder, csvData.isSuccessfulWorkflowResult());
    }

    private void verifyValidationResult(final CommandResult result, final OrderExecutionCsvData csvData, final List<APNodeTestInfo> nodesToOrder) {
        verifyCommandResult(csvData.isSuccessfulCommandResult(), result);
        verifyValidationMessage(csvData.getValidationMsg(), result);
        verifyStatus(nodesToOrder, csvData.isSuccessfulWorkflowResult());
    }

    private CommandResult orderNode(final APServiceOperator operator, final List<APNodeTestInfo> nodesToOrder) {
        final String nodeName = nodesToOrder.iterator().next().getName();
        return operator.orderNode(nodeName);
    }

    private void verifyCommandResult(final boolean expectedCommandResult, final CommandResult result) {
        setTestStep("Verifying command result");
        assertEquals("Order integration command returned unexpected result", expectedCommandResult, result.isSuccessful());
    }

    private void verifyValidationMessage(final String expectedValidationMsg, final CommandResult result) {
        assertTrue(result.getValidationMessage().contains(expectedValidationMsg));
    }

    private void validateWorkflow(final List<APNodeTestInfo> nodesToIntegrate, final boolean successfulWorkflowResult) {

        for (final APNodeTestInfo nodeInfo : nodesToIntegrate) {
            nodeStateTransitionTimer.waitForStateTransitionFromState(nodeInfo.getApNodeFdn(), "ORDER_STARTED");

            if (successfulWorkflowResult) {
                orderWorkflowSuccessValidator.execute(nodeInfo);
            } else {
                orderWorkflowRollbackValidator.execute(nodeInfo);
            }
        }
    }

    private void verifyStatus(final List<APNodeTestInfo> nodesToOrder, final boolean successfulWorkflowResult) {
        setTestStep("Verifying node status");
        for (final APNodeTestInfo nodeInfo : nodesToOrder) {
            if (successfulWorkflowResult) {
                assertEquals("ORDER_COMPLETED", nodeStatusHelper.getState(nodeInfo.getApNodeFdn()));
            } else {
                assertEquals("ORDER_FAILED", nodeStatusHelper.getState(nodeInfo.getApNodeFdn()));
            }
        }
    }

    private void importProject(final byte[] zipFileContents, final APProjectTestInfo projectInfo) {
        setTestStep("Prepare and import project with nodes.");
        final CommandResult importResult = importProjectHelper.importProject(zipFileContents, projectInfo);

        if (!importResult.isSuccessful()) {
            fail("Failed to import project " + projectInfo.getName());
        }

        testDataCleaner.markFdnForCleanUp(projectInfo.getProjectFdn());
    }

    /**
     * This method is used to created node NetworkElement with a specific name. This pre-created MeContext is used to fail the unique name validation
     * of order
     */
    private void createDuplicatedMOsForUniquenessValidation(final List<APNodeTestInfo> nodesToOrder, final String importDataProvider) {
        setTestStep("Pre-create NetworkElement and CppConnectivityInformation MOs for node uniqueness validation");

        if (!nodesToOrder.isEmpty()) {
            final APNodeTestInfo nodeTestInfo = nodesToOrder.get(0);
            final String nodeName = importDataProvider.contains("IPAddress") ? nodeTestInfo.getName() + "-Preserved" : nodeTestInfo.getName();
            final String ipAddress = nodeTestInfo.getAttributeAsString("ipAddress");

            createNetworkElementMo(nodeName);
            if (importDataProvider.contains("IPAddress")) {
                createCppConnectivityInformationMo(nodeName, ipAddress);
            }
        }
    }

    private void createNetworkElementMo(final String nodeName) {
        createMeContextMo(nodeName);

        setTestStep("Create NetworkElement MO.");

        final String networkElementFdn = "NetworkElement=" + nodeName;

        final Map<String, Object> networkElementAttributes = new HashMap<String, Object>();
        networkElementAttributes.put("networkElementId", nodeName);
        networkElementAttributes.put("neType", "ERBS");
        networkElementAttributes.put("platformType", "CPP");

        final ManagedObjectDto networkElementMo = cmOperator.createMo(hostResolver.getApacheHost(), networkElementFdn, "OSS_NE_DEF",
                NETWORK_ELEMENT_VERSION, networkElementAttributes);

        if (networkElementMo != null) {
            testDataCleaner.markFdnForCleanUp(networkElementFdn);
        }
    }

    private void createMeContextMo(final String nodeName) {

        setTestStep("Create MeContext MO.");

        final String meContextFdn = "MeContext=" + nodeName;

        final Map<String, Object> meContextAttributes = new HashMap<String, Object>();
        meContextAttributes.put("MeContextId", nodeName);
        meContextAttributes.put("neType", "ERBS");
        meContextAttributes.put("platformType", "CPP");

        final ManagedObjectDto meContextMo = cmOperator.createMo(hostResolver.getApacheHost(), meContextFdn, "OSS_TOP", ME_CONTEXT_VERSION,
                meContextAttributes);

        if (meContextMo != null) {
            testDataCleaner.markFdnForCleanUp(meContextFdn);
        }
    }

    private void createCppConnectivityInformationMo(final String nodeName, final String ipAddress) {

        setTestStep("Create CppConnectivityInformation MO.");

        final String cppConnectivityFdn = String.format("NetworkElement=%s,CppConnectivityInformation=1", nodeName);

        final Map<String, Object> cppConnectivityAttributes = new HashMap<String, Object>();
        cppConnectivityAttributes.put("CppConnectivityInformationId", "1");
        cppConnectivityAttributes.put("ipAddress", "\"" + ipAddress + "\"");
        cppConnectivityAttributes.put("port", "80");

        final ManagedObjectDto cppConnectivityMo = cmOperator.createMo(hostResolver.getApacheHost(), cppConnectivityFdn, "CPP_MED",
                CPP_CONNECTIVITY_VERSION, cppConnectivityAttributes);

        if (cppConnectivityMo != null) {
            testDataCleaner.markFdnForCleanUp(cppConnectivityFdn);
        }
    }

    private void setInvalidNodeStatePriorToExection(final String state, final APNodeTestInfo nodeTestInfo) {
        nodeStatusHelper.setState(nodeTestInfo.getApNodeFdn(), state);
    }

    /**
     * Deletes test data after execution of this class completed.
     */
    @AfterSuite(alwaysRun = true)
    public void teardown() {
        testDataCleaner.performCleanup();
    }
}
