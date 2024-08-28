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
import com.ericsson.oss.ap.pib.test.cases.APUpgrade;

/**
 * Test data for {@link APUpgrade} TAF test.
 * 
 * @since 1.2.3
 */
public class APUpgradeTestData extends AbstractTestData {

    private APUpgradeTestData() { // added for PMD purposes
    }

    @DataProvider(name = "ap_upgrade")
    public static Object[][] getUpgradeServiceIdentifiers() {
        final List<Object[]> result = new ArrayList<>();
        final List<UpgradeTestCsvData> csvDataList = loadCsvFile("/data/apUpgrade.csv", APUpgradeTestData.class, UpgradeTestCsvData.class,
                UpgradeTestCsvData.CELL_PROCESSORS);
        for (final UpgradeTestCsvData csvData : csvDataList) {
            final Object[] rowData = new Object[3];
            final UpgradeTestData testData = new UpgradeTestData();
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

    public static class UpgradeTestData {

        public String expectedServiceIdentifier;
        public String expectedNodeIdentifier;
        public String testCase;
    }

    public static class UpgradeTestCsvData {

        public static final CellProcessor[] CELL_PROCESSORS = { null, null, new Optional() };

        private String testCase;
        private String serviceIdentifier;
        private String nodeIdentifier;

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
