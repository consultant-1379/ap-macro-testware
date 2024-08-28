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

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.nms.security.ENMUser;
import com.ericsson.oss.ap.core.getters.APHostResolver;
import com.ericsson.oss.ap.core.helpers.LoginHelper;
import com.ericsson.oss.ap.core.operators.APServiceOperator;
import com.ericsson.oss.ap.core.operators.UserManagementOperator;
import com.ericsson.oss.ap.core.test.helper.ImportProjectHelper;
import com.ericsson.oss.ap.core.test.helper.TestDataCleaner;
import com.ericsson.oss.ap.core.test.model.APUploadTestInfo;
import com.ericsson.oss.ap.core.test.model.CommandResult;
import com.ericsson.oss.ap.erbs.test.data.APImportERBSZipTestData;
import com.ericsson.oss.ap.erbs.test.data.AbstractTestData;
import com.ericsson.oss.ap.erbs.test.model.APProjectTestInfo;
import com.ericsson.oss.services.ap.common.Constants;

/**
 * TAF test case for access control and authentication for AP use-cases.
 * 
 * @author eshemeh
 * @since 1.6.3
 */
public class APAccessControl extends TorTestCaseHelper {

    private final static Logger LOGGER = Logger.getLogger(APAccessControl.class);
    private final static String UNAUTHORIZED_USER_ERROR_MESSAGE = (String) AbstractTestData.EXPECTED_TEXT_PROPERTIES
            .get("ap.access.control.text.NOT_AUTHORIZED");

    private final ENMUser apAdmin = new ENMUser();
    private final ENMUser apOperator = new ENMUser();
    private final ENMUser apSecurityAdmin = new ENMUser();
    private final ENMUser currentUser = new ENMUser();

    APProjectTestInfo projectInfo = null;
    private String projectName = "";
    private String nodeName = "";

    @Inject
    private ImportProjectHelper importProjectHelper;

    @Inject
    private UserManagementOperator createUser;

    @Inject
    private OperatorRegistry<APServiceOperator> operatorRegistry;

    @Inject
    private APHostResolver hostResolver;

    @Inject
    private UserManagementOperator createUserOperator;

    @Inject
    private TestDataCleaner testDataCleaner;

    @BeforeSuite(alwaysRun = true)
    public void preTAFSetup() {
        createUserOperator.createTAFUserInNonLocalEnv();
        createRBACUsers();
    }

    /**
     * Test case to test AP use-cases against access control predefined roles:
     * <ul>
     * <li>ADMINISTRATOR</li>
     * <li>OPERATOR</li>
     * <li>SECURITY ADMINISTRATOR</li>
     * </ul>
     */
    @Context(context = Context.CLI)
    @TestId(id = "TORF-25562", title = "Testing access control for predefined-roles for AP use-cases")
    @DataDriven(name = "ap_access_control")
    @Test(groups = { "Acceptance", "GAT" })
    public void testExecutionWithPreDefinedRoles(@Input("testCase") final String testCase, @Input("testDescription") final String testDescription,
            @Input("userName") final String userName, @Input("password") final String password,
            @Input("importDataProvider") final String importDataProvider, @Input("command") final String command,
            @Input("expectedResult") final String expectedResult) {

        setTestcase(testCase, testDescription);
        setTestStep("Executing: ap " + command);

        currentUser.setUsername(userName);
        currentUser.setPassword(password);
        LoginHelper.performSecureLogin(userName, password, hostResolver.getApacheHost());

        final CommandResult commandResult = executeCommand(command, importDataProvider);
        final String resultMessage = commandResult.buildResponseWithSolution();

        if (expectedResult.equals("SUCCESS")) {
            assertTrue(resultMessage, commandResult.getErrorCode() != 5001);
        } else {
            assertTrue("User should not be authorized", resultMessage == null ? false : resultMessage.equals(UNAUTHORIZED_USER_ERROR_MESSAGE));
        }
    }

    private CommandResult executeCommand(final String command, final String importDataProvider) {
        final APServiceOperator operator = operatorRegistry.provide(APServiceOperator.class);

        switch (command) {
        case "import":
            return importProject(importDataProvider);
        case "view":
            return operator.viewProjects();
        case "view -p":
            return operator.viewProject(projectName);
        case "view -n":
            return operator.viewNodeDetails(nodeName);
        case "order -n":
            return operator.orderNode(nodeName);
        case "status -n":
            return operator.viewNodeStatus(nodeName);
        case "unorder -n":
            return operator.unorder(nodeName);
        case "order -p":
            return orderProject(projectName);
        case "status -p":
            return operator.viewProjectStatus(projectName);
        case "upload -n":
            return uploadArtifact(operator);
        case "delete -n":
            return operator.deleteNode(nodeName);
        case "delete -p":
            return operator.deleteProject(projectName);
        default:
            return null;
        }
    }

    private CommandResult importProject(final String importDataProvider) {
        final Object[][] projectData = APImportERBSZipTestData.createDataForProvider(importDataProvider);
        final byte[] zipFileContents = (byte[]) projectData[0][0];
        projectInfo = (APProjectTestInfo) projectData[0][1];

        final CommandResult importResult = importProjectHelper.importProject(zipFileContents, projectInfo);

        if (!importResult.isSuccessful()) {
            final Host host = hostResolver.getApacheHost();

            //Workaround to bypass RBAC when not authenticated to run import command,
            //since we need valid project/node names for remaining use-cases.
            LoginHelper.performDefaultSecureLogin(host);
            final CommandResult importResultNoAccessControl = importProjectHelper.importProject(zipFileContents, projectInfo);
            LoginHelper.performSecureLogin(currentUser.getUsername(), currentUser.getPassword(), host);

            if (!importResultNoAccessControl.isSuccessful()) {
                fail("Failed to import project " + projectInfo.getName());
            }
        }

        projectName = projectInfo.getName();
        nodeName = projectInfo.getNodes().get(0).getName();
        testDataCleaner.markFdnForCleanUp(projectInfo.getProjectFdn());

        return importResult;
    }

    private CommandResult orderProject(final String projectName) {
        final APServiceOperator operator = operatorRegistry.provide(APServiceOperator.class);
        final CommandResult commandResult = operator.orderProject(projectName);

        testDataCleaner.markOrderedNodesForCleanup(projectInfo.getNodes());

        return commandResult;
    }

    private CommandResult uploadArtifact(final APServiceOperator operator) {
        final APUploadTestInfo uploadArtifactInfo = new APUploadTestInfo();
        final byte[] artifactContent = getArtifactContent("siteBasic.xml");

        uploadArtifactInfo.setAttribute("artifactByteContent", artifactContent);

        final CommandResult uploadResult = operator.upload(nodeName, "SiteBasic", "UploadArtifact-1SiteBasic.xml", artifactContent);

        return uploadResult;
    }

    private byte[] getArtifactContent(final String templateName) {
        byte[] artifactContent = null;
        try {
            final InputStream in = getClass().getResourceAsStream("/templates/node/artifacts/" + templateName);
            artifactContent = IOUtils.toByteArray(in);
        } catch (final IOException e) {
            fail("Unable to create file to upload");
        }
        return artifactContent;
    }

    private void createRBACUsers() {
        if (!Constants.ENV_LOCAL) {
            LOGGER.info("Creating RBAC users");
            apAdmin.setUsername("ap_admin");
            apAdmin.setPassword("T3stP4ssw0rd");
            apAdmin.setFirstName("ap");
            apAdmin.setLastName("admin");
            apAdmin.setEmail("autopadmin123@ericsson.com");
            apAdmin.setEnabled(true);

            apOperator.setUsername("ap_operator");
            apOperator.setPassword("T3stP4ssw0rd");
            apOperator.setFirstName("ap");
            apOperator.setLastName("operator");
            apOperator.setEmail("autopop123@ericsson.com");
            apOperator.setEnabled(true);

            apSecurityAdmin.setUsername("ap_sec_admin");
            apSecurityAdmin.setPassword("T3stP4ssw0rd");
            apSecurityAdmin.setFirstName("ap");
            apSecurityAdmin.setLastName("secadmin");
            apSecurityAdmin.setEmail("autopsec123@ericsson.com");
            apSecurityAdmin.setEnabled(true);

            createUser.createUser(apAdmin, UserManagementOperator.ROLE_ADMIN, UserManagementOperator.ROLE_OPERATOR);
            createUser.createUser(apOperator, UserManagementOperator.ROLE_OPERATOR);
            createUser.createUser(apSecurityAdmin, UserManagementOperator.ROLE_SEC_ADMIN);
        }
    }

    /**
     * Logs back in as administrator, for following test cases
     */
    @AfterClass(alwaysRun = true)
    public void loginAsAdmin() {
        LoginHelper.performDefaultSecureLogin(hostResolver.getApacheHost());
    }

    /**
     * Deletes test data after execution of this class completed.
     */
    @AfterSuite(alwaysRun = true)
    public void teardown() {
        testDataCleaner.performCleanup();
    }

    /**
     * Deletes users with pre-defined roles.
     */
    @AfterSuite(alwaysRun = true)
    public void deleteRBACUsers() {
        if (!Constants.ENV_LOCAL) {
            LOGGER.info("Deleting RBAC users");
            createUser.deleteUser(apAdmin);
            createUser.deleteUser(apOperator);
            createUser.deleteUser(apSecurityAdmin);
        }
    }
}
