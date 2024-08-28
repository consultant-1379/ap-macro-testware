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
package com.ericsson.oss.ap.erbs.test.steps;

import static org.junit.Assert.assertFalse;
import static se.ericsson.jcat.fw.assertion.JcatAssertApi.assertEquals;
import static se.ericsson.jcat.fw.assertion.JcatAssertApi.assertNotNull;
import static se.ericsson.jcat.fw.assertion.JcatAssertApi.assertTrue;
import static se.ericsson.jcat.fw.assertion.JcatAssertApi.fail;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setSubTestStep;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setTestStepBegin;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setTestStepEnd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.oss.ap.core.getters.APHostResolver;
import com.ericsson.oss.ap.core.operators.NetsimOperator;
import com.ericsson.oss.ap.core.operators.NodeDiscoveryOperator;
import com.ericsson.oss.ap.core.operators.cm.CmCliOperator;
import com.ericsson.oss.ap.core.operators.cm.model.ManagedObjectDto;
import com.ericsson.oss.ap.core.test.helper.NodeStateTransitionTimer;
import com.ericsson.oss.ap.core.test.helper.NodeStatusHelper;
import com.ericsson.oss.ap.core.test.helper.TestDataCleaner;
import com.ericsson.oss.ap.erbs.test.model.APNodeTestInfo;
import com.ericsson.oss.ap.erbs.test.model.APProjectTestInfo;

/**
 * Contains the Integrate Test Steps.
 *
 * @author eeibky
 * @since 1.6.4
 */
public class IntegrateNodeTestSteps {

    public static final String TEST_STEP_INTEGRATE_SETUP = "integrateNode";
    public static final String TEST_STEP_LOCAL_INTEGRATE_SETUP = "localIntegrateNode";
    public static final String TEST_STEP_INTEGRATE_NODE_UP = "integrateNodeUp";
    public static final String TEST_STEP_INTEGRATE_SYNC_NODE = "integrateSyncNode";
    public static final String TEST_STEP_INTEGRATE_SET_SITE_CONFIG_COMPLETE = "integrateSetSiteConfigComplete";
    public static final String TEST_STEP_INTEGRATE_SET_S1_COMPLETE = "integrateSetS1Complete";
    public static final String TEST_STEP_VERIFY_INTEGRATE_SUCCESS = "verifyIntegrateSuccess";

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrateNodeTestSteps.class);

    private static final String SYNC_COMPLETE = "SYNCHRONIZED";
    private static final String UNSYNCHRONIZED = "UNSYNCHRONIZED";
    private static final String ORDER_COMPLETED = "ORDER_COMPLETED";
    private static final String INTEGRATION_COMPLETED = "INTEGRATION_COMPLETED";
    private static final String INTEGRATION_FAILED = "INTEGRATION_FAILED";
    private static final String SITE_CONFIG_COMPLETE = "SITE_CONFIG_COMPLETE";
    private static final String S1_COMPLETE = "S1_COMPLETE";

    private final static String ATTR_SYNCH_STATUS = "syncStatus";
    private final static String ATTR_RBS_CONFIG_LEVEL = "rbsConfigLevel";
    private final static String SUPERVISION_STATUS_ATTRIBUTE = "active";

    private final static String ADMINISTRATIVE_STATE = "administrativeState";
    private final static String UNLOCKED = "UNLOCKED";
    private final static String LOCKED = "LOCKED";

    private final static String MIM_VERSION = "6.1.60";
    private final static String MANAGED_ELEMENT_VERSION = "15.150.2";
    private final static String CM_NODE_HEARTBEAT_SUPERVISION_MO_VERSION = "1.0.1";
    private final static String CM_FUNCTION_MO_VERSION = "1.0.0";
        
    private final static int MAX_WAIT_TIME = 30;
    private final static int SLEEP_TIME = 1;

    @Inject
    private APHostResolver hostResolver;

    @Inject
    private CmCliOperator cmOperator;

    @Inject
    private TestContext context;

    @Inject
    private NodeStateTransitionTimer nodeStateTransitionTimer;

    @Inject
    private NodeStatusHelper nodeStatusHelper;

    @Inject
    private NodeDiscoveryOperator corbaOperator;

    @Inject
    private NetsimOperator netsimOperator;

    @Inject
    private TestDataCleaner testDataCleaner;

    /**
     * Setup Netsim Nodes required for Order Node integrate workflow Tests.
     * <p>
     * Requires the following test context attributes to be set
     * <ul>
     * <li><code>TEST_CONTEXT_KEY_PROJECTINFO</code></li> - the <code>APProjectTestInfo</code> for an imported project
     * </ul>
     * </p>
     */
    @TestStep(id = TEST_STEP_INTEGRATE_SETUP)
    public void integrateSetup(@Input("simulation") final String simulation, @Input("netsimRestoreFile") final String netsimRestoreFile) {
        final APProjectTestInfo projectInfo = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_PROJECTINFO);
        prepareNetsimNodes(projectInfo, simulation, netsimRestoreFile);
    }

    /**
     * Setup MOs required for local Order Node integrate workflow Tests.
     * <p>
     * Requires the following test context attributes to be set
     * <ul>
     * <li><code>TEST_CONTEXT_KEY_PROJECTINFO</code></li> - the <code>APProjectTestInfo</code> for an imported project
     * </ul>
     * </p>
     */
    @TestStep(id = TEST_STEP_LOCAL_INTEGRATE_SETUP)
    public void localIntegrateSetup() {
        final APProjectTestInfo projectInfo = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_PROJECTINFO);
        final List<APNodeTestInfo> nodesToIntegrate = projectInfo.getNodes();
        createMediationMos(nodesToIntegrate);
    }

    /**
     * Initiate node up for all Nodes in a Project.
     * <p>
     * Requires the following test context attributes to be set
     * <ul>
     * <li><code>TEST_CONTEXT_KEY_PROJECTINFO</code></li> - the <code>APProjectTestInfo</code> for an imported project
     * </ul>
     * </p>
     */
    @TestStep(id = TEST_STEP_INTEGRATE_NODE_UP)
    public void initiateNodeUp() {
        final APProjectTestInfo projectInfo = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_PROJECTINFO);
        for (final APNodeTestInfo nodeInfo : projectInfo.getNodes()) {
            final String nodeName = nodeInfo.getName();
            final String nodeIp = nodeInfo.getIpAddress();
            final String nameServiceIp = hostResolver.getNamingServiceHost().getIp();

            try {
                corbaOperator.invokeNodeUp(nodeName, nameServiceIp, nodeIp);
                testDataCleaner.markFdnToDisableSupervision("NetworkElement=" + nodeName + ",CmNodeHeartbeatSupervision=1");
            } catch (Exception e) {
                fail("Node Up invocation failed with the following error message: " + e.getMessage());
            }
        }
    }

    /**
     * Synch all Nodes in a Project. This sets the CmFunction syncStatus to SYNCHRONIZED.
     * <p>
     * Requires the following test context attributes to be set
     * <ul>
     * <li><code>TEST_CONTEXT_KEY_PROJECTINFO</code></li> - the <code>APProjectTestInfo</code> for an imported project
     * </ul>
     * </p>
     */
    @TestStep(id = TEST_STEP_INTEGRATE_SYNC_NODE)
    public void setSynchStatus() {
        final APProjectTestInfo projectInfo = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_PROJECTINFO);

        for (final APNodeTestInfo nodeInfo : projectInfo.getNodes()) {
            nodeStateTransitionTimer.waitForStateTransitionToNewState(nodeInfo.getApNodeFdn(), ORDER_COMPLETED);

            final String nodeName = nodeInfo.getName();
            final String fdn = "NetworkElement=" + nodeName + ",CmFunction=1";
            setMoAttributes(fdn, ATTR_SYNCH_STATUS, SYNC_COMPLETE);
        }
    }

    /**
     * Set RbsConfiguration RbsConfigLevel = SITE_CONFIG_COMPLETE.
     * <p>
     * Requires the following test context attributes to be set
     * <ul>
     * <li><code>TEST_CONTEXT_KEY_PROJECTINFO</code></li> - the <code>APProjectTestInfo</code> for an imported project
     * </ul>
     * </p>
     */
    @TestStep(id = TEST_STEP_INTEGRATE_SET_SITE_CONFIG_COMPLETE)
    public void setRBSConfigLevelToSiteConfigComplete() {
        sleep(10);
        setRbsConfigLevelToSiteConfigComplete();
    }

    /**
     * Set RbsConfiguration RbsConfigLevel = S1_COMPLETE.
     * <p>
     * Requires the following test context attributes to be set
     * <ul>
     * <li><code>TEST_CONTEXT_KEY_PROJECTINFO</code></li> - the <code>APProjectTestInfo</code> for an imported project
     * </ul>
     * </p>
     */
    @TestStep(id = TEST_STEP_INTEGRATE_SET_S1_COMPLETE)
    public void setRBSConfigLevelToS1Complete() {
        sleep(10);
        waitUntilAllNodesChangeFromState(ORDER_COMPLETED);
        setRbsConfigLevelToS1Complete();
    }

    /**
     * Verify Integration Success.
     * <p>
     * Requires the following test context attributes to be set
     * <ul>
     * <li><code>TEST_CONTEXT_KEY_PROJECTINFO</code></li> - the <code>APProjectTestInfo</code> for an imported project
     * </ul>
     * </p>
     */
    @TestStep(id = TEST_STEP_VERIFY_INTEGRATE_SUCCESS)
    public void verifyNodesSuccessfullyIntegrated(@Input("success") final boolean success) {

        final APProjectTestInfo projectInfo = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_PROJECTINFO);

        for (final APNodeTestInfo nodeInfo : projectInfo.getNodes()) {
            if (success) {
                verifySuccess(nodeInfo);
            } else {
                verifyFailure(nodeInfo);
            }
        }
        setTestStepEnd();
    }

    private void verifySuccess(final APNodeTestInfo nodeInfo) {
        nodeStateTransitionTimer.waitForStateTransitionToNewState(nodeInfo.getApNodeFdn(), INTEGRATION_COMPLETED);

        final String nodeName = nodeInfo.getName();

        setTestStepBegin("Verify Integration for: " + nodeName);

        verifyNodeState(nodeName);
        verifyImportRnTnConfiguration(nodeInfo);
        verifyUnlockCells(nodeInfo);
        verifyArtifactCleanUp(nodeInfo);
        verifySuccessStatus(nodeInfo);
    }

    private void verifyFailure(final APNodeTestInfo nodeInfo) {
        nodeStateTransitionTimer.waitForStateTransitionToNewState(nodeInfo.getApNodeFdn(), INTEGRATION_FAILED);

        verifyNoImportRnTnConfiguration(nodeInfo);
        verifyFailedStatus(nodeInfo);
    }

    private void verifyNoImportRnTnConfiguration(final APNodeTestInfo nodeInfo) {
        assertFalse(verifyCellCreated(nodeInfo.getName()));
        assertFalse(verifySctpMoCreated(nodeInfo.getName()));
    }

    private void verifyFailedStatus(final APNodeTestInfo nodeInfo) {
        setSubTestStep("Verifying node status");
        assertEquals(INTEGRATION_FAILED, nodeStatusHelper.getState(nodeInfo.getApNodeFdn()));
    }

    private void createMediationMos(final List<APNodeTestInfo> nodesToIntegrate) {

        for (final APNodeTestInfo nodeInfo : nodesToIntegrate) {
            final String nodeName = nodeInfo.getName();
            setTestStepBegin("Create MOs for local environment for node: " + nodeName);

            createCmFunction(nodeName);
            createCmHeartbeatSupervision(nodeName);
            createManagedElement(nodeName);
            createENodeBFunction(nodeName);
            createEUtranCellFDD(nodeName);
            createNodeManagementFunction(nodeName);
            createRbsConfiguration(nodeName);
        }
        setTestStepEnd();
    }

    private void createEUtranCellFDD(final String nodeName) {
        setSubTestStep("Create EUtranCellFDD MO");

        final Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("physicalLayerSubCellId", 1);
        attributes.put("EUtranCellFDDId", "1");
        attributes.put("earfcnul", 19000);
        attributes.put("cellId", 1);
        attributes.put("tac", 1);
        attributes.put("physicalLayerCellIdGroup", 40);
        attributes.put("earfcndl", 1000);
        attributes.put("sectorFunctionRef", "ref1");

        final String fdn = "MeContext=" + nodeName + ",ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=Cell_1";
        final ManagedObjectDto mo = cmOperator.createMo(hostResolver.getApacheHost(), fdn, "ERBS_NODE_MODEL", MIM_VERSION, attributes);
        assertNotNull("Error creating " + fdn, mo);
    }

    private void createManagedElement(final String nodeName) {
        setSubTestStep("Create ManagedElement MO");

        final Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("ManagedElementId", 1);
        attributes.put("platformType", "CPP");
        attributes.put("neType", "ERBS");

        final String fdn = "MeContext=" + nodeName + ",ManagedElement=1";
        final ManagedObjectDto mo = cmOperator.createMo(hostResolver.getApacheHost(), fdn, "CPP_NODE_MODEL", MANAGED_ELEMENT_VERSION, attributes);
        assertNotNull("Error creating " + fdn, mo);
    }

    private void createENodeBFunction(final String nodeName) {
        setSubTestStep("Create ENodeBFunction MO");

        final Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("ENodeBFunctionId", 1);

        attributes.put("eNodeBPlmnId", "(mcc=1,mnc=1,mncLength=2)");

        final String fdn = "MeContext=" + nodeName + ",ManagedElement=1,ENodeBFunction=1";
        final ManagedObjectDto mo = cmOperator.createMo(hostResolver.getApacheHost(), fdn, "ERBS_NODE_MODEL", MIM_VERSION, attributes);
        assertNotNull("Error creating " + fdn, mo);
    }

    private void createCmHeartbeatSupervision(final String nodeName) {
        setSubTestStep("Create CmHeartbeatSupervision MO");

        final Map<String, Object> cmHeartbeatAttributes = new HashMap<String, Object>();
        cmHeartbeatAttributes.put("CmNodeHeartbeatSupervisionId", 1);

        final String cmHeartbeatFdn = "NetworkElement=" + nodeName + ",CmNodeHeartbeatSupervision=1";
        final ManagedObjectDto cmHeartbeatMo = cmOperator.createMo(hostResolver.getApacheHost(), cmHeartbeatFdn, "OSS_NE_CM_DEF",
                CM_NODE_HEARTBEAT_SUPERVISION_MO_VERSION, cmHeartbeatAttributes);
        assertNotNull("Error creating " + cmHeartbeatFdn, cmHeartbeatMo);
    }

    private void createCmFunction(final String nodeName) {
        setSubTestStep("Create CmFunction MO");

        final Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("CmFunctionId", 1);

        final String fdn = "NetworkElement=" + nodeName + ",CmFunction=1";
        final ManagedObjectDto mo = cmOperator.createMo(hostResolver.getApacheHost(), fdn, "OSS_NE_CM_DEF", CM_FUNCTION_MO_VERSION, attributes);
        assertNotNull("Error creating " + fdn, mo);
    }

    private void createNodeManagementFunction(final String nodeName) {
        setSubTestStep("Create NodeManagementFunction MO");

        final Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("NodeManagementFunctionId", 1);

        final String fdn = "MeContext=" + nodeName + ",ManagedElement=1,NodeManagementFunction=1";
        final ManagedObjectDto mo = cmOperator.createMo(hostResolver.getApacheHost(), fdn, "ERBS_NODE_MODEL", MIM_VERSION, attributes);
        assertNotNull("Error creating " + fdn, mo);
    }

    private void createRbsConfiguration(final String nodeName) {
        setSubTestStep("Create RbsConfiguration MO");

        final Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("RbsConfigurationId", 1);

        final String fdn = "MeContext=" + nodeName + ",ManagedElement=1,NodeManagementFunction=1,RbsConfiguration=1";
        final ManagedObjectDto mo = cmOperator.createMo(hostResolver.getApacheHost(), fdn, "ERBS_NODE_MODEL", MIM_VERSION, attributes);
        assertNotNull("Error creating " + fdn, mo);
    }

    private void setRbsConfigLevelToSiteConfigComplete() {
        final APProjectTestInfo projectInfo = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_PROJECTINFO);

        for (final APNodeTestInfo nodeInfo : projectInfo.getNodes()) {
            final String nodeName = nodeInfo.getName();
            final String rbsConfigurationFdn = "MeContext=" + nodeName + ",ManagedElement=1,NodeManagementFunction=1,RbsConfiguration=1";
            verifyNodeSyncedSuccessfully(nodeName, rbsConfigurationFdn);
            setMoAttributes(rbsConfigurationFdn, ATTR_RBS_CONFIG_LEVEL, SITE_CONFIG_COMPLETE);
            resetSyncStatusAttributeIfNodeIsAlreadySynced(nodeInfo);
        }
    }

    private void setRbsConfigLevelToS1Complete() {
        final APProjectTestInfo projectInfo = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_PROJECTINFO);

        for (final APNodeTestInfo nodeInfo : projectInfo.getNodes()) {
            final String nodeName = nodeInfo.getName();
            final String fdn = "MeContext=" + nodeName + ",ManagedElement=1,NodeManagementFunction=1,RbsConfiguration=1";
            setMoAttributes(fdn, ATTR_RBS_CONFIG_LEVEL, S1_COMPLETE);
        }
    }

    private void setMoAttributes(final String fdn, final String attributeName, final String attributeValue) {
        setTestStepBegin("Set attribute " + attributeName + " = " + attributeValue + " for: " + fdn);

        final Map<String, Object> attribute = new HashMap<String, Object>();
        attribute.put(attributeName, attributeValue);
        cmOperator.updateMo(hostResolver.getApacheHost(), fdn, attribute);
    }

    private void verifyImportRnTnConfiguration(final APNodeTestInfo nodeInfo) {
        setSubTestStep("Verify import of Rn and Tn configuration");
        if (isImportRnFileSuppliedForNode(nodeInfo)) {
            assertTrue(verifyCellCreated(nodeInfo.getName()));
        }

        if (isImportTnFileSuppliedForNode(nodeInfo)) {
            assertTrue(verifySctpMoCreated(nodeInfo.getName()));
        }
    }

    private boolean isImportTnFileSuppliedForNode(final APNodeTestInfo nodeInfo) {
        return nodeInfo.getTransport() != null && !nodeInfo.getTransport().isEmpty();
    }

    private boolean isImportRnFileSuppliedForNode(final APNodeTestInfo nodeInfo) {
        return nodeInfo.getRadio() != null && !nodeInfo.getRadio().isEmpty();
    }

    private boolean verifyCellCreated(final String nodeName) {
        final String expectedEUtranCellFdn = "MeContext=" + nodeName + ",ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=EUtranCellFDD_1";
        return cmOperator.findMoByFdn(hostResolver.getApacheHost(), expectedEUtranCellFdn) != null;
    }

    private boolean verifySctpMoCreated(final String nodeName) {
        final String expectedSctpFdn = "MeContext=" + nodeName + ",ManagedElement=1,TransportNetwork=1,Sctp=1";
        return cmOperator.findMoByFdn(hostResolver.getApacheHost(), expectedSctpFdn) != null;
    }

    private void verifyNodeState(final String nodeName) {
        setSubTestStep("verifyNodeState");

        final String rbsConfigFdn = "MeContext=" + nodeName + ",ManagedElement=1,NodeManagementFunction=1,RbsConfiguration=1";
        final ManagedObjectDto mo = cmOperator.findMoByFdn(hostResolver.getApacheHost(), rbsConfigFdn);
        final String rbsConfigLevel = mo.getAttribute("rbsConfigLevel");
        assertEquals("Integration Not Complete for node " + nodeName, "INTEGRATION_COMPLETE", rbsConfigLevel);
    }

    private void verifyUnlockCells(final APNodeTestInfo nodeInfo) {
        setSubTestStep("verify EUTranCellFDD UnlockCells");

        final String nodeName = nodeInfo.getName();

        final String meContextFdn = "MeContext=" + nodeName;
        final List<ManagedObjectDto> eUtranCellFDDs = cmOperator.getMosByType(hostResolver.getApacheHost(), "OSS_TOP", "EUtranCellFDD", meContextFdn,
                "ManagedElement,ENodeBFunction,EUtranCellFDD");

        for (final ManagedObjectDto eutranCell : eUtranCellFDDs) {

            final String lockState = eutranCell.getAttribute(ADMINISTRATIVE_STATE).toString();
            final boolean unlockCells = Boolean.parseBoolean(nodeInfo.getUnlockCells());
            if (unlockCells) {
                assertEquals("Cell " + eutranCell.getName() + " should be unlocked", UNLOCKED, lockState);
            } else {
                assertEquals("Cell " + eutranCell.getName() + " should be locked", LOCKED, lockState);
            }

            setSubTestStep("Verify Log entry administrativeState=" + lockState);
        }
    }

    private void verifyArtifactCleanUp(final APNodeTestInfo nodeInfo) {
        setSubTestStep("Verify Artifacts deleted");
        final List<ManagedObjectDto> artifacts = cmOperator.getMosByType(hostResolver.getApacheHost(), "ap", "NodeArtifact", nodeInfo.getApNodeFdn(),
                "NodeArtifactContainer,NodeArtifact");
        assertTrue(nodeInfo.getApNodeFdn() + " artifacts not deleted after Integration.", artifacts.isEmpty());
    }

    private void verifySuccessStatus(final APNodeTestInfo nodeInfo) {
        setSubTestStep("Verifying node status");
        assertEquals(INTEGRATION_COMPLETED, nodeStatusHelper.getState(nodeInfo.getApNodeFdn()));
    }

    private void sleep(final long timeInSeconds) {
        try {
            LOGGER.info("Sleeping for {} seconds...", timeInSeconds);
            Thread.sleep(timeInSeconds * 1000);
        } catch (final java.lang.InterruptedException ie) {
        }
    }

    private void waitUntilAllNodesChangeFromState(final String state) {
        final APProjectTestInfo projectInfo = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_PROJECTINFO);
        for (final APNodeTestInfo nodeInfo : projectInfo.getNodes()) {
            nodeStateTransitionTimer.waitForStateTransitionFromState(nodeInfo.getApNodeFdn(), state);
        }
    }

    private void prepareNetsimNodes(final APProjectTestInfo projectInfo, final String simulation, final String netsimRestoreFile) {
        for (final APNodeTestInfo nodeInfo : projectInfo.getNodes()) {
            final String nodeIp = nodeInfo.getIpAddress();
            try {
                netsimOperator.prepareNetsimNeForIntegration(nodeIp, simulation, netsimRestoreFile);
            } catch (Exception e) {
                fail("Netsim Node preperation failed with the following error message: " + e.getMessage());
            }
        }
    }
    
    private void verifyNodeSyncedSuccessfully(final String nodeName, final String fdn){
        if(cmOperator.findMoByFdn(hostResolver.getApacheHost(), fdn) == null){
            final String cmFunctionFdn = "NetworkElement=" + nodeName + ",CmFunction=1";
            final String cmHeartbeatSupervisionFdn = "NetworkElement=" + nodeName + ",CmNodeHeartbeatSupervision=1";
            resyncNode(cmHeartbeatSupervisionFdn);

            final long commandStartTime = getCurrentTimeInSeconds();
            while (commandExecutionTimeNotExceeded(commandStartTime)) {
                if (verifySyncStatusIsSynchronized(cmFunctionFdn)) {
                    LOGGER.info(nodeName + " has been synced successfully.");
                    return;
                }
                LOGGER.info("Verifying that " + nodeName + " has been synced successfully.");
                sleep(SLEEP_TIME);
            }
            handleSyncFailure(nodeName, cmFunctionFdn, cmHeartbeatSupervisionFdn);            
        }     
    }

    private void resyncNode(final String cmHeartbeatSupervisionFdn) {
        final Map<String, Object> cmHeartbeatAttributes = new HashMap<>();
        cmHeartbeatAttributes.put(SUPERVISION_STATUS_ATTRIBUTE, false);
        cmOperator.updateMo(hostResolver.getApacheHost(), cmHeartbeatSupervisionFdn, cmHeartbeatAttributes);
        cmHeartbeatAttributes.clear();
        cmHeartbeatAttributes.put(SUPERVISION_STATUS_ATTRIBUTE, true);
        cmOperator.updateMo(hostResolver.getApacheHost(), cmHeartbeatSupervisionFdn, cmHeartbeatAttributes);
    }
    
    private void handleSyncFailure(final String nodeName, final String cmFunctionFdn, final String cmHeartbeatSupervisionFdn){
        final ManagedObjectDto cmHeartbeatSupervisionMo = cmOperator.findMoByFdn(hostResolver.getApacheHost(), cmHeartbeatSupervisionFdn);
        final ManagedObjectDto cmFunctionMo = cmOperator.findMoByFdn(hostResolver.getApacheHost(), cmFunctionFdn);
        final String supervisionStatus = cmHeartbeatSupervisionMo.getAttribute(SUPERVISION_STATUS_ATTRIBUTE);
        final String syncStatus = cmFunctionMo.getAttribute(ATTR_SYNCH_STATUS);   
        fail(nodeName + " failed to sync, supervision status: " + supervisionStatus + ", sync status: " + syncStatus);
    }
    
    private void resetSyncStatusAttributeIfNodeIsAlreadySynced(final APNodeTestInfo nodeInfo) {    
        if (nodeStatusHelper.getState(nodeInfo.getApNodeFdn()).equals(ORDER_COMPLETED)) {
            final String nodeName = nodeInfo.getName();
            final String cmFunctionFdn = "NetworkElement=" + nodeName + ",CmFunction=1";
            
            if (verifySyncStatusIsSynchronized(cmFunctionFdn)) {
                setMoAttributes(cmFunctionFdn, ATTR_SYNCH_STATUS, UNSYNCHRONIZED);
                setMoAttributes(cmFunctionFdn, ATTR_SYNCH_STATUS, SYNC_COMPLETE);
                LOGGER.info(nodeName + " syncStatus attribute has been reset successfully, in order to resend AVC notifcation.");
            }
        }
    }
    
    private boolean verifySyncStatusIsSynchronized(final String cmFunctionFdn) {
        final ManagedObjectDto cmFunctionMo = cmOperator.findMoByFdn(hostResolver.getApacheHost(), cmFunctionFdn);
        final String syncStatus = cmFunctionMo.getAttribute(ATTR_SYNCH_STATUS);
        return syncStatus.equals(SYNC_COMPLETE);
    }
    
    private boolean commandExecutionTimeNotExceeded(final long commandStartTime) {
        return (getCurrentTimeInSeconds() - commandStartTime) < MAX_WAIT_TIME;
    }
    
    private long getCurrentTimeInSeconds(){
        return System.currentTimeMillis() / 1000;
    }

}
