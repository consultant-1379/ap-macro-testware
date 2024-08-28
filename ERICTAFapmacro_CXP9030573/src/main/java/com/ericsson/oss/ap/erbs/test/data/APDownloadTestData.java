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
package com.ericsson.oss.ap.erbs.test.data;

import java.util.ArrayList;
import java.util.List;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.testng.annotations.DataProvider;

import com.ericsson.oss.ap.erbs.test.model.APDownloadTestInfo;
import com.ericsson.oss.ap.erbs.test.model.APProjectTestInfo;

/**
 * Test data for Export Artifact.
 * 
 * @since 1.2.1
 * 
 */
public class APDownloadTestData extends AbstractTestData {

    public static final String DOWNLOAD_ARTIFACT_EXECUTION = "artifact";
    public static final String DOWNLOAD_SCHEMA_EXECUTION = "schema";
    private static final String AP_DOWNLOAD_EXECUTION = "apDownload_execution";
    public static final String AP_DOWNLOAD_COMMAND = "apDownload_command";
    
    private APDownloadTestData() {
    }

    @DataProvider(name = DOWNLOAD_ARTIFACT_EXECUTION)
    public static Object[][] getDownloadExecutionTestData() {

        final List<DownloadExecutionCsvData> csvDataList = loadCsvFile("/data/" + AP_DOWNLOAD_EXECUTION + ".csv", APDownloadTestData.class, DownloadExecutionCsvData.class,
                DownloadExecutionCsvData.CELL_PROCESSORS);
        
        final List<Object[]> result = new ArrayList<>();
        
        for (int i = 0; i < csvDataList.size(); i++) {
            final DownloadExecutionCsvData csvData = csvDataList.get(i);
            if (csvData.getDownloadType().equals(DOWNLOAD_ARTIFACT_EXECUTION)) {
                final String importDataProvider = csvData.getImportDataProvider();
                final Object[] rowData = new Object[3];
                if (importDataProvider != null && !importDataProvider.equals("")) {
                    final Object[][] projectData = APImportERBSZipTestData.createDataForProvider(importDataProvider);
                    final byte[] zipFileContents = (byte[]) projectData[0][0];
                    final APProjectTestInfo projectInfo = (APProjectTestInfo) projectData[0][1];
                    rowData[0] = zipFileContents;
                    rowData[1] = projectInfo;
                } else {
                    rowData[0] = null;
                    rowData[1] = null;
                }
                final APDownloadTestInfo testInfo = updateTestInfo(csvData);
                rowData[2] = testInfo;
                result.add(rowData);
            }
        }
        return result.toArray(new Object[result.size()][3]);
    }


    @DataProvider(name = DOWNLOAD_SCHEMA_EXECUTION)
    public static Object[][] getDownloadExecutionTestData1() {

        final List<DownloadExecutionCsvData> csvDataList = loadCsvFile("/data/" + AP_DOWNLOAD_EXECUTION + ".csv", APDownloadTestData.class, DownloadExecutionCsvData.class,
                DownloadExecutionCsvData.CELL_PROCESSORS);

        final List<Object[]> result = new ArrayList<>();

        for (int i = 0; i < csvDataList.size(); i++) {
            final DownloadExecutionCsvData csvData = csvDataList.get(i);
            if (csvData.getDownloadType().equals(DOWNLOAD_SCHEMA_EXECUTION)) {
                final String importDataProvider = csvData.getImportDataProvider();
                final Object[] rowData = new Object[3];
                if (importDataProvider != null && !importDataProvider.equals("")) {
                    final Object[][] projectData = APImportERBSZipTestData.createDataForProvider(importDataProvider);
                    final byte[] zipFileContents = (byte[]) projectData[0][0];
                    final APProjectTestInfo projectInfo = (APProjectTestInfo) projectData[0][1];

                    rowData[0] = zipFileContents;
                    rowData[1] = projectInfo;
                } else {
                    rowData[0] = null;
                    rowData[1] = null;
                }
                final APDownloadTestInfo testInfo = updateTestInfo(csvData);
                rowData[2] = testInfo;
                result.add(rowData);
            }
        }

        return result.toArray(new Object[result.size()][3]);
    }

    @DataProvider(name = AP_DOWNLOAD_COMMAND)
    public static Object[][] getDownloadCommandTestData() {
        final List<Object[]> result = new ArrayList<>();
        final List<DownloadCommandCsvData> csvDataList = loadCsvFile("/data/" + AP_DOWNLOAD_COMMAND + ".csv", APDownloadTestData.class, DownloadCommandCsvData.class,
                DownloadCommandCsvData.CELL_PROCESSORS);
        for (final DownloadCommandCsvData csvData : csvDataList) {
            final Object[] rowData = new Object[4];
            rowData[0] = csvData.getDescription();
            rowData[1] = csvData.getCommandParameters();
            rowData[2] = csvData.getStatusMessage().replace("\\n", "\n");
            rowData[3] = csvData.getExpectedResult();
            result.add(rowData);
        }
        return result.toArray(new Object[result.size()][4]);
    }

    /**
     * @param csvData
     * @return
     */
    private static APDownloadTestInfo updateTestInfo(final DownloadExecutionCsvData csvData) {
        final APDownloadTestInfo testInfo = new APDownloadTestInfo();
        testInfo.setAttribute("description", csvData.getDescription());
        testInfo.setAttribute("commandOptions", csvData.getCommandOptions());
        testInfo.setAttribute("artifactType", csvData.getArtifactType());
        testInfo.setAttribute("orderIntegration", csvData.getOrderIntegration());
        testInfo.setAttribute("result", csvData.getResult());
        testInfo.setAttribute("statusMessage", csvData.getStatusMessage());
        testInfo.setAttribute("filename", csvData.getFilename());
        testInfo.setAttribute("xmlRootTag", csvData.getXmlRootTag());
        testInfo.setAttribute("nodeType", csvData.getNodeType());
        testInfo.setAttribute("nodeVersion", csvData.getNodeVersion());
        return testInfo;
    }
    
    public static class DownloadExecutionCsvData {

        private String description;
        private String downloadType;
        private String commandOptions;
        private String artifactType;
        private String importDataProvider;
        private String orderIntegration;
        private String result;
        private String statusMessage;
        private String filename;
        private String xmlRootTag;
        private String nodeType;
        private String nodeVersion;

        public static final CellProcessor[] CELL_PROCESSORS = { null, null, null, null, null, null, null, null, null, null, null, null };

        /**
         * @return the cellProcessors
         */
        public static CellProcessor[] getCellProcessors() {
            return CELL_PROCESSORS;
        }

        /**
         * @return the xmlRootTag
         */
        public String getXmlRootTag() {
            return xmlRootTag;
        }

        /**
         * @param xmlRootTag
         *            the xmlRootTag to set
         */
        public void setXmlRootTag(final String xmlRootTag) {
            this.xmlRootTag = xmlRootTag;
        }

        /**
         * @return the orderIntegration
         */
        public String getOrderIntegration() {
            return orderIntegration;
        }

        /**
         * @param orderIntegration
         *            the orderIntegration to set
         */
        public void setOrderIntegration(final String orderIntegration) {
            this.orderIntegration = orderIntegration;
        }

        /**
         * @return the expectedStatusMessage
         */
        public String getStatusMessage() {
            return statusMessage;
        }

        /**
         * @param expectedStatusMessage
         *            the expectedStatusMessage to set
         */
        public void setStatusMessage(final String statusMessage) {
            this.statusMessage = statusMessage;
        }

        /**
         * @return the expectedFileName
         */
        public String getFilename() {
            return filename;
        }

        /**
         * @param expectedFileName
         *            the expectedFileName to set
         */
        public void setFilename(final String filename) {
            this.filename = filename;
        }

        /**
         * @return the importDataProvider
         */
        public String getImportDataProvider() {
            return importDataProvider;
        }

        /**
         * @param importDataProvider
         *            the importDataProvider to set
         */
        public void setImportDataProvider(final String importDataProvider) {
            this.importDataProvider = importDataProvider;
        }

        /**
         * @return the expectedResult
         */
        public String getResult() {
            return result;
        }

        /**
         * @param expectedResult
         *            the expectedResult to set
         */
        public void setResult(final String result) {
            this.result = result;
        }

        /**
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        /**
         * @param description
         *            the description to set
         */
        public void setDescription(final String description) {
            this.description = description;
        }

        /**
         * @return
         */
        public String getCommandOptions() {
            return commandOptions;
        }

        /**
         * @return the downloadType
         */

        public String getDownloadType() {
            return downloadType;
        }

        /**
         * @param downloadType
         *            the downloadType to set
         */
        public void setDownloadType(String downloadType) { //NOPMD
            this.downloadType = downloadType;
        }

        /**
         * 
         * @param commandOptions
         */
        public void setCommandOptions(final String commandOptions) {
            this.commandOptions = commandOptions;
        }

        /**
         * @return the artifactType
         */
        public String getArtifactType() {
            return artifactType;
        }

        /**
         * @param artifactType
         *            the artifactType to set
         */
        public void setArtifactType(final String artifactType) {
            this.artifactType = artifactType;
        }

        /**
         * @return the nodeType
         */
        public String getNodeType() {
            return nodeType;
        }

        /**
         * @param nodeType
         *            the nodeType to set
         */
        public void setNodeType(final String nodeType) {
            this.nodeType = nodeType;
        }

        /**
         * @return the nodeVersion
         */
        public String getNodeVersion() {
            return nodeVersion;
        }

        /**
         * @param nodeVersion
         *            the nodeVersion to set
         */
        public void setNodeVersion(final String nodeVersion) {
            this.nodeVersion = nodeVersion;
        }
    }

    public static class DownloadCommandCsvData {

        private String description;
        private String commandParameters;
        private String statusMessage;
        private String expectedResult;

        public static final CellProcessor[] CELL_PROCESSORS = { null, null, null, null };

        /**
         * @return the cellProcessors
         */
        public static CellProcessor[] getCellProcessors() {
            return CELL_PROCESSORS;
        }

        /**
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        /**
         * @param description
         *            the description to set
         */
        public void setDescription(final String description) {
            this.description = description;
        }

        /**
         * @return the commandParameters
         */
        public String getCommandParameters() {
            return commandParameters;
        }

        /**
         * @param commandParameters
         *            the commandParameters to set
         */
        public void setCommandParameters(final String commandParameters) {
            this.commandParameters = commandParameters;
        }

        /**
         * @return the statusMessage
         */
        public String getStatusMessage() {
            return statusMessage;
        }

        /**
         * @param statusMessage
         *            the statusMessage to set
         */
        public void setStatusMessage(final String statusMessage) {
            this.statusMessage = statusMessage;
        }

        /**
         * @return the expectedResult
         */
        public String getExpectedResult() {
            return expectedResult;
        }

        /**
         * @param expectedResult
         *            the expectedResult to set
         */
        public void setExpectedResult(final String expectedResult) {
            this.expectedResult = expectedResult;
        }
    }
}
