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
import Network

enum NetworkStatus {
    case connected(NetworkType)
    case disconnected
}

enum NetworkType {
    case wifi
    case cellular
    case ethernet
    case other
}

extension NetworkStatus: Equatable {
    static func == (lhs: Self, rhs: Self) -> Bool {
        switch (lhs, rhs) {
        case (.connected(let lhsNetworkType), .connected(let rhsNetworkType)):
            return lhsNetworkType == rhsNetworkType
        case (.disconnected, .disconnected):
            return true
        default:
            return false
        }
    }
}

extension NetworkType: Equatable {}
