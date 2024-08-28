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

package com.ericsson.oss.ap.core.test.helper;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;

import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.api.TafDataSourceDefinitionBuilder;
import com.ericsson.cifwk.taf.scenario.api.TestStepDefinition;
import com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilder;

/**
 * Allows building an Incremental Flow of Test Steps
 * @since 1.14.7
 */
public class IncrementedTestStepFlow {

    public class IncrementedFlowLoop {
        private final IncrementedTestStepFlow incrementedTestStepFlow;
        private final TestStepDefinition[] steps;

        private IncrementedFlowLoop(
            final IncrementedTestStepFlow incrementedTestStepFlow,
            final TestStepDefinition[] steps) {

            this.incrementedTestStepFlow = incrementedTestStepFlow;
            this.steps = steps;
        }

        public IncrementedTestStepFlow times(final int count) {
            for (int i = 0; i < count; i++) {
                for (final TestStepDefinition step : steps) {
                    flowBuilder.addTestStep(step);
                }
            }

            return incrementedTestStepFlow;
        }

    }

    public static IncrementedTestStepFlow withFlow(final String name) {
        return new IncrementedTestStepFlow(name);
    }

    private final TestStepFlowBuilder flowBuilder;

    private IncrementedTestStepFlow(final String name) {
        this.flowBuilder = flow(name);
    }

    public IncrementedFlowLoop loopTestSteps(final TestStepDefinition... steps) {
        return new IncrementedFlowLoop(this, steps);
    }

    public IncrementedTestStepFlow addTestSteps(final TestStepDefinition... steps) {
        for (final TestStepDefinition step : steps) {
            this.flowBuilder.addTestStep(step);
        }

        return this;
    }

    public IncrementedTestStepFlow withDataSources(final TafDataSourceDefinitionBuilder... dataSources) {
        this.flowBuilder.withDataSources(dataSources);
        return this;
    }

    public TestStepFlow build() {
        return this.flowBuilder.build();
    }

}
