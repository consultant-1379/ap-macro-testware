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

import javax.inject.Inject;

import org.apache.log4j.Logger;

/**
 * Provides blocking methods to wait for node state transitions.
 * <p>
 * Timer is configured with a default wait interval set to 3 seconds and max retires set to 20.
 * </p>
 * @since 1.5.3
 *
 */
public class NodeStateTransitionTimer {

    private final static int MAX_EXECUTION_TIME = 60000;
    private final static int SLEEP_TIME = 3000;

    @Inject
    private NodeStatusHelper nodeStatusHelper;

    private final Logger logger = Logger.getLogger(getClass());

    /**
     * Blocks until the node state has changed from the specified fromState or
     * the max wait time has been reached.
     * @param nodeFdn the ap node fdn
     * @param fromState the from state
     */
    public void waitForStateTransitionFromState(final String nodeFdn, final String fromState) {
        final long commandStartTime = System.currentTimeMillis();
        logger.info("Waiting for transition from state " + fromState + " for node " + nodeFdn);

        while (commandExecutionTimeNotExceeded(commandStartTime)) {
            final String currentState = nodeStatusHelper.getState(nodeFdn);
            if (currentState.equals(fromState)) {
                logger.info("Sleeping. Current State = " + currentState);
                sleep();
            } else {
                logger.info("Returning, state changed  " + fromState + "->" + currentState);
                return;
            }
        }
        logger.info("Returning, no state change from " + fromState + " in " + MAX_EXECUTION_TIME + "ms");
    }

    /**
     * Blocks until the node state has changed to a specified toState or
     * the max wait time has been reached.
     * @param nodeFdn the ap node fdn
     * @param toState the to state
     */
    public void waitForStateTransitionToNewState(final String nodeFdn, final String toState) {
        final long commandStartTime = System.currentTimeMillis();
        logger.info("Waiting for transition to state " + toState + " for node " + nodeFdn);
        String currentState = "";

        while (commandExecutionTimeNotExceeded(commandStartTime)) {
            currentState = nodeStatusHelper.getState(nodeFdn);
            if (currentState.equals(toState)) {
                logger.info("Returning, state changed to " + toState);
                return;
            } else {
                logger.info("Sleeping. Current State = " + currentState);
                sleep();
            }
        }
        logger.info("Returning, no state change to " + toState + " from current state " + currentState + " in " + MAX_EXECUTION_TIME + "ms");
    }

    private boolean commandExecutionTimeNotExceeded(final long commandStartTime) {
        return (System.currentTimeMillis() - commandStartTime) < MAX_EXECUTION_TIME;
    }

    private void sleep(){
        try {
            Thread.sleep(SLEEP_TIME);
        } catch (final InterruptedException e) {
            // do nothing
        }
    }
}
