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
public actor SequenceNumberGenerator: SequenceNumberGeneratable {
    private static let shared = SequenceNumberGenerator()
    private var sequenceNumber: UInt16 = 0
    private let maxSequenceNumber: UInt16 = 65_535

    private init() {}

    /// Generates a new sequence number.
    /// - Returns: The sequence number generated.
    public static func next() async -> UInt16 {
        await shared.nextSequenceNumber()
    }

    private func nextSequenceNumber() -> UInt16 {
        sequenceNumber = UInt16((Int(sequenceNumber) + 1) % (Int(maxSequenceNumber) + 1))
        return sequenceNumber
    }
}
