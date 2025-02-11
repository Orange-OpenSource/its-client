/*
 * Software Name : ITSClient
 * SPDX-FileCopyrightText: Copyright (c) Orange SA
 * SPDX-License-Identifier: MIT
 *
 * This software is distributed under the MIT license,
 * see the "LICENSE.txt" file for more details or https://opensource.org/license/MIT/
 *
 * Software description: Swift ITS client.
 */

/// A structure to configure a MQTT client.
public struct MQTTClientConfiguration: Sendable {
    let host: String
    let port: Int
    let clientIdentifier: String
    let userName: String?
    let password: String?
    let useSSL: Bool
    let useWebSockets: Bool

    /// Initializes a `MQTTClientConfiguration`.
    /// - Parameters:
    ///   - host: The MQTT server host.
    ///   - port: The MQTT server port.
    ///   - clientIdentifier: The MQTT client identifier.
    ///   - userName: The MQTT user name if authentication is enabled on the server.
    ///   - password: The MQTT password if authentication is enabled on the server.
    ///   - useSSL: `true` to use an encrypted connection to the server.
    ///   - useWebSockets: `true` to use a websocket connection to the server.
    public init(
        host: String,
        port: Int,
        clientIdentifier: String,
        userName: String? = nil,
        password: String? = nil,
        useSSL: Bool,
        useWebSockets: Bool = false
    ) {
        self.host = host
        self.port = port
        self.clientIdentifier = clientIdentifier
        self.userName = userName
        self.password = password
        self.useSSL = useSSL
        self.useWebSockets = useWebSockets
    }
}
