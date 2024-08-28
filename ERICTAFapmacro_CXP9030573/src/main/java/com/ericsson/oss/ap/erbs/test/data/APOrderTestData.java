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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.testng.annotations.DataProvider;

import com.ericsson.oss.ap.erbs.test.model.APProjectTestInfo;

public class APOrderTestData extends AbstractTestData {

    public static final String AP_ORDER_EXECUTION = "apOrder_execution";
    public static final String AP_ORDER_VALIDATION = "apOrder_validation";
    public static final String AP_ORDER_FROM_ORDER_FAILED_STATE = "apOrder_orderFailed";

    private static final String IMPORT_DATA_PROVIDER_FROM_ORDER_FAILED = "apOrder_SuccessFromOrderFailedState";

    private APOrderTestData() {}


    @DataProvider(name = AP_ORDER_EXECUTION)
    public static Object[][] getValidOrderTestData() {

        final List<OrderExecutionCsvData> csvDataList = loadCsvFile("/data/" + AP_ORDER_EXECUTION + ".csv",
                APOrderTestData.class, OrderExecutionCsvData.class, OrderExecutionCsvData.CELL_PROCESSORS);

        final Object[][] data = new Object[csvDataList.size()][3];

        int i = 0;
        for (final OrderExecutionCsvData csvData : csvDataList) {

            if (StringUtils.isBlank(csvData.getValidationMsg()) && !csvData.getImportDataProvider().contains(IMPORT_DATA_PROVIDER_FROM_ORDER_FAILED)) {

                final Object[][] projectData = APImportERBSZipTestData.createDataForProvider(csvData.getImportDataProvider());
                final byte[] zipFileContents = (byte[]) projectData[0][0];
                final APProjectTestInfo projectInfo = (APProjectTestInfo) projectData[0][1];

                data[i][0] = zipFileContents;
                data[i][1] = projectInfo;
                data[i][2] = csvData;

                i++;
            }
        }

        return Arrays.copyOfRange(data, 0, i);
    }

    @DataProvider(name = AP_ORDER_FROM_ORDER_FAILED_STATE)
    public static Object[][] getOrderTestDataForTestOrderFailed() {

        final List<OrderExecutionCsvData> csvDataList = loadCsvFile("/data/" + AP_ORDER_EXECUTION + ".csv", APOrderTestData.class, OrderExecutionCsvData.class, OrderExecutionCsvData.CELL_PROCESSORS);

        final Object[][] data = new Object[csvDataList.size()][3];

        int i = 0;
        for (final OrderExecutionCsvData csvData : csvDataList) {

            if (csvData.getImportDataProvider().contains(IMPORT_DATA_PROVIDER_FROM_ORDER_FAILED)) {

                final Object[][] projectData = APImportERBSZipTestData.createDataForProvider(csvData.getImportDataProvider());
                final byte[] zipFileContents = (byte[]) projectData[0][0];
                final APProjectTestInfo projectInfo = (APProjectTestInfo) projectData[0][1];

                data[i][0] = zipFileContents;
                data[i][1] = projectInfo;
                data[i][2] = csvData;

                i++;
            }
        }

        return Arrays.copyOfRange(data, 0, i);
    }

    @DataProvider(name = AP_ORDER_VALIDATION)
    public static Object[][] getOrderFailedInValidationTestData() {

        final List<OrderExecutionCsvData> csvDataList = loadCsvFile("/data/" + AP_ORDER_EXECUTION + ".csv", APOrderTestData.class, OrderExecutionCsvData.class, OrderExecutionCsvData.CELL_PROCESSORS);

        final Object[][] data = new Object[csvDataList.size()][3];

        int i = 0;
        for (final OrderExecutionCsvData csvData : csvDataList) {

            if (!StringUtils.isBlank(csvData.getValidationMsg())) {

                final Object[][] projectData = APImportERBSZipTestData.createDataForProvider(csvData.getImportDataProvider());
                final byte[] zipFileContents = (byte[]) projectData[0][0];
                final APProjectTestInfo projectInfo = (APProjectTestInfo) projectData[0][1];

                data[i][0] = zipFileContents;
                data[i][1] = projectInfo;
                data[i][2] = csvData;

                i++;
            }
        }

        return Arrays.copyOfRange(data, 0, i);
    }

    public static class OrderExecutionCsvData {

        public static final CellProcessor[] CELL_PROCESSORS = { null, null, null, null, null, null };

        private String importDataProvider;
        private String nodeCount;
        private String description;
        private String workflowResult;
        private String validationMsg;
        private String commandResult;

        public String getImportDataProvider() {
            return  importDataProvider.split(":")[1];
        }

        public void setImportDataProvider(final String importDataProvider) {
            this.importDataProvider = importDataProvider;
        }

        public String getNodeCount() {
            return nodeCount;
        }

        public void setNodeCount(final String nodeCount) {
            this.nodeCount = nodeCount;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(final String description) {
            this.description = description;
        }

        public boolean isSuccessfulCommandResult() {
            return commandResult.equalsIgnoreCase("SUCCESSFUL");
        }

        public void setCommandResult(final String commandResult) {
            this.commandResult = commandResult;
        }

        public String getValidationMsg() {
            return validationMsg;
        }

        public void setValidationMsg(final String validationMsg) {
            this.validationMsg = validationMsg;
        }

        public boolean isSuccessfulWorkflowResult() {
            return workflowResult.equalsIgnoreCase("SUCCESSFUL");
        }

        public void setWorkflowResult(final String workflowResult) {
            this.workflowResult = workflowResult;
        }
    }
}
