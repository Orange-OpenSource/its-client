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

/// The CAM message.
public struct CAMMessage: Codable, Sendable {
    /// The version of the ITS message and/or communication protocol.
    public let protocolVersion: UInt8
    /// The identifier for an ITS-S.
    public let stationID: UInt32
    /// The time of the reference position in the CAM, considered as time of the CAM generation.
    public let etsiGenerationDeltaTime: Int
    /// The basic container.
    public let basicContainer: BasicContainer
    /// The high frequency container.
    public let highFrequencyContainer: HighFrequencyContainer?
    /// The low frequency container.
    public let lowFrequencyContainer: LowFrequencyContainer?

    private static let generationDeltaTimeModulo = 65_536

    enum CodingKeys: String, CodingKey {
        case protocolVersion = "protocol_version"
        case stationID = "station_id"
        case etsiGenerationDeltaTime = "generation_delta_time"
        case basicContainer = "basic_container"
        case highFrequencyContainer = "high_frequency_container"
        case lowFrequencyContainer = "low_frequency_container"
    }

    init(
        protocolVersion: UInt8 = 1,
        stationID: UInt32,
        generationDeltaTime: TimeInterval,
        basicContainer: BasicContainer,
        highFrequencyContainer: HighFrequencyContainer?,
        lowFrequencyContainer: LowFrequencyContainer? = nil
        ) {
        self.protocolVersion = protocolVersion
        self.stationID = stationID
        self.etsiGenerationDeltaTime = ETSI.epochTimestampToETSIMilliseconds(
            generationDeltaTime
        ) % Self.generationDeltaTimeModulo
        self.basicContainer = basicContainer
        self.highFrequencyContainer = highFrequencyContainer
        self.lowFrequencyContainer = lowFrequencyContainer
    }
}
