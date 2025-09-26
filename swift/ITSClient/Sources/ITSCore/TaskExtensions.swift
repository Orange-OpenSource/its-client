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

extension Task where Success == Never, Failure == Never {
    /// Suspends the current task for at least the given duration in seconds.
    /// - Parameter duration: The duraiton in seconds.
    public static func sleep(seconds duration: Double) async throws {
        if #available(iOS 16, *) {
            try await Task.sleep(for: .seconds(duration))
        } else {
            try await Task.sleep(nanoseconds: UInt64(1_000_000_000 * duration))
        }
    }
}
