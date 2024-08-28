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
package com.ericsson.oss.ap.erbs.test.cases;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.ericsson.oss.ap.core.test.helper.IncrementedTestStepFlow.withFlow;

import javax.inject.Inject;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.oss.ap.core.operators.UserManagementOperator;
import com.ericsson.oss.ap.core.test.helper.TestDataCleaner;
import com.ericsson.oss.ap.erbs.test.steps.GeneralTestSteps;
import com.ericsson.oss.ap.erbs.test.steps.ImportProjectTestSteps;
import com.ericsson.oss.ap.erbs.test.steps.ViewSummaryOfProjectStatesTestSteps;

/**
 * Tests to verify viewing of all project statuses.
 *  @since 1.14.7
 */
public class APViewSummaryOfProjectStates extends TorTestCaseHelper {

    @Inject
    private ViewSummaryOfProjectStatesTestSteps viewSummaryOfProjectStatesTestSteps;

    @Inject
    private GeneralTestSteps generalTestSteps;

    @Inject
    private ImportProjectTestSteps importProjectTestSteps;

    @Inject
    private TestDataCleaner testDataCleaner;

    @Inject
    private UserManagementOperator createUserOperator;

    @BeforeSuite(alwaysRun = true)
    public void preTAFSetup() {
        createUserOperator.createTAFUserInNonLocalEnv();
    }

    /**
     * Executes test scenarios for viewing the summary of a project's states.
     */
    @Test(groups = { "Acceptance", "GAT" })
    @Context(context = {Context.CLI})
    @DataDriven(name = "ap_view_all_project_statuses_flows")
    @TestId(id = "TORF-15317", title = "View Summary of Project States")
    public void testViewProjectStatus(
        @Input("rowIndex") final int rowIndex,
        @Input("projectCount") final int loopCount) {

        final TestStepFlow execute = withFlow("ViewSummaryOfProjectStates")
            .loopTestSteps(
                annotatedMethod(viewSummaryOfProjectStatesTestSteps, ViewSummaryOfProjectStatesTestSteps.TEST_STEP_SET_RIGHT_CONTEXT),
                annotatedMethod(viewSummaryOfProjectStatesTestSteps, ViewSummaryOfProjectStatesTestSteps.TEST_STEP_SET_LOOP_INDEX),
                annotatedMethod(generalTestSteps, GeneralTestSteps.TEST_STEP_SET_DESCRIPTION),
                annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_CREATE_PROJECT),
                annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_IMPORT_PROJECT),
                annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_VERIFY_RESPONSE_SUCCESS),
                annotatedMethod(viewSummaryOfProjectStatesTestSteps, ViewSummaryOfProjectStatesTestSteps.TEST_STEP_AGGREGATE_DATA),
                annotatedMethod(viewSummaryOfProjectStatesTestSteps, ViewSummaryOfProjectStatesTestSteps.TEST_STEP_SET_NODE_STATES))
            .times(loopCount)
            .addTestSteps(
                annotatedMethod(viewSummaryOfProjectStatesTestSteps, ViewSummaryOfProjectStatesTestSteps.TEST_STEP_EXECUTE_COMMAND),
                annotatedMethod(viewSummaryOfProjectStatesTestSteps, ViewSummaryOfProjectStatesTestSteps.TEST_STEP_VERIFY_COMMAND_RESULT))
            .withDataSources(dataSource("ap_view_all_project_statuses").withFilter("rowIndex==" + rowIndex))
            .build();

        final TestScenario viewSummaryOfStatesScenario = scenario().addFlow(execute).build();

        final TestScenarioRunner runner = runner().build();
        runner.start(viewSummaryOfStatesScenario);
    }

    @AfterSuite(alwaysRun = true)
    public void teardown() {
        testDataCleaner.performCleanup();
    }

}
