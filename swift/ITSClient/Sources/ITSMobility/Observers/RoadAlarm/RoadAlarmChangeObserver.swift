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

/// A protocol that defines an observer that receives events of changed road alarms.
@ChangeObserverActor
public protocol RoadAlarmChangeObserver: AnyObject, Sendable {
    /// Provides a new road alarm to the observer.
    /// - Parameter roadAlarm: The new `RoadAlarm`.
    func didCreate(_ roadAlarm: RoadAlarm)

    /// Provides an updated road alarm to the observer.
    /// - Parameter roadAlarm: The updated `RoadAlarm`.
    func didUpdate(_ roadAlarm: RoadAlarm)

    /// Provides a deleted road alarm to the observer.
    /// - Parameter roadAlarm: The deleted `RoadAlarm`.
    func didDelete(_ roadAlarm: RoadAlarm)

    /// Provides the underlying `DENM` received to the observer.
    /// - Parameter denm: The underlying `DENM`.
    func didReceiveDENM(_ denm: DENM)
}

extension RoadAlarmChangeObserver {
    /// Provides the underlying `DENM` received to the observer.
    /// - Parameter denm: The underlying `DENM`.
    public func didReceiveDENM(_ denm: DENM) {}
}
