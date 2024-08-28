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
package com.ericsson.oss.ap.erbs.test.cases.delete;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;

import javax.inject.Inject;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.api.ExceptionHandler;
import com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilder;
import com.ericsson.oss.ap.core.operators.UserManagementOperator;
import com.ericsson.oss.ap.core.test.helper.TestDataCleaner;
import com.ericsson.oss.ap.erbs.test.steps.GeneralTestSteps;
import com.ericsson.oss.ap.erbs.test.steps.ImportProjectTestSteps;
import com.ericsson.oss.ap.erbs.test.steps.delete.DeleteNodeTestSteps;

/**
 * Tests to verify delete
 * 
 * @author exuuguu
 * @since 1.8.2
 */
public class APDeleteNode extends TorTestCaseHelper {

    @Inject
    private ImportProjectTestSteps importProjectTestSteps;

    @Inject
    private DeleteNodeTestSteps deleteTestSteps;

    @Inject
    private GeneralTestSteps generalTestSteps;

    @Inject
    private TestDataCleaner testDataCleaner;

    @Inject
    private UserManagementOperator createUserOperator;

    @BeforeSuite(alwaysRun = true)
    public void preTAFSetup() {
        createUserOperator.createTAFUserInNonLocalEnv();
    }

    /**
     * Executes test scenarios of delete node
     */
    @Test(groups = { "Acceptance", "GAT" })
    @Context(context = Context.CLI)
    @TestId(id = "TORF-15763", title = "Successfully delete node when node is in valid state")
    public void testDeleteNodeSuccessWhenNodeInValidState() {

        final TestScenario deleteScenario = createTestScenarioOfDeleteNodeWithStateCheck("testScenario=='delete_node_valid_state'", true);
        final TestScenarioRunner runner = runner()
                .withExceptionHandler(ExceptionHandler.IGNORE)
                .build();
        runner.start(deleteScenario);
    }

    @Test(groups = { "Acceptance", "GAT" })
    @Context(context = Context.CLI)
    @TestId(id = "TORF-15763", title = "Failed delete node when node is in invalid states")
    public void testDeleteNodeFailsWhenNodeInInvalidState() {
        final TestScenario deleteScenario = createTestScenarioOfDeleteNodeWithStateCheck("testScenario=='delete_node_invalid_state'", false);
        final TestScenarioRunner runner = runner()
                .withExceptionHandler(ExceptionHandler.IGNORE)
                .build();
        runner.start(deleteScenario);
    }

    private TestScenario createTestScenarioOfDeleteNodeWithStateCheck(final String filter, final boolean isExpectedDeleteNodeSuccessful) {

        final TestStepFlowBuilder deleteNodeFlowBuilder = flow("DeleteNode")
                .addTestStep(annotatedMethod(generalTestSteps, GeneralTestSteps.TEST_STEP_SET_DESCRIPTION))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_CREATE_PROJECT))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_IMPORT_PROJECT))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_VERIFY_RESPONSE_SUCCESS))
                .addTestStep(annotatedMethod(deleteTestSteps, DeleteNodeTestSteps.TEST_STEP_EDIT_NODE_STATUS))
                .addTestStep(annotatedMethod(deleteTestSteps, DeleteNodeTestSteps.TEST_STEP_DELETE_NODE));

        if (isExpectedDeleteNodeSuccessful) {
            deleteNodeFlowBuilder.addTestStep(annotatedMethod(deleteTestSteps, DeleteNodeTestSteps.TEST_STEP_VERIFY_NODE_DELETED));
        }

        final TestStepFlow deleteNodeFlow = deleteNodeFlowBuilder
                .withDataSources(dataSource("ap_delete_node").withFilter(filter))
                .build();

        final TestScenario deleteNodeScenario = scenario()
                .addFlow(deleteNodeFlow)
                .build();

        return deleteNodeScenario;
    }

    @AfterSuite(alwaysRun = true)
    public void postTAFCleanup() {

        testDataCleaner.performCleanup();
    }
}
