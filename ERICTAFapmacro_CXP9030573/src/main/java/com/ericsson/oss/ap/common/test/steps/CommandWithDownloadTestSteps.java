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
package com.ericsson.oss.ap.common.test.steps;

import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setTestStepBegin;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setTestStepEnd;
import static se.ericsson.jcat.fw.ng.JcatNGTestBase.assertEquals;

import javax.inject.Inject;

import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.oss.ap.core.operators.APServiceOperator;
import com.ericsson.oss.ap.core.test.model.CommandResult;

/**
 * This generic Test Step is used to execute any AP cli download command and
 * check that the expected status message to the user is correct.
 * <p>
 * The expected message is listed in a test data csv file, under the
 * 'statusMessage' column. The cli command is also listed in a test data csv
 * file under the 'cliCommand' column.
 * 
 * @author epaudoy
 * @Since 3.21.0
 */
public class CommandWithDownloadTestSteps {

    public final static String VERIFY_COMMAND_EXPECTED_RESULT = "VerifyCommandExpectedResult";

    public final static String VERIFY_COMMAND_EXPECTED_RESPONSE_MESSAGE = "VerifyCommandExpectedResposeMessage";

    public final static String COMMAND_RESULT = "commandResult";

    @Inject
    private TestContext context;

    @Inject
    private OperatorRegistry<APServiceOperator> operatorRegistry;


    @TestStep(id = VERIFY_COMMAND_EXPECTED_RESULT)
    public void verifyDownloadCmdResponseResult(final @Input("cliCommand") String cliCommand, @Input("expectedResult") final String expectedResult) {

        setTestStepBegin(String.format("Verify that a download command result is as expected"));

        final APServiceOperator operator = operatorRegistry.provide(APServiceOperator.class);
        final CommandResult commandResult = operator.downloadSchemasAndSamples(cliCommand);

        assertEquals(expectedResult, commandResult.getResult().toString());

        context.setAttribute(COMMAND_RESULT, commandResult);

        setTestStepEnd();

    }

    @TestStep(id = VERIFY_COMMAND_EXPECTED_RESPONSE_MESSAGE)
    public void verifyDownloadCmdResponseMessage(final @Input("statusMessage") String expectedStatusMessage) {

        setTestStepBegin(String.format("Verify that a download command returns the expected message '%s' to the user", expectedStatusMessage));

        assertEquals(expectedStatusMessage, ((CommandResult) context.getAttribute(COMMAND_RESULT)).getStatusMessage());

        setTestStepEnd();

    }
   

}
