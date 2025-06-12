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

/// The bootstrap configuration.
public struct BootstrapConfiguration {
    /// The bootstrap identifier.
    public let identifier: String
    /// The bootstrap user.
    public let user: String
    /// The bootstrap password.
    public let password: String
    /// The bootstrap role.
    public let role: String

    /// Initializes a `BootstrapConfiguration`.
    /// - Parameters:
    ///   - identifier: The bootstrap identifier.
    ///   - user: The bootstrap user.
    ///   - password: The bootstrap password.
    ///   - role: The bootstrap role.
    public init(identifier: String, user: String, password: String, role: String) {
        self.identifier = identifier
        self.user = user
        self.password = password
        self.role = role
    }
}
