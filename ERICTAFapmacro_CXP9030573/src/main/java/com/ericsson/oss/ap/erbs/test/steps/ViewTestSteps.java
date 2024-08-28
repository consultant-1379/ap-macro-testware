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
package com.ericsson.oss.ap.erbs.test.steps;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.oss.ap.core.operators.APServiceOperator;
import com.ericsson.oss.ap.core.test.model.CommandResult;
import com.ericsson.oss.ap.core.test.model.ResultEntity;
import com.ericsson.oss.ap.erbs.test.model.APNodeTestInfo;
import com.ericsson.oss.ap.erbs.test.model.APProjectTestInfo;

/**
 * Contains the <code><b>ap view</b></code> test steps for the following
 * commands:
 * <ul>
 * <li>ap view</li>
 * <li>ap view -p projectName</li>
 * <li>ap view -n nodeName</li>
 * </ul>
 * 
 * @author eshemeh
 * @since 1.10.8
 */
public class ViewTestSteps extends TorTestCaseHelper {

    public static final String TEST_STEP_VIEW_ALL_PROJECTS = "viewAllProjects";
    public static final String TEST_STEP_VIEW_PROJECTS = "viewIndividualProjects";
    public static final String TEST_STEP_VIEW_NODES = "viewIndividualNodes";

    @Inject
    private OperatorRegistry<APServiceOperator> operatorRegistry;

    @Inject
    private TestContext context;

    /**
     * Executes the command to view all projects, <code><b>ap view</b></code>.
     * <p>
     * Verifies the command result message, and that imported project exists in
     * the results.
     */
    @TestStep(id = TEST_STEP_VIEW_ALL_PROJECTS)
    public void viewAllProjects() {
        setTestStepBegin("View all projects when test project has been imported");
        final APServiceOperator operator = operatorRegistry.provide(APServiceOperator.class);
        final CommandResult viewAllProjectsResult = operator.viewProjects();
        final List<ResultEntity> projectResults = viewAllProjectsResult.getResultEntities();

        verifyResultMessage(viewAllProjectsResult);

        final ArrayList<APProjectTestInfo> projectInfos = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_MULTIPLE_PROJECTINFOS);

        for (final APProjectTestInfo projectInfo : projectInfos) {
            verifyProjectExists(projectInfo.getName(), projectResults);
        }

    }

    private void verifyResultMessage(final CommandResult viewAllProjectsResult) {
        setSubTestStep("Verify view all result message is correct");
        final String resultMessage = viewAllProjectsResult.getStatusMessage();
        final String expectedMessage = " project(s) found";

        assertTrue(resultMessage.contains(expectedMessage));
    }

    private void verifyProjectExists(final String projectName, final List<ResultEntity> projectResults) {
        setSubTestStep("Verify project " + projectName + " is shown in results");

        for (final ResultEntity projectResult : projectResults) {
            final Map<String, String> projectAttributes = projectResult.getAttributes();
            if (projectAttributes.get("projectName").equals(projectName)) {
                return;
            }
        }

        fail("Imported test project " + projectName + " not found in view results");
    }

    /**
     * Executes command to view single project,
     * <code><b>ap view -p projectName</b></code>.
     * <p>
     * Finds test project and verifies project attribute values in result.
     */
    @TestStep(id = TEST_STEP_VIEW_PROJECTS)
    public void viewSingleProject() {
        setTestStepBegin("View individual projects");
        final ArrayList<APProjectTestInfo> projectInfos = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_MULTIPLE_PROJECTINFOS);
        final APServiceOperator operator = operatorRegistry.provide(APServiceOperator.class);

        for (final APProjectTestInfo projectInfo : projectInfos) {
            final String projectName = projectInfo.getName();
            final CommandResult viewProjectResult = operator.viewProject(projectName);
            final ResultEntity projectResult = viewProjectResult.getResultEntities().get(0);
            verifyProjectAttributeValues(projectName, projectInfo, projectResult);
        }
    }

    private void verifyProjectAttributeValues(final String projectName, final APProjectTestInfo projectInfo, final ResultEntity projectResult) {
        setSubTestStep("Verify project attributes values are correct for project: " + projectName);
        final Map<String, String> actualProjectAttributes = projectResult.getAttributes();

        assertEquals(projectInfo.getProjectType(), actualProjectAttributes.get("projectType"));
        assertEquals(projectInfo.getProjectFdn(), actualProjectAttributes.get("FDN"));
        assertEquals(String.valueOf(projectInfo.getNodes().size()), actualProjectAttributes.get("nodeQuantity"));
        assertEquals(projectInfo.getCreator(), actualProjectAttributes.get("creator"));
        assertEquals(new SimpleDateFormat("yyyy-MM-dd").format(new Date()), actualProjectAttributes.get("creationDate").substring(0, 10));
        assertEquals(projectInfo.getDescription(), actualProjectAttributes.get("description"));
    }

    /**
     * Executes command to view single node,
     * <code><b>ap view -n nodeName</b></code>.
     * <p>
     * Finds test node from imported project and verifies node attribute values
     * in view result.
     */
    @TestStep(id = TEST_STEP_VIEW_NODES)
    public void viewSingleNode() {
        final ArrayList<APProjectTestInfo> projectInfos = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_MULTIPLE_PROJECTINFOS);
        final APProjectTestInfo projectInfo = projectInfos.get(0);
        final APServiceOperator operator = operatorRegistry.provide(APServiceOperator.class);
        final String projectName = projectInfo.getName();
        setTestStepBegin("View individual nodes for project: " + projectName);

        for (final APNodeTestInfo nodeInfo : projectInfo.getNodes()) {
            final String nodeName = nodeInfo.getName();
            final CommandResult viewNodeResult = operator.viewNodeDetails(nodeName);
            final ResultEntity nodeResultEntity = viewNodeResult.getResultEntities().get(0);

            verifyNodeAttributeValues(projectName, nodeInfo, nodeResultEntity);
        }
    }

    private void verifyNodeAttributeValues(final String projectName, final APNodeTestInfo nodeInfo, final ResultEntity nodeResultEntity) {
        final String nodeName = nodeInfo.getName();
        final String upgradePackageName = nodeInfo.getUpgradePackageName();
        final Map<String, String> actualNodeAttributes = nodeResultEntity.getAttributes();
        setSubTestStep("Verify node attributes values are correct for node: " + nodeName);

        assertEquals(nodeName, actualNodeAttributes.get("nodeName"));
        assertEquals(projectName, actualNodeAttributes.get("projectName"));
        assertEquals(nodeInfo.getIpAddress(), actualNodeAttributes.get("ipAddress"));
        assertEquals(nodeInfo.getMimVersion(), actualNodeAttributes.get("mimVersion"));
        assertEquals(nodeInfo.getNodeType(), actualNodeAttributes.get("nodeType"));
        assertEquals(nodeInfo.getSite(), actualNodeAttributes.get("site"));
        assertEquals("", actualNodeAttributes.get("Integration Settings"));
        assertEquals("false", actualNodeAttributes.get("activateLicense"));
        assertEquals("false", actualNodeAttributes.get("installLicense"));
        assertEquals(nodeInfo.getUnlockCells(), actualNodeAttributes.get("unlockCells"));
        assertEquals(upgradePackageName == null ? "" : upgradePackageName, actualNodeAttributes.get("upgradePackageName"));
        assertEquals(nodeInfo.getUploadCVAfterConfiguration(), actualNodeAttributes.get("uploadCVAfterConfiguration"));
        assertEquals(nodeInfo.getUploadCVAfterIntegration(), actualNodeAttributes.get("uploadCVAfterIntegration"));
        assertEquals(("siteBasic, siteEquipment, siteInstall"), actualNodeAttributes.get("Integration Artifacts"));
    }
}