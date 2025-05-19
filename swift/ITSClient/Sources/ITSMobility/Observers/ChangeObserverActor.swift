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

/// A global actor for change observer protocols.
@globalActor public actor ChangeObserverActor: GlobalActor {
    /// The shared actor instance.
    public static let shared = ChangeObserverActor()
}
