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
actor SequenceNumberGenerator {
    private static var sequenceNumber: UInt16 = 0
    private static let maxSequenceNumber = 65_535

    static func next() -> UInt16 {
        sequenceNumber = UInt16((Int(sequenceNumber) + 1) % (maxSequenceNumber + 1))
        return sequenceNumber
    }
}
