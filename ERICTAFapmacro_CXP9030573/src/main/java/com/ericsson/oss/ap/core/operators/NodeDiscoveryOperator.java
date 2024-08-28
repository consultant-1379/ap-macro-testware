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
package com.ericsson.oss.ap.core.operators;

import java.util.Properties;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.mediation.idl.OssNodeDiscovery.NodeDiscovery;
import com.ericsson.oss.mediation.idl.OssNodeDiscovery.NodeDiscoveryHelper;
import com.ericsson.oss.mediation.idl.OssNodeDiscovery.NodeDiscoveryPackage.NotAcceptedException;

/**
 * Operator used to send node up event.
 * 
 * @author ebenmoo
 * @since 1.9.2
 */
public class NodeDiscoveryOperator {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeDiscoveryOperator.class);
    private static ORB orb;
    private static org.omg.CORBA.Object namingServiceRef;
    private static org.omg.CosNaming.NamingContext namingContext;
    private static org.omg.CosNaming.NameComponent[] wmaNodeDiscoveryPath;

    static {
        initializeORB();
        wmaNodeDiscoveryPath = new NameComponent[] { new NameComponent("com", ""), new NameComponent("ericsson", ""), new NameComponent("nms", ""),
                new NameComponent("umts", ""), new NameComponent("ranos", ""), new NameComponent("WmaNodeDiscovery", "") };
    }

    public void invokeNodeUp(final String nodeName, final String namingServiceHost, final String ipAddress) throws NotAcceptedException, NotFound,
            CannotProceed, InvalidName {
        final NodeDiscovery nodeDisc = resolveNodeDiscoveryService(namingServiceHost);
        nodeDisc.nodeUp(ipAddress);
        LOGGER.info("Node Up invoked on " + nodeName + " using IP Address: " + ipAddress);
    }

    private static void initializeORB() {
        final Properties properties = new Properties();
        properties.setProperty("vbroker.security.disable", "true");
        // NSA Properties
        properties.setProperty("vbroker.orb.warn", "2");
        properties.setProperty("vbroker.orb.debug", "true");
        properties.setProperty("vbroker.orb.logLevel", "debug");
        orb = ORB.init(new String[] {}, properties);
    }

    private NodeDiscovery resolveNodeDiscoveryService(final String namingServiceHost) throws NotFound, CannotProceed, InvalidName {
        namingServiceRef = orb.string_to_object("corbaloc::" + namingServiceHost + ":50073/NameService");
        namingContext = org.omg.CosNaming.NamingContextHelper.narrow(namingServiceRef);
        return NodeDiscoveryHelper.narrow(namingContext.resolve(wmaNodeDiscoveryPath));
    }

}