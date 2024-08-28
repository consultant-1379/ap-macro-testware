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

import static com.ericsson.oss.ap.erbs.test.steps.helper.StateConsumer.StateConsumerBuilder.newBuilder;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setSubTestStep;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setTestInfo;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setTestStepBegin;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setTestStepEnd;
import static se.ericsson.jcat.fw.ng.JcatNGTestBase.assertEquals;
import static se.ericsson.jcat.fw.ng.JcatNGTestBase.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.cifwk.taf.management.TafContext;
import com.ericsson.cifwk.taf.management.TafExecutionAttributes;
import com.ericsson.oss.ap.core.getters.APHostResolver;
import com.ericsson.oss.ap.core.operators.APServiceOperator;
import com.ericsson.oss.ap.core.operators.cm.CmCliOperator;
import com.ericsson.oss.ap.core.test.model.CommandResult;
import com.ericsson.oss.ap.core.test.model.ResultEntity;
import com.ericsson.oss.ap.erbs.test.model.APNodeTestInfo;
import com.ericsson.oss.ap.erbs.test.model.APProjectTestInfo;
import com.ericsson.oss.ap.erbs.test.steps.helper.StateConsumer;
import com.ericsson.oss.ap.erbs.test.steps.helper.StateWrapper;

/**
 * Test Steps for AP Status Command
 *  @since 1.14.7
 */
public class ViewSummaryOfProjectStatesTestSteps {

    private static final String CURRENT_LOOP_INDEX = "CurrentLoopIndex";
    private static final String COMPOUND_TEST_DATA = "CompoundTestData";

    public static final String TEST_STEP_EXECUTE_COMMAND = "ExecuteViewProjectStatusCommand";
    public static final String TEST_STEP_VERIFY_COMMAND_RESULT = "VerifyViewProjectStatusResult";
    public static final String TEST_STEP_AGGREGATE_DATA = "AggregateData";
    public static final String TEST_STEP_SET_NODE_STATES = "SetNodeStates";
    public static final String TEST_STEP_SET_RIGHT_CONTEXT = "SetContext";
    public static final String TEST_STEP_SET_LOOP_INDEX = "SetLoopIndex";

    public static final String TEST_CONTEXT_KEY_COMMAND_RESULT = "commandResult";
    public static final String TEST_CONTEXT_KEY_PROJECT_SUMMARY = "projectSummary";
    public static final String TEST_CONTEXT_KEY_NODE_SUMMARY = "nodeSummary";

    @Inject
    private CmCliOperator cmOperator;

    @Inject
    private TestContext context;

    @Inject
    private APHostResolver hostResolver;

    @Inject
    private OperatorRegistry<APServiceOperator> operatorRegistry;

    @TestStep(id = TEST_STEP_SET_RIGHT_CONTEXT)
    public void setCliContext() {
        final TafExecutionAttributes attr = TafContext.getRuntimeAttributes();
        attr.setAttribute(TafContext.CONTEXT, Context.CLI);

        final TafExecutionAttributes parentAttr = TafContext.getParentRuntimeAttributes();
        parentAttr.setAttribute(TafContext.CONTEXT, Context.CLI);
    }

    @TestStep(id = TEST_STEP_AGGREGATE_DATA)
    public void aggregateData() {
        final List<APProjectTestInfo> allInfo = getCurrentAggregateData();
        final APProjectTestInfo currentInfo = getCurrentProjectInfo();

        allInfo.add(currentInfo);
        context.setAttribute(COMPOUND_TEST_DATA, allInfo);
    }

    private APProjectTestInfo getCurrentProjectInfo() {
        return context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_PROJECTINFO);
    }

    private List<APProjectTestInfo> getCurrentAggregateData() {
        final List<APProjectTestInfo> result = context.getAttribute(COMPOUND_TEST_DATA);
        if (result == null) {
            return new ArrayList<>();
        }

        return result;
    }

    @TestStep(id = TEST_STEP_SET_LOOP_INDEX)
    public void updateCurrentIndex() {
        final Integer current = context.getAttribute(CURRENT_LOOP_INDEX);
        if (current == null) {
            context.setAttribute(CURRENT_LOOP_INDEX, 0);
        } else {
            context.setAttribute(CURRENT_LOOP_INDEX, current + 1);
        }
    }

    private Integer getCurrentIndex() {
        return context.getAttribute(CURRENT_LOOP_INDEX);
    }

    @TestStep(id = TEST_STEP_SET_NODE_STATES)
    public void setNodeStates(@Input("nodeStates") final int[] allNodeStates) {
        setSubTestStep("Set node states for project");
        final StateWrapper nodeStates = getNodeStates(allNodeStates, getCurrentIndex());
        final StateConsumer consumer = newBuilder()
            .fromWrapper(nodeStates)
            .build();

        setNodeStates(getCurrentProjectInfo(), consumer);
    }

    private StateWrapper getNodeStates(final int[] allNodeStates, final int index) {
        final int[] result = new int[4];
        for (int i = 0; i < result.length; i++) {
            result[i] = allNodeStates[4*index + i];
        }

        return new StateWrapper(result);
    }

    private void setNodeStates(final APProjectTestInfo projectInfo, final StateConsumer consumer) {
        for (final APNodeTestInfo nodeInfo : projectInfo.getNodes()) {
            setNodeState(nodeInfo, consumer);
        }
    }

    private void setNodeState(final APNodeTestInfo nodeInfo, final StateConsumer consumer) {
        final String nextState = consumer.getNextState();

        final Map<String, Object> nodeStatusAttrs = new HashMap<String, Object>();
        nodeStatusAttrs.put("state", nextState);

        cmOperator.updateMo(hostResolver.getApacheHost(), nodeInfo.getApNodeFdn() + ",NodeStatus=1", nodeStatusAttrs);
        setTestInfo("Set state " + nextState + " for node" + nodeInfo.getName());
    }

    /**
     * Executes the ap status command.
     * <p>
     * Sets the following test context attributes
     * <ul>
     * <li><code>TEST_CONTEXT_KEY_COMMAND_RESULT</code></li> -the <code>CommandResult</code> for the command execution
     * </ul>
     * </p>
     */
    @TestStep(id = TEST_STEP_EXECUTE_COMMAND)
    public void executeViewProjectStatusCommand() {
        setTestStepBegin("Execute 'ap status' command");
        final APServiceOperator operator = operatorRegistry.provide(APServiceOperator.class);
        final CommandResult commandResult = operator.viewSummaryOfProjectStates();
        context.setAttribute(TEST_CONTEXT_KEY_COMMAND_RESULT, commandResult);
        setTestStepEnd();
    }

    /**
     * Verifies the result of viewing the summary of project states.
     * <p>
     * Requires the following test context attributes to be set
     * <ul>
     * <li><code>TEST_CONTEXT_KEY_PROJECTINFO</code></li> - the <code>APProjectTestInfo</code> for an imported project
     * <li><code>TEST_CONTEXT_KEY_COMMAND_RESULT</code></li> -the <code>CommandResult</code> for the command execution
     * </ul>
     * </p>
     *
     * @param expectedCommandResult
     *            - the expected command result
     * @param errorMessage
     *            - the expected error message in case of failure
     */
    @TestStep(id = TEST_STEP_VERIFY_COMMAND_RESULT)
    public void verifyViewProjectStatusResult(
        @Input("projectCount") final int projectCount,
        @Input("nodeStates") final int[] allNodeStates,
        @Input("commandResult") final String expectedCommandResult) {

        setTestStepBegin("Verify result of 'ap status' command");

        final CommandResult commandResult = context.getAttribute(TEST_CONTEXT_KEY_COMMAND_RESULT);
        assertEquals("View Summary of Project States command returned unexpected result", expectedCommandResult, commandResult.getResult().toString());

        final Set<String> checkedProjects = new HashSet<>();
        for (final ResultEntity resultEntity : commandResult.getResultEntities()) {
            final Map<String, String> attributes = resultEntity.getAttributes();
            final String projectName = attributes.get("Project Name");

            final int projectIndexInList = indexOf(projectName);
            if (projectIndexInList >= 0) {
                final StateWrapper nodeStates = getNodeStates(allNodeStates, projectIndexInList);
                final int nodeQuantity =   nodeStates.getNotStartedNodes()
                    + nodeStates.getStartedNodes()
                    + nodeStates.getCompletedNodes()
                    + nodeStates.getFailedNodes();

                assertTrue("Project " + projectName + " appeared more than once in result", checkedProjects.add(projectName));
                assertEquals("Invalid amount of nodes on summary", String.valueOf(nodeQuantity), attributes.get("Node Quantity"));
                assertEquals("Invalid ammount of not started nodes on summary", String.valueOf(nodeStates.getNotStartedNodes()), attributes.get("Not Started"));
                assertEquals("Invalid ammount of started nodes on summary", String.valueOf(nodeStates.getStartedNodes()), attributes.get("Started"));
                assertEquals("Invalid ammount of completed nodes on summary", String.valueOf(nodeStates.getCompletedNodes()), attributes.get("Completed"));
                assertEquals("Invalid ammount of failed nodes on summary", String.valueOf(nodeStates.getFailedNodes()), attributes.get("Failed"));
            }
        }

        assertEquals("Did not check all projects", projectCount, checkedProjects.size());
        setTestStepEnd();
    }

    private int indexOf(final String projectName) {
        final List<APProjectTestInfo> projects = getCurrentAggregateData();
        for (int index = 0; index < projects.size(); index++) {
            final APProjectTestInfo info = projects.get(index);
            if (info.getName().equals(projectName)) {
                return index;
            }
        }

        return -1;
    }

}
