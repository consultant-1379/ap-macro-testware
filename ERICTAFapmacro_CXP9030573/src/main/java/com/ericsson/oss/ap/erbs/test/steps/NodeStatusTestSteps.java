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
package com.ericsson.oss.ap.erbs.test.steps;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import org.supercsv.io.CsvMapReader;
import org.supercsv.prefs.CsvPreference;
import org.testng.Assert;

import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.oss.ap.core.operators.APServiceOperator;
import com.ericsson.oss.ap.core.test.helper.NodeStateTransitionTimer;
import com.ericsson.oss.ap.core.test.model.CommandResult;
import com.ericsson.oss.ap.core.test.model.ResultEntity;
import com.ericsson.oss.ap.erbs.test.model.APNodeTestInfo;
import com.ericsson.oss.ap.erbs.test.model.APProjectTestInfo;

/**
 * Contains the <code><b>ap status</b></code> test steps for the following commands:
 * <ul>
 * <li>ap status -n nodeName</li>
 * </ul>
 *
 * @author eshemeh
 * @since 1.14.3
 */
public class NodeStatusTestSteps extends TorTestCaseHelper {

    public static final String TEST_STEP_VERIFY_STATUS_ENTRIES = "verifyStatusEntries";
    public static final String TEST_STEP_UNORDER_NODE = "unorderNode";

    private final static String CSV_DATA_FILE = "/data/apNodeStatusEntries.csv";
    private final static String STATUS_KEY = "Status";
    private final static String TASK_KEY = "Task";

    @Inject
    private OperatorRegistry<APServiceOperator> operatorRegistry;

    @Inject
    private NodeStateTransitionTimer nodeStateTransitionTimer;

    @Inject
    private TestContext context;

    /**
     * Executes the command to view the node's status, <code><b>ap status -n {@literal <}nodeName{@literal >}</b></code>.
     * <p>
     * Verifies the status entries are valid.
     */
    @TestStep(id = TEST_STEP_VERIFY_STATUS_ENTRIES)
    public void verifyNodeStatusEntries(@Input("usecase") final String statusUseCase) {
        final APProjectTestInfo projectInfo = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_PROJECTINFO);
        final String nodeName = projectInfo.getNodes().get(0).getName();

        setTestStepBegin("Verify status entries for node: " + nodeName);
        final APServiceOperator operator = operatorRegistry.provide(APServiceOperator.class);
        final CommandResult result = operator.viewNodeStatus(nodeName);

        final ResultEntity parentEntity = result.getResultEntities().get(0);
        final List<ResultEntity> childEntities = parentEntity.getChildren();

        final Map<String, String> statusResponse = new HashMap<>();
        for (final ResultEntity entity : childEntities) {
            final Map<String, String> attributes = entity.getAttributes();
            final String task = attributes.get(TASK_KEY);
            final String status = attributes.get(STATUS_KEY);
            statusResponse.put(task, status);
        }

        final Map<String, String> expectedResponse = loadStatusEntriesIntoMap(statusUseCase);
        Assert.assertEquals(statusResponse, expectedResponse);
    }

    /**
     * Test step to unorder node.
     * <p>
     * TODO: Temporary, and should be removed once unorder TAF test is updated to use scenarios.
     */
    @TestStep(id = TEST_STEP_UNORDER_NODE)
    public void unorderNode() {
        final APProjectTestInfo projectInfo = context.getAttribute(ImportProjectTestSteps.TEST_CONTEXT_KEY_PROJECTINFO);
        final APNodeTestInfo orderedNode = projectInfo.getNodes().get(0);
        final String nodeFdn = orderedNode.getApNodeFdn();

        nodeStateTransitionTimer.waitForStateTransitionFromState(nodeFdn, "ORDER_STARTED");

        final APServiceOperator operator = operatorRegistry.provide(APServiceOperator.class);
        operator.unorder(orderedNode.getName());
        nodeStateTransitionTimer.waitForStateTransitionFromState(nodeFdn, "UNORDER_STARTED");
    }

    private Map<String, String> loadStatusEntriesIntoMap(final String statusUseCase) {
        final InputStream csvStream = NodeStatusTestSteps.class.getResourceAsStream(CSV_DATA_FILE);
        final InputStreamReader reader = new InputStreamReader(csvStream);
        Map<String, String> csvTestData = new TreeMap<String, String>();

        try (CsvMapReader mapReader = new CsvMapReader(reader, CsvPreference.STANDARD_PREFERENCE)) {
            final String[] headers = mapReader.getHeader(true);
            while ((csvTestData = mapReader.read(headers)) != null) {
                if (csvTestData.containsValue(statusUseCase)) {
                    csvTestData.values().removeAll(Collections.singleton(null));
                    csvTestData.values().remove(statusUseCase);
                    return csvTestData;
                }
            }
        } catch (final Exception e) {
            fail("An exception occurred while loading the status entries into a map.");
        }
        return csvTestData;
    }
}