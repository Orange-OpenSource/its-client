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
import ITSMobility
import OSLog

final class MobilityRoadAlarmChangeObserver: RoadAlarmChangeObserver {
    private let logger = Logger()

    func didCreate(_ roadAlarm: RoadAlarm) {
        logger.debug("New road alarm: \(roadAlarm)")
    }

    func didUpdate(_ roadAlarm: RoadAlarm) {
        logger.debug("Road alarm updated: \(roadAlarm)")
    }

    func didDelete(_ roadAlarm: RoadAlarm) {
        logger.debug("Road alarm deleted: \(roadAlarm)")
    }
}

extension RoadAlarm: @retroactive CustomStringConvertible {
    public var description: String {
        "\(id) - \(latitude), \(longitude)"
    }
}
