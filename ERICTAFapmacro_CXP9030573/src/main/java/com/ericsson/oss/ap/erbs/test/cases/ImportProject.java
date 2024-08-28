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
import com.ericsson.oss.ap.common.test.steps.CreateMoTestSteps;
import com.ericsson.oss.ap.core.operators.UserManagementOperator;
import com.ericsson.oss.ap.core.test.helper.TestDataCleaner;
import com.ericsson.oss.ap.erbs.test.steps.GeneralTestSteps;
import com.ericsson.oss.ap.erbs.test.steps.ImportProjectTestSteps;

/**
 * Tests to verify import project
 */
public class ImportProject extends TorTestCaseHelper {

    @Inject
    private ImportProjectTestSteps importProjectTestSteps;

    @Inject
    private CreateMoTestSteps createMoTestSteps;
    
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
     * Executes test scenarios of import precreated project with multiple projectInfo files.
     */
    @Test(groups = { "Acceptance", "GAT" })
    @Context(context = Context.CLI)
    @TestId(id = "TORF-29774", title = "Import Precreated Project")
    public void testImportProjectWithMultipleProjectInfo() {

        final TestStepFlow execute = flow("ImportInvalidProject")
                .addTestStep(annotatedMethod(generalTestSteps, GeneralTestSteps.TEST_STEP_SET_DESCRIPTION))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_READ_PROJECT))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_IMPORT_PROJECT))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_VERIFY_IMPORT))
                .withDataSources(dataSource("import_project").withFilter("testScenario=='ap-import-validation-multiple-projectInfo-files'"))
                .build();

        final TestScenario importProjectScenario = scenario()
                .addFlow(execute)
                .build();

        final TestScenarioRunner runner = runner().build();
        runner.start(importProjectScenario);
    }


    /**
     * Executes test scenarios of import project 
     */
    @Test(groups = { "Acceptance", "GAT" })
    @Context(context = Context.CLI)
    @TestId(id = "TORF-8232", title = "Import Project")
    public void testImportProject() {

        final TestStepFlow execute = flow("ImportProject")
                .addTestStep(annotatedMethod(generalTestSteps, GeneralTestSteps.TEST_STEP_SET_DESCRIPTION))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_CREATE_PROJECT))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_IMPORT_PROJECT))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_VERIFY_IMPORT))
                .withDataSources(dataSource("import_project").withFilter("testScenario=='ap_import'"))
                .build();

        final TestScenario importProjectScenario = scenario()
                .addFlow(execute)
                .build();

        final TestScenarioRunner runner = runner().build();
        runner.start(importProjectScenario);
    }

    /**
     * Executes test scenarios of import project
     */
    @Test(groups = { "Acceptance", "GAT" })
    @Context(context = Context.CLI)
    @TestId(id = "TORF-28570", title = "Validation of import project failed as the ip address of a node is same as that of an existing CppConnectivityInformation MO")
    public void testImportProjectFailValidationDuplicateIPInNRM() {

        final TestStepFlow execute = flow("ImportProject")
                .addTestStep(annotatedMethod(generalTestSteps, GeneralTestSteps.TEST_STEP_SET_DESCRIPTION))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_CREATE_PROJECT))
                .addTestStep(annotatedMethod(createMoTestSteps, CreateMoTestSteps.TEST_STEP_CREATE_NETWORK_ELEMENT))
                .addTestStep(annotatedMethod(createMoTestSteps, CreateMoTestSteps.TEST_STEP_CREATE_CPPCONNECTIVITY_MO))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_IMPORT_PROJECT))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_VERIFY_IMPORT))
                .withDataSources(dataSource("import_project").withFilter("testScenario=='ap_import_validation_ip'"))
                .build();

        final TestScenario importProjectScenario = scenario()
                .addFlow(execute)
                .build();

        final TestScenarioRunner runner = runner().build();
        runner.start(importProjectScenario);
    }
    

    /**
     * Executes test scenarios of import project when a NetworkElement 
     * is created to validate uniqueness
     */
    @Test(groups = { "Acceptance", "GAT" })
    @Context(context = Context.CLI)
    @TestId(id = "TORF-28569", title =  "Validation of import project failed as the name of a node is same as that of an existing NetworkElement.")
    public void testImportProjectValidateNodeUniquenessFailure() {

        final TestStepFlow execute = flow("ImportProject")
                .addTestStep(annotatedMethod(generalTestSteps, GeneralTestSteps.TEST_STEP_SET_DESCRIPTION))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_CREATE_PROJECT))
                .addTestStep(annotatedMethod(createMoTestSteps, CreateMoTestSteps.TEST_STEP_CREATE_NETWORK_ELEMENT))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_IMPORT_PROJECT))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_VERIFY_IMPORT))
                .withDataSources(dataSource("import_project").withFilter("testScenario=='ap_import_validation_name'"))
                .build();

        final TestScenario importProjectScenario = scenario()
                .addFlow(execute)
                .build();

        final TestScenarioRunner runner = runner().build();
        runner.start(importProjectScenario);
    }

    
    
    @AfterSuite(alwaysRun = true)
    public void teardown() {
        testDataCleaner.performCleanup();
    }
}
