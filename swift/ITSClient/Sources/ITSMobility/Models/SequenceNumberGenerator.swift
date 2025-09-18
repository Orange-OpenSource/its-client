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

/// Generates an incremental sequence number.
/// If the number overflows `maxSequenceNumber`, the sequence number sequence is started over.
public actor SequenceNumberGenerator {
    private static var sequenceNumber: UInt16 = 0
    private static let maxSequenceNumber = 65_535

    /// Generates a new sequence number.
    /// - Returns: The sequence number generated.
    public static func next() -> UInt16 {
        sequenceNumber = UInt16((Int(sequenceNumber) + 1) % (maxSequenceNumber + 1))
        return sequenceNumber
    }
}
