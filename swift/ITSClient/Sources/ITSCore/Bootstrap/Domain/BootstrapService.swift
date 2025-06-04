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

/// The bootstrap service to retrieve the configuration.
public final class BootstrapService {
    private let repository: BootstrapRepository

    /// Initializes a `BootstrapService`.
    /// - Parameter url: The bootstrap server URL.
    public convenience init(url: URL) {
        self.init(repository: ITSBootstrapRepository(url: url))
    }

    init(repository: BootstrapRepository) {
        self.repository = repository
    }

    /// Retrieves a `Bootstrap`.
    /// - Parameters:
    ///   - bootstrapConfiguration: The bootstrap configuration.
    /// - Returns: A `Bootstrap` or `nil` if an error occurs.
    public func bootstrap(bootstrapConfiguration: BootstrapConfiguration) async throws -> Bootstrap? {
        do {
            return try await repository.bootstrap(bootstrapConfiguration: bootstrapConfiguration)
        } catch {
            return nil
        }
    }
}
