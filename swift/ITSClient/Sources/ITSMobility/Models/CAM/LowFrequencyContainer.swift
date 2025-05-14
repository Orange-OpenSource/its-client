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
    /// The vehicule role.
    public let vehicleRole: VehiculeRole?
    /// The exterior lights status with a bit representation in a string.
    public let etsiExteriorLights: String
    /// The path history, a path with a set of path points
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

    init(vehicleRole: VehiculeRole?, exteriorLights: ExteriorLightStatus, pathHistory: [PathHistory]) {
        self.vehicleRole = vehicleRole
        self.etsiExteriorLights = String(exteriorLights.rawValue, radix: 2)
        self.pathHistory = Array(pathHistory.prefix(Self.maxPathHistory))
    }
}

/// The vehicule role
public enum VehiculeRole: Int, Codable, Sendable {
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
    public let rawValue: UInt

    public init(rawValue: UInt) {
        self.rawValue = rawValue
    }

    static let lowBeamHeadlightsOn  = AccelerationControl(rawValue: 1 << 0)
    static let highBeamHeadlightsOn  = AccelerationControl(rawValue: 1 << 1)
    static let leftTurnSignalOn   = AccelerationControl(rawValue: 1 << 2)
    static let rightTurnSignalOn   = AccelerationControl(rawValue: 1 << 3)
    static let daytimeRunningLightsOn   = AccelerationControl(rawValue: 1 << 4)
    static let reverseLightOn   = AccelerationControl(rawValue: 1 << 5)
    static let fogLightOn   = AccelerationControl(rawValue: 1 << 6)
    static let parkingLightsOn   = AccelerationControl(rawValue: 1 << 7)
}
