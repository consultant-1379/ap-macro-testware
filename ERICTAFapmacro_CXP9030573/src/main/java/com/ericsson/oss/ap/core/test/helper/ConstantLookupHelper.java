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
package com.ericsson.oss.ap.core.test.helper;

import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.TorTestCaseHelper;

public class ConstantLookupHelper {

    private final static Logger logger = Logger.getLogger(TorTestCaseHelper.class);

    public String getStringConstant(final Class<?> classObj, final String fieldName) {
        String constant = fieldName;
        try {
            constant = (String) FieldUtils.readStaticField(classObj, fieldName, true);
        } catch (final Exception e) {
            logger.info(e);
        }
        return constant;
    }

}
