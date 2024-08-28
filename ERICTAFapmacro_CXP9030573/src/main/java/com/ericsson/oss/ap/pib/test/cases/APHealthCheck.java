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
import com.ericsson.oss.ap.pib.test.data.APHealthCheckTestData;
import com.ericsson.oss.services.ap.common.Constants;

/**
 * Tests the invocation of the PIB HealthCheck on AP Services.
 *
 * @since 1.2.1
 */
public class APHealthCheck extends TorTestCaseHelper {

    @Inject
    private APHostResolver hostResolver;

    private final static String HEALTHCHECK_REST_SERVICE_IDENTIFIER_URL = "/pib/healthcheck/getStatus";
    private final static String HEALTHCHECK_REST_UNIQUE_ID_URL = "/pib/healthcheck/getResponseAsText";
    private final static String HEALTHCHECK_NO_SERVICE_REPSONSE = "NONE";

    /**
     * Tests the correct response is returned when an AP Healthcheck is executed.
     */
    @Context(context = Context.CLI)
    @Test(dataProvider = "ap_healthcheck", dataProviderClass = APHealthCheckTestData.class, groups = { "GAT", "Acceptance" })
    public void verifyCorrectHealthCheckResponse(final String serviceIdentifier, final String nodeIdentifier,
            final APHealthCheckTestData.HealthCheckTestData testData) {

        final Host host = hostResolver.getApacheHost();
        final HttpTool tool = HttpToolBuilder.newBuilder(host).trustSslCertificates(true).followRedirect(false)
                .useHttpsIfProvided(!Constants.ENV_LOCAL).build();

        setTestCase(testData.testCase, "AP:CLI - Auto Provisioning HealthCheck. ");
        setTestStep("Run HealthCheck with Service Identifier : " + serviceIdentifier);
        final HttpResponse serviceIdentifierResponse = tool.request().queryParam("all", "false").queryParam("service_identifier", serviceIdentifier)
                .get(HEALTHCHECK_REST_SERVICE_IDENTIFIER_URL);
        final String uniqueId = serviceIdentifierResponse.getBody();

        // A sleep is needed for the PIB rest calls, temporary fix. 
        sleep(5);

        setTestStep("Run HealthCheck with Unique ID : " + uniqueId);
        final HttpResponse uniqueIdResponse = tool.request().queryParam("id", uniqueId).get(HEALTHCHECK_REST_UNIQUE_ID_URL);
        final String healthcheckOutput = uniqueIdResponse.getBody();
        setTestStep("Verify HealthCheck returns correct response");
        assertTrue(healthcheckOutput.contains(serviceIdentifier));
        assertNotEquals(healthcheckOutput, HEALTHCHECK_NO_SERVICE_REPSONSE);
    }
}
