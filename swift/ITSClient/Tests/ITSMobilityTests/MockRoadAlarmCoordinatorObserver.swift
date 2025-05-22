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
@testable import ITSMobility
import Testing

final class MockRoadAlarmCoordinatorObserver: RoadAlarmChangeObserver {
    var didCreateCallsCount = 0
    var didUpdateCallsCount = 0
    var didDeleteCallsCount = 0
    var didReceiveDENMCount = 0
    var currentCreatedRoadAlarm: RoadAlarm?
    var currentUpdatedRoadAlarm: RoadAlarm?
    var currentDeletedRoadAlarm: RoadAlarm?
    var currentDENM: DENM?

    func didCreate(_ alarm: RoadAlarm) {
        didCreateCallsCount += 1
        currentCreatedRoadAlarm = alarm
    }

    func didUpdate(_ alarm: RoadAlarm) {
        didUpdateCallsCount += 1
        currentUpdatedRoadAlarm = alarm
    }

    func didDelete(_ alarm: RoadAlarm) {
        didDeleteCallsCount += 1
        currentDeletedRoadAlarm = alarm
    }

    func didReceiveDENM(_ denm: DENM) {
        didReceiveDENMCount += 1
        currentDENM = denm
    }
}
