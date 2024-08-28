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

package com.ericsson.oss.ap.erbs.test.model;

import com.ericsson.oss.ap.core.test.model.GenericAPTestInfo;

/**
 * Test data for APUploadTestInfo.
 * 
 * @author ekatbeb
 * @since 1.6.6
 * 
 */
public class APValidateTestInfo extends GenericAPTestInfo {

    public String getTestName() {
        return this.getAttribute("testName");
    }

    public String getDescription() {
        return this.getAttribute("testDescription");
    }

    public String getExpectedResult() {
        return this.getAttribute("expectedResult");
    }

    public String getExpectedResultMessage() {
        return this.getAttribute("expectedResultMsg");
    }

    public String getExpectedValidationMessage() {
        return this.getAttribute("expectedValidationMsg");
    }

}