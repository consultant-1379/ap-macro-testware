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
package com.ericsson.oss.ap.common.test.cases.download;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilder;
import com.ericsson.oss.ap.common.test.steps.ArchiveContentTestSteps;
import com.ericsson.oss.ap.common.test.steps.CommandWithDownloadTestSteps;
import com.ericsson.oss.ap.core.operators.UserManagementOperator;
import com.ericsson.oss.ap.erbs.test.steps.GeneralTestSteps;
import com.google.inject.Inject;

/**
 * Tests for the command 'ap download -x'. This command downloads a zip archive containing scheamas and sample artifacts.
 * 
 * @author epaudoy
 * @since 3.21.0
 */
public class DownloadSampleArtifactsAndSchemas extends TorTestCaseHelper {

    @Inject
    private CommandWithDownloadTestSteps executeCommandWithDownloadTestStep;

    @Inject
    private ArchiveContentTestSteps verifyArchiveContentTestStep;

    @Inject
    private GeneralTestSteps generalTestSteps;

    @Inject
    private UserManagementOperator createUserOperator;

    @BeforeSuite(alwaysRun = true)
    public void preTAFSetup() {
        createUserOperator.createTAFUserInNonLocalEnv();
    }

    @Test(groups = { "Acceptance", "GAT" })
    @Context(context = Context.CLI)
    @TestId(id = "TORF-20667", title = "Successful Download Sample Artifacts and Schemas")
    public void testDownloadSampleArtifactsAndSchemasSuccessfull() {
        startTestScenario("testScenario=='valid_node_type'", true);
    }

    @Test(groups = { "Acceptance", "GAT" })
    @Context(context = Context.CLI)
    @TestId(id = "TORF-20667", title = "Unsuccessful Download Sample Artifacts and Schemas with incorrect node type")
    public void testDownloadSampleArtifactsAndSchemasNodeTypeNotSupported() {
        startTestScenario("testScenario=='invalid_node_type'", false);
    }

    private void startTestScenario(final String filter, final boolean successfulResultExpected) {
        final TestStepFlowBuilder testStepFlowBuilder = flow("Download Sample Artifacts and Schemas")
                .addTestStep(annotatedMethod(generalTestSteps, GeneralTestSteps.TEST_STEP_SET_DESCRIPTION))
                .addTestStep(annotatedMethod(executeCommandWithDownloadTestStep, CommandWithDownloadTestSteps.VERIFY_COMMAND_EXPECTED_RESULT))
                .addTestStep(annotatedMethod(executeCommandWithDownloadTestStep, CommandWithDownloadTestSteps.VERIFY_COMMAND_EXPECTED_RESPONSE_MESSAGE));

        if (successfulResultExpected) {
            testStepFlowBuilder.addTestStep(annotatedMethod(verifyArchiveContentTestStep, ArchiveContentTestSteps.VERIFY_ARCHIVE_CONTENTS_OK));
        }

        final TestStepFlow testStepFlow = testStepFlowBuilder.withDataSources(
                dataSource("ap_download_sample_artifacts_and_schemas").withFilter(filter)).build();

        final TestScenario scenario = scenario().addFlow(testStepFlow).build();

        final TestScenarioRunner runner = runner().build();
        runner.start(scenario);
    }
}
