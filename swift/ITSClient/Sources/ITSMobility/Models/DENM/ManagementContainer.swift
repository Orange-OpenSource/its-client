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

/// The management container.
public struct ManagementContainer: Codable, Sendable {
    /// The action identifier.
    public let actionID: ActionID
    /// The time at which the event is detected by the originating ITS-S. For the DENM repetition, this DE shall remain.
    /// Unit: millisecond since ETSI epoch (2004/01/01, so 1072915200000). 
    public let etsiDetectionTime: Int
    /// The time at which a new DENM, an update DENM or a cancellation DENM is generated.
    /// Unit: millisecond since ETSI epoch (2004/01/01, so 1072915200000).
    public let etsiReferenceTime: Int
    /// The event position.
    public let eventPosition: Position
    /// The termination.
    public let termination: Termination?
    /// The relevance distance.
    public let relevanceDistance: RelevanceDistance?
    /// The relevance traffic direction
    public let relevanceTrafficDirection: RelevanceTrafficDirection?
    /// The validity duration in seconds.
    public let etsiValidityDuration: Int?
    /// The transmission interval in milliseconds.
    public let etsiTransmissionInterval: Int?
    /// The station type.
    public let stationType: StationType?
    /// The management container confidence.
    public let confidence: Confidence?
    /// The detection time since Unix Epoch in seconds.
    public var detectionTime: TimeInterval {
        ETSI.etsiMillisecondsToEpochTimestamp(etsiDetectionTime)
    }
    /// The reference time since Unix Epoch in seconds.
    public var referenceTime: TimeInterval {
        ETSI.etsiMillisecondsToEpochTimestamp(etsiReferenceTime)
    }
    public var validityDuration: TimeInterval {
        etsiValidityDuration.map({ TimeInterval($0) }) ?? Self.defaultValidityDuration
    }
    /// The transmission interval in seconds.
    public var transmissionInterval: TimeInterval? {
        etsiTransmissionInterval.map({ Double($0) / 1000 })
    }

    private static let minValidityDuration: TimeInterval = 0
    private static let maxValidityDuration: TimeInterval = 86400
    public static let defaultValidityDuration: TimeInterval = 600

    enum CodingKeys: String, CodingKey {
        case actionID = "action_id"
        case confidence
        case etsiDetectionTime = "detection_time"
        case eventPosition = "event_position"
        case etsiReferenceTime = "reference_time"
        case relevanceDistance = "relevance_distance"
        case relevanceTrafficDirection = "relevance_traffic_direction"
        case stationType = "station_type"
        case termination
        case etsiTransmissionInterval = "transmission_interval"
        case etsiValidityDuration = "validity_duration"
    }

    init(
        actionID: ActionID,
        detectionTime: TimeInterval,
        referenceTime: TimeInterval,
        eventPosition: Position,
        termination: Termination? = nil,
        relevanceDistance: RelevanceDistance? = nil,
        relevanceTrafficDirection: RelevanceTrafficDirection? = nil,
        validityDuration: TimeInterval? = nil,
        transmissionInterval: TimeInterval? = nil,
        stationType: StationType? = .unknown,
        confidence: Confidence? = nil
    ) {
        self.actionID = actionID
        self.etsiDetectionTime = ETSI.epochTimestampToETSIMilliseconds(detectionTime)
        self.etsiReferenceTime = ETSI.epochTimestampToETSIMilliseconds(referenceTime)
        self.eventPosition = eventPosition
        self.termination = termination
        self.relevanceDistance = relevanceDistance
        self.relevanceTrafficDirection = relevanceTrafficDirection
        self.etsiValidityDuration = validityDuration.map({
            Int(clip($0, Self.minValidityDuration, Self.maxValidityDuration))
        })
        self.etsiTransmissionInterval = transmissionInterval.map({ Int($0 * 1000) })
        self.stationType = stationType
        self.confidence = confidence
    }
}

/// The action identifier.
public struct ActionID: Codable, Sendable {
    /// The identifier of an its station.
    public let originatingStationID: UInt32
    /// The sequence number is set each time a new DENM is created. It is used to differentiate
    /// from events detected by the same ITS-S.
    public let sequenceNumber: UInt16

    var id: String {
        "\(originatingStationID)_\(sequenceNumber)"
    }

    enum CodingKeys: String, CodingKey {
        case originatingStationID = "originating_station_id"
        case sequenceNumber = "sequence_number"
    }

    init(originatingStationID: UInt32, sequenceNumber: UInt16 = SequenceNumberGenerator.next()) {
        self.originatingStationID = originatingStationID
        self.sequenceNumber = sequenceNumber
    }
}

/// The termination.
public enum Termination: Int, Codable, Sendable {
    case isCancellation = 0
    case isNegation = 1
}

/// The relevance distance.
public enum RelevanceDistance: Int, Codable, Sendable {
    case lessThan50m = 0
    case lessThan100m = 1
    case lessThan200m = 2
    case lessThan500m = 3
    case lessThan1000m = 4
    case lessThan5km = 5
    case lessThan10km = 6
    case over10km = 7
}

/// The relevance traffic direction.
public enum RelevanceTrafficDirection: Int, Codable, Sendable {
    case allTrafficDirections = 0
    case upstreamTraffic = 1
    case downstreamTraffic = 2
    case oppositeTraffic = 3
}

