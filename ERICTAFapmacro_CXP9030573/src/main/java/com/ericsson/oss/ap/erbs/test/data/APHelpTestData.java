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

import java.util.ArrayList;
import java.util.List;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.testng.annotations.DataProvider;

/**
 * Loads csv data for {@link com.ericsson.oss.ap.core.test.ui.APHelpCLIResponse}
 * . <br />
 * Data includes commands and expected response messages.
 * 
 * @author eshemeh
 * @since 1.3.1
 */
public class APHelpTestData extends AbstractTestData {

    public static final String HELP_AP = "help_ap";

    private APHelpTestData() {

    }

    /**
     * Loads data from csv file and adds to 2D array for test class.
     * 
     * @return csv data as 2D array of Objects
     */
    @DataProvider(name = HELP_AP)
    public static Object[][] getHelpCommands() {
        final List<Object[]> result = new ArrayList<>();
        final List<HelpTestCsvData> csvDataList = loadCsvFile("/data/help.csv", APHelpTestData.class, HelpTestCsvData.class,
                HelpTestCsvData.CELL_PROCESSORS);

        for (final HelpTestCsvData csvData : csvDataList) {
            final Object[] rowData = new Object[2];
            rowData[0] = csvData.getCommand();
            rowData[1] = (String) EXPECTED_TEXT_PROPERTIES.get("ap.expected.help.text." + csvData.getExpected());
            result.add(rowData);
        }
        return result.toArray(new Object[result.size()][2]);
    }

    public static class HelpTestCsvData {
        public static final CellProcessor[] CELL_PROCESSORS = { null, null };

        private String command;
        private String expected;

        public String getExpected() {
            return expected;
        }

        public void setExpected(final String expected) {
            this.expected = expected;
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(final String command) {
            this.command = command;
        }
    }
}