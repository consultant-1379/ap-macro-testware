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
package com.ericsson.oss.ap.core.test.helper;

import javax.inject.Inject;

import com.ericsson.oss.ap.core.operators.ApCliOperator;
import com.ericsson.oss.ap.core.test.model.APFileInfo;
import com.ericsson.oss.ap.core.test.model.CommandResult;
import com.ericsson.oss.ap.erbs.test.data.APImportERBSZipTestData;
import com.ericsson.oss.ap.erbs.test.model.APProjectTestInfo;

/**
 * 
 * @author erobkav
 * @since 1.2.1
 */
public class ImportProjectHelper {

    @Inject
    private ApCliOperator apOperator;

    public CommandResult importProject(final byte[] zipFileContents, final APProjectTestInfo projectInfo) {
        final APFileInfo fileInfo = new APFileInfo();
        fileInfo.setName(projectInfo.getAttribute("fileName").toString());
        fileInfo.setContents(zipFileContents);

        return apOperator.importArchive(fileInfo);
    }

    /**
     * Imports a project defined in apMacroArchiveSpec.csv.
     * 
     * @param importDataProvider
     *            the name of the data provider in apMacroArchiveSpec.csv
     * @throws IllegalStateException
     *             if import fails for any reason
     * @return <code>APProjectTestInfo</code>
     */
    public APProjectTestInfo importProject(final String importDataProvider) {
        final Object[][] projectData = APImportERBSZipTestData.createDataForProvider(importDataProvider);
        final byte[] projectContents = (byte[]) projectData[0][0];
        final APProjectTestInfo projectInfo = (APProjectTestInfo) projectData[0][1];
        final CommandResult result = importProject(projectContents, projectInfo);

        if (!result.isSuccessful()) {
            throw new IllegalStateException("Failed to import project - " + result.getStatusMessage());
        }

        return projectInfo;
    }

}
