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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.supercsv.cellprocessor.ift.CellProcessor;

import com.ericsson.cifwk.taf.annotations.DataSource;
import com.ericsson.oss.ap.erbs.test.model.APProjectTestInfo;
import com.ericsson.oss.ap.erbs.test.model.APValidateTestInfo;

/**
 * Test data for Validate Project.
 * 
 * Loads csv data for
 * {@link com.ericsson.oss.ap.erbs.test.cases.APValidateERbsZip}
 * 
 * Data includes the import datasource and expected results, response messages
 * and validation messages.
 * 
 * @since 1.6.6
 * 
 */
public class APValidateProjectTestData extends AbstractTestData {

    public static final String AP_VALIDATE_EXECUTION = "apValidate";

    /**
     * Loads data from csv file and adds to a map called testData for test
     * class.
     * 
     * @return csv data as a list of maps called testDataList
     */
    @DataSource
    public List<Map<String, Object>> getValidationExecutionTestData() throws URISyntaxException, IOException {
        final List<ValidateExecutionCsvData> csvDataList = loadCsvFile("/data/" + AP_VALIDATE_EXECUTION + ".csv", APValidateProjectTestData.class, ValidateExecutionCsvData.class,
                ValidateExecutionCsvData.CELL_PROCESSORS);

        final List<Map<String, Object>> testDataList = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < csvDataList.size(); i++) {
            final Map<String, Object> testData = new HashMap<String, Object>();

            final ValidateExecutionCsvData csvData = csvDataList.get(i);
            final String importDataProvider = csvData.getImportDataProvider();
            final Object[][] projectData = APImportERBSZipTestData.createDataForProvider(importDataProvider);
            final byte[] zipFileContents = (byte[]) projectData[0][0];
            final APProjectTestInfo projectInfo = (APProjectTestInfo) projectData[0][1];

            testData.put("zipFileContents", zipFileContents);
            testData.put("projectInfo", projectInfo);
            final APValidateTestInfo validateTestInfo = new APValidateTestInfo();
            validateTestInfo.setAttribute("testDescription", csvData.getTestDescription());
            validateTestInfo.setAttribute("expectedResult", csvData.getExpectedResult());
            validateTestInfo.setAttribute("expectedResultMsg", String.format((String) EXPECTED_TEXT_PROPERTIES.get(csvData.getExpectedResultMessage()), projectInfo.getAttribute("fileName")));
            validateTestInfo.setAttribute("expectedValidationMsg", csvData.getExpectedValidationMessage());

            testData.put("testInfo", validateTestInfo);

            testDataList.add(testData);
        }

        return testDataList;
    }

    public static class ValidateExecutionCsvData {
        public static final CellProcessor[] CELL_PROCESSORS = { null, null, null, null, null };

        private String testDescription;
        private String importDataProvider;
        private String expectedResult;
        private String expectedResultMsg;
        private String expectedValidationMsg;

        public String getTestDescription() {
            return testDescription;
        }

        public void setTestDescription(final String testDescription) {
            this.testDescription = testDescription;
        }

        public String getImportDataProvider() {
            return importDataProvider;
        }

        public void setImportDataProvider(final String importDataProvider) {
            this.importDataProvider = importDataProvider;
        }

        public String getExpectedResultMsg() {
            return expectedResultMsg;
        }

        public void setExpectedResultMsg(final String expectedResultMsg) {
            this.expectedResultMsg = expectedResultMsg;
        }

        public String getExpectedResult() {
            return expectedResult;
        }

        public void setExpectedResult(final String expectedResult) {
            this.expectedResult = expectedResult;
        }

        public String getExpectedResultMessage() {
            return expectedResultMsg;
        }

        public void setExpectedResultMessage(final String expectedResultMsg) {
            this.expectedResultMsg = expectedResultMsg;
        }

        public String getExpectedValidationMessage() {
            return expectedValidationMsg;
        }

        public void setExpectedValidationMsg(final String expectedValidationMsg) {
            this.expectedValidationMsg = expectedValidationMsg;
        }

    }
}