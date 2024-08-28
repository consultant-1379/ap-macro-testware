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
import com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilder;
import com.ericsson.oss.ap.core.operators.UserManagementOperator;
import com.ericsson.oss.ap.core.test.helper.TestDataCleaner;
import com.ericsson.oss.ap.erbs.test.steps.GeneralTestSteps;
import com.ericsson.oss.ap.erbs.test.steps.ImportProjectTestSteps;
import com.ericsson.oss.ap.erbs.test.steps.IntegrateNodeTestSteps;
import com.ericsson.oss.ap.erbs.test.steps.NodeStatusTestSteps;
import com.ericsson.oss.ap.erbs.test.steps.OrderNodeTestSteps;
import com.ericsson.oss.services.ap.common.Constants;

/**
 * Tests the <code><b>ap status -n {@literal <}nodeName{@literal >}</b></code> use case, using TAF secnarios.
 * 
 * @author eshemeh
 * @since 1.14.3
 */
public class APNodeStatus extends TorTestCaseHelper {

    @Inject
    private GeneralTestSteps generalSteps;

    @Inject
    private ImportProjectTestSteps importSteps;

    @Inject
    private OrderNodeTestSteps orderSteps;

    @Inject
    private IntegrateNodeTestSteps integrateSteps;

    @Inject
    private NodeStatusTestSteps nodeStatusSteps;

    @Inject
    private TestDataCleaner testDataCleaner;

    @Inject
    private UserManagementOperator createUserOperator;

    @BeforeSuite(alwaysRun = true)
    public void preTAFSetup() {
        createUserOperator.createTAFUserInNonLocalEnv();
    }

    @TestId(id = "TORF-2335", title = "LTE Macro View Integration Progress, Status and Report CLI")
    @Context(context = { Context.CLI })
    @Test(groups = { "Acceptance", "GAT" })
    public void testNodeStatusOrderUseCase() {

        final TestStepFlow orderStatusScenario = flow("View node status after order")
                .addTestStep(annotatedMethod(generalSteps, GeneralTestSteps.TEST_STEP_SET_DESCRIPTION))

                .addTestStep(annotatedMethod(importSteps, ImportProjectTestSteps.TEST_STEP_CREATE_PROJECT))

                .addTestStep(annotatedMethod(importSteps, ImportProjectTestSteps.TEST_STEP_IMPORT_PROJECT))
                .addTestStep(annotatedMethod(importSteps, ImportProjectTestSteps.TEST_STEP_VERIFY_RESPONSE_SUCCESS))

                .addTestStep(annotatedMethod(orderSteps, OrderNodeTestSteps.TEST_STEP_ORDER_NODE))
                .addTestStep(annotatedMethod(orderSteps, OrderNodeTestSteps.TEST_STEP_VERIFY_ORDER_SUCCESS))

                .addTestStep(annotatedMethod(nodeStatusSteps, NodeStatusTestSteps.TEST_STEP_VERIFY_STATUS_ENTRIES))

                .withDataSources(dataSource("nodeStatus").withFilter("usecase=='order'")).build();

        final TestScenario scenario = scenario().addFlow(orderStatusScenario).build();

        final TestScenarioRunner runner = runner().build();
        runner.start(scenario);
    }

    @TestId(id = "TORF-2335", title = "LTE Macro View Integration Progress, Status and Report CLI")
    @Context(context = { Context.CLI })
    @Test(groups = { "Acceptance", "GAT" })
    public void testNodeStatusUnorderUseCase() {

        final TestStepFlow unorderStatusScenario = flow("View node status after unorder")
                .addTestStep(annotatedMethod(generalSteps, GeneralTestSteps.TEST_STEP_SET_DESCRIPTION))

                .addTestStep(annotatedMethod(importSteps, ImportProjectTestSteps.TEST_STEP_CREATE_PROJECT))

                .addTestStep(annotatedMethod(importSteps, ImportProjectTestSteps.TEST_STEP_IMPORT_PROJECT))
                .addTestStep(annotatedMethod(importSteps, ImportProjectTestSteps.TEST_STEP_VERIFY_RESPONSE_SUCCESS))

                .addTestStep(annotatedMethod(orderSteps, OrderNodeTestSteps.TEST_STEP_ORDER_NODE))
                .addTestStep(annotatedMethod(orderSteps, OrderNodeTestSteps.TEST_STEP_VERIFY_ORDER_SUCCESS))

                .addTestStep(annotatedMethod(nodeStatusSteps, NodeStatusTestSteps.TEST_STEP_UNORDER_NODE))
                .addTestStep(annotatedMethod(nodeStatusSteps, NodeStatusTestSteps.TEST_STEP_VERIFY_STATUS_ENTRIES))

                .withDataSources(dataSource("nodeStatus").withFilter("usecase=='unorder'")).build();

        final TestScenario scenario = scenario().addFlow(unorderStatusScenario).build();

        final TestScenarioRunner runner = runner().build();
        runner.start(scenario);
    }

    @TestId(id = "TORF-2335", title = "LTE Macro View Integration Progress, Status and Report CLI")
    @Context(context = { Context.CLI })
    @Test(groups = { "Acceptance", "GAT" })
    public void testNodeStatusIntegrateUseCase() {

        final TestStepFlowBuilder integrateStatusScenarioBuilder = flow("View node status after successful integration")
                .addTestStep(annotatedMethod(generalSteps, GeneralTestSteps.TEST_STEP_SET_DESCRIPTION))

                .addTestStep(annotatedMethod(importSteps, ImportProjectTestSteps.TEST_STEP_CREATE_PROJECT))

                .addTestStep(annotatedMethod(importSteps, ImportProjectTestSteps.TEST_STEP_IMPORT_PROJECT))
                .addTestStep(annotatedMethod(importSteps, ImportProjectTestSteps.TEST_STEP_VERIFY_RESPONSE_SUCCESS))

                .addTestStep(annotatedMethod(orderSteps, OrderNodeTestSteps.TEST_STEP_ORDER_NODE))
                .addTestStep(annotatedMethod(orderSteps, OrderNodeTestSteps.TEST_STEP_VERIFY_ORDER_SUCCESS));

        if (Constants.ENV_LOCAL) {
            integrateStatusScenarioBuilder.addTestStep(annotatedMethod(integrateSteps, IntegrateNodeTestSteps.TEST_STEP_LOCAL_INTEGRATE_SETUP))
                    .addTestStep(annotatedMethod(integrateSteps, IntegrateNodeTestSteps.TEST_STEP_INTEGRATE_NODE_UP))
                    .addTestStep(annotatedMethod(integrateSteps, IntegrateNodeTestSteps.TEST_STEP_INTEGRATE_SYNC_NODE));
        } else {
            integrateStatusScenarioBuilder.addTestStep(annotatedMethod(integrateSteps, IntegrateNodeTestSteps.TEST_STEP_INTEGRATE_SETUP))
                    .addTestStep(annotatedMethod(integrateSteps, IntegrateNodeTestSteps.TEST_STEP_INTEGRATE_NODE_UP));
        }

        integrateStatusScenarioBuilder
                .addTestStep(annotatedMethod(integrateSteps, IntegrateNodeTestSteps.TEST_STEP_INTEGRATE_SET_SITE_CONFIG_COMPLETE))
                .addTestStep(annotatedMethod(integrateSteps, IntegrateNodeTestSteps.TEST_STEP_INTEGRATE_SET_S1_COMPLETE))
                .addTestStep(annotatedMethod(integrateSteps, IntegrateNodeTestSteps.TEST_STEP_VERIFY_INTEGRATE_SUCCESS))

                .addTestStep(annotatedMethod(nodeStatusSteps, NodeStatusTestSteps.TEST_STEP_VERIFY_STATUS_ENTRIES));

        final TestStepFlow integrateStatusScenario = integrateStatusScenarioBuilder.withDataSources(
                dataSource("nodeStatus").withFilter("usecase contains 'integrate'")).build();

        final TestScenario scenario = scenario().addFlow(integrateStatusScenario).build();

        final TestScenarioRunner runner = runner().build();
        runner.start(scenario);
    }

    @AfterSuite(alwaysRun = true)
    public void teardown() {
        testDataCleaner.performCleanup();
    }
}
