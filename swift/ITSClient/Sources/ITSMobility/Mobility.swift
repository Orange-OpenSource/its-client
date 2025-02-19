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

    /// Initializes a `Mobility`.
    public init() {
        core = Core()
    }

    /// Starts the `Mobility` with a configuration to connect to a MQTT server and initialize the telemetry client.
    /// - Parameter coreConfiguration: The configuration used to start the MQTT client and the telemetry client.
    /// - Throws: A `CoreError` if the MQTT connection fails.
    public func start(coreConfiguration: CoreConfiguration) async throws(CoreError) {
        try await core.start(coreConfiguration: coreConfiguration)
    }

    /// Stops the `Mobility` disconnecting the MQTT client and stopping the telemetry client.
    /// - Throws: A `CoreError` if the MQTT unsubscriptions or disconnection fails.
    public func stop() async throws(CoreError) {
        try await core.stop()
    }
}
