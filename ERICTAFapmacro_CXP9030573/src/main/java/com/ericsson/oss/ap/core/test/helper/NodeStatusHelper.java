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
package com.ericsson.oss.ap.core.test.helper;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.ericsson.oss.ap.core.getters.APHostResolver;
import com.ericsson.oss.ap.core.operators.cm.CmCliOperator;
import com.ericsson.oss.ap.core.operators.cm.model.ManagedObjectDto;

/**
 * Class for reading and updating the nodes status
 * 
 * @author erobkav
 * @Since 1.5.2
 */
public class NodeStatusHelper {

    @Inject
    private CmCliOperator cmOperator;

    @Inject
    private APHostResolver hostResolver;

    private final static String STATE_ATTRIBUTE = "state";

    /**
     * Changes the state in the NodeStatus MO to that of the specified invalid
     * state for testing purposes
     * 
     * @param nodeFdn
     * @param stateString
     */
    public void setState(final String nodeFdn, final String state) {
        final Map<String, Object> nodeStatusAttributes = new HashMap<String, Object>();
        nodeStatusAttributes.put(STATE_ATTRIBUTE, state);
        cmOperator.updateMo(hostResolver.getApacheHost(), getNodeStatusFdn(nodeFdn), nodeStatusAttributes);

    }

    /**
     * Gets the state in the NodeStatus MO for the corresponding Node
     * 
     * @param nodeFdn
     */
    public String getState(final String nodeFdn) {
        return (String) getNodeStatus(nodeFdn).getAttribute(STATE_ATTRIBUTE);

    }

    /**
     * Returns the StateMo for the specified FDN
     * 
     * @param nodeFdn
     * @return
     */
    public ManagedObjectDto getNodeStatus(final String nodeFdn) {
        final ManagedObjectDto nodeStatusMO = cmOperator.findMoByFdn(hostResolver.getApacheHost(), getNodeStatusFdn(nodeFdn));
        return nodeStatusMO;

    }

    private String getNodeStatusFdn(final String nodeFdn) {
        return new StringBuilder().append(nodeFdn).append(",NodeStatus=1").toString();
    }

}
