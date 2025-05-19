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

/// The decentralized environmental notification message typealias.
public typealias DENM = DecentralizedEnvironmentalNotificationMessage

/// The DENM representation.
public struct DecentralizedEnvironmentalNotificationMessage: Codable, Sendable {
    /// The DENM message.
    public let message: DENMMessage
    /// The entity responsible for this message.
    public let origin: Origin
    /// The identifier of the entity responsible for emitting the message.
    public let sourceUUID: String
    /// The timestamp when the message was generated since Unix Epoch in milliseconds.
    public let millisecondsTimestamp: Int
    /// The message type.
    public let type: MessageType = .denm
    /// The message format version.
    public let version: String = "1.1.3"
    /// The list of ordered elements root source of the message.
    public let path: [Path]?
    /// The timestamp when the message was generated since Unix Epoch in seconds.
    public var timestamp: TimeInterval { Double(millisecondsTimestamp) / 1_000 }

    enum CodingKeys: String, CodingKey {
        case message, origin, path, type, version
        case sourceUUID = "source_uuid"
        case millisecondsTimestamp = "timestamp"
    }

    init(
        message: DENMMessage,
        origin: Origin = .originSelf,
        sourceUUID: String,
        timestamp: TimeInterval,
        path: [Path]? = nil
    ) {
        self.message = message
        self.origin = origin
        self.path = path
        self.sourceUUID = sourceUUID
        self.millisecondsTimestamp = Int(timestamp * 1_000)
    }
}

/// The path.
public struct Path: Codable, Sendable {
    /// The root message type source of the element.
    public let messageType: MessageType
    /// The position.
    public let position: Position

    enum CodingKeys: String, CodingKey {
        case messageType = "message_type"
        case position
    }
}
