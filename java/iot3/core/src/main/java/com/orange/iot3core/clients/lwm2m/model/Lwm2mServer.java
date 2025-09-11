/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author
    Maciej Ćmiel       <maciej.cmiel@orange.com>
    Zbigniew Krawczyk  <zbigniew2.krawczyk@orange.com>
 */
package com.orange.iot3core.clients.lwm2m.model;

import org.eclipse.leshan.core.request.BindingMode;

import java.util.EnumSet;

/**
 * Configuration for the LwM2M Server (1) Object.
 */
public class Lwm2mServer {
    private final int shortServerId;
    private final int lifetime;
    private final EnumSet<BindingMode> bindingModes;
    private final boolean notifyWhenDisable;
    private final BindingMode preferredTransport;

    /**
     * Constructor for the LwM2M Server (1) Object.
     *
     * @param shortServerId      The short server ID assigned to the target server.
     * @param lifetime           The registration lifetime in seconds with the LwM2M server.
     * @param bindingModes       The set of binding modes that specify the supported communication protocols.
     * @param notifyWhenDisable  Indicates whether the client should notify the server upon disabling.
     * @param preferredTransport The preferred binding mode specifying the supported communication protocol.
     */
    public Lwm2mServer(
            int shortServerId,
            int lifetime,
            EnumSet<BindingMode> bindingModes,
            boolean notifyWhenDisable,
            BindingMode preferredTransport
    ) {
        this.shortServerId = shortServerId;
        this.lifetime = lifetime;
        this.bindingModes = bindingModes;
        this.notifyWhenDisable = notifyWhenDisable;
        this.preferredTransport = preferredTransport;
    }

    /**
     * Constructor for the LwM2M Server (1) Object.
     *
     * @param shortServerId     The short server ID assigned to the target server.
     * @param lifetime          The registration lifetime in seconds with the LwM2M server.
     * @param bindingMode       The binding mode specifying supported communication protocol. Needed to identify the server in a multi-server setup.
     * @param notifyWhenDisable Indicates whether the client should notify the server upon disabling.
     */
    public Lwm2mServer(int shortServerId, int lifetime, String bindingMode, boolean notifyWhenDisable) {
        this(shortServerId, lifetime, EnumSet.of(BindingMode.valueOf(bindingMode)), notifyWhenDisable, BindingMode.valueOf(bindingMode));
    }

    /**
     * Constructor for the LwM2M Server (1) Object.
     *
     * @param shortServerId The short server ID assigned to the target server.
     * @param lifetime      The registration lifetime in seconds with the LwM2M server.
     * @param bindingMode   The binding mode specifying supported communication protocol. Needed to identify the server in a multi-server setup.
     */
    public Lwm2mServer(int shortServerId, int lifetime, String bindingMode) {
        this(shortServerId, lifetime, bindingMode, false);
    }

    public int getShortServerId() {
        return shortServerId;
    }

    public int getLifetime() {
        return lifetime;
    }

    public EnumSet<BindingMode> getBindingModes() {
        return bindingModes;
    }

    public boolean isNotifyWhenDisable() {
        return notifyWhenDisable;
    }

    public BindingMode getPreferredTransport() {
        return preferredTransport;
    }
}