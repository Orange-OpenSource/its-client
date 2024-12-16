/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @authors
    Maciej Ćmiel       <maciej.cmiel@orange.com>
    Zbigniew Krawczyk  <zbigniew2.krawczyk@orange.com>
 */
package com.orange.iot3core.clients.lwm2m.model;

import java.io.Serializable;

public abstract class Lwm2mConfig implements Serializable {
    public abstract String getEndpointName();
    public abstract String getUri();
    public abstract String getPskIdentity();
    public abstract String getPrivateKey();
    public abstract Lwm2mServer getServerConfig();

    /**
     * Configuration for a Bootstrap PSK setup.
     *
     * This configuration is used when the LwM2M client needs to start its lifecycle
     * by communicating with a bootstrap server. The bootstrap server provides the client
     * with configuration details (e.g., security and server information) for the main LwM2M server.
     *
     */
    public static class Lwm2mBootstrapConfig extends Lwm2mConfig {
        private final String endpointName;
        private final String uri;
        private final String pskIdentity;
        private final String privateKey;
        private final Lwm2mServer serverConfig;

        /**
         * Constructor for a Bootstrap PSK setup.
         *
         * @param endpointName The unique identifier for the client endpoint.
         * @param uri The URI of the bootstrap server. Example: `coaps://bootstrap.lwm2m.liveobjects.orange-business.com:5684`.
         * @param pskIdentity The PSK identity for authenticating with the bootstrap server.
         * @param privateKey The PSK private key for securing the connection with the bootstrap server.
         */
        public Lwm2mBootstrapConfig(String endpointName, String uri, String pskIdentity, String privateKey, Lwm2mServer serverConfig) {
            this.endpointName = endpointName;
            this.uri = uri;
            this.pskIdentity = pskIdentity;
            this.privateKey = privateKey;
            this.serverConfig = serverConfig;
        }

        @Override
        public String getEndpointName() {
            return endpointName;
        }

        @Override
        public String getUri() {
            return uri;
        }

        @Override
        public String getPskIdentity() {
            return pskIdentity;
        }

        @Override
        public String getPrivateKey() {
            return privateKey;
        }

        @Override
        public Lwm2mServer getServerConfig() {
            return serverConfig;
        }
    }

    /**
     * Configuration for a Classic PSK (non-bootstrap) setup.
     *
     * This configuration is used when the LwM2M client directly connects to a regular LwM2M server
     * without first contacting a bootstrap server. The client must include a `shortServerId` to
     * identify the target server during communication.
     *
     */
    public static class Lwm2mClassicConfig extends Lwm2mConfig {
        private final String endpointName;
        private final String uri;
        private final String pskIdentity;
        private final String privateKey;
        private final int shortServerId;
        private final Lwm2mServer serverConfig;

        /**
         * Configuration for a Classic PSK (non-bootstrap) setup.
         *
         * This configuration is used when the LwM2M client directly connects to a regular LwM2M server
         * without first contacting a bootstrap server. The client must include a `shortServerId` to
         * identify the target server during communication.
         *
         * @param endpointName The unique identifier for the client endpoint.
         * @param uri The URI of the LwM2M server. Example: `coaps://lwm2m.liveobjects.orange-business.com:5684`.
         * @param pskIdentity The PSK identity for authenticating with the server.
         * @param privateKey The PSK private key for securing the connection with the server.
         * @param shortServerId The short server ID assigned to the target server. This value is required
         *                         by the LwM2M protocol to identify the server in a multi-server setup.
         */
        public Lwm2mClassicConfig(String endpointName, String uri, String pskIdentity, String privateKey, int shortServerId, Lwm2mServer serverConfig) {
            this.endpointName = endpointName;
            this.uri = uri;
            this.pskIdentity = pskIdentity;
            this.privateKey = privateKey;
            this.shortServerId = shortServerId;
            this.serverConfig = serverConfig;
        }

        @Override
        public String getEndpointName() {
            return endpointName;
        }

        @Override
        public String getUri() {
            return uri;
        }

        @Override
        public String getPskIdentity() {
            return pskIdentity;
        }

        @Override
        public String getPrivateKey() {
            return privateKey;
        }

        public int getShortServerId() {
            return shortServerId;
        }

        @Override
        public Lwm2mServer getServerConfig() {
            return serverConfig;
        }
    }
}

