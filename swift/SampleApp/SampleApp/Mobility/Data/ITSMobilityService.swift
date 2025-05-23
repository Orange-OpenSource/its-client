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
import ITSCore
import ITSMobility

actor ITSMobilityService: MobilityService {
    private let mobility: Mobility
    private var roadAlarmChangeObserver: RoadAlarmChangeObserver?
    private var sendPositionTask: Task<Void, any Error>?

    init() {
        mobility = Mobility()
    }

    func start() async throws(MobilityError) {
        // Update this to set your broker connection configuration
        let host = ""
        let port = 1883
        let clientIdentifier = UUID().uuidString
        let username: String? = nil
        let password: String? = nil
        let useSSL = false

        let mqttClientConfiguration = MQTTClientConfiguration(host: host,
                                                              port: port,
                                                              clientIdentifier: clientIdentifier,
                                                              userName: username,
                                                              password: password,
                                                              useSSL: useSSL)
        let coreConfiguration = CoreConfiguration(mqttClientConfiguration: mqttClientConfiguration)
        let mobilityConfiguration = MobilityConfiguration(coreConfiguration: coreConfiguration,
                                                          stationID: 165_315, // Random number
                                                          reportZoomLevel: 22)
        do {
            try await mobility.start(mobilityConfiguration: mobilityConfiguration)
        } catch {
            throw .startFailed
        }

        let observer = await MobilityRoadAlarmChangeObserver()
        await mobility.setRoadAlarmObserver(observer)
        roadAlarmChangeObserver = observer

        startSendingPosition()
    }

    func stop() async throws(MobilityError) {
        stopSendingPosition()
        roadAlarmChangeObserver = nil
        do {
            try await mobility.stop()
        } catch {
            throw .stopFailed
        }
    }

    func startSendingPosition() {
        stopSendingPosition()
        sendPositionTask = Task {
            while !Task.isCancelled {
                let latitude = 48.866667
                let longitude = 2.333333
                try await mobility.updateRoadAlarmRegionOfInterest(latitude: latitude,
                                                                   longitude: longitude,
                                                                   zoomLevel: 15)
                try await mobility.sendPosition(latitude: latitude,
                                                longitude: longitude,
                                                altitude: 35,
                                                heading: 35,
                                                speed: 5)
                try await Task.sleep(for: .seconds(5))
            }
        }
    }

    func stopSendingPosition() {
        sendPositionTask?.cancel()
    }
}
