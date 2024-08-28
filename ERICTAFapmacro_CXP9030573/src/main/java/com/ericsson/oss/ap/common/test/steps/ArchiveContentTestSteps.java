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
import static se.ericsson.jcat.fw.ng.JcatNGTestBase.assertTrue;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.oss.ap.core.test.model.CommandResult;
import com.ericsson.oss.ap.core.test.util.archive.ZipFileReader;
import com.ericsson.oss.services.commonCLI.operator.CommonCLIFileWrapper;

/**
 * This generic Test Step is used to check that a ziped archive contains the expected
 * list of files. 
 * <p>
 * The expected list of files are listed in a test data csv file,
 * under the 'listOfArchivedFiles' column. The actual list of files is retrieved
 * from a cli download command that has been executed in a previous Test Step. 
 * 
 * @author epaudoy
 * @Since 3.21.0
 *
 */
public class ArchiveContentTestSteps {
    
    public final static String VERIFY_ARCHIVE_CONTENTS_OK = "VerifyArchiveContentsOk";
    
    @Inject
    private TestContext context;
    
    @Inject
    private ZipFileReader zipFileReader;
    
    @TestStep(id = VERIFY_ARCHIVE_CONTENTS_OK )
    public void verifyArchiveFileContentCorrect(final @Input("listOfArchivedFiles") String[] expectedListOfFiles) {

        setTestStepBegin(String.format("Verify that a ziped archive contains the expected list of files"));

        final CommonCLIFileWrapper fileWrapper = ((CommandResult) context.getAttribute(CommandWithDownloadTestSteps.COMMAND_RESULT)).getCommonCLIFileWrapper();
        final List<String> actualListOfFiles = zipFileReader.getListOfFilesInZipArchive(fileWrapper.getFileContent());

        assertTrue((actualListOfFiles.containsAll(Arrays.asList(expectedListOfFiles))) && (actualListOfFiles.size() == expectedListOfFiles.length));

        setTestStepEnd();

    }

}
