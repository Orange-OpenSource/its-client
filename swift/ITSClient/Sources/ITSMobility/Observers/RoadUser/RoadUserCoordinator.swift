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

actor RoadUserCoordinator {
    private let cache = Cache<String, RoadUser>()
    private weak var observer: RoadUserChangeObserver?

    init() {
        Task { [weak self] in
            guard let self else { return }

            await cache.setExpiredEntryHandler { [weak self] entry in
                Task {
                    await self?.observer?.didDelete(entry.value)
                }
            }
        }
    }

    func setObserver(_ observer: RoadUserChangeObserver) {
        self.observer = observer
    }

    func handleRoadUser(withPayload payload: Data) async {
        guard let observer,
              let cam = try? JSONDecoder().decode(CAM.self, from: payload) else { return }

        await observer.didReceiveCAM(cam)

        let id = "\(cam.sourceUUID)_\(cam.message.stationID)"
        let stationType = cam.message.basicContainer.stationType
        let latitude = cam.message.basicContainer.referencePosition.latitude
        let longitude = cam.message.basicContainer.referencePosition.longitude
        let speed = cam.message.highFrequencyContainer?.speed
        let heading = cam.message.highFrequencyContainer?.heading
        let timestamp = Date(timeIntervalSince1970: cam.timestamp)

        let roadUser = RoadUser(id: id,
                                stationType: stationType,
                                longitude: longitude,
                                latitude: latitude,
                                speed: speed,
                                heading: heading,
                                timestamp: timestamp,
                                underlyingCAM: cam)

        let userExists = await cache.value(for: id) != nil
        await cache.setValue(roadUser, for: id, expirationDate: roadUser.expirationDate)

        if userExists {
            await observer.didUpdate(roadUser)
        } else {
            await observer.didCreate(roadUser)
        }
    }
}
