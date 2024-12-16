/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author
    Maciej Ćmiel       <maciej.cmiel@orange.com>
 */
package com.orange.iot3core.clients.lwm2m.model;

/**
 * Configuration for the LwM2M Server (1) Object.
 */
public class Lwm2mServer {
    private final int shortServerId;
    private final int lifetime;
    private final String bindingMode;
    private final boolean notifyWhenDisable;

    /**
     * Constructor for the LwM2M Server (1) Object.
     *
     * @param shortServerId The short server ID assigned to the target server.
     * @param lifetime The registration lifetime in seconds with the LwM2M server.
     * @param bindingMode The binding mode specifying supported communication protocol. Needed to identify the server in a multi-server setup.
     * @param notifyWhenDisable Indicates whether the client should notify the server upon disabling.
     */
    public Lwm2mServer(int shortServerId, int lifetime, String bindingMode, boolean notifyWhenDisable) {
        this.shortServerId = shortServerId;
        this.lifetime = lifetime;
        this.bindingMode = bindingMode;
        this.notifyWhenDisable = notifyWhenDisable;
    }

    /**
     * Constructor for the LwM2M Server (1) Object.
     *
     * @param shortServerId The short server ID assigned to the target server.
     * @param lifetime The registration lifetime in seconds with the LwM2M server.
     * @param bindingMode The binding mode specifying supported communication protocol. Needed to identify the server in a multi-server setup.
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

    public String getBindingMode() {
        return bindingMode;
    }

    public boolean isNotifyWhenDisable() {
        return notifyWhenDisable;
    }
}