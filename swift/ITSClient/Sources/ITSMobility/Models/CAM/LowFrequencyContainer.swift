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

/// The low frequency container.
public struct LowFrequencyContainer: Codable, Sendable {
    /// The vehicle role.
    public let vehicleRole: VehicleRole?
    /// The exterior lights status with a bit representation in a string.
    public let etsiExteriorLights: String
    /// The path history, a path with a set of path points.
    public let pathHistory: [PathHistory]
    /// The exterior lights status.
    public var exteriorLights: ExteriorLightStatus {
        ExteriorLightStatus(rawValue: strtoul(etsiExteriorLights, nil, 2))
    }

    private static let maxPathHistory = 40

    enum CodingKeys: String, CodingKey {
        case vehicleRole = "vehicle_role"
        case etsiExteriorLights = "exterior_lights"
        case pathHistory = "path_history"
    }

    /// Initializes a `LowFrequencyContainer`.
    /// - Parameters:
    ///   - vehicleRole: The vehicle role.
    ///   - exteriorLights: The exterior lights status.
    ///   - pathHistory: The path history, a path with a set of path points.
    public init(vehicleRole: VehicleRole?, exteriorLights: ExteriorLightStatus, pathHistory: [PathHistory]) {
        self.vehicleRole = vehicleRole
        self.etsiExteriorLights = String(exteriorLights.rawValue, radix: 2)
        self.pathHistory = Array(pathHistory.prefix(Self.maxPathHistory))
    }
}

/// The vehicle role
public enum VehicleRole: Int, Codable, Sendable {
    case `default` = 0
    case publicTransport = 1
    case specialTransport = 2
    case dangerousGoods = 3
    case roadWork = 4
    case rescue = 5
    case emergency = 6
    case safetyCar = 7
    case agriculture = 8
    case commercial = 9
    case military = 10
    case roadOperator = 11
    case taxi = 12
    case reserved1 = 13
    case reserved2 = 14
    case reserved3 = 15
}

/// The exterior light status.
public struct ExteriorLightStatus: OptionSet, Sendable {
    /// The raw value.
    public let rawValue: UInt

    /// Initializes a `ExteriorLightStatus`.
    /// - Parameter rawValue: The raw value.
    public init(rawValue: UInt) {
        self.rawValue = rawValue
    }

    /// The low beamed headlights on status.
    public static let lowBeamHeadlightsOn = ExteriorLightStatus(rawValue: 1 << 0)
    /// The high beamed headlights on status.
    public static let highBeamHeadlightsOn = ExteriorLightStatus(rawValue: 1 << 1)
    /// The left turn signal on status.
    public static let leftTurnSignalOn = ExteriorLightStatus(rawValue: 1 << 2)
    /// The right turn signal on status.
    public static let rightTurnSignalOn = ExteriorLightStatus(rawValue: 1 << 3)
    /// The daytime running lights on status.
    public static let daytimeRunningLightsOn = ExteriorLightStatus(rawValue: 1 << 4)
    /// The reverse light on status.
    public static let reverseLightOn = ExteriorLightStatus(rawValue: 1 << 5)
    /// The fog light on status.
    public static let fogLightOn = ExteriorLightStatus(rawValue: 1 << 6)
    /// The parking lights on status.
    public static let parkingLightsOn = ExteriorLightStatus(rawValue: 1 << 7)
}
