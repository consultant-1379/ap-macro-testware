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
package com.ericsson.oss.ap.erbs.test.steps;

import static se.ericsson.jcat.fw.assertion.JcatAssertApi.assertEquals;
import static se.ericsson.jcat.fw.assertion.JcatAssertApi.assertTrue;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setSubTestStep;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setTestStepBegin;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setTestStepEnd;

import java.util.List;

import javax.inject.Inject;

import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.oss.ap.core.operators.APServiceOperator;
import com.ericsson.oss.ap.core.test.helper.NodeStateTransitionTimer;
import com.ericsson.oss.ap.core.test.helper.NodeStatusHelper;
import com.ericsson.oss.ap.core.test.helper.TestDataCleaner;
import com.ericsson.oss.ap.core.test.model.CommandResult;
import com.ericsson.oss.ap.erbs.test.cases.validators.OrderWorkflowValidator;
import com.ericsson.oss.ap.erbs.test.model.APNodeTestInfo;
import com.ericsson.oss.ap.erbs.test.model.APProjectTestInfo;

/**
 * Contains the Order Node Test Steps
 * 
 * @author eeibky
 * @since 1.6.4
 */
public class OrderNodeTestSteps {

    public static final String TEST_STEP_ORDER_NODE = "orderNode";
    public static final String TEST_STEP_VERIFY_ORDER_SUCCESS = "verifyOrderSuccess";

    @Inject
    private OperatorRegistry<APServiceOperator> operatorRegistry;

    @Inject
    private TestDataCleaner testDataCleaner;

    @Inject
    private OrderWorkflowValidator orderWorkflowSuccessValidator;

    @Inject
    private NodeStateTransitionTimer nodeStateTransitionTimer;

    @Inject
    private NodeStatusHelper nodeStatusHelper;

    @Inject
    private TestContext context;

    /**
     * TestStep for Initiate Order Node integrate workflow.
     * <p>
     * This Test Step will only complete when the Order has completed. I.e. even though an Order itself is asynchronous, this Test Step is not.
     * <p>
     * Requires the following test context attributes to be set
     * <ul>
     * <li><code>TEST_CONTEXT_KEY_PROJECTINFO</code></li> - the <code>APProjectTestInfo</code> for an imported project
     * </ul>
     * </p>
     */
    @TestStep(id = TEST_STEP_ORDER_NODE)
    public void orderIntegration() {

        final APProjectTestInfo projectInfo = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_PROJECTINFO);

        final String projectName = projectInfo.getName();

        final List<APNodeTestInfo> nodesToOrder = projectInfo.getNodes();

        setTestStepBegin("Order Integration for project " + projectName + " (" + nodesToOrder.size() + " node(s))");

        final APServiceOperator operator = operatorRegistry.provide(APServiceOperator.class);
        for (final APNodeTestInfo nodeInfo : nodesToOrder) {

            final String nodeName = nodeInfo.getName();

            final CommandResult result = operator.orderNode(nodeName);
            markOrderedNodeForCleanup(nodeName);

            setSubTestStep("Verifying command result succeeded");
            assertTrue("Order integration command unexpected failure", result.isSuccessful());

            final String msg = "Order integration for node " + nodeName + " is initiated. Execute command 'ap status -n " + nodeName + "' for details";
            assertEquals("Incorrect Status Message", msg, result.getStatusMessage());

            nodeStateTransitionTimer.waitForStateTransitionFromState(nodeInfo.getApNodeFdn(), "ORDER_STARTED");
        }
        setTestStepEnd();
    }

    /**
     * TestStep for verifying Successful Order Node integrate workflow.
     * <p>
     * Requires the following test context attributes to be set
     * <ul>
     * <li><code>TEST_CONTEXT_KEY_PROJECTINFO</code></li> - the <code>APProjectTestInfo</code> for an imported project
     * </ul>
     * </p>
     */
    @TestStep(id = TEST_STEP_VERIFY_ORDER_SUCCESS)
    public void verifyOrderSuccess() {
        final APProjectTestInfo projectInfo = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_PROJECTINFO);

        final List<APNodeTestInfo> nodesToOrder = projectInfo.getNodes();

        validateOrderWorkflow(nodesToOrder);
        verifyStatus(nodesToOrder);
    }


    private void validateOrderWorkflow(final List<APNodeTestInfo> nodesToIntegrate) {
        setTestStepBegin("Validate order workflow");

        for (final APNodeTestInfo nodeInfo : nodesToIntegrate) {
            orderWorkflowSuccessValidator.execute(nodeInfo);
        }
    }

    private void verifyStatus(final List<APNodeTestInfo> nodesToOrder) {
        setTestStepBegin("Verifying node status");

        for (final APNodeTestInfo nodeInfo : nodesToOrder) {
            assertEquals("ORDER_COMPLETED", nodeStatusHelper.getState(nodeInfo.getApNodeFdn()));
        }
    }

    private void markOrderedNodeForCleanup(final String nodeName) {
        testDataCleaner.markFdnForCleanUp("NetworkElement=" + nodeName);
        testDataCleaner.markFdnForCleanUp("MeContext=" + nodeName);
    }
}
