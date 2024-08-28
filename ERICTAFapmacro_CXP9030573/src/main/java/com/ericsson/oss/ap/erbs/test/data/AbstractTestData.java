/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import com.ericsson.cifwk.taf.TestData;

/**
 * @since 1.0.10
 */
public class AbstractTestData implements TestData { //NOPMD

    // TODO: SUPPORTED_ARTIFACTS should be removed from here. It is test case specific, not general.
    public static final String[] SUPPORTED_ARTIFACTS = { "siteBasic", "siteEquipment", "siteInstall", "radio", "transport", "postIntegration" };
    public static final Properties EXPECTED_TEXT_PROPERTIES = new Properties();

    private final static Logger LOGGER = Logger.getLogger(AbstractTestData.class);
    private final static String EXPECTED_TEST_FILE_PATH = "/data/expected_text.properties";

    static {
        try {
            EXPECTED_TEXT_PROPERTIES.load(AbstractTestData.class.getResourceAsStream(EXPECTED_TEST_FILE_PATH));
        } catch (final IOException e) {
            LOGGER.error("Failed to load properties file " + EXPECTED_TEST_FILE_PATH, e);
        }
    }

    /**
     * Loads a CSV using CsvBeanReader file.
     * 
     * @param resource
     * @param loadFromClass
     * @param dataClass
     * @param theCellProcessors
     * @return ArrayList testData
     */
    protected static <T> List<T> loadCsvFile(final String resource, final Class<?> loadFromClass, final Class<T> dataClass,
            final CellProcessor[] theCellProcessors) {
        final List<T> testData = new ArrayList<>();

        final InputStream csvStream = loadFromClass.getResourceAsStream(resource);
        final InputStreamReader reader = new InputStreamReader(csvStream);

        try (final ICsvBeanReader beanReader = new CsvBeanReader(reader, CsvPreference.STANDARD_PREFERENCE)) {
            final String[] headers = beanReader.getHeader(true);

            T csvTestData;
            while ((csvTestData = beanReader.read(dataClass, headers, theCellProcessors)) != null) {
                testData.add(csvTestData);
            }
        } catch (final Exception e) {
            LOGGER.error("An exception occurred while loading CSV file " + resource, e);
        }
        return testData;
    }
}
