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
import Testing
@testable import ITSMobility

final class MockRoadUserCoordinatorObserver: RoadUserChangeObserver {
    var didCreateCallsCount = 0
    var didUpdateCallsCount = 0
    var didDeleteCallsCount = 0
    var didReceiveCAMCount = 0
    var currentCreatedRoadUser: RoadUser?
    var currentUpdatedRoadUser: RoadUser?
    var currentDeletedRoadUser: RoadUser?
    var currentCAM: CAM?

    func didCreate(_ user: RoadUser) {
        didCreateCallsCount += 1
        currentCreatedRoadUser = user
    }

    func didUpdate(_ user: RoadUser) {
        didUpdateCallsCount += 1
        currentUpdatedRoadUser = user
    }

    func didDelete(_ user: RoadUser) {
        didDeleteCallsCount += 1
        currentDeletedRoadUser = user
    }

    func didReceiveCAM(_ cam: CAM) {
        didReceiveCAMCount += 1
        currentCAM = cam
    }
}
