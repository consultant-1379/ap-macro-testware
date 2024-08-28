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
package com.ericsson.oss.ap.erbs.test.data;

import java.util.List;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.testng.annotations.DataProvider;

import com.ericsson.oss.ap.erbs.test.model.APProjectTestInfo;

public class APUnorderTestData extends AbstractTestData {

    public static final String AP_UNORDER_EXECUTION = "apUnorder_execution";

    private APUnorderTestData() {}


    @DataProvider(name = AP_UNORDER_EXECUTION)
    public static Object[][] getValidUnorderTestData() {

        final List<UnorderExecutionCsvData> csvDataList = loadCsvFile("/data/" + AP_UNORDER_EXECUTION + ".csv",
                APUnorderTestData.class, UnorderExecutionCsvData.class, UnorderExecutionCsvData.CELL_PROCESSORS);

        final Object[][] data = new Object[csvDataList.size()][5];

        for (int i = 0; i < csvDataList.size(); i++) {
            final UnorderExecutionCsvData csvData = csvDataList.get(i);
            final Object[][] projectData = APImportERBSZipTestData.createDataForProvider(csvData.getImportDataProvider());
            final byte[] zipFileContents =  (byte[]) projectData[0][0];
            final APProjectTestInfo projectInfo = (APProjectTestInfo) projectData[0][1];
            final boolean successfulCommandResult = csvData.isSuccessfulCommandResult();

            data[i][0] = zipFileContents;
            data[i][1] = projectInfo;
            data[i][2] = Integer.parseInt(csvData.getOrderNodeCount());
            data[i][3] = csvData.getDescription();
            data[i][4] = successfulCommandResult;
        }

        return data;
    }

    public static class UnorderExecutionCsvData {

        public static final CellProcessor[] CELL_PROCESSORS = {null, null, null, null};

        private String importDataProvider;
        private String orderNodeCount;
        private String description;
        private String commandResult;

        /**
         * @return the importDataProvider delimited after '[csvDataProviderFileName]:'
         */
        public String getImportDataProvider() {
            return  importDataProvider.split(":")[1];
        }
        /**
         * @param importDataProvider the importDataProvider to set
         */
        public void setImportDataProvider(final String importDataProvider) {
            this.importDataProvider = importDataProvider;
        }
        /**
         * @return the orderNodeCount
         */
        public String getOrderNodeCount() {
            return orderNodeCount;
        }
        /**
         * @param orderNodeCount the orderNodeCount to set
         */
        public void setOrderNodeCount(final String orderNodeCount) {
            this.orderNodeCount = orderNodeCount;
        }
        /**
         * @return the description
         */
        public String getDescription() {
            return description;
        }
        /**
         * @param description the description to set
         */
        public void setDescription(final String description) {
            this.description = description;
        }
        /**
         * @return the commandResult
         */
        public boolean isSuccessfulCommandResult() {
            return commandResult.equalsIgnoreCase("SUCCESSFUL");
        }
        /**
         * @param commandResult the commandResult to set
         */
        public void setCommandResult(final String commandResult) {
            this.commandResult = commandResult;
        }
    }
}
