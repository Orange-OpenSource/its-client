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

/// The representation of a road alarm.
public struct RoadAlarm: Sendable {
    /// The unique identifier.
    public let id: String
    /// The cause.
    public let cause: Cause?
    /// The longitude.
    public let longitude: Double
    /// The latitude.
    public let latitude: Double
    /// The road alarm timestamp.
    public let timestamp: Date
    /// The road alarm lifetime.
    public let lifetime: TimeInterval
    /// The underlying DENM object.
    public let underlyingDENM: DENM

    var expirationDate: Date {
        timestamp.addingTimeInterval(lifetime)
    }

    var isExpired: Bool {
        Date() > expirationDate
    }

    /// Initializes a `RoadAlarm`.
    /// - Parameters:
    ///   - id: The unique identifier.
    ///   - cause: The cause.
    ///   - longitude: The longitude.
    ///   - latitude: The latitude.
    ///   - timestamp: The road alarm timestamp.
    ///   - lifetime: The road alarm lifetime.
    ///   - underlyingDENM: The underlying DENM object.
    public init(
        id: String,
        cause: Cause?,
        longitude: Double,
        latitude: Double,
        timestamp: Date,
        lifetime: TimeInterval,
        underlyingDENM: DENM
    ) {
        self.id = id
        self.cause = cause
        self.longitude = longitude
        self.latitude = latitude
        self.timestamp = timestamp
        self.lifetime = lifetime
        self.underlyingDENM = underlyingDENM
    }
}
