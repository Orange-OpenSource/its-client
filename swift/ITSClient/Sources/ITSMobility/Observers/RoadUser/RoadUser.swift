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

/// The representation of a road user.
public struct RoadUser: Sendable {
    /// The unique identifier.
    public let id: String
    /// The station type.
    public let stationType: StationType?
    /// The longitude.
    public let longitude: Double
    /// The latitude.
    public let latitude: Double
    /// The speed in m/s.
    public let speed: Double?
    /// The heading in degrees.
    public let heading: Double?
    /// The road user timestamp.
    public let timestamp: Date
    /// The underlying CAM object.
    public let underlyingCAM: CAM

    var expirationDate: Date {
        // Expiration 1.5s after
        timestamp.addingTimeInterval(1.5)
    }
    
    /// Initializes a `RoadUser`.
    /// - Parameters:
    ///   - id: The unique identifier.
    ///   - stationType: The station type.
    ///   - longitude: The longitude.
    ///   - latitude: The latitude.
    ///   - speed: The speed in m/s.
    ///   - heading: The heading in degrees.
    ///   - timestamp: The road user timestamp.
    ///   - underlyingCAM: The underlying CAM object.
    public init(
        id: String,
        stationType: StationType?,
        longitude: Double,
        latitude: Double,
        speed: Double?,
        heading: Double?,
        timestamp: Date,
        underlyingCAM: CAM
    ) {
        self.id = id
        self.stationType = stationType
        self.longitude = longitude
        self.latitude = latitude
        self.speed = speed
        self.heading = heading
        self.timestamp = timestamp
        self.underlyingCAM = underlyingCAM
    }
}
