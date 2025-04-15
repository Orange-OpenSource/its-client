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

/// A structure to make conversion for ESTI units.
struct ETSI {
    private static let deciMicroDegreesFactor: Double = 10_000_000
    /// The ETSI reference date is the 2004-01-01 00:00:00 UTC.
    /// This value is the number of seconds between the 1970-01-01T00:00:00Z and 2004-01-01T00:00:00Z.
    private static let etsiUnixEpochDifference = 1072915200
    private static let centiSecondsFactor: Double = 100
    private static let centimetersFactor: Double = 100
    private static let decimetersFactor: Double = 10
    private static let centimetersPerSecondFactor: Double = 100
    private static let deciDegreesFactor: Double = 10
    private static let centiDegreesPerSecondFactor: Double = 100
    private static let decimetersPerSquaredSecondFactor: Double = 10

    static func degreesToDeciMicroDegrees(_ degrees: Double) -> Int {
        Int(degrees * deciMicroDegreesFactor)
    }

    static func deciMicroDegreesToDegrees(_ deciMicroDegrees: Int) -> Double {
        Double(deciMicroDegrees) / deciMicroDegreesFactor
    }

    static func epochTimestampToETSIMilliseconds(_ epochTimestamp: TimeInterval) -> Int {
        Int(Int(epochTimestamp * 1000) - (etsiUnixEpochDifference * 1000))
    }

    static func etsiMillisecondsToEpochTimestamp(_ etsiMilliseconds: Int) -> TimeInterval {
        TimeInterval(etsiMilliseconds + etsiUnixEpochDifference * 1000) / 1000
    }

    static func epochTimestampToETSISeconds(_ epochTimestamp: TimeInterval) -> Int {
        Int(epochTimestamp) - etsiUnixEpochDifference
    }

    static func secondsToCentiSeconds(_ seconds: TimeInterval) -> Int {
        Int(seconds * centiSecondsFactor)
    }

    static func centiSecondsToSeconds(_ centiSeconds: Int) -> TimeInterval {
        Double(centiSeconds) / centiSecondsFactor
    }

    static func metersToCentimeters(_ meters: Double) -> Int {
        Int(meters * centimetersFactor)
    }

    static func centimetersToMeters(_ centimeters: Int) -> Double {
        Double(centimeters) / centimetersFactor
    }

    static func metersToDecimeters(_ meters: Double) -> Int {
        Int(meters * decimetersFactor)
    }

    static func decimetersToMeters(_ decimeters: Int) -> Double {
        Double(decimeters) / decimetersFactor
    }

    static func metersPerSecondToCentimetersPerSecond(_ meters: Double) -> Int {
        Int(meters * centimetersPerSecondFactor)
    }

    static func centimetersPerSecondToMetersPerSecond(_ centimetersPerSecond: Int) -> Double {
        Double(centimetersPerSecond) / centimetersPerSecondFactor
    }

    static func degreesToDeciDegrees(_ degrees: Double) -> Int {
        Int(degrees * deciDegreesFactor)
    }

    static func deciDegreesToDegrees(_ deciDegrees: Int) -> Double {
        Double(deciDegrees) / deciDegreesFactor
    }

    static func degreesPerSecondToCentiDegreesPerSecond(_ degreesPerSecond: Double) -> Int {
        Int(degreesPerSecond * centiDegreesPerSecondFactor)
    }

    static func centiDegreesPerSecondToDegreesPerSecond(_ centiDegreesPerSecond: Int) -> Double {
        Double(centiDegreesPerSecond) / centiDegreesPerSecondFactor
    }

    static func metersPerSquaredSecondToDecimetersPerSquaredSecond(_ metersPerSquaredSecond: Double) -> Int {
        Int(metersPerSquaredSecond * decimetersPerSquaredSecondFactor)
    }

    static func decimetersPerSquaredSecondToMetersPerSquaredSecond(_ decimetersPerSquaredSecond: Int) -> Double {
        Double(decimetersPerSquaredSecond) / decimetersPerSquaredSecondFactor
    }
}

@inlinable func clip<T>(_ value: T, _ minimum: T, _ maximum: T) -> T where T : Comparable {
    min(max(minimum, value), maximum)
}
