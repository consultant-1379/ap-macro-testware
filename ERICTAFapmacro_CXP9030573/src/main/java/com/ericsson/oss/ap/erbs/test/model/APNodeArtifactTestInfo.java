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
package com.ericsson.oss.ap.erbs.test.model;

import com.ericsson.oss.ap.core.test.model.GenericAPTestInfo;

/**
 * @author ekarasi
 * @since 1.0.1
 */
public class APNodeArtifactTestInfo extends GenericAPTestInfo {

    private String nodeName;

    public void setNodeName(final String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeName() {
        return nodeName;
    }
}
