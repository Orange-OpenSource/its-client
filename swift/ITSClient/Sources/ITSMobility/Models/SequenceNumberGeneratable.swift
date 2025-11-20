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

/// Protocol to generate a new sequence number.
public protocol SequenceNumberGeneratable {
    /// Generates a new sequence number.
    /// - Returns: The sequence number generated.
    static func next() async -> UInt16
}
