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

public struct CoreConfiguration: Sendable {
    let mqttClientConfiguration: MQTTClientConfiguration
    let telemetryClientConfiguration: TelemetryClientConfiguration?

    init(
        mqttClientConfiguration: MQTTClientConfiguration,
        telemetryClientConfiguration: TelemetryClientConfiguration?
    ) {
        self.mqttClientConfiguration = mqttClientConfiguration
        self.telemetryClientConfiguration = telemetryClientConfiguration
    }
}
