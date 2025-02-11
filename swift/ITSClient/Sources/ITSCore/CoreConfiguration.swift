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

/// A structure to configure the core.
public struct CoreConfiguration: Sendable {
    let mqttClientConfiguration: MQTTClientConfiguration
    let telemetryClientConfiguration: TelemetryClientConfiguration?

    /// Initializes a `CoreConfiguration`.
    /// - Parameters:
    ///   - mqttClientConfiguration: The MQTT client configuration.
    ///   - telemetryClientConfiguration: The telemetry client configuration. Can be nil to opt-out telemetry.
    public init(
        mqttClientConfiguration: MQTTClientConfiguration,
        telemetryClientConfiguration: TelemetryClientConfiguration? = nil
    ) {
        self.mqttClientConfiguration = mqttClientConfiguration
        self.telemetryClientConfiguration = telemetryClientConfiguration
    }
}
