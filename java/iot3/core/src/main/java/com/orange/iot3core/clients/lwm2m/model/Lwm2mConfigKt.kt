/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @authors
    Maciej Ćmiel       <maciej.cmiel@orange.com>
    Zbigniew Krawczyk  <zbigniew2.krawczyk@orange.com>
 */
package com.orange.iot3core.clients.lwm2m.model


/**
 * Kotlin version of {@link Lwm2mConfig.Lwm2mBootstrapConfig}
 * Base sealed class for LwM2M client configurations.
 */
sealed class Lwm2mConfigKt {
    abstract val endpointName: String
    abstract val uri: String
    abstract val pskIdentity: String
    abstract val privateKey: String

    abstract val serverConfig: Lwm2mServer

    /**
     * Configuration for a Bootstrap PSK setup.
     *
     * This configuration is used when the LwM2M client needs to start its lifecycle
     * by communicating with a bootstrap server. The bootstrap server provides the client
     * with configuration details (e.g., security and server information) for the main LwM2M server.
     *
     * @property endpointName The unique identifier for the client endpoint.
     * @property uri The URI of the bootstrap server. Example: `coaps://bootstrap.lwm2m.liveobjects.orange-business.com:5684`.
     * @property pskIdentity The PSK identity for authenticating with the bootstrap server.
     * @property privateKey The PSK private key for securing the connection with the bootstrap server.
     */
    data class Lwm2mBootstrapConfig(
            override val endpointName: String,
            override val uri: String,
            override val pskIdentity: String,
            override val privateKey: String,
            override val serverConfig: Lwm2mServer
    ) : Lwm2mConfigKt()

    /**
     * Configuration for a Classic PSK (non-bootstrap) setup.
     *
     * This configuration is used when the LwM2M client directly connects to a regular LwM2M server
     * without first contacting a bootstrap server. The client must include a `shortServerId` to
     * identify the target server during communication.
     *
     * @property endpointName The unique identifier for the client endpoint.
     * @property uri The URI of the LwM2M server. Example: `coaps://lwm2m.liveobjects.orange-business.com:5684`.
     * @property pskIdentity The PSK identity for authenticating with the server.
     * @property privateKey The PSK private key for securing the connection with the server.
     * @property shortServerId The short server ID assigned to the target server. This value is required
     *                         by the LwM2M protocol to identify the server in a multi-server setup.
     */
    data class Lwm2mClassicConfig(
            override val endpointName: String,
            override val uri: String,
            override val pskIdentity: String,
            override val privateKey: String,
            val shortServerId: Int,
            override val serverConfig: Lwm2mServer
    ) : Lwm2mConfigKt()
}
