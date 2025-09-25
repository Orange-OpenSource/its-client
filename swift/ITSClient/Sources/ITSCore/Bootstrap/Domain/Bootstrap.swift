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

import Foundation

/// The bootstrap which contains properties to configure services.
public struct Bootstrap: Sendable {
    /// The  identifier.
    public let identifier: String
    /// The  username.
    public let user: String
    /// The password.
    public let password: String
    /// The MQTT URL.
    public let mqttURL: URL
    /// `true` if an encrypted connection to the MQTT server is used.
    public let useMQTTSSL: Bool
    /// `true` if a websocket connection to the MQTT server is used.
    public let useMQTTWebSockets: Bool
    /// The MQTT root topic.
    public let mqttRootTopic: String
    /// The telemetry URL.
    public let telemetryURL: URL?

    /// Builds a `MQTTClientConfiguration`.
    /// - Returns: A `MQTTClientConfiguration` or nil if the MQTT `host` or `port` is nil.
    public func mqttClientConfiguration() -> MQTTClientConfiguration? {
        guard let host = mqttURL.host, let port = mqttURL.port else { return nil }

        return MQTTClientConfiguration(host: host,
                                       port: port,
                                       clientIdentifier: identifier,
                                       userName: user,
                                       password: password,
                                       useSSL: useMQTTSSL,
                                       useWebSockets: useMQTTWebSockets)
    }

    /// Builds a `TelemetryClientConfiguration`.
    /// - Parameter serviceName: The telemetry service name.
    /// - Returns: A `TelemetryClientConfiguration` or nil if the `telemetryURL` is nil.
    public func telemetryClientConfiguration(serviceName: String) -> TelemetryClientConfiguration? {
        guard let telemetryURL else { return nil }

        return TelemetryClientConfiguration(url: telemetryURL,
                                            user: user,
                                            password: password,
                                            serviceName: serviceName)
    }
}
