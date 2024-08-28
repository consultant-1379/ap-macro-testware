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
package com.ericsson.oss.ap.pib.test.cases;

import javax.inject.Inject;

import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.tools.http.HttpResponse;
import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.cifwk.taf.tools.http.HttpToolBuilder;
import com.ericsson.oss.ap.core.getters.APHostResolver;
import com.ericsson.oss.ap.pib.test.data.APUpgradeTestData;
import com.ericsson.oss.services.ap.common.Constants;

/**
 * Tests the invocation of the PIB Upgrade on AP Services.
 *
 * @since 1.2.3
 */
public class APUpgrade extends TorTestCaseHelper {

    @Inject
    private APHostResolver hostResolver;

    private final static String UPGRADE_REST_SERVICE_IDENTIFIER_URL = "/pib/upgradeService/startUpgrade";
    private final static String UPGRADE_REST_UNIQUE_ID_URL = "/pib/upgradeService/getUpgradeResponse";
    private final static String UPGRADE_NO_SERVICE_REPSONSE = "NONE";

    /**
     * Tests the correct response is returned when an AP Upgrade is initiated.
     */
    @Context(context = Context.CLI)
    @Test(dataProvider = "ap_upgrade", dataProviderClass = APUpgradeTestData.class, groups = { "GAT", "Acceptance" })
    public void verifyCorrectUpgradeResponse(final String serviceIdentifier, final String nodeIdentifier,
            final APUpgradeTestData.UpgradeTestData testData) {

        final Host host = hostResolver.getApacheHost();
        final HttpTool tool = HttpToolBuilder.newBuilder(host).trustSslCertificates(true).followRedirect(false)
                .useHttpsIfProvided(!Constants.ENV_LOCAL).build();

        setTestCase(testData.testCase, "AP:CLI - Auto Provisioning Upgrade. ");
        setTestStep("Run Upgrade with Service Identifier : " + serviceIdentifier + ", Node Identifier : " + nodeIdentifier);

        final HttpResponse serviceIdentifierResponse = tool.request().queryParam("app_server_identifier", nodeIdentifier)
                .queryParam("service_identifier", serviceIdentifier).queryParam("upgrade_operation_type", "service")
                .queryParam("upgrade_phase", "SERVICE_INSTANCE_UPGRADE_PREPARE").get(UPGRADE_REST_SERVICE_IDENTIFIER_URL);
        final String uniqueId = serviceIdentifierResponse.getBody();

        // A sleep is needed for the PIB rest calls, temporary fix.
        sleep(5);

        setTestStep("Run Upgrade with Unique ID : " + uniqueId);

        final HttpResponse uniqueIdResponse = tool.request().queryParam("id", uniqueId).get(UPGRADE_REST_UNIQUE_ID_URL);
        final String healthcheckOutput = uniqueIdResponse.getBody();

        setTestStep("Verify Upgrade returns a response");
        assertFalse(healthcheckOutput.equals(UPGRADE_NO_SERVICE_REPSONSE));
    }
}
