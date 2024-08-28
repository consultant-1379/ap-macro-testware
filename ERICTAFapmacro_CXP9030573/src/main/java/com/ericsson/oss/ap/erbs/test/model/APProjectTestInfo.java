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

import java.util.ArrayList;
import java.util.List;

import com.ericsson.oss.ap.core.test.model.GenericAPTestInfo;

/**
 * @author ekarasi
 * @since 1.0.1
 */
public class APProjectTestInfo extends GenericAPTestInfo {
    private final List<APNodeTestInfo> nodes = new ArrayList<>();

    public List<APNodeTestInfo> getNodes() {
        return nodes;
    }

    public void addNode(final APNodeTestInfo node) {
        nodes.add(node);
    }

    public String getProjectFdn() {
        return this.getAttribute("apProjectFdn");
    }

    public String getCreator() {
        return this.getAttribute("creator");
    }

    public String getDescription() {
        return this.getAttribute("description");
    }

    public String getProjectType() {
        return this.getAttribute("projectType");
    }

    public String getUnlockCells() {
        return this.getAttributeAsString("unlockCells");
    }

    public String getUploadCVAfterConfiguration() {
        return this.getAttributeAsString("uploadCVAfterConfiguration");
    }

    public String getUploadCVAfterIntegration() {
        return this.getAttributeAsString("uploadCVAfterIntegration");
    }

    public String getUpgradePackageName() {
        return this.getAttribute("upgradePackageName");
    }

    public Boolean getAiAttributesPresent() {
        return this.getAttribute("aiAttributesPresent");
    }

    public String getInstallLicense() {
        return this.getAttributeAsString("installLicense");
    }
}
