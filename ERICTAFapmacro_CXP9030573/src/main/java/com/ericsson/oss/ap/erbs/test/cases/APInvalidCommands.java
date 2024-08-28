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

import javax.inject.Inject;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.oss.ap.core.operators.APServiceOperator;
import com.ericsson.oss.ap.core.operators.UserManagementOperator;
import com.ericsson.oss.ap.core.test.model.CommandResult;
import com.ericsson.oss.ap.erbs.test.data.AbstractTestData;

/**
 * Tests that the correct responses are returned for valid commands with invalid syntaxes & invalid AP commands.
 * 
 * @author eshemeh
 * @since 1.5.2
 */
public class APInvalidCommands extends TorTestCaseHelper {

    @Inject
    private UserManagementOperator createUserOperator;

    @Inject
    private OperatorRegistry<APServiceOperator> operatorRegistry;

    @BeforeSuite(alwaysRun = true)
    public void preTAFSetup() {
        createUserOperator.createTAFUserInNonLocalEnv();
    }

    /**
     * Tests the response message when an invalid command is executed.
     * <p>
     * Note that this test runs directly against the Common CLI operator rather than the APServiceOperator as these tests are only related to the CLI
     * execution.
     * </p>
     * 
     * @param command
     *            the command being tested
     * @param testCase
     *            the testCase
     * @param testDescription
     *            the description of the current testCase
     * @param commandParameters
     *            the command parameters
     * @param expectedResultMsg
     *            the expected result message
     */
    @Context(context = Context.CLI)
    @TestId(id = "TORF-8568_Func_2", title = "Testing response for invalid commands")
    @DataDriven(name = "ap_invalid_command")
    @Test(groups = { "Acceptance", "GAT" })
    public void invalidCommands(@Input("command") final String command, @Input("testCase") final String testCase,
            @Input("testDescription") final String testDescription, @Input("commandParameters") final String commandParameters,
            @Input("expectedResultMsg") final String expectedResultMsg) {

        final String commandToExecute = command + " " + commandParameters;

        setTestcase(testCase, testDescription);
        setTestStep("Testing invalid " + command + " command. Command: ap " + commandToExecute);
        final APServiceOperator operator = operatorRegistry.provide(APServiceOperator.class);
        final CommandResult result = operator.executeCommand(commandToExecute);
        final String actual = result.buildResponseWithSolution();

        setTestStep("Verify ap " + command + " command returns correct result");
        final String expected = getExpectedInvalidResponse(expectedResultMsg);
        assertEquals(expected, actual);
    }

    private String getExpectedInvalidResponse(final String expectedMessageKey) {
        return (String) AbstractTestData.EXPECTED_TEXT_PROPERTIES.get("ap.invalid.text." + expectedMessageKey);
    }
}