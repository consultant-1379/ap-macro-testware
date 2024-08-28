/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
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
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.oss.ap.core.operators.APServiceOperator;
import com.ericsson.oss.ap.core.operators.UserManagementOperator;
import com.ericsson.oss.ap.core.test.model.APFileInfo;
import com.ericsson.oss.ap.core.test.model.CommandResult;
import com.ericsson.oss.ap.erbs.test.model.APProjectTestInfo;
import com.ericsson.oss.ap.erbs.test.model.APValidateTestInfo;

/**
 * The {@code APValidateERbsZIP} is test validating a project file and the files
 * within (schemas etc).
 * 
 * @author ekatbeb
 * @since 1.6.6
 */
public class APValidateERbsZip extends TorTestCaseHelper {

    @Inject
    private UserManagementOperator createUserOperator;

    @Inject
    private OperatorRegistry<APServiceOperator> operatorRegistry;

    @BeforeSuite(alwaysRun = true)
    public void preTAFSetup() {
        createUserOperator.createTAFUserInNonLocalEnv();
    }

    /**
     * @DESCRIPTION Tests the successful validation of a project.
     * 
     * @param zipContents
     *            the project zip file contents
     * @param projectInfo
     *            {@link APProjectTestInfo} for importing a project
     * @param testDescription
     *            description of the test
     * @param expectedResult
     *            expected result of test - success or failure
     * @param expectedResultMsg
     *            expected result message after executing test
     */
    @Context(context = Context.CLI)
    @DataDriven(name = "validate_project")
    @Test(groups = { "Acceptance", "GAT", })
    public void validateZipFile(@Input("zipFileContents") final byte[] zipFileContents, @Input("projectInfo") final APProjectTestInfo projectInfo,
            @Input("testInfo") final APValidateTestInfo validateTestInfo) {

        setTestCase("TORF-11289_Func_1", validateTestInfo.getDescription());

        final String fileName = projectInfo.getAttributeAsString("fileName");

        setTestStep("Validate project file: " + fileName);

        final APFileInfo fileInfo = new APFileInfo();
        fileInfo.setName(projectInfo.getAttribute("fileName").toString());
        fileInfo.setContents(zipFileContents);
        final APServiceOperator operator = operatorRegistry.provide(APServiceOperator.class);
        final CommandResult result = operator.validateProjectFile(fileInfo);

        setTestStep("Verify result matches expected result");
        assertEquals(result.getResult().name(), validateTestInfo.getExpectedResult());

        setTestStep("Verify Validation Message matches expected Validation Message");

        if (!result.getResult().isSuccessful()) {
            assertTrue(result.getValidationMessage().contains(validateTestInfo.getExpectedValidationMessage()));
        }

        setTestStep("Verify validate result status message for file:  " + fileName);
        assertEquals(result.getStatusMessage(), validateTestInfo.getExpectedResultMessage());
    }

}