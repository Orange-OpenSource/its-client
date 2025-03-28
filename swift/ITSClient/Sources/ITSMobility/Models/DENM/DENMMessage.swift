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

/// The DENM message.
public struct DENMMessage: Codable {
    /// The version of the ITS message and/or communication protocol.
    public let protocolVersion: UInt8
    /// The identifier for an ITS-S.
    public let stationID: UInt32
    /// Contains information related to the DENM management and the DENM protocol.
    public let managementContainer: ManagementContainer
    /// Contains information related to the type of the detected event.
    public let situationContainer: SituationContainer?
    /// Contains information specific to the use case which requires the transmission of
    /// additional information that is not included in the three previous containers.
    public let alacarteContainer: AlacarteContainer?
    /// Contains information of the event location, and the location referencing.
    public let locationContainer: LocationContainer?

    enum CodingKeys: String, CodingKey {
        case alacarteContainer = "alacarte_container"
        case locationContainer = "location_container"
        case managementContainer = "management_container"
        case protocolVersion = "protocol_version"
        case situationContainer = "situation_container"
        case stationID = "station_id"
    }

    init(
        protocolVersion: UInt8 = 1,
        stationID: UInt32,
        managementContainer: ManagementContainer,
        situationContainer: SituationContainer,
        locationContainer: LocationContainer? = nil,
        alacarteContainer: AlacarteContainer? = nil
    ) {
        self.protocolVersion = protocolVersion
        self.stationID = stationID
        self.managementContainer = managementContainer
        self.situationContainer = situationContainer
        self.locationContainer = locationContainer
        self.alacarteContainer = alacarteContainer
    }
}
