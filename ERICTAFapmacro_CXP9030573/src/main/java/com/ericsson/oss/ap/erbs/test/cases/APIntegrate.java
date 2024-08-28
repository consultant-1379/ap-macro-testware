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
import com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilder;
import com.ericsson.oss.ap.core.operators.UserManagementOperator;
import com.ericsson.oss.ap.core.test.helper.TestDataCleaner;
import com.ericsson.oss.ap.erbs.test.steps.GeneralTestSteps;
import com.ericsson.oss.ap.erbs.test.steps.ImportProjectTestSteps;
import com.ericsson.oss.ap.erbs.test.steps.IntegrateNodeTestSteps;
import com.ericsson.oss.ap.erbs.test.steps.OrderNodeTestSteps;
import com.ericsson.oss.ap.erbs.test.steps.UploadPackageTestSteps;
import com.ericsson.oss.services.ap.common.Constants;

/**
 * This contains the TAF Integration Scenario(s).
 *
 * @author eeibky
 * @since 1.6.4
 */
public class APIntegrate extends TorTestCaseHelper {

    @Inject
    private TestDataCleaner testDataCleaner;

    @Inject
    private GeneralTestSteps generalSteps;

    @Inject
    private ImportProjectTestSteps importSteps;

    @Inject
    private OrderNodeTestSteps orderSteps;

    @Inject
    private IntegrateNodeTestSteps integrateSteps;

    @Inject
    private UploadPackageTestSteps uploadPackage;

    @Inject
    private UserManagementOperator createUserOperator;

    @BeforeSuite(alwaysRun = true)
    public void preTAFSetup() {
        createUserOperator.createTAFUserInNonLocalEnv();
    }
    
    @TestId(id = "TORF-8510_Func_1", title = "Single Macro Integration Complete Notification Received")
    @Context(context = { Context.CLI })
    @Test(groups = { "Acceptance", "GAT" })
    public void testNodeIntegration() {

        final TestStepFlow integrateNodeScenario = buildIntegrateFlow()
                .withDataSources(dataSource("integrate"))
                .build();
        
        final TestScenario scenario = scenario()
                .addFlow(integrateNodeScenario)
                .build();

        final TestScenarioRunner runner = runner().build();
        runner.start(scenario);
    }
    
    private TestStepFlowBuilder buildIntegrateFlow() {
        final TestStepFlowBuilder integrateNodeFlowBuilder = flow("Integrate Nodes")
                .addTestStep(annotatedMethod(generalSteps, GeneralTestSteps.TEST_STEP_SET_DESCRIPTION))

                .addTestStep(annotatedMethod(importSteps, ImportProjectTestSteps.TEST_STEP_CREATE_PROJECT))
                .addTestStep(annotatedMethod(uploadPackage, UploadPackageTestSteps.TEST_STEP_UPGRADE_PACKAGE))

                .addTestStep(annotatedMethod(importSteps, ImportProjectTestSteps.TEST_STEP_IMPORT_PROJECT))
                .addTestStep(annotatedMethod(importSteps, ImportProjectTestSteps.TEST_STEP_VERIFY_RESPONSE_SUCCESS))

                .addTestStep(annotatedMethod(orderSteps, OrderNodeTestSteps.TEST_STEP_ORDER_NODE))
                .addTestStep(annotatedMethod(orderSteps, OrderNodeTestSteps.TEST_STEP_VERIFY_ORDER_SUCCESS));

        if (Constants.ENV_LOCAL) {
            integrateNodeFlowBuilder.addTestStep(annotatedMethod(integrateSteps, IntegrateNodeTestSteps.TEST_STEP_LOCAL_INTEGRATE_SETUP))
                    .addTestStep(annotatedMethod(integrateSteps, IntegrateNodeTestSteps.TEST_STEP_INTEGRATE_NODE_UP))
                    .addTestStep(annotatedMethod(integrateSteps, IntegrateNodeTestSteps.TEST_STEP_INTEGRATE_SYNC_NODE));
        } else {
            integrateNodeFlowBuilder.addTestStep(annotatedMethod(integrateSteps, IntegrateNodeTestSteps.TEST_STEP_INTEGRATE_SETUP))
                    .addTestStep(annotatedMethod(integrateSteps, IntegrateNodeTestSteps.TEST_STEP_INTEGRATE_NODE_UP));
        }

        integrateNodeFlowBuilder.addTestStep(annotatedMethod(integrateSteps, IntegrateNodeTestSteps.TEST_STEP_INTEGRATE_SET_SITE_CONFIG_COMPLETE))
                .addTestStep(annotatedMethod(integrateSteps, IntegrateNodeTestSteps.TEST_STEP_INTEGRATE_SET_S1_COMPLETE))
                .addTestStep(annotatedMethod(integrateSteps, IntegrateNodeTestSteps.TEST_STEP_VERIFY_INTEGRATE_SUCCESS));
        return integrateNodeFlowBuilder;
    }

    /**
     * Cleanup database/SFS at the end of a test suite execution.
     */
    @AfterSuite(alwaysRun = true)
    public void teardown() {
        testDataCleaner.performCleanup();
    }
   
}
