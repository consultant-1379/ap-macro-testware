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

import javax.inject.Inject;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.oss.ap.core.getters.APHostResolver;
import com.ericsson.oss.ap.core.operators.APServiceOperator;
import com.ericsson.oss.ap.core.operators.UserManagementOperator;
import com.ericsson.oss.ap.core.operators.cm.CmCliOperator;
import com.ericsson.oss.ap.core.test.helper.ImportProjectHelper;
import com.ericsson.oss.ap.core.test.helper.NodeStateTransitionTimer;
import com.ericsson.oss.ap.core.test.helper.TestDataCleaner;
import com.ericsson.oss.ap.core.test.model.CommandResult;
import com.ericsson.oss.ap.core.test.model.CommandResult.Result;
import com.ericsson.oss.ap.erbs.test.data.APDownloadTestData;
import com.ericsson.oss.ap.erbs.test.model.APDownloadTestInfo;
import com.ericsson.oss.ap.erbs.test.model.APNodeTestInfo;
import com.ericsson.oss.ap.erbs.test.model.APProjectTestInfo;
import com.ericsson.oss.services.ap.common.Constants;
import com.ericsson.oss.services.commonCLI.operator.ApCmEditorRestOperator;
import com.ericsson.oss.services.commonCLI.operator.CommonCLIFileWrapper;

/**
 * Tests the export of AutoProvisioning artifacts through script-engine.
 * 
 * @since 1.2.1
 */
public class APDownload extends TorTestCaseHelper {

    @Inject
    private APHostResolver hostResolver;

    @Inject
    private CmCliOperator cmOperator;

    @Inject
    private ImportProjectHelper importProjectHelper;

    @Inject
    private NodeStateTransitionTimer nodeStateTransitionTimer;

    @Inject
    private UserManagementOperator createUserOperator;

    @Inject
    private TestDataCleaner testDataCleaner;

    @Inject
    private OperatorRegistry<APServiceOperator> operatorRegistry;

    @BeforeSuite(alwaysRun = true)
    public void preTAFSetup() {
        createUserOperator.createTAFUserInNonLocalEnv();
    }

    /**
     * Tests exporting of AutoProvisioning node artifacts.
     * 
     * @param zipFileContents
     *            the project zip file contents
     * @param projectInfo
     *            {@link APProjectTestInfo} for importing a project
     * @param downloadTestInfo
     *            test info for export artifact
     * @throws IOException
     */
    @Context(context = Context.CLI)
    @Test(dataProvider = APDownloadTestData.DOWNLOAD_ARTIFACT_EXECUTION, dataProviderClass = APDownloadTestData.class, groups = { "Acceptance", "GAT" })
    public void downloadArtifact(final byte[] zipFileContents, final APProjectTestInfo projectInfo, final APDownloadTestInfo downloadTestInfo) throws IOException {
        setTestcase("TORF-11086_Func_1", "Download AutoProvisioning artifacts");
        setTestInfo(downloadTestInfo.getDescription());

        CommandResult result = null;
        final APNodeTestInfo node = projectInfo.getNodes().iterator().next();

        setTestStep("Import project " + projectInfo.getName());
        if (!projectExists(projectInfo.getName())) {
            importProject(zipFileContents, projectInfo);
        }

        if (downloadTestInfo.getCommandOptions().contains("-o")) {
            if (downloadTestInfo.getOrderIntegration()) {
                setTestStep("Order Integration for node " + node.getName());
                orderIntegration(node);
            }
        }

        setTestStep("Download requested artifact to download staging area ");
        result = downloadInitialArtifact(downloadTestInfo, node);

        if (Result.SUCCESS != null) {

            setTestStep("Download artifact to browser");
            final CommonCLIFileWrapper downloadedFile = result.getCommonCLIFileWrapper();

            setTestStep("Verify downloaded artifact file name");
            assertTrue(downloadedFile.getFileName().contains(downloadTestInfo.getFilename()));

            //verifyDownloadedFileContents(downloadTestInfo, downloadedFile, node.getArtifacts().size()); 

        } else {
            assertEquals(downloadTestInfo.getStatusMessage(), result.getStatusMessage());
        }

    }

    //    TODO eniahal need to get support to find out how to extract this content
    //    private void verifyDownloadedFileContents(final APDownloadTestInfo downloadArtifactTestInfo, final CommonCLIFileWrapper downloadedFile, final int... importArtifactCount) throws IOException {
    //        setTestStep("Verify downloaded artifact file contents");
    //        
    //        if (downloadedFile.getMimeType().equals(MIME_TYPE_XML)) {
    //            if (downloadArtifactTestInfo.getXmlRootTag() != null) {
    //                final String fileContents = new String(downloadedFile.getFileContent());
    //                final Document doc = DocumentBuilder.getDocument(fileContents);
    //                final String xmlRootElement = doc.getDocumentElement().getTagName();
    //                assertEquals(downloadArtifactTestInfo.getXmlRootTag(), xmlRootElement);
    //            }
    //        } else if (downloadedFile.getMimeType().equals(MIME_TYPE_ZIP)) {
    //            final InputStream is = new ByteArrayInputStream(downloadedFile.getFileContent());
    //            final ZipInputStream zis = new ZipInputStream(is);
    //            @SuppressWarnings("unused")
    //            ZipEntry ze;
    //            int downloadFileArtifactCount = 0;
    //            while ((ze = zis.getNextEntry()) != null) {
    //                downloadFileArtifactCount += 1;
    //                zis.closeEntry();
    //            }
    //            assertEquals(downloadFileArtifactCount, importArtifactCount);
    //        }
    //    }

    private void importProject(final byte[] zipFileContents, final APProjectTestInfo projectInfo) {
        final CommandResult importResult = importProjectHelper.importProject(zipFileContents, projectInfo);
        if (!importResult.isSuccessful()) {
            fail("Failed to import project " + projectInfo.getName());
        }
        testDataCleaner.markFdnForCleanUp(projectInfo.getProjectFdn());
    }

    private boolean projectExists(final String projectName) {
        return cmOperator.findMoByFdn(hostResolver.getApacheHost(), Constants.PROJECT_FDN_PREFIX + projectName) != null;
    }

    private CommandResult orderIntegration(final APNodeTestInfo node) {
        final APServiceOperator operator = operatorRegistry.provide(APServiceOperator.class);
        final CommandResult result = operator.orderNode(node.getName());
        nodeStateTransitionTimer.waitForStateTransitionFromState(node.getApNodeFdn(), "ORDER_STARTED");
        return result;
    }

    private CommandResult downloadInitialArtifact(final APDownloadTestInfo downloadArtifactTestInfo, final APNodeTestInfo node) {
        final APServiceOperator operator = operatorRegistry.provide(APServiceOperator.class);
        final CommandResult result = operator.downloadNodeArtifact(node.getName(), downloadArtifactTestInfo.getCommandOptions());
        assertEquals(downloadArtifactTestInfo.getResult(), result.getResult().name());
        return result;
    }

    /**
     * Tests the result and response message when invalid get command is
     * executed.
     * 
     * @param description
     *            the test description
     * @param commandParameters
     *            the delete command parameters
     * @param statusMessage
     *            the expected status message
     * @param expectedResult
     *            the expected result
     */
    @Context(context = Context.CLI)
    @Test(dataProvider = APDownloadTestData.AP_DOWNLOAD_COMMAND, dataProviderClass = APDownloadTestData.class, groups = { "Acceptance", "GAT" })
    public void invalidDownloadCommands(final String description, final String commandParameters, final String statusMessage, final String expectedResult) {
        setTestCase("TORF-11086_Func_1", "Download AutoProvisioning command syntax");
        setTestInfo(description);

        final ApCmEditorRestOperator operator = new ApCmEditorRestOperator();
        final CommandResult downloadResult = operator.executeCommand(hostResolver.getApacheHost(), "ap download " + commandParameters);

        final String result = downloadResult.isSuccessful() ? "SUCCESS" : "FAILED";

        assertEquals(expectedResult, result);
        assertEquals(statusMessage, downloadResult.getStatusMessage());
    }

    /**
     * Deletes test data after execution of this class completed.
     */
    @AfterSuite(alwaysRun = true)
    public void teardown() {
        testDataCleaner.performCleanup();
    }
}
