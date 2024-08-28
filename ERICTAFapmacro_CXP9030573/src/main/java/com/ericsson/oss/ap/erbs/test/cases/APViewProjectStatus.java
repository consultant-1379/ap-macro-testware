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
import com.ericsson.oss.ap.erbs.test.steps.ViewProjectStatusTestSteps;

/**
 * Tests to verify viewing of project status. 
 *
 */
public class APViewProjectStatus extends TorTestCaseHelper {
    
    @Inject
    private ViewProjectStatusTestSteps viewProjectStatusTestSteps;
    
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
     * Executes test scenarios for viewing project status.
     */
    @Test(groups = { "Acceptance", "GAT" })
    @Context(context = Context.CLI)
    @TestId(id="TORF-15317", title ="View Project Status")
    public void testViewProjectStatus() {
 
        final TestStepFlow execute = flow("ViewProjectStatus")
                .addTestStep(annotatedMethod(generalTestSteps, GeneralTestSteps.TEST_STEP_SET_DESCRIPTION))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_CREATE_PROJECT))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_IMPORT_PROJECT))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_VERIFY_RESPONSE_SUCCESS))
                .addTestStep(annotatedMethod(viewProjectStatusTestSteps, ViewProjectStatusTestSteps.TEST_STEP_SET_NODE_STATES))
                .addTestStep(annotatedMethod(viewProjectStatusTestSteps, ViewProjectStatusTestSteps.TEST_STEP_EXECUTE_COMMAND))
                .addTestStep(annotatedMethod(viewProjectStatusTestSteps, ViewProjectStatusTestSteps.TEST_STEP_VERIFY_COMMAND_RESULT))
                .withDataSources(dataSource("ap_view_project_status"))
                .build();

        final TestScenario viewProjectStatusScenario = scenario()
                .addFlow(execute)
                .build();

        final TestScenarioRunner runner = runner().build();
        runner.start(viewProjectStatusScenario);
    }
    
    
    @AfterSuite(alwaysRun = true)
    public void teardown() {
        testDataCleaner.performCleanup();
    }

}
