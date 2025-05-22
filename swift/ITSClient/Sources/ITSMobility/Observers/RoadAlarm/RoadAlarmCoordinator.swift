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

actor RoadAlarmCoordinator {
    private let cache = Cache<String, RoadAlarm>()
    private weak var observer: RoadAlarmChangeObserver?

    init() {
        Task { [weak self] in
            guard let self else { return }

            await cache.setExpiredEntryHandler { [observer] entry in
                Task {
                    await observer?.didDelete(entry.value)
                }
            }
        }
    }

    func setObserver(_ observer: RoadAlarmChangeObserver) {
        self.observer = observer
    }

    func handleRoadAlarm(withPayload payload: Data) async {
        guard let observer,
              let denm = try? JSONDecoder().decode(DENM.self, from: payload) else { return }

        await observer.didReceiveDENM(denm)

        let id = denm.message.managementContainer.actionID.id
        let cause = denm.message.situationContainer?.eventType
        let latitude = denm.message.managementContainer.eventPosition.latitude
        let longitude = denm.message.managementContainer.eventPosition.longitude
        let lifetime = denm.message.managementContainer.validityDuration
        let timestamp = Date(timeIntervalSince1970: denm.timestamp)
        let isTerminated = denm.message.managementContainer.termination != nil

        let roadAlarm = RoadAlarm(id: id,
                                  cause: cause,
                                  longitude: longitude,
                                  latitude: latitude,
                                  timestamp: timestamp,
                                  lifetime: lifetime,
                                  underlyingDENM: denm)

        if let cachedRoadAlarm = await cache.value(for: id) {
            if isTerminated {
                await cache.removeValue(for: id)
                await observer.didDelete(cachedRoadAlarm)
            } else {
                await cache.setValue(roadAlarm, for: id, expirationDate: roadAlarm.expirationDate)
                await observer.didUpdate(roadAlarm)
            }
        } else if !roadAlarm.isExpired && !isTerminated {
            await cache.setValue(roadAlarm, for: id, expirationDate: roadAlarm.expirationDate)
            await observer.didCreate(roadAlarm)
        }
    }
}
