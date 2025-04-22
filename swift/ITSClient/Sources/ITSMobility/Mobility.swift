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

import ITSCore
import Foundation

/// An object that manages a mobility client using the `Core`.
public actor Mobility {
    private let core: Core
    private var mobilityConfiguration: MobilityConfiguration?
    
    /// Initializes a `Mobility`.
    public init() {
        core = Core()
    }

    /// Starts the `Mobility` with a configuration to connect to a MQTT server and initialize the telemetry client.
    /// - Parameter mobilityConfiguration: The configuration used to start the mobility including `CoreConfiguration`.
    /// - Throws: A `MobilityError` if the MQTT connection fails.
    public func start(mobilityConfiguration: MobilityConfiguration) async throws(MobilityError) {
        self.mobilityConfiguration = mobilityConfiguration
        do {
            try await core.start(coreConfiguration: mobilityConfiguration.coreConfiguration)
        } catch {
            throw .startFailed(error)
        }
    }
    
    /// Stops the `Mobility` disconnecting the MQTT client and stopping the telemetry client.
    /// - Throws: A `MobilityError` if the MQTT unsubscriptions or disconnection fails.
    public func stop() async throws(MobilityError) {
        do {
            try await core.stop()
        } catch {
            throw .stopFailed(error)
        }
    }
    
    /// Sends a position to share it.
    /// - Parameters:
    ///   - latitude: The latitude in decimal degrees.
    ///   - longitude: The longitude in decimal degrees.
    ///   - altitude: The altitude in meters.
    ///   - heading: The heading in degrees.
    ///   - speed: The speed in meters per second.
    ///   - acceleration: The longitudinal acceleration in meters per squared second.
    ///   - yawRate: The rotational acceleration in degrees per squared second.
    public func sendPosition(
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
        let basicContainer = BasicContainer(stationType: mobilityConfiguration.stationType,
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
        let quadkey = QuadkeyBuilder().quadkeyFrom(latitude: latitude,
                                                   longitude: longitude,
                                                   zoomLevel: mobilityConfiguration.reportZoomLevel,
                                                   separator: "/")
        try await publish(cam, topic: try topic(for: .cam, in: quadkey))
    }
    
    /// Sends an alert to share it.
    /// - Parameters:
    ///   - latitude: The latitude in decimal degrees.
    ///   - longitude: The longitude in decimal degrees.
    ///   - altitude: The altitude in meters.
    ///   - cause: The alert cause.
    public func sendAlert(
        latitude: Double,
        longitude: Double,
        altitude: Double,
        cause: CauseType = .dangerousSituation
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
                                                      stationType: mobilityConfiguration.stationType)
        let situationContainer = SituationContainer(eventType: Cause(cause: cause))
        let denmMessage = DENMMessage(stationID: mobilityConfiguration.stationID,
                                     managementContainer: managementContainer,
                                     situationContainer: situationContainer)
        let denm = DENM(message: denmMessage,
                        sourceUUID: mobilityConfiguration.userIdentifier,
                        timestamp: now)

        // Publish DENM
        let quadkey = QuadkeyBuilder().quadkeyFrom(latitude: latitude,
                                                   longitude: longitude,
                                                   zoomLevel: mobilityConfiguration.reportZoomLevel,
                                                   separator: "/")
        try await publish(denm, topic: try topic(for: .denm, in: quadkey))
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
