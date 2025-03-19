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

enum MQTTClientError: Error {
    case connectionFailed
    case subscriptionFailed
    case unsubscriptionFailed
    case disconnectionFailed
    case sendPayloadFailed
}

extension MQTTClientError {
    var localizedDescription: String {
        switch self {
        case .connectionFailed:
            return "The connection to the server has failed."
        case .subscriptionFailed:
            return "The subscription has failed."
        case .unsubscriptionFailed:
            return "The unsubscription has failed."
        case .disconnectionFailed:
            return "The disconnection from the server has failed."
        case .sendPayloadFailed:
            return "The MQTT message can't be sent."
        }
    }
}
