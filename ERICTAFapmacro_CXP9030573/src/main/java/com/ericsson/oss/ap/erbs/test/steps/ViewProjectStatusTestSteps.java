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
package com.ericsson.oss.ap.erbs.test.steps;

import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setTestInfo;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setTestStepBegin;
import static se.ericsson.jcat.fw.ng.JcatNGTestBase.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;

import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.oss.ap.core.getters.APHostResolver;
import com.ericsson.oss.ap.core.operators.APServiceOperator;
import com.ericsson.oss.ap.core.operators.cm.CmCliOperator;
import com.ericsson.oss.ap.core.test.model.CommandResult;
import com.ericsson.oss.ap.core.test.model.ResultEntity;
import com.ericsson.oss.ap.erbs.test.model.APNodeTestInfo;
import com.ericsson.oss.ap.erbs.test.model.APProjectTestInfo;
import com.ericsson.oss.services.ap.model.State;

/**
 * Test Steps for viewing project status.
 * 
 */
public class ViewProjectStatusTestSteps {

    @Inject
    private CmCliOperator cmOperator;

    @Inject
    private APHostResolver hostResolver;

    @Inject
    private OperatorRegistry<APServiceOperator> operatorRegistry;

    @Inject
    private TestContext context;

    public static final String TEST_STEP_IMPORT_PROJECT = "ImportProject";
    public static final String TEST_STEP_SET_NODE_STATES = "SetNodeStates";
    public static final String TEST_STEP_EXECUTE_COMMAND = "ExecuteViewProjectStatusCommand";
    public static final String TEST_STEP_VERIFY_COMMAND_RESULT = "VerifyViewProjectStatusResult";

    public static final String TEST_CONTEXT_KEY_PROJECT_SUMMARY = "projectSummary";
    public static final String TEST_CONTEXT_KEY_NODE_SUMMARY = "nodeSummary";
    public static final String TEST_CONTEXT_KEY_COMMAND_RESULT = "commandResult";

    /**
     * Sets the states for each node in the project to a random state value.
     * <p>
     * Requires the following test context attributes to be set
     * <ul>
     * <li><code>TEST_CONTEXT_KEY_PROJECTINFO</code></li> - the
     * <code>APProjectTestInfo</code> for an imported project
     * </ul>
     * </p>
     * <p>
     * Sets the following test context attributes
     * <ul>
     * <li><code>TEST_CONTEXT_KEY_PROJECT_SUMMARY</code></li> - Map with key
     * equal to state and value equal to the number of nodes in that state
     * <li><code>TEST_CONTEXT_KEY_NODE_SUMMARY</code></li> - Map with key equal
     * to node name and value equal to the state of the node
     * </ul>
     * </p>
     */
    @TestStep(id = TEST_STEP_SET_NODE_STATES)
    public void setStatesForAllNodesInProject() {
        final APProjectTestInfo projectInfo = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_PROJECTINFO);

        setTestStepBegin("Set random state for all nodes in project " + projectInfo.getName());

        final Map<String, Integer> projectSummary = new HashMap<>();
        final Map<String, String> nodeSummary = new HashMap<>();

        for (final APNodeTestInfo nodeTestInfo : projectInfo.getNodes()) {
            final int randomOrdinal = new Random().nextInt(3);
            final State randomState = State.values()[randomOrdinal];

            final Map<String, Object> nodeStatusAttrs = new HashMap<String, Object>();
            nodeStatusAttrs.put("state", randomState.toString());

            cmOperator.updateMo(hostResolver.getApacheHost(), nodeTestInfo.getApNodeFdn() + ",NodeStatus=1", nodeStatusAttrs);
            setTestInfo("Set state " + randomState + " for node" + nodeTestInfo.getName());

            if (projectSummary.containsKey(randomState.toString())) {
                Integer stateCount = projectSummary.get(randomState.toString());
                projectSummary.put(randomState.toString(), ++stateCount);
            } else {
                projectSummary.put(randomState.toString(), Integer.valueOf(1));
            }
            nodeSummary.put(nodeTestInfo.getName(), randomState.toString());
        }

        context.setAttribute(TEST_CONTEXT_KEY_PROJECT_SUMMARY, projectSummary);
        context.setAttribute(TEST_CONTEXT_KEY_NODE_SUMMARY, nodeSummary);
    }

    /**
     * Executes the ap status command for the project.
     * <p>
     * Requires the following test context attributes to be set
     * <ul>
     * <li><code>TEST_CONTEXT_KEY_PROJECTINFO</code></li> - the
     * <code>APProjectTestInfo</code> for an imported project
     * </ul>
     * </p>
     * <p>
     * Sets the following test context attributes
     * <ul>
     * <li><code>TEST_CONTEXT_KEY_COMMAND_RESULT</code></li> -the
     * <code>CommandResult</code> for the command execution
     * </ul>
     * </p>
     */
    @TestStep(id = TEST_STEP_EXECUTE_COMMAND)
    public void executeViewProjectStatusCommand() {
        final APProjectTestInfo projectInfo = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_PROJECTINFO);
        setTestStepBegin("Execute 'ap status' command for project " + projectInfo.getName());
        final APServiceOperator operator = operatorRegistry.provide(APServiceOperator.class);
        final CommandResult commandResult = operator.viewProjectStatus(projectInfo.getName());
        context.setAttribute(TEST_CONTEXT_KEY_COMMAND_RESULT, commandResult);
    }

    /**
     * Verifies the result of viewing project status.
     * <p>
     * Requires the following test context attributes to be set
     * <ul>
     * <li><code>TEST_CONTEXT_KEY_PROJECTINFO</code></li> - the
     * <code>APProjectTestInfo</code> for an imported project
     * <li><code>TEST_CONTEXT_KEY_COMMAND_RESULT</code></li> -the
     * <code>CommandResult</code> for the command execution
     * </ul>
     * </p>
     * 
     * @param expectedCommandResult
     *            - the expected command result
     * @param errorMessage
     *            - the expected error message in case of failure
     */
    @TestStep(id = TEST_STEP_VERIFY_COMMAND_RESULT)
    public void verifyViewProjectStatusResult(@Input("commandResult") final String expectedCommandResult) {
        final APProjectTestInfo projectInfo = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_PROJECTINFO);
        setTestStepBegin("Verify result of 'ap status' command for project " + projectInfo.getName());

        final CommandResult commandResult = context.getAttribute(TEST_CONTEXT_KEY_COMMAND_RESULT);

        assertEquals("View Project Status command returned unexpected result", expectedCommandResult, commandResult.getResult().toString());

        final ResultEntity resultEntity = commandResult.getResultEntities().iterator().next();

        assertEquals(projectInfo.getName(), resultEntity.getAttributes().get("Project Name"));
        assertEquals(String.valueOf(projectInfo.getNodes().size()), resultEntity.getAttributes().get("Number of Nodes"));

        final List<ResultEntity> tablesEntries = resultEntity.getChildren(); //contains contents of both tables

        final Map<String, Integer> projectSummary = context.getAttribute(TEST_CONTEXT_KEY_PROJECT_SUMMARY);
        final Map<String, String> nodeSummary = context.getAttribute(TEST_CONTEXT_KEY_NODE_SUMMARY);

        assertEquals("Unexpected number of table entries", projectSummary.size() + nodeSummary.size(), tablesEntries.size());

        final List<ResultEntity> projectSummaryTableEntries = tablesEntries.subList(0, projectSummary.size());
        final List<ResultEntity> nodeSummaryTableEntries = tablesEntries.subList(projectSummary.size(), tablesEntries.size());

        verifyOverallProjectStates(projectSummary, projectSummaryTableEntries);
        verifyIndividualNodeStates(nodeSummary, nodeSummaryTableEntries);
    }

    private void verifyOverallProjectStates(final Map<String, Integer> projectSummary, final List<ResultEntity> projectSummaryTableEntries) {
        for (final ResultEntity tableEntry : projectSummaryTableEntries) {
            final Map<String, String> tableRow = tableEntry.getAttributes();
            final String state = tableRow.get("State");
            final Integer expectedNumNodesInState = projectSummary.get(toStateEnumValue(state));
            final Integer actualNumNodesInState = Integer.valueOf(tableRow.get("Number of Nodes"));
            assertEquals("Unexpected number of nodes in state " + state, expectedNumNodesInState, actualNumNodesInState);
        }
    }

    private void verifyIndividualNodeStates(final Map<String, String> nodeSummary, final List<ResultEntity> nodeSummaryTableEntries) {
        for (final ResultEntity tableEntry : nodeSummaryTableEntries) {
            final Map<String, String> tableRow = tableEntry.getAttributes();
            final String nodeName = tableRow.get("Node Name");
            final String expectedNodeState = nodeSummary.get(nodeName);
            final String actualNodeState = toStateEnumValue(tableRow.get("State"));
            assertEquals("Unexpected state for node " + nodeName, expectedNodeState, actualNodeState);
        }
    }

    private String toStateEnumValue(final String state) {
        return state.replaceAll("\\s", "_").toUpperCase();
    }

}
