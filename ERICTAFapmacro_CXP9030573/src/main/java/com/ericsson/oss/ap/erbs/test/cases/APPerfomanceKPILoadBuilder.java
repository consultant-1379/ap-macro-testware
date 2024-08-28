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
package com.ericsson.oss.ap.erbs.test.cases;

import javax.inject.Inject;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.oss.ap.core.getters.APHostResolver;
import com.ericsson.oss.ap.core.operators.APServiceOperator;
import com.ericsson.oss.ap.core.operators.UserManagementOperator;
import com.ericsson.oss.ap.core.operators.cm.CmCliOperator;
import com.ericsson.oss.ap.core.operators.cm.model.ManagedObjectDto;
import com.ericsson.oss.ap.core.test.helper.TestDataCleaner;
import com.ericsson.oss.ap.core.test.model.APFileInfo;
import com.ericsson.oss.ap.core.test.performance.PerformanceKPIThresholds;
import com.ericsson.oss.ap.erbs.test.data.APImportERBSZipTestData;
import com.ericsson.oss.ap.erbs.test.model.APProjectTestInfo;

/**
 * Prepares the load (number of projects and nodes in the data base) required to run the Performance KPI test. Not a testcase as such, but it needs to
 * be run at the start of the Performance KPI test suite to create the load.
 * <p>
 * Cannot set this up as part of a {@literal @}BeforeSuite or {@literal @}BeforeGroups, as we can't get a handle yet on the APServiceOperator required
 * to import the data (they haven't been loaded in the operatorRegistry yet).
 * <p>
 * Although Context.CLI is specified for tests these are actually using the Common CLI through its REST service. Context.Rest will be used for the AP
 * Rest service which already exists but is temporarily not being used.
 * <p>
 * <b>NOTE:</b> To be included as first class in the APMacroPerformanceKPISuite.xml
 * 
 * @since 1.2.1
 */
public class APPerfomanceKPILoadBuilder extends TorTestCaseHelper {

    @Inject
    private UserManagementOperator createUserOperator;

    @Inject
    private OperatorRegistry<APServiceOperator> operatorRegistry;

    @Inject
    private CmCliOperator cmOperator;

    @Inject
    private APHostResolver hostResolver;

    @Inject
    private TestDataCleaner testDataCleaner;

    @BeforeSuite(alwaysRun = true)
    public void preTAFSetup() {
        createUserOperator.createTAFUserInNonLocalEnv();
    }

    /**
     * Preparing the Performance KPI Project Load.
     */
    @TestId(id = "Performance KPI preparation", title = "Prepares the load (number of projects and nodes in the data base) required to run the Performance KPI test.")
    @Context(context = Context.CLI)
    @Test(groups = { "PerformanceKPI" })
    public void buildPerformanceKPILoad() {

        setTestStep("Preparing Projects for Performance KPI Load");
        for (int count = 1; count <= PerformanceKPIThresholds.REQUIRED_PROJECT_LOAD; count++) {
            final APProjectTestInfo projectInfo = importLoadProject();
            final String projectFdn = projectInfo.getProjectFdn();
            final ManagedObjectDto projectMo = cmOperator.findMoByFdn(hostResolver.getApacheHost(), projectFdn);

            if (projectMo != null) {
                testDataCleaner.markFdnForCleanUp(projectFdn);
            }
        }
    }

    private APProjectTestInfo importLoadProject() {
        final Object[][] provider = APImportERBSZipTestData.createDataForProvider("PerformanceKPI");
        final byte[] zipContents = (byte[]) provider[0][0];
        final APProjectTestInfo projectInfo = (APProjectTestInfo) provider[0][1];
        final APServiceOperator operator = operatorRegistry.provide(APServiceOperator.class);
        final APFileInfo fileInfo = new APFileInfo();
        final String projectName = projectInfo.getAttribute("fileName").toString();
        fileInfo.setName(projectName);
        fileInfo.setContents(zipContents);
        operator.importArchive(fileInfo);

        return projectInfo;
    }

    @AfterSuite(alwaysRun = true)
    public void teardown() {
        testDataCleaner.performCleanup();
    }
}
