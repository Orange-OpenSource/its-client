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

/// The position with coordinates and altitude.
public struct Position: Codable, Sendable {
    /// The latitude in decimicrodegrees.
    public let etsiLatitude: Int
    /// The longitude in decimicrodegrees.
    public let etsiLongitude: Int
    /// The altitude in centimeters.
    public let etsiAltitude: Int
    /// The latitiude in degrees.
    public var latitude: Double { ETSI.deciMicroDegreesToDegrees(etsiLatitude) }
    /// The longitude in degrees.
    public var longitude: Double { ETSI.deciMicroDegreesToDegrees(etsiLongitude) }
    /// The altitude in meters.
    public var altitude: Double { ETSI.centimetersToMeters(etsiAltitude) }

    enum CodingKeys: String, CodingKey {
        case etsiLatitude = "latitude"
        case etsiLongitude = "longitude"
        case etsiAltitude = "altitude"
    }

    /// Initializes a `Position`.
    /// - Parameters:
    ///   - latitude: The latitiude in degrees.
    ///   - longitude: The longitude in degrees.
    ///   - altitude: The altitude in meters.
    public init(latitude: Double, longitude: Double, altitude: Double) {
        self.etsiLatitude = ETSI.degreesToDeciMicroDegrees(latitude)
        self.etsiLongitude = ETSI.degreesToDeciMicroDegrees(longitude)
        self.etsiAltitude = ETSI.metersToCentimeters(altitude)
    }
}
