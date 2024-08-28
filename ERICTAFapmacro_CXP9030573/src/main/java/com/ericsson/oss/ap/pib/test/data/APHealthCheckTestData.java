/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2013
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.ap.pib.test.data;

import java.util.ArrayList;
import java.util.List;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.testng.annotations.DataProvider;

import com.ericsson.oss.ap.erbs.test.data.AbstractTestData;
import com.ericsson.oss.ap.pib.test.cases.APHealthCheck;

/**
 * Test data for {@link APHealthCheck} TAF test.
 * 
 * @since 1.2.1
 */
public class APHealthCheckTestData extends AbstractTestData {

    private APHealthCheckTestData() { // added for PMD purposes
    }

    @DataProvider(name = "ap_healthcheck")
    public static Object[][] getHealthCheckServiceIdentifiers() {
        final List<Object[]> result = new ArrayList<>();
        final List<HealthCheckTestCsvData> csvDataList = loadCsvFile("/data/apHealthCheck.csv", APHealthCheckTestData.class,
                HealthCheckTestCsvData.class, HealthCheckTestCsvData.CELL_PROCESSORS);
        for (final HealthCheckTestCsvData csvData : csvDataList) {
            final Object[] rowData = new Object[3];
            final HealthCheckTestData testData = new HealthCheckTestData();
            testData.expectedServiceIdentifier = csvData.getServiceIdentifier();
            testData.expectedNodeIdentifier = csvData.getNodeIdentifier();
            testData.testCase = csvData.getTestCase();
            rowData[0] = csvData.getServiceIdentifier();
            rowData[1] = csvData.getNodeIdentifier();
            rowData[2] = testData;
            result.add(rowData);
        }
        return result.toArray(new Object[result.size()][3]);
    }

    public static class HealthCheckTestData {

        public String expectedServiceIdentifier;
        public String expectedNodeIdentifier;
        public String testCase;
    }

    public static class HealthCheckTestCsvData {

        public static final CellProcessor[] CELL_PROCESSORS = { null, null, new Optional() };

        private String testCase;
        private String nodeIdentifier;
        private String serviceIdentifier;

        public String getTestCase() {
            return testCase;
        }

        public void setTestCase(final String testCase) {
            this.testCase = testCase;
        }

        public String getServiceIdentifier() {
            return serviceIdentifier;
        }

        public void setServiceIdentifier(final String serviceIdentifier) {
            this.serviceIdentifier = serviceIdentifier;
        }

        public String getNodeIdentifier() {
            return nodeIdentifier;
        }

        public void setNodeIdentifier(final String nodeIdentifier) {
            this.nodeIdentifier = nodeIdentifier;
        }
    }
}
