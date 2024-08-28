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
package com.ericsson.oss.ap.common.test.steps;

import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setTestStepBegin;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.oss.ap.core.getters.APHostResolver;
import com.ericsson.oss.ap.core.operators.cm.CmCliOperator;
import com.ericsson.oss.ap.core.operators.cm.model.ManagedObjectDto;
import com.ericsson.oss.ap.core.test.helper.TestDataCleaner;
import com.ericsson.oss.ap.erbs.test.model.APProjectTestInfo;
import com.ericsson.oss.ap.erbs.test.steps.ImportProjectTestSteps;

/**
 * Contains the Create MO Test Steps
 * 
 * @author ekatbeb,ereilda
 * @since 1.9.3
 */
public class CreateMoTestSteps {

    public static final String TEST_STEP_CREATE_NETWORK_ELEMENT = "createNetworkElement";
    public static final String TEST_STEP_CREATE_CPPCONNECTIVITY_MO = "createCppConnectivityMo";

    public static final String NETWORK_ELEMENT_FDN_PREFIX = "NetworkElement=";

    public static final String NAME_SPACE = "OSS_NE_DEF";
    public static final String VERSION = "2.0.0";

    @Inject
    private CmCliOperator cmOperator;

    @Inject
    private APHostResolver hostResolver;

    @Inject
    private TestDataCleaner testDataCleaner;

    @Inject
    private TestContext context;

    /**
     * Create a CppConnectivityInformation MO, which will be used to validate IP
     * address uniqueness
     */
    @TestStep(id = TEST_STEP_CREATE_CPPCONNECTIVITY_MO)
    public void createCppConnectivityMo() {

        setTestStepBegin("Pre-create CppConnectivityInformation MO for IP address uniqueness validation");

        final APProjectTestInfo projectInfo = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_PROJECTINFO);
        se.ericsson.jcat.fw.assertion.JcatAssertApi.assertTrue("No node in project to be validated.", projectInfo.getNodes().size() > 0);

        final String nodeName = projectInfo.getNodes().get(0).getName();
        final String ipAddress = projectInfo.getNodes().get(0).getIpAddress();

        final String fdn = String.format("NetworkElement=%s,CppConnectivityInformation=1", nodeName);

        final Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("CppConnectivityInformationId", "1");
        attributes.put("ipAddress", "\"" + ipAddress + "\"");
        attributes.put("port", "80");

        final ManagedObjectDto mo = cmOperator.createMo(hostResolver.getApacheHost(), fdn, "CPP_MED", "1.0.0", attributes);
        if (mo != null) {
            testDataCleaner.markFdnForCleanUp(fdn);
        }

    }

    /**
     * Create a node NetworkElement with a specific name. This pre-created
     * NetworkElement is used to fail unique name validation
     * 
     */
    @TestStep(id = TEST_STEP_CREATE_NETWORK_ELEMENT)
    public void createNetworkElement() {

        setTestStepBegin("Pre-create NetworkElement for validation tests");

        final APProjectTestInfo projectInfo = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_PROJECTINFO);
        se.ericsson.jcat.fw.assertion.JcatAssertApi.assertTrue("No node in project to be validated.", projectInfo.getNodes().size() > 0);

        final String nodeName = projectInfo.getNodes().get(0).getName();
        final String neType = projectInfo.getProjectType().toUpperCase();

        final String fdn = NETWORK_ELEMENT_FDN_PREFIX + nodeName;

        createMeContextMo(nodeName, neType);

        final Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("networkElementId", nodeName);
        attributes.put("neType", neType);
        attributes.put("platformType", "CPP");

        final ManagedObjectDto mo = cmOperator.createMo(hostResolver.getApacheHost(), fdn, NAME_SPACE, VERSION, attributes);

        if (mo != null) {
            testDataCleaner.markFdnForCleanUp(fdn);
        }
    }

    private void createMeContextMo(final String nodeName, final String neType) {
        final String fdn = "MeContext=" + nodeName;

        final Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("MeContextId", nodeName);
        attributes.put("neType", neType);
        attributes.put("platformType", "CPP");

        final ManagedObjectDto mo = cmOperator.createMo(hostResolver.getApacheHost(), fdn, "OSS_TOP", "3.0.0", attributes);

        if (mo != null) {
            testDataCleaner.markFdnForCleanUp(fdn);
        }

    }

}
