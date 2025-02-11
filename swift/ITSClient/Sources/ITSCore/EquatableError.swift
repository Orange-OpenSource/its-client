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

/// A structure to manage equatable errors for underlying errors.
public struct EquatableError: Error, Equatable {
    /// The wrapped error.
    public let wrappedError: any Error & Equatable
    private let equalsClosure: (@Sendable (any Error & Equatable) -> Bool)

    /// The localized  description.
    public var localizedDescription: String {
        return wrappedError.localizedDescription
    }

    init<T: Error & Equatable>(wrappedError: T) {
        self.wrappedError = wrappedError
        // To avoid generic on struct, do the test in the closure and store it
        equalsClosure = { $0 as? T == wrappedError }
    }

    public static func == (lhs: Self, rhs: Self) -> Bool {
        return lhs.equalsClosure(rhs.wrappedError)
    }
}
