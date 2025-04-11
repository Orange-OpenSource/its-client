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

/// A protocol that defines an observer that receives events of changed road users.
@ChangeObserverActor
public protocol RoadUserChangeObserver: AnyObject, Sendable {
    /// Provides a new road user to the observer.
    /// - Parameter roadUser: The new `RoadUser`.
    func didCreate(_ roadUser: RoadUser)

    /// Provides an updated road user to the observer.
    /// - Parameter roadUser: The updated `RoadUser`.
    func didUpdate(_ roadUser: RoadUser)

    /// Provides a deleted road user to the observer.
    /// - Parameter roadUser: The deleted `RoadUser`.
    func didDelete(_ roadUser: RoadUser)

    /// Provides the underlying `CAM` received to the observer.
    /// - Parameter cam: The underlying `CAM`.
    func didReceiveCAM(_ cam: CAM)
}

extension RoadUserChangeObserver {
    public func didReceiveCAM(_ cam: CAM) {}
}
