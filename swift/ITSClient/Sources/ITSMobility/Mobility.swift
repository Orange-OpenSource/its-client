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

/// An object that manages a mobility client using the `Core`.
public actor Mobility {
    private let core: Core
    private let regionOfInterestCoordinator: RegionOfInterestCoordinator
    private var mobilityConfiguration: MobilityConfiguration?
    private let roadAlarmCoordinator: RoadAlarmCoordinator
    private let roadUserCoordinator: RoadUserCoordinator
    private var reportZoomLevel: Int

    /// Initializes a `Mobility`.
    public init() {
        core = Core()
        regionOfInterestCoordinator = RegionOfInterestCoordinator()
        roadAlarmCoordinator = RoadAlarmCoordinator()
        roadUserCoordinator = RoadUserCoordinator()
        reportZoomLevel = 22
    }

    /// Starts the `Mobility` with a configuration to connect to a MQTT server and initialize the telemetry client.
    /// - Parameter mobilityConfiguration: The configuration used to start the mobility including `CoreConfiguration`.
    /// - Throws: A `MobilityError` if the MQTT connection fails.
    public func start(mobilityConfiguration: MobilityConfiguration) async throws(MobilityError) {
        self.mobilityConfiguration = mobilityConfiguration
        do {
            try await core.start(coreConfiguration: mobilityConfiguration.coreConfiguration)
            await core.setMessageReceivedHandler { [weak self] message in
                Task {
                    await self?.processIncomingMessage(message)
                }
            }
        } catch {
            throw .startFailed(error)
        }
    }

    /// Starts the `Mobility` mainly with a bootstrap to connect to a MQTT server and initialize the telemetry client.
    /// - Parameters:
    ///   - bootstrap: The `Bootstrap` which can be retrieved with `BootstrapService`.
    ///   - stationID: The station identifier.
    ///   - telemetryServiceName: The telemetry service name (default: nil, no telemetry).
    public func start(
        bootstrap: Bootstrap,
        stationID: UInt32,
        telemetryServiceName: String? = nil
    ) async throws(MobilityError) {
        guard let mqttClientConfiguration = bootstrap.mqttClientConfiguration() else { throw .notStarted }

        var telemetryClientConfiguration: TelemetryClientConfiguration?
        if let telemetryServiceName {
            telemetryClientConfiguration = bootstrap.telemetryClientConfiguration(serviceName: telemetryServiceName)
        }

        let coreConfiguration = CoreConfiguration(mqttClientConfiguration: mqttClientConfiguration,
                                                  telemetryClientConfiguration: telemetryClientConfiguration)

        let mobilityConfiguration = MobilityConfiguration(coreConfiguration: coreConfiguration,
                                                          stationID: stationID,
                                                          namespace: bootstrap.mqttRootTopic)

        try await start(mobilityConfiguration: mobilityConfiguration)
    }

    /// Stops the `Mobility` disconnecting the MQTT client and stopping the telemetry client.
    public func stop() async {
        await core.stop()
    }

    /// Sets an observer to observe changes on road alarms.
    /// - Parameter observer: The `RoadAlarmChangeObserver` to set.
    public func setRoadAlarmObserver(_ observer: RoadAlarmChangeObserver) async {
        await roadAlarmCoordinator.setObserver(observer)
    }

    /// Sets an observer to observe changes on road users.
    /// - Parameter observer: The `RoadUserChangeObserver` to set.
    public func setRoadUserObserver(_ observer: RoadUserChangeObserver) async {
        await roadUserCoordinator.setObserver(observer)
    }

    /// Sets the report zoom level.
    /// - Parameter reportZoomLevel: The report zoom level.
    public func setReportZoomLevel(_ reportZoomLevel: Int) {
        self.reportZoomLevel = reportZoomLevel
    }

    /// Sends a position to share it.
    /// - Parameters:
    ///   - stationType: The user `StationType`.
    ///   - latitude: The latitude in decimal degrees.
    ///   - longitude: The longitude in decimal degrees.
    ///   - altitude: The altitude in meters.
    ///   - heading: The heading in degrees.
    ///   - speed: The speed in meters per second.
    ///   - acceleration: The longitudinal acceleration in meters per squared second.
    ///   - yawRate: The rotational acceleration in degrees per squared second.
    public func sendPosition(
        stationType: StationType,
        latitude: Double,
        longitude: Double,
        altitude: Double,
        heading: Double?,
        speed: Double?,
        acceleration: Double? = nil,
        yawRate: Double? = nil
    ) async throws(MobilityError) {
        guard let mobilityConfiguration else { throw .notStarted }

        // Build CAM
        let now = Date().timeIntervalSince1970
        let position = Position(latitude: latitude, longitude: longitude, altitude: altitude)
        let basicContainer = BasicContainer(stationType: stationType,
                                            referencePosition: position)
        let highFrequencyContainer = HighFrequencyContainer(heading: heading,
                                                            speed: speed,
                                                            longitudinalAcceleration: acceleration,
                                                            yawRate: yawRate)
        let camMessage = CAMMessage(stationID: mobilityConfiguration.stationID,
                                    generationDeltaTime: now,
                                    basicContainer: basicContainer,
                                    highFrequencyContainer: highFrequencyContainer)
        let cam = CAM(message: camMessage,
                      sourceUUID: mobilityConfiguration.userIdentifier,
                      timestamp: now)

        // Publish CAM
        try await sendCAM(cam)
    }

    /// Sends a `CAM` to share it.
    /// - Parameter cam: The `CAM` to send.
    public func sendCAM(_ cam: CAM) async throws(MobilityError)  {
        let quadkey = QuadkeyBuilder().quadkeyFrom(latitude: cam.message.basicContainer.referencePosition.latitude,
                                                   longitude: cam.message.basicContainer.referencePosition.longitude,
                                                   zoomLevel: reportZoomLevel,
                                                   separator: "/")
        try await publish(cam, topic: try topic(for: .cam, in: quadkey))
    }

    /// Sends an alert to share it.
    /// - Parameters:
    ///   - stationType: The user `StationType`.
    ///   - latitude: The latitude in decimal degrees.
    ///   - longitude: The longitude in decimal degrees.
    ///   - altitude: The altitude in meters.
    ///   - cause: The alert cause.
    public func sendAlert(
        stationType: StationType,
        latitude: Double,
        longitude: Double,
        altitude: Double,
        cause: Cause = .dangerousSituation()
    ) async throws(MobilityError) {
        guard let mobilityConfiguration else { throw MobilityError.notStarted }

        // Build DENM
        let now = Date().timeIntervalSince1970
        let actionID = ActionID(originatingStationID: mobilityConfiguration.stationID)
        let position = Position(latitude: latitude, longitude: longitude, altitude: altitude)
        let managementContainer = ManagementContainer(actionID: actionID,
                                                      detectionTime: now,
                                                      referenceTime: now,
                                                      eventPosition: position,
                                                      stationType: stationType)
        let situationContainer = SituationContainer(eventType: cause)
        let denmMessage = DENMMessage(stationID: mobilityConfiguration.stationID,
                                      managementContainer: managementContainer,
                                      situationContainer: situationContainer)
        let denm = DENM(message: denmMessage,
                        sourceUUID: mobilityConfiguration.userIdentifier,
                        timestamp: now)

        // Publish DENM
        let quadkey = QuadkeyBuilder().quadkeyFrom(latitude: latitude,
                                                   longitude: longitude,
                                                   zoomLevel: reportZoomLevel,
                                                   separator: "/")
        try await publish(denm, topic: try topic(for: .denm, in: quadkey))
    }

    /// Updates the road alarm region of interest according the coordinates and the zoom level.
    /// - Parameters:
    ///   - latitude: The latitude in decimal degrees.
    ///   - longitude: The longitude in decimal degrees.
    ///   - zoomLevel: The zoom level.
    public func updateRoadAlarmRegionOfInterest(
        latitude: Double,
        longitude: Double,
        zoomLevel: Int) async throws(MobilityError) {
        guard let mobilityConfiguration else { throw .notStarted }

        let topicUpdateRequest = regionOfInterestCoordinator.updateRoadAlarmRegionOfInterest(
            latitude: latitude,
            longitude: longitude,
            zoomLevel: zoomLevel,
            namespace: mobilityConfiguration.namespace)
        await updateSubscriptions(topicUpdateRequest: topicUpdateRequest)
    }

    /// Updates the road position region of interest according the coordinates and the zoom level.
    /// - Parameters:
    ///   - latitude: The latitude in decimal degrees.
    ///   - longitude: The longitude in decimal degrees.
    ///   - zoomLevel: The zoom level.
    public func updateRoadPositionRegionOfInterest(
        latitude: Double,
        longitude: Double,
        zoomLevel: Int) async throws(MobilityError) {
        guard let mobilityConfiguration else { throw .notStarted }

        let topicUpdateRequest = regionOfInterestCoordinator.updateRoadPositionRegionOfInterest(
            latitude: latitude,
            longitude: longitude,
            zoomLevel: zoomLevel,
            namespace: mobilityConfiguration.namespace)
        await updateSubscriptions(topicUpdateRequest: topicUpdateRequest)
    }

    private func publish<T: Codable>(_ payload: T, topic: String) async throws(MobilityError) {
        do {
            let coreMQTTMessage = CoreMQTTMessage(payload: try JSONEncoder().encode(payload),
                                                  topic: topic)
            try await core.publish(message: coreMQTTMessage)
        } catch let error as CoreError {
            throw .payloadPublishingFailed(error)
        } catch {
            throw .payloadEncodingFailed
        }
    }

    private func updateSubscriptions(
        topicUpdateRequest: RegionOfInterestCoordinator.TopicUpdateRequest?
    ) async {
        guard let topicUpdateRequest else { return }

        await subscribe(to: topicUpdateRequest.subscriptions)
        await unsubscribe(from: topicUpdateRequest.unsubscriptions)
    }

    private func subscribe(to topics: [String]) async {
        for topic in topics {
            do {
                try await core.subscribe(to: topic)
            } catch {}
        }
    }

    private func unsubscribe(from topics: [String]) async {
        for topic in topics {
            do {
                try await core.unsubscribe(from: topic)
            } catch {}
        }
    }

    private func processIncomingMessage(_ message: CoreMQTTMessage) async {
        if message.topic.contains(MessageType.denm.rawValue) {
            await roadAlarmCoordinator.handleRoadAlarm(withPayload: message.payload)
        } else if message.topic.contains(MessageType.cam.rawValue) {
            await roadUserCoordinator.handleRoadUser(withPayload: message.payload)
        }
    }

    private func topic(
        for messageType: MessageType,
        in quadkey: String
    ) throws(MobilityError) -> String {
        guard let mobilityConfiguration else { throw .notStarted }

        let namespace = mobilityConfiguration.namespace
        let userIdentifier = mobilityConfiguration.userIdentifier
        return "\(namespace)/inQueue/v2x/\(messageType.rawValue)/\(userIdentifier)/\(quadkey)"
    }
}
