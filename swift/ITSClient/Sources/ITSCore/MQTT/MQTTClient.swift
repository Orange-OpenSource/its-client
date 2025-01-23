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

protocol MQTTClient: Actor {
    var isConnected: Bool { get }

    init(configuration: MQTTClientConfiguration)
    func setMessageReceivedHandler(messageReceivedHandler: (@escaping @Sendable (MQTTMessage) -> Void))
    func connect() async throws(MQTTClientError)
    func subscribe(to topic: String) async throws(MQTTClientError)
    func unsubscribe(from topic: String) async throws(MQTTClientError)
    func disconnect() async throws(MQTTClientError)
    func publish(_ message: MQTTMessage) async throws(MQTTClientError)
}
