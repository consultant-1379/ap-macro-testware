/*
 * ------------------------------------------------------------------------------
 *  *******************************************************************************
 *  * COPYRIGHT Ericsson 2013
 *  *
 *  * The copyright to the computer program(s) herein is the property of
 *  * Ericsson Inc. The programs may be used and/or copied only with written
 *  * permission from Ericsson Inc. or in accordance with the terms and
 *  * conditions stipulated in the agreement/contract under which the
 *  * program(s) have been supplied.
 *  *******************************************************************************
 *  *----------------------------------------------------------------------------
 */

package com.ericsson.oss.ap.erbs.test.cases;

import java.io.IOException;
import java.io.Writer;

import com.ericsson.oss.ap.erbs.test.data.APImportERBSZipTestData;

import freemarker.template.*;

/**
 * {@code TemplateHandler} is used to encapsulate access to a template engine.
 * 
 * @author ekarasi
 * @since 1.0.1
 */
public class TemplateHandler {
    private final Configuration config = new Configuration();

    public TemplateHandler(final Class<APImportERBSZipTestData> theClass, final String resourceRoot) {
        this.config.setClassForTemplateLoading(theClass, resourceRoot);
    }

    public void processTemplate(final String resource, final Object theTemplateData, final Writer theWriter) throws IOException, TemplateException {
        final Template template = config.getTemplate(resource);
        template.process(theTemplateData, theWriter);
    }
}
