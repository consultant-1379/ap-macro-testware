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

import static se.ericsson.jcat.fw.assertion.JcatAssertApi.assertEquals;
import static se.ericsson.jcat.fw.assertion.JcatAssertApi.assertFalse;
import static se.ericsson.jcat.fw.assertion.JcatAssertApi.assertNotNull;
import static se.ericsson.jcat.fw.assertion.JcatAssertApi.assertTrue;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setSubTestStep;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setTestStepBegin;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setTestStepEnd;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.testng.Assert;

import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.oss.ap.core.getters.APHostResolver;
import com.ericsson.oss.ap.core.helpers.PersistentMOHelper;
import com.ericsson.oss.ap.core.operators.APServiceOperator;
import com.ericsson.oss.ap.core.operators.cm.CmCliOperator;
import com.ericsson.oss.ap.core.operators.cm.model.ManagedObjectDto;
import com.ericsson.oss.ap.core.operators.file.CommonFileOperator;
import com.ericsson.oss.ap.core.test.helper.TestDataCleaner;
import com.ericsson.oss.ap.core.test.model.APFileInfo;
import com.ericsson.oss.ap.core.test.model.CommandResult;
import com.ericsson.oss.ap.core.test.model.GenericAPTestInfo;
import com.ericsson.oss.ap.erbs.test.data.APImportERBSZipTestData;
import com.ericsson.oss.ap.erbs.test.model.APNodeArtifactTestInfo;
import com.ericsson.oss.ap.erbs.test.model.APNodeTestInfo;
import com.ericsson.oss.ap.erbs.test.model.APProjectTestInfo;
import com.ericsson.oss.services.ap.common.Constants;

/**
 * Contains the <code><b>ap import</b></code> project test steps.
 * 
 * @author eeibky
 * @since 1.6.4
 */
public class ImportProjectTestSteps {

    public static final String TEST_STEP_CREATE_PROJECT = "loadProject";
    public static final String TEST_STEP_IMPORT_PROJECT = "import";
    public static final String TEST_STEP_IMPORT_MULTIPLE_PROJECTS = "multipleImports";
    public static final String TEST_STEP_READ_PROJECT = "readProject";
    public static final String TEST_STEP_VERIFY_IMPORT = "verifyImport";
    public static final String TEST_STEP_VERIFY_RESPONSE_SUCCESS = "verifyResponseSuccess";
    public static final String TEST_STEP_VERIFY_RESPONSE_FAILED = "verifyResponseFailure";
    public static final String TEST_STEP_VERIFY_PROJECT_IN_DPS = "verifyProjectInDPS";
    public static final String TEST_CONTEXT_KEY_ZIPCONTENTS = "zipFileContents";
    public static final String TEST_CONTEXT_KEY_PROJECTINFO = "projectInfo";
    public static final String TEST_CONTEXT_KEY_IMPORTRESPONSE = "ImportResponse";
    public static final String TEST_CONTEXT_KEY_MULTIPLE_PROJECTINFOS = "multipleProjectInfos";

    private final static String EXPECTED_RESULT = "EXPECTED: \"%s\" ACTUAL: \"%s\"";

    @Inject
    private OperatorRegistry<APServiceOperator> operatorRegistry;

    @Inject
    private APHostResolver hostResolver;

    @Inject
    private CmCliOperator cmOperator;

    @Inject
    private CommonFileOperator fileOperator;

    @Inject
    private TestDataCleaner testDataCleaner;

    @Inject
    private TestContext context;

    /**
     * Create the Project Data that will be used during an Import.
     * <p>
     * The input DataSource csv should contain a column "importDataProvider"
     * which should match the required "dataProvider" column in
     * apMacroArchiveSpec.csv. That "dataProvider" defined the Project data to
     * be imported.
     * <p>
     * Sets the following test context attributes:
     * <ul>
     * <li><code>TEST_CONTEXT_KEY_PROJECTINFO</code></li> - the
     * <code>APProjectTestInfo</code> for an imported project
     * <li><code>TEST_CONTEXT_KEY_ZIPCONTENTS</code></li> - the byte array of
     * the zipped file
     * </ul>
     * </p>
     */
    @TestStep(id = TEST_STEP_CREATE_PROJECT)
    public void createProject(@Input("importDataProvider") final String importName) {
        final Object[][] projectData = APImportERBSZipTestData.createDataForProvider(importName);
        final byte[] zipFileContents = (byte[]) projectData[0][0];
        final APProjectTestInfo projectInfo = (APProjectTestInfo) projectData[0][1];

        context.setAttribute(TEST_CONTEXT_KEY_PROJECTINFO, projectInfo);
        context.setAttribute(TEST_CONTEXT_KEY_ZIPCONTENTS, zipFileContents);
    }

    @TestStep(id = TEST_STEP_READ_PROJECT)
    public void readProject(@Input("projectZip") final String fileName) throws IOException {
        final byte[] zipFileContents = readProjectZipFile(fileName);
        final APProjectTestInfo projectInfo = new APProjectTestInfo();
        projectInfo.setAttribute("fileName", fileName);

        context.setAttribute(TEST_CONTEXT_KEY_PROJECTINFO, projectInfo);
        context.setAttribute(TEST_CONTEXT_KEY_ZIPCONTENTS, zipFileContents);
    }

    private byte[] readProjectZipFile(final String fileName) throws IOException {
        final InputStream inputStream = this.getClass().getResourceAsStream("/templates/zip/" + fileName);
        return IOUtils.toByteArray(inputStream, inputStream.available());
    }

    /**
     * Execute the command to import the project into AP.
     * <p>
     * Requires the following test context attributes to be set:
     * <ul>
     * <li><code>TEST_CONTEXT_KEY_PROJECTINFO</code></li> - the
     * <code>APProjectTestInfo</code> for an imported project
     * <li><code>TEST_CONTEXT_KEY_ZIPCONTENTS</code></li> - the byte array of
     * the zipped file
     * </ul>
     * Sets the following test context attributes
     * <ul>
     * <li><code>TEST_CONTEXT_KEY_IMPORTRESPONSE</code></li> - the
     * <code>CommandResult</code> for the response of import project
     * </ul>
     * </p>
     */
    @TestStep(id = TEST_STEP_IMPORT_PROJECT)
    public void importProject() {
        final byte[] zipFileContents = context.getAttribute(TEST_CONTEXT_KEY_ZIPCONTENTS);
        final APProjectTestInfo projectInfo = context.getAttribute(TEST_CONTEXT_KEY_PROJECTINFO);
        final String projectFileName = projectInfo.getAttribute("fileName");
        final CommandResult importResult = executeProjectImport(zipFileContents, projectInfo, projectFileName);

        context.setAttribute(TEST_CONTEXT_KEY_IMPORTRESPONSE, importResult);
    }

    /**
     * Creates the project data for multiple projects, and then executes the
     * command to import these projects into AP.
     * <p>
     * The input DataSource csv should contain a column "importDataProviders"
     * which should contain a semicolon-separated (;) list of dataProviders that
     * match the required "dataProvider" column in apMacroArchiveSpec.csv. These
     * "dataProvider" values define the projects to be imported.
     * <p>
     * Sets the following test context attributes:
     * <ul>
     * <li><code>TEST_CONTEXT_KEY_MULTIPLE_PROJECTINFOS</code></li> - an
     * {@link ArrayList} of the {@link APProjectTestInfo} for all imported
     * projects
     * </ul>
     * 
     * @param importDataProviders
     *            the semicolon-separated names of the dataProviders to import
     */
    @TestStep(id = TEST_STEP_IMPORT_MULTIPLE_PROJECTS)
    public void importMultipleProjects(@Input("importDataProviders") final String importDataProviders) {
        final String[] importNames = importDataProviders.split(";");
        final ArrayList<APProjectTestInfo> importedProjectInfos = new ArrayList<>();

        for (final String importName : importNames) {
            final Object[][] projectData = APImportERBSZipTestData.createDataForProvider(importName);
            final byte[] zipFileContents = (byte[]) projectData[0][0];
            final APProjectTestInfo projectInfo = (APProjectTestInfo) projectData[0][1];
            final String projectFileName = projectInfo.getAttribute("fileName");

            importedProjectInfos.add(projectInfo);
            executeProjectImport(zipFileContents, projectInfo, projectFileName);
        }

        context.setAttribute(TEST_CONTEXT_KEY_MULTIPLE_PROJECTINFOS, importedProjectInfos);
    }

    private CommandResult executeProjectImport(final byte[] zipFileContents, final APProjectTestInfo projectInfo, final String projectFileName) {
        setTestStepBegin("Import project file: " + projectFileName);

        final APFileInfo fileInfo = new APFileInfo();
        fileInfo.setName(projectFileName);
        fileInfo.setContents(zipFileContents);

        final APServiceOperator operator = operatorRegistry.provide(APServiceOperator.class);
        final CommandResult importResult = operator.importArchive(fileInfo);

        if (importResult.isSuccessful()) {
            testDataCleaner.markFdnForCleanUp(projectInfo.getProjectFdn());
        }
        return importResult;
    }

    /**
     * Verify the execution result of import project.
     * 
     * @param expectedResult
     *            the expected result
     * @param expectedErrorMessage
     *            the expected error message if any when command failed
     * @param validationMessage
     *            the expected validation message if any when command failed
     */
    @TestStep(id = TEST_STEP_VERIFY_IMPORT)
    public void verifyImportResult(@Input("expectedResult") final String expectedResult,
            @Input("expectedErrorMessage") final String expectedErrorMessage, @Input("expectedValidationMessage") final String validationMessage) {
        if ("SUCCESS".equals(expectedResult)) {
            verifyImportSuccess();
            verifyProjectInDps();
        } else {
            verifyImportFailed(expectedErrorMessage, validationMessage);
        }
    }

    /**
     * Verify the command response when successful.
     * <p>
     * Require the following test context attributes to be set:
     * <ul>
     * <li><code>TEST_CONTEXT_KEY_PROJECTINFO</code></li> - the
     * <code>APProjectTestInfo</code> for an imported project
     * <li><code>TEST_CONTEXT_KEY_IMPORTRESPONSE</code></li> - the
     * <code>CommandResult</code> for the response of import project
     * </ul>
     * </p>
     */
    @TestStep(id = TEST_STEP_VERIFY_RESPONSE_SUCCESS)
    public void verifyImportSuccess() {
        final APProjectTestInfo projectInfo = context.getAttribute(TEST_CONTEXT_KEY_PROJECTINFO);
        final CommandResult importResult = context.getAttribute(TEST_CONTEXT_KEY_IMPORTRESPONSE);

        verifySuccessfulImport(importResult);
        verifySuccessfulImportStatusMessage(projectInfo, importResult);
        setTestStepEnd();
    }

    private void verifySuccessfulImport(final CommandResult importResult) {
        setSubTestStep("Verifying import result");
        assertTrue("Import failed with " + importResult.getStatusMessage() + " (Validation error: " + importResult.getValidationMessage() + ")",
                importResult.isSuccessful());
    }

    private void verifySuccessfulImportStatusMessage(final APProjectTestInfo projectInfo, final CommandResult importResult) {
        setSubTestStep("Verifying status message");
        final String resultMessage = importResult.getStatusMessage();
        final String expectedMessage = "Import of zip file " + projectInfo.getAttribute("fileName") + " successful";
        assertEquals(String.format(EXPECTED_RESULT, expectedMessage, resultMessage), expectedMessage, resultMessage);
    }

    /**
     * Verify the command response when failed.
     * <p>
     * Require the following test context attributes to be set:
     * <ul>
     * <li><code>TEST_CONTEXT_KEY_PROJECTINFO</code></li> - the
     * <code>APProjectTestInfo</code> for an imported project
     * <li><code>TEST_CONTEXT_KEY_IMPORTRESPONSE</code></li> - the
     * <code>CommandResult</code> for the response of import project
     * </ul>
     * </p>
     * 
     * @param expectedErrorMessage
     *            the expected error message if any when command failed
     * @param expectedValidationMessage
     *            the expected validation message if any when command failed
     */
    @TestStep(id = TEST_STEP_VERIFY_RESPONSE_FAILED)
    public void verifyImportFailed(@Input("expectedErrorMessage") final String expectedErrorMessage,
            @Input("expectedValidationMessage") final String expectedValidationMessage) {
        final APProjectTestInfo projectInfo = context.getAttribute(TEST_CONTEXT_KEY_PROJECTINFO);
        final CommandResult importResult = context.getAttribute(TEST_CONTEXT_KEY_IMPORTRESPONSE);

        verifyFailedImport(importResult);
        verifyFailedImportStatusMessage(expectedErrorMessage, expectedValidationMessage, projectInfo, importResult);
        setTestStepEnd();
    }

    private void verifyFailedImport(final CommandResult importResult) {
        setSubTestStep("Verifying import result");
        assertFalse("Test case failed due to unexpected success of import project", importResult.isSuccessful());
    }

    private void verifyFailedImportStatusMessage(final String expectedErrorMessage, final String expectedValidationMessage,
            final APProjectTestInfo projectInfo, final CommandResult importResult) {
        setSubTestStep("Verifying status message");
        final String resultMessage = importResult.getStatusMessage();

        if (StringUtils.isNotBlank(expectedErrorMessage)) {
            assertEquals(String.format(EXPECTED_RESULT, expectedErrorMessage, resultMessage), expectedErrorMessage, resultMessage);
        } else {
            final String expectedMessage = String.format("Error(s) found validating project file %s", projectInfo.getAttribute("fileName"));
            assertEquals(String.format(EXPECTED_RESULT, expectedMessage, resultMessage), expectedMessage, resultMessage);

            setSubTestStep("Verifying validation error message");
            assertTrue(importResult.getValidationMessage().contains(expectedValidationMessage));
        }
    }

    /**
     * Verify successful import of the project into AP.
     * <p>
     * Requires the following test context attributes to be set:
     * <ul>
     * <li><code>TEST_CONTEXT_KEY_PROJECTINFO</code></li> - the
     * <code>APProjectTestInfo</code> for an imported project
     * </ul>
     * </p>
     */
    @TestStep(id = TEST_STEP_VERIFY_PROJECT_IN_DPS)
    public void verifyProjectInDps() {
        setTestStepBegin("Verifying project in DPS");

        final APProjectTestInfo projectInfo = context.getAttribute(TEST_CONTEXT_KEY_PROJECTINFO);
        final String projectFdn = Constants.PROJECT_FDN_PREFIX + projectInfo.getName();
        final ManagedObjectDto projectMo = cmOperator.findMoByFdn(hostResolver.getApacheHost(), projectFdn);

        verifyProjectExists(projectFdn, projectMo);
        verifyMoData(projectMo, projectInfo);
        verifyCreationDatePresent(projectMo);

        for (final APNodeTestInfo nodeInfo : projectInfo.getNodes()) {
            final ManagedObjectDto nodeMo = getNodeMo(projectMo, nodeInfo);
            verifyAutoIntegrationOptions(nodeMo, nodeInfo);
            verifyNodeMo(nodeMo, nodeInfo);
            verifySecurity(nodeMo, nodeInfo);
        }
        setTestStepEnd();
    }

    private void verifyProjectExists(final String projectFdn, final ManagedObjectDto projectMo) {
        Assert.assertNotNull(projectMo, String.format("Expecting Project %s in DPS", projectFdn));
    }

    private void verifyMoData(final ManagedObjectDto mo, final GenericAPTestInfo nodeInfo) {
        final Map<String, Object> moAttributes = mo.getAttributes();
        for (final String attribute : moAttributes.keySet()) {

            final Object expected = nodeInfo.getAttributeAsString(attribute);
            if (expected == null) {
                continue;
            }
            final Object actual = PersistentMOHelper.getAttributeAsString(mo, attribute);
            assertEquals("For attribute " + attribute + String.format(EXPECTED_RESULT, expected, actual), expected, actual);
        }
    }

    private void verifyCreationDatePresent(final ManagedObjectDto projectMo) {
        Assert.assertNotNull(projectMo.getAttribute("creationDate"), "No creation date");
    }

    private ManagedObjectDto getNodeMo(final ManagedObjectDto projectMo, final APNodeTestInfo nodeInfo) {
        final String nodeFdn = projectMo.getFdn() + "," + Constants.NODE_FDN_PREFIX + nodeInfo.getName();
        setSubTestStep("Verifying node in DPS : " + nodeFdn);

        final ManagedObjectDto nodeMo = cmOperator.findMoByFdn(hostResolver.getApacheHost(), nodeFdn);
        Assert.assertNotNull(nodeMo, String.format("Expecting Node %s in DPS", nodeFdn));

        return nodeMo;
    }

    private void verifyNodeMo(final ManagedObjectDto nodeMo, final APNodeTestInfo nodeInfo) {
        verifyMoData(nodeMo, nodeInfo);

        final List<ManagedObjectDto> createdNodeArtifactMos = cmOperator.getChildren(hostResolver.getApacheHost(), "ap", nodeMo.getFdn(),
                new String[] { "NodeArtifact" });
        for (final Map.Entry<String, APNodeArtifactTestInfo> entry : nodeInfo.getArtifacts().entrySet()) {
            final APNodeArtifactTestInfo nodeArtifact = entry.getValue();
            verifyNodeArtifact(nodeArtifact, createdNodeArtifactMos);
        }
    }

    private void verifyNodeArtifact(final APNodeArtifactTestInfo nodeArtifactInfo, final List<ManagedObjectDto> nodeArtifactMos) {
        final String expectedArtifactType = (String) nodeArtifactInfo.getAttribute("type");

        setSubTestStep("Verifying artifact on SFS for : " + expectedArtifactType);

        final ManagedObjectDto nodeArtifactMo = getNodeArtifactMoByType(expectedArtifactType, nodeArtifactMos);

        verifyArtifactExistsInDps(expectedArtifactType, nodeArtifactMo);
        verifyNoGeneratedFileExists(nodeArtifactMo);

        final String rawFileName = nodeArtifactMo.getAttribute("rawLocation");

        verifyRawImportedFileName(rawFileName, nodeArtifactInfo);
        verifyXmlFileContents(nodeArtifactInfo, rawFileName);
    }

    private ManagedObjectDto getNodeArtifactMoByType(final String artifactName, final List<ManagedObjectDto> nodeArtifactMos) {
        for (final ManagedObjectDto nodeArtifactMo : nodeArtifactMos) {
            final String currentArtifactName = nodeArtifactMo.getAttribute("name");
            if (currentArtifactName.equalsIgnoreCase(artifactName)) {
                return nodeArtifactMo;
            }
        }
        return null;
    }

    private void verifyArtifactExistsInDps(final String artifactType, final ManagedObjectDto artifactMo) {
        Assert.assertNotNull(artifactMo, String.format("No NodeArtifact mo of type %s found in DPS", artifactType));
    }

    private void verifyNoGeneratedFileExists(final ManagedObjectDto artifactMo) {
        final String generatedFileName = artifactMo.getAttribute("generatedLocation");
        Assert.assertNull(generatedFileName, "Expecting generated file name to be null. It is: " + generatedFileName);
    }

    private void verifyRawImportedFileName(final String rawFileName, final APNodeArtifactTestInfo nodeArtifactInfo) {
        final String expectedArtifactName = nodeArtifactInfo.getName();
        final String actualArtifactName = new File(rawFileName).getName();

        assertTrue("File does not exist: " + rawFileName, fileOperator.fileExists(rawFileName));
        assertEquals(String.format(EXPECTED_RESULT, expectedArtifactName, actualArtifactName), expectedArtifactName, actualArtifactName);
    }

    private void verifyXmlFileContents(final APNodeArtifactTestInfo nodeArtifactInfo, final String rawFileName) {
        final String xmlFileContents = fileOperator.readFile(rawFileName);
        final String expectedXmlContents = nodeArtifactInfo.getAttribute("contents").toString();

        assertNotNull(rawFileName + " file does not exist", xmlFileContents);
        assertEquals(String.format(EXPECTED_RESULT, expectedXmlContents, xmlFileContents), expectedXmlContents, xmlFileContents);
    }

    private void verifyAutoIntegrationOptions(final ManagedObjectDto theNodeMo, final APNodeTestInfo nodeInfo) {
        final String fdn = theNodeMo.getFdn() + ",AutoIntegrationOptions=1";
        setSubTestStep("Verifying AutoIntegrationOptions in DPS : " + fdn);

        final ManagedObjectDto aiOptionsMo = cmOperator.findMoByFdn(hostResolver.getApacheHost(), fdn);
        Assert.assertNotNull(aiOptionsMo, String.format("AutoIntegrationOptions should exists in DPS for %s", fdn));

        verifyMoData(aiOptionsMo, nodeInfo);
    }

    private void verifySecurity(final ManagedObjectDto nodeMo, final APNodeTestInfo nodeInfo) {
        final String securityFdn = nodeMo.getFdn() + ",Security=1";
        setSubTestStep("Verifying Security in DPS: " + securityFdn);

        final ManagedObjectDto securityMo = cmOperator.findMoByFdn(hostResolver.getApacheHost(), securityFdn);
        if (nodeInfo.getSecurityPresent()) {
            Assert.assertNotNull(securityMo, String.format("Expecting Security %s in DPS", securityFdn));
            verifyMoData(securityMo, nodeInfo);
        } else {
            Assert.assertNull(securityMo, String.format("Security should not exist in DPS: %s", securityFdn));
        }
    }
}
