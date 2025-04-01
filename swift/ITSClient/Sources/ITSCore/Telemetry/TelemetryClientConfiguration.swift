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

/// A structure to configure a telemetry client.
public struct TelemetryClientConfiguration: Sendable {
    /// The server url.
    public let url: URL
    /// The user name if authentication is enabled on the server.
    public let user: String?
    /// The password if authentication is enabled on the server.
    public let password: String?
    /// The service name to use.
    public let serviceName: String
    /// The delay to send spans (Default: 5 seconds).
    public let scheduleDelay: TimeInterval
    /// The maximum of spans to send in a batch (Default: 50).
    public let batchSize: Int

    /// Initializes a `TelemetryClientConfiguration`.
    /// - Parameters:
    ///   - url: The server url.
    ///   - user: The user name if authentication is enabled on the server.
    ///   - password: The password if authentication is enabled on the server.
    ///   - serviceName: The service name to use.
    ///   - scheduleDelay: The delay to send spans (Default: 5 seconds).
    ///   - batchSize: The maximum of spans to send in a batch (Default: 50).
    public init(
        url: URL,
        user: String? = nil,
        password: String? = nil,
        serviceName: String,
        scheduleDelay: TimeInterval = 5,
        batchSize: Int = 50
    ) {
        self.url = url
        self.user = user
        self.password = password
        self.serviceName = serviceName
        self.scheduleDelay = scheduleDelay
        self.batchSize = batchSize
    }
}
