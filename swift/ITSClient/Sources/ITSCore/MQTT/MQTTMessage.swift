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

struct MQTTMessage {
    let payload: Data
    let topic: String
    let userProperty: MQTTMessageUserProperty?
}

struct MQTTMessageUserProperty {
    let key: String
    let value: String
}

extension MQTTMessageUserProperty: Equatable {}
