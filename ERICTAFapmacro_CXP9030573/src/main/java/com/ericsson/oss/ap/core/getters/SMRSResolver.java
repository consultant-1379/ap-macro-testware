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
package com.ericsson.oss.ap.core.getters;

import javax.inject.Inject;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.tools.http.HttpResponse;
import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.oss.ap.core.helpers.LoginHelper;

/**
 * Resolves local and remote AutoProvisioning directories.
 * 
 */
public class SMRSResolver {

    private static final String CONFIG_SERVICE_REST_URL = "/pib/configurationService/getConfigParameter";

    @Inject
    private APHostResolver hostResolver;

    private String smrsLranHome;

    private String smrsAiHome;

    public String getSmrsAiDir() {
        if (smrsAiHome == null) {
            smrsAiHome = getConfigParameterValue("aiRootDirectory");
        }

        return smrsAiHome;
    }

    public String getSmrsLranRootDir() {
        if (smrsLranHome == null) {
            smrsLranHome = getConfigParameterValue("smsrLranRootDirectory");
        }
        return smrsLranHome;
    }

    private String getConfigParameterValue(final String paramName) {    
        final Host host = hostResolver.getApacheHost();

        final HttpTool httpTool = LoginHelper.performDefaultSecureLogin(host);
        final HttpResponse response = httpTool.request().queryParam("paramName", paramName).get(CONFIG_SERVICE_REST_URL);

        return extractConfigParamValue(response.getBody()); //REST response does not return JSON, need to extract the value from the returned string
    }


    private String extractConfigParamValue(final String configurationParameter) {
        final int startIndex = configurationParameter.indexOf("value") + 6;
        final int endIndex = configurationParameter.indexOf(",", startIndex);
        return configurationParameter.substring(startIndex, endIndex);
    }

    public String getAbsoluteRbsSummaryFilePath(final String nodeName) {
        return getSmrsAiDir() + nodeName + "/" + "AutoIntegrationRbsSummaryFile.xml";
    }

    public String getRelativeRbsSummaryFilePath(final String nodeName) {
        final String absoluteDir = getAbsoluteRbsSummaryFilePath(nodeName);
        return absoluteDir.substring(getSmrsLranRootDir().length() -1);
    }

    public String getAbsoluteSiteBasicFilePath(final String nodeName) {
        return getSmrsAiDir() + nodeName + "/" + "SiteBasic.xml";
    }

    public String getRelativeSiteBasicFilePath(final String nodeName) {
        final String absoluteDir = getAbsoluteSiteBasicFilePath(nodeName);
        return absoluteDir.substring(getSmrsLranRootDir().length() -1);
    }

    public String getAbsoluteSiteEquipmentFilePath(final String nodeName) {
        return getSmrsAiDir() + nodeName + "/" + "RbsEquipment.xml";
    }

    public String getRelativeSiteEquipmentFilePath(final String nodeName) {
        final String absoluteDir = getAbsoluteSiteEquipmentFilePath(nodeName);
        return absoluteDir.substring(getSmrsLranRootDir().length() -1);
    }

    public String getAbsoluteIscfFilePath(final String nodeName) {
        return getSmrsAiDir() + nodeName + "/" + "Iscf.xml";
    }

    public String getRelativeIscfFilePath(final String nodeName) {
        final String absoluteDir = getAbsoluteIscfFilePath(nodeName);
        return absoluteDir.substring(getSmrsLranRootDir().length() -1);
    }
}