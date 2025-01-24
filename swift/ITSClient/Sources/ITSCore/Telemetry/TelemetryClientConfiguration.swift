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

public struct TelemetryClientConfiguration: Sendable {
    let url: URL
    let user: String?
    let password: String?
    let serviceName: String
    let scheduleDelay: TimeInterval
    let batchSize: Int

    init(
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
