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
package com.ericsson.oss.ap.erbs.test.steps;

import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setTestInfo;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;

/**
 * Contains general Test Steps unrelated to specific AP functionality.
 * 
 * @author eeibky
 * @since 1.6.4
 */
public class GeneralTestSteps {

    public static final String TEST_STEP_SET_DESCRIPTION = "setDescription";

    /**
     * Set the JCAT Test Case Description.
     * <p>
     * Requires a <code>description</code> attribute to exist in the input Data Source csv.
     */
    @TestStep(id = TEST_STEP_SET_DESCRIPTION)
    public void setTestCaseDescription(@Input("description") final String description) {
        setTestInfo(description);
    }
}
