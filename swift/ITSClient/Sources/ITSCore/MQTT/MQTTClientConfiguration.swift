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
    /// The MQTT server host.
    public let host: String
    /// The MQTT server port.
    public let port: Int
    /// The MQTT client identifier.
    public let clientIdentifier: String
    /// The MQTT user name if authentication is enabled on the server.
    public let userName: String?
    /// The MQTT password if authentication is enabled on the server.
    public let password: String?
    /// `true` if an encrypted connection to the server is used.
    public let useSSL: Bool
    /// `true` if a websocket connection to the server is used.
    public let useWebSockets: Bool

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
