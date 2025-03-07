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

typealias CAM = CooperativeAwarenessMessage

/// The CAM representation.
public struct CooperativeAwarenessMessage: Codable {
    /// The CAM message.
    public let message: CAMMessage
    /// The entity responsible for this message.
    public let origin: Origin
    /// The identifier of the entity responsible for emitting the message.
    public let sourceUUID: String
    /// The timestamp when the message was generated since Unix Epoch in milliseconds.
    public let millisecondsTimestamp: Int
    /// The message type.
    public let type: MessageType = .cam
    /// The message format version.
    public let version: String = "1.1.3"
    /// The timestamp when the message was generated since Unix Epoch in seconds.
    public var timestamp: TimeInterval { Double(millisecondsTimestamp) / 1000 }

    enum CodingKeys: String, CodingKey {
        case message, origin, type, version
        case sourceUUID = "source_uuid"
        case millisecondsTimestamp = "timestamp"
    }

    init(
        message: CAMMessage,
        origin: Origin = .originSelf,
        sourceUUID: String,
        timestamp: TimeInterval
    ) {
        self.message = message
        self.origin = origin
        self.sourceUUID = sourceUUID
        self.millisecondsTimestamp = Int(timestamp * 1000)
    }
}
