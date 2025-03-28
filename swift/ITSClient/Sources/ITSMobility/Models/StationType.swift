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

/// The station type.
public enum StationType: Int, Codable {
    case unknown = 0
    case pedestrian = 1
    case cyclist = 2
    case moped = 3
    case motorcycle = 4
    case passengerCar = 5
    case bus = 6
    case lightTruck = 7
    case heavyTruck = 8
    case trailer = 9
    case specialVehicles = 10
    case tram = 11
    case roadSideUnit = 15
}

