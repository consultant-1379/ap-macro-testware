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
import com.ericsson.oss.ap.core.operators.UserManagementOperator;
import com.ericsson.oss.ap.core.test.helper.TestDataCleaner;
import com.ericsson.oss.ap.erbs.test.steps.GeneralTestSteps;
import com.ericsson.oss.ap.erbs.test.steps.ImportProjectTestSteps;
import com.ericsson.oss.ap.erbs.test.steps.ViewTestSteps;

/**
 * Tests the <code><b>ap view</b></code> use cases, using TAF scenarios:
 * <p>
 * <ul>
 * <li>View all projects</li>
 * <li>View single project</li>
 * <li>View single node</li>
 * </ul>
 * 
 * @author eshemeh
 * @since 1.10.8
 */
public class APView extends TorTestCaseHelper {

    @Inject
    private TestDataCleaner testDataCleaner;

    @Inject
    private GeneralTestSteps generalSteps;

    @Inject
    private ImportProjectTestSteps importSteps;

    @Inject
    private ViewTestSteps viewSteps;

    @Inject
    private UserManagementOperator createUserOperator;

    /**
     * Creates TAF user on server for testing.
     */
    @BeforeSuite(alwaysRun = true)
    public void preTAFSetup() {
        createUserOperator.createTAFUserInNonLocalEnv();
    }

    /**
     * Tests all of the <code><b>ap view</b></code> use-cases.
     */
    @TestId(id = "TORF-6096", title = "View Project with project properties")
    @Context(context = { Context.CLI })
    @Test(groups = { "Acceptance", "GAT" })
    public void testViewUseCases() {

        final TestStepFlow viewScenario = flow("View projects, project & node")
                .addTestStep(annotatedMethod(generalSteps, GeneralTestSteps.TEST_STEP_SET_DESCRIPTION))

                .addTestStep(annotatedMethod(importSteps, ImportProjectTestSteps.TEST_STEP_IMPORT_MULTIPLE_PROJECTS))

                .addTestStep(annotatedMethod(viewSteps, ViewTestSteps.TEST_STEP_VIEW_ALL_PROJECTS))
                .addTestStep(annotatedMethod(viewSteps, ViewTestSteps.TEST_STEP_VIEW_PROJECTS))
                .addTestStep(annotatedMethod(viewSteps, ViewTestSteps.TEST_STEP_VIEW_NODES))
                .withDataSources(dataSource("view"))
                .build();

        final TestScenario scenario = scenario()
                .addFlow(viewScenario)
                .build();

        final TestScenarioRunner runner = runner().build();
        runner.start(scenario);
    }

    /**
     * Cleanup database/SFS at the end of a test suite execution.
     */
    @AfterSuite(alwaysRun = true)
    public void teardown() {
        testDataCleaner.performCleanup();
    }
}
