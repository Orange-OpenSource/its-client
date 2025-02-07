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

/// The representation of a message received or sent by the core.
public struct CoreMQTTMessage: Sendable {
    /// The message payload.
    public let payload: Data
    /// The topic on which the message is received or sent.
    public let topic: String

    /// Initializes a `CoreMQTTMessage`
    /// - Parameters:
    ///   - payload: The message payload.
    ///   - topic: The topic on which the message is received or sent.
    public init(payload: Data, topic: String) {
        self.payload = payload
        self.topic = topic
    }
}
