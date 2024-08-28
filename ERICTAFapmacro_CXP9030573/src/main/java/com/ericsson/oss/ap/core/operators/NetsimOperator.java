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
package com.ericsson.oss.ap.core.operators;

import javax.inject.Inject;
import javax.jms.IllegalStateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommandHandler;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NeGroup;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;
import com.ericsson.oss.ap.core.getters.APHostResolver;

/**
 * Operator used to interact with Netsim.
 * 
 * @author ebenmoo
 * @since 1.13.4
 */
public class NetsimOperator {

    @Inject
    private APHostResolver hostResolver;

    private final static int MAX_WAIT_TIME = 10;
    private final static int SLEEP_TIME = 1;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NetsimOperator.class);

    public void prepareNetsimNeForIntegration(final String nodeIp, final String simulation, final String netsimRestoreFile)
            throws IllegalStateException {
        final NetworkElement netsimNode = getNetsimNe(nodeIp, simulation);
        restoreNetsimNeState(netsimNode, netsimRestoreFile);
        checkIfNeIsStarted(netsimNode);
    }

    private NetworkElement getNetsimNe(final String nodeIp, final String simulation) throws IllegalStateException {
        final NetSimCommandHandler netsimHandler = NetSimCommandHandler.getInstance(hostResolver.getNetsimHost());
        final NeGroup netsimNes = netsimHandler.getSimulationNEs(simulation);
        for (final NetworkElement networkElement : netsimNes.getNetworkElements()) {
            if (networkElement.getIp().equals(nodeIp)) {
                return networkElement;
            }
        }
        throw new IllegalStateException(String.format("Cannot find Netsim NE with IP Address: %s in Netsim Simulation: %s", nodeIp, simulation));
    }

    private void restoreNetsimNeState(final NetworkElement netsimNode, final String netsimRestoreFile) {
        netsimNode.exec(NetSimCommands.stop());
        LOGGER.info(netsimNode.getName() + " has been stopped.");
        netsimNode.exec(NetSimCommands.restorenedatabase(netsimRestoreFile));
        LOGGER.info(netsimNode.getName() + " has been restored.");
        netsimNode.exec(NetSimCommands.start());
    }

    private void checkIfNeIsStarted(final NetworkElement netsimNode) throws IllegalStateException {
        final long commandStartTime = getCurrentTimeInSeconds();
        while (commandExecutionTimeNotExceeded(commandStartTime)) {
            if (netsimNode.isStarted()) {
                LOGGER.info(netsimNode.getName() + " has been started.");
                return;
            }
            sleep(SLEEP_TIME);
        }
        throw new IllegalStateException(netsimNode.getName() + " has failed to start.");
    }

    private boolean commandExecutionTimeNotExceeded(final long commandStartTime) {
        return (getCurrentTimeInSeconds() - commandStartTime) < MAX_WAIT_TIME;
    }

    private void sleep(final long timeInSeconds) {
        try {
            LOGGER.info("Sleeping for {} seconds...", timeInSeconds);
            Thread.sleep(timeInSeconds * 1000);
        } catch (final InterruptedException ie) {
        }
    }
    
    private long getCurrentTimeInSeconds(){
        return System.currentTimeMillis() / 1000;
    }

}
