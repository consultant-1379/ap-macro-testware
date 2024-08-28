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
package com.ericsson.oss.ap.erbs.test.cases.validators;

import com.ericsson.oss.ap.erbs.test.model.APNodeTestInfo;

/**
 * Performs validation to verify a test case has executed successfully.
 *
 */
public interface WorkflowValidator {

    /**
     * Perform validation for the test case.
     * 
     * @param args the arguments required to validate the test case
     */
    void execute(final APNodeTestInfo nodeInfo);
}
