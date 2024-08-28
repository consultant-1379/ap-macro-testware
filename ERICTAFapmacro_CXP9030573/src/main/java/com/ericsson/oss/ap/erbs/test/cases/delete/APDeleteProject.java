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
import com.ericsson.oss.ap.erbs.test.steps.delete.DeleteProjectTestSteps;

/**
 * Tests to verify delete of a project
 * 
 * @since 1.9.3
 */
public class APDeleteProject extends TorTestCaseHelper {

    @Inject
    private ImportProjectTestSteps importProjectTestSteps;

    @Inject
    private DeleteNodeTestSteps deleteNodeTestSteps;

    @Inject
    private DeleteProjectTestSteps deleteProjectTestSteps;

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
     * Executes test scenarios of delete project
     */
    @Test(groups = { "Acceptance", "GAT" })
    @Context(context = Context.CLI)
    @TestId(id = "TORF-8568", title = "Delete project successfully and the project data is completely deleted (with valid node states)")
    public void testDeleteProjectSuccess() {

        final TestScenario deleteScenario = createTestScenarioOfDeleteProjectWithStateCheck("testScenario=='delete_project_with_node_in_valid_state'", true);
        final TestScenarioRunner runner = runner()
                .withExceptionHandler(ExceptionHandler.IGNORE)
                .build();
        runner.start(deleteScenario);
    }

    /**
     * Executes test scenarios of delete project failure
     */
    @Test(groups = { "Acceptance", "GAT" })
    @Context(context = Context.CLI)
    @TestId(id = "TORF-11288", title = "Delete project failure with child node(s) in a state that blocks the delete")
    public void testDeleteProjectFailureWithInvalidNodeStates() {

        final TestScenario deleteScenario = createTestScenarioOfDeleteProjectWithStateCheck("testScenario=='delete_project_with_node_in_invalid_state'", false);
        final TestScenarioRunner runner = runner()
                .withExceptionHandler(ExceptionHandler.IGNORE)
                .build();
        runner.start(deleteScenario);
    }


    private TestScenario createTestScenarioOfDeleteProjectWithStateCheck(final String filter, final boolean expectDeleteSuccess) {

        final TestStepFlowBuilder testStepFlowBuilder = flow("DeleteProject")
                .addTestStep(annotatedMethod(generalTestSteps, GeneralTestSteps.TEST_STEP_SET_DESCRIPTION))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_CREATE_PROJECT))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_IMPORT_PROJECT))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_VERIFY_RESPONSE_SUCCESS))
                .addTestStep(annotatedMethod(deleteNodeTestSteps, DeleteNodeTestSteps.TEST_STEP_EDIT_NODE_STATUS))
                .addTestStep(annotatedMethod(deleteProjectTestSteps, DeleteProjectTestSteps.TEST_STEP_DELETE_PROJECT));

        if (expectDeleteSuccess) {
            testStepFlowBuilder.addTestStep(annotatedMethod(deleteProjectTestSteps, DeleteProjectTestSteps.TEST_STEP_VERIFY_PROJECT_DELETED));
        } else {
            testStepFlowBuilder.addTestStep(annotatedMethod(deleteProjectTestSteps, DeleteProjectTestSteps.TEST_STEP_VERIFY_PROJECT_DELETE_FAILURE));
        }

        final TestStepFlow deleteProjectFlow = testStepFlowBuilder
                .withDataSources(dataSource("ap_delete_project").withFilter(filter))
                .build();

        final TestScenario deleteProjectScenario = scenario()
                .addFlow(deleteProjectFlow)
                .build();

        return deleteProjectScenario;
    }

    @AfterSuite(alwaysRun = true)
    public void postTAFCleanup() {

        testDataCleaner.performCleanup();
    }
}
