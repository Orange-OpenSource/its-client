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
        do {
            // Build your configuration manually and start
            let coreConfiguration = buildCoreConfiguration()
            let mobilityConfiguration = MobilityConfiguration(coreConfiguration: coreConfiguration,
                                                              stationID: 165_315) // Random number

            try await mobility.start(mobilityConfiguration: mobilityConfiguration)

            // or bootstrap it and start
            /*let bootstrap = await bootstrapConfiguration()

            guard let bootstrap = bootstrap  else { throw MobilityError.startFailed }

            try await mobility.start(bootstrap: bootstrap,
                                     stationID: 165_315)*/
        } catch {
            throw .startFailed
        }

        let observer = await MobilityRoadAlarmChangeObserver()
        await mobility.setRoadAlarmObserver(observer)
        roadAlarmChangeObserver = observer

        startSendingPosition()
    }

    func stop() async {
        stopSendingPosition()
        roadAlarmChangeObserver = nil
        await mobility.stop()
    }

    private func buildCoreConfiguration() -> CoreConfiguration {
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

        return CoreConfiguration(mqttClientConfiguration: mqttClientConfiguration)
    }

    private func bootstrapConfiguration() async -> Bootstrap? {
        // Update this to set your bootstrap server url and configuration
        guard let url = URL(string: "") else { return nil }

        let service = BootstrapService(url: url)
        let bootstrapConfiguration = BootstrapConfiguration(identifier: "swift-sample-app",
                                                            user: "",
                                                            password: "",
                                                            role: "external-app")
        return try? await service.bootstrap(bootstrapConfiguration: bootstrapConfiguration)
    }

    private func startSendingPosition() {
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

    private func stopSendingPosition() {
        sendPositionTask?.cancel()
    }
}
