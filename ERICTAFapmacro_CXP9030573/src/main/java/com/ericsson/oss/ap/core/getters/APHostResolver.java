package com.ericsson.oss.ap.core.getters;

import javax.inject.Singleton;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.nms.host.HostConfigurator;

/**
 * @since 1.0.10
 */
@Singleton
public class APHostResolver {

    private Host sc1Host;
    private Host netsimHost;
    private Host apacheHost;
    private Host namingServiceHost;

    public Host getApacheHost() {
        if (apacheHost == null) {
            apacheHost = HostConfigurator.getApache();
        }
        return apacheHost;
    }

    public Host getSC1Host() {
        if (sc1Host == null) {
            sc1Host = HostConfigurator.getSC1();
        }
        return sc1Host;
    }

    public Host getNetsimHost() {
        if (netsimHost == null) {
            netsimHost = HostConfigurator.getNetsim();
        }
        return netsimHost;
    }

    public Host getNamingServiceHost() {
        if (namingServiceHost == null) {
            namingServiceHost = HostConfigurator.getSouthboundNamingServiceHost();
        }
        return namingServiceHost;
    }
}