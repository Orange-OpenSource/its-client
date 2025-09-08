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

/// The location container.
public struct LocationContainer: Codable, Sendable {
    /// The traces, 1 or more path history.
    public let traces: [Trace]
    /// The event speed in centimeters per second.
    public let etsiEventSpeed: Int?
    /// The event heading in decidegrees.
    public let etsiEventPositionHeading: Int?
    /// The road type.
    public let roadType: RoadType?
    /// The location container confidence.
    public let confidence: LocationContainerConfidence?
    /// The event heading in degrees.
    public var eventPositionHeading: Double? {
        etsiEventPositionHeading.map { ETSI.deciDegreesToDegrees($0) }
    }
    /// The event speed in meters per second.
    public var eventSpeed: Double? {
        etsiEventSpeed.map { ETSI.centimetersPerSecondToMetersPerSecond($0) }
    }

    private static let minEventSpeed = 0
    private static let maxEventSpeed = 16_383
    private static let maxTraces = 7

    enum CodingKeys: String, CodingKey {
        case confidence
        case etsiEventPositionHeading = "event_position_heading"
        case etsiEventSpeed = "event_speed"
        case roadType = "road_type"
        case traces
    }

    /// Initializes a `LocationContainer`.
    /// - Parameters:
    ///   - traces: The traces, 1 or more path history.
    ///   - eventSpeed: The event speed in meters per second.
    ///   - eventPositionHeading: The event heading in degrees.
    ///   - roadType: The road type.
    ///   - confidence: The location container confidence.
    public init(
        traces: [Trace] = [],
        eventSpeed: Double? = nil,
        eventPositionHeading: Double? = nil,
        roadType: RoadType? = nil,
        confidence: LocationContainerConfidence? = nil
    ) {
        self.traces = Array(traces.prefix(Self.maxTraces))
        self.etsiEventSpeed = eventSpeed.map {
            clip(ETSI.metersPerSecondToCentimetersPerSecond($0),
                 Self.minEventSpeed,
                 Self.maxEventSpeed)
        }
        self.etsiEventPositionHeading = eventPositionHeading.map { ETSI.degreesToDeciDegrees($0) }
        self.roadType = roadType
        self.confidence = confidence
    }
}

/// The trace.
public struct Trace: Codable, Sendable {
    /// The path history, a path with a set of path points.
    public let pathHistory: [PathHistory]

    private static let maxPathHistory = 40

    enum CodingKeys: String, CodingKey {
        case pathHistory = "path_history"
    }

    /// Initializes a `Trace`.
    /// - Parameter pathHistory: The path history, a path with a set of path points.
    public init(pathHistory: [PathHistory]) {
        self.pathHistory = Array(pathHistory.prefix(Self.maxPathHistory))
    }
}

/// The road type.
public enum RoadType: Int, Codable, Sendable {
    case urban_NoStructuralSeparationToOppositeLanes = 0
    case urban_WithStructuralSeparationToOppositeLanes = 1
    case nonUrban_NoStructuralSeparationToOppositeLanes = 2
    case nonUrban_WithStructuralSeparationToOppositeLanes = 3

    enum CodingKeys: String, CodingKey {
        case urban_NoStructuralSeparationToOppositeLanes = "urban-NoStructuralSeparationToOppositeLanes"
        case urban_WithStructuralSeparationToOppositeLanes = "urban-WithStructuralSeparationToOppositeLanes"
        case nonUrban_NoStructuralSeparationToOppositeLanes = "nonUrban-NoStructuralSeparationToOppositeLanes"
        case nonUrban_WithStructuralSeparationToOppositeLanes = "nonUrban-WithStructuralSeparationToOppositeLanes"
    }
}

/// The location container confidence.
public struct LocationContainerConfidence: Codable, Sendable {
    /// The event speed in centimeters per second.
    public let etsiEventSpeed: Int?
    /// The event position heading in decidegrees.
    public let etsiEventPositionHeading: Int?
    /// The event speed in meters per second.
    public var eventSpeed: Double? {
        etsiEventSpeed.map { ETSI.centimetersPerSecondToMetersPerSecond($0) }
    }
    /// The event position in degrees.
    public var eventPositionHeading: Double? {
        etsiEventPositionHeading.map { ETSI.deciDegreesToDegrees($0) }
    }

    enum CodingKeys: String, CodingKey {
        case etsiEventSpeed = "event_speed"
        case etsiEventPositionHeading = "event_position_heading"
    }

    /// Initializes  a `LocationContainerConfidence`.
    /// - Parameters:
    ///   - eventSpeed: The event speed in meters per second.
    ///   - eventPositionHeading: The event position in degrees.
    public init(eventSpeed: Double?, eventPositionHeading: Double?) {
        self.etsiEventSpeed = eventSpeed.map {
            ETSI.metersPerSecondToCentimetersPerSecond($0)
        }
        self.etsiEventPositionHeading = eventPositionHeading.map {
            ETSI.degreesToDeciDegrees($0)
        }
    }
}
