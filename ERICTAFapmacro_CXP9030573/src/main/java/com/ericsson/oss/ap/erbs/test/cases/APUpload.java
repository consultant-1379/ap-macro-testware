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
import com.ericsson.cifwk.taf.scenario.api.ExceptionHandler;
import com.ericsson.oss.ap.core.operators.UserManagementOperator;
import com.ericsson.oss.ap.core.test.helper.TestDataCleaner;
import com.ericsson.oss.ap.erbs.test.steps.GeneralTestSteps;
import com.ericsson.oss.ap.erbs.test.steps.ImportProjectTestSteps;
import com.ericsson.oss.ap.erbs.test.steps.UploadTestSteps;

/**
 * Tests to verify upload artifact
 * 
 * @author exuuguu
 * @since 1.8.2
 */
public class APUpload extends TorTestCaseHelper {

    @Inject
    private GeneralTestSteps generalTestSteps;

    @Inject
    private ImportProjectTestSteps importProjectTestSteps;

    @Inject
    private UploadTestSteps uploadTestSteps;

    @Inject
    private TestDataCleaner testDataCleaner;

    @Inject
    private UserManagementOperator createUserOperator;

    @BeforeSuite(alwaysRun = true)
    public void preTAFSetup() {
        createUserOperator.createTAFUserInNonLocalEnv();
    }

    /**
     * Executes test scenarios of upload artifact
     */
    @Test(groups = { "Acceptance", "GAT" })
    @Context(context = Context.CLI)
    @TestId(id = "TORF-11283_Func_1", title = "Upload artifact for a specific node")
    public void testUploadArtifact() {

        final TestStepFlow execute = flow("UploadArtifact")
                .addTestStep(annotatedMethod(generalTestSteps, GeneralTestSteps.TEST_STEP_SET_DESCRIPTION))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_CREATE_PROJECT))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_IMPORT_PROJECT))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_VERIFY_RESPONSE_SUCCESS))
                .addTestStep(annotatedMethod(uploadTestSteps, UploadTestSteps.TEST_STEP_UPLOAD_ARTIFACT))
                .addTestStep(annotatedMethod(uploadTestSteps, UploadTestSteps.TEST_STEP_VERIFY_UPLOAD))
                .withDataSources(dataSource("ap_upload").withFilter("testScenario=='upload'"))
                .build();

        final TestScenario uploadArtifactScenario = scenario()
                .addFlow(execute)
                .build();

        final TestScenarioRunner runner = runner()
                .withExceptionHandler(ExceptionHandler.IGNORE)
                .build();
        runner.start(uploadArtifactScenario);
    }

    /**
     * Executes test scenarios of upload artifact for a node with valid state successfully
     */
    @Test(groups = { "Acceptance", "GAT" })
    @Context(context = Context.CLI)
    @TestId(id = "TORF-6388_Func_1", title = "Upload artifact for a specific node with valid state successfully")
    public void testUploadArtifactSuccessWhenNodeInValidState() {

        final TestStepFlow execute = flow("UploadArtifact")
                .addTestStep(annotatedMethod(generalTestSteps, GeneralTestSteps.TEST_STEP_SET_DESCRIPTION))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_CREATE_PROJECT))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_IMPORT_PROJECT))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_VERIFY_RESPONSE_SUCCESS))
                .addTestStep(annotatedMethod(uploadTestSteps, UploadTestSteps.TEST_STEP_EDIT_NODE_STATUS))
                .addTestStep(annotatedMethod(uploadTestSteps, UploadTestSteps.TEST_STEP_UPLOAD_ARTIFACT))
                .addTestStep(annotatedMethod(uploadTestSteps, UploadTestSteps.TEST_STEP_VERIFY_UPLOAD))
                .withDataSources(dataSource("ap_upload").withFilter("testScenario=='upload_with_valid_state'"))
                .build();

        final TestScenario uploadArtifactScenario = scenario()
                .addFlow(execute)
                .build();

        final TestScenarioRunner runner = runner()
                .withExceptionHandler(ExceptionHandler.IGNORE)
                .build();
        runner.start(uploadArtifactScenario);
    }

    /**
     * Executes test scenarios of upload artifact for a node with invalid state failed
     */
    @Test(groups = { "Acceptance", "GAT" })
    @Context(context = Context.CLI)
    @TestId(id = "TORF-6388_Func_2", title = "Upload artifact for a specific node with invalid state failed")
    public void testUploadArtifactFailsWhenNodeInInvalidState() {

        final TestStepFlow execute = flow("UploadArtifact")
                .addTestStep(annotatedMethod(generalTestSteps, GeneralTestSteps.TEST_STEP_SET_DESCRIPTION))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_CREATE_PROJECT))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_IMPORT_PROJECT))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_VERIFY_RESPONSE_SUCCESS))
                .addTestStep(annotatedMethod(uploadTestSteps, UploadTestSteps.TEST_STEP_EDIT_NODE_STATUS))
                .addTestStep(annotatedMethod(uploadTestSteps, UploadTestSteps.TEST_STEP_UPLOAD_ARTIFACT))
                .addTestStep(annotatedMethod(uploadTestSteps, UploadTestSteps.TEST_STEP_VERIFY_UPLOAD))
                .withDataSources(dataSource("ap_upload").withFilter("testScenario=='upload_with_invalid_state'"))
                .build();

        final TestScenario uploadArtifactScenario = scenario()
                .addFlow(execute)
                .build();

        final TestScenarioRunner runner = runner()
                .withExceptionHandler(ExceptionHandler.IGNORE)
                .build();
        runner.start(uploadArtifactScenario);
    }

    /**
     * Executes test scenarios of upload artifact with artifact(s) missing
     */
    @Test(groups = { "Acceptance", "GAT" })
    @Context(context = Context.CLI)
    @TestId(id = "TORF-6388_Func_3", title = "Upload artifact for a specific node with artifact(s) missing")
    public void testUploadMissingArtifact() {

        final TestStepFlow execute = flow("UploadArtifact")
                .addTestStep(annotatedMethod(generalTestSteps, GeneralTestSteps.TEST_STEP_SET_DESCRIPTION))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_CREATE_PROJECT))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_IMPORT_PROJECT))
                .addTestStep(annotatedMethod(importProjectTestSteps, ImportProjectTestSteps.TEST_STEP_VERIFY_RESPONSE_SUCCESS))
                .addTestStep(annotatedMethod(uploadTestSteps, UploadTestSteps.TEST_STEP_DELETE_ARTIFACT))
                .addTestStep(annotatedMethod(uploadTestSteps, UploadTestSteps.TEST_STEP_UPLOAD_ARTIFACT))
                .addTestStep(annotatedMethod(uploadTestSteps, UploadTestSteps.TEST_STEP_VERIFY_UPLOAD))
                .withDataSources(dataSource("ap_upload").withFilter("testScenario=='upload_with_artifact_missing'"))
                .build();

        final TestScenario uploadArtifactScenario = scenario()
                .addFlow(execute)
                .build();

        final TestScenarioRunner runner = runner()
                .withExceptionHandler(ExceptionHandler.IGNORE)
                .build();
        runner.start(uploadArtifactScenario);
    }

    @AfterSuite(alwaysRun = true)
    public void postTAFCleanup() {

        testDataCleaner.performCleanup();
    }
}
