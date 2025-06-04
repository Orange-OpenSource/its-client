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
import ITSCore

/// A structure to configure the mobility.
public struct MobilityConfiguration: Sendable {
    /// The `CoreConfiguration` which includes MQTT and telemetry configurations.
    public let coreConfiguration: CoreConfiguration
    /// The station identifier.
    public let stationID: UInt32
    /// The namespace used in the MQTT topic.
    public let namespace: String
    /// The zoom level used when a position or an alert is sent.
    public let reportZoomLevel: Int

    var userIdentifier: String {
        coreConfiguration.mqttClientConfiguration.clientIdentifier
    }

    /// Initializes a `MobilityConfiguration`.
    /// - Parameters:
    ///   - coreConfiguration: The `CoreConfiguration` which includes MQTT and telemetry configurations.
    ///   - stationID: The station identifier. Must be unique and the same each time on a same device.
    ///   - namespace: The namespace used in the MQTT topic.
    ///   - reportZoomLevel: The zoom level used when a position or an alert is sent.
    public init(
        coreConfiguration: CoreConfiguration,
        stationID: UInt32,
        namespace: String = "default",
        reportZoomLevel: Int = 22
    ) {
        self.coreConfiguration = coreConfiguration
        self.stationID = stationID
        self.namespace = namespace
        self.reportZoomLevel = reportZoomLevel
    }
}
