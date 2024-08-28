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

import java.util.Map;
import java.util.TreeMap;

import com.ericsson.oss.ap.core.test.model.GenericAPTestInfo;

/**
 * @author ekarasi
 * @since 1.0.1
 */
public class APNodeTestInfo extends GenericAPTestInfo {
    private final Map<String, APNodeArtifactTestInfo> artifacts = new TreeMap<>();

    public Map<String, APNodeArtifactTestInfo> getArtifacts() {
        return artifacts;
    }

    public void addArtifact(final String artifact, final APNodeArtifactTestInfo artifactInfo) {
        artifacts.put(artifact, artifactInfo);
    }

    public String getApNodeFdn() {
        return this.getAttribute("apNodeFdn");
    }

    public String getMimVersion() {
        return this.getAttribute("mimVersion");
    }

    public String getIpAddress() {
        return this.getAttribute("ipAddress");
    }

    public String getNodeType() {
        return this.getAttribute("nodeType");
    }

    public String getSite() {
        return this.getAttribute("site");
    }

    public String getUpgradePackageName() {
        return this.getAttribute("upgradePackageName");
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

    public String getSiteInstall() {
        return getNonNull("siteInstall");
    }

    public String getSiteBasic() {
        return getNonNull("siteBasic");
    }

    public String getSiteEquipment() {
        return getNonNull("siteEquipment");
    }

    public String getRadio() {
        return (String) this.getAttribute("radio");
    }

    public String getTransport() {
        return (String) this.getAttribute("transport");
    }

    public Boolean getAiAttributesPresent() {
        return this.getAttribute("aiAttributesPresent");
    }

    public Boolean getSecurityPresent() {
        return this.getAttribute("securityPresent");
    }

    public String getMinimumSecurityLevel() {
        return this.getAttribute("minimumSecurityLevel");
    }

    public String getOptimumSecurityLevel() {
        return this.getAttribute("optimumSecurityLevel");
    }

    public String getEnrollmentMode() {
        return this.getAttribute("enrollmentMode");
    }

    public Boolean getIpSecurityPresent() {
        return this.getAttribute("ipSecurityPresent");
    }

    public String getIpSecLevel() {
        return this.getAttribute("ipSecLevel");
    }

    public String getSubjectAltNameType() {
        return this.getAttribute("subjectAltNameType");
    }

    public String getSubjectAltName() {
        return this.getAttribute("subjectAltName");
    }

    public String getInstallLicense() {
        return this.getAttributeAsString("installLicense");
    }

    private String getNonNull(final String attributeName) {
        final String attributeValue = (String) this.getAttribute(attributeName);
        return (attributeValue == null) ? "" : attributeValue;
    }
}
