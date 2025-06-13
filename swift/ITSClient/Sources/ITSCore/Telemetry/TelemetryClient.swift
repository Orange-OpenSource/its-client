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

protocol TelemetryClient: Actor {
    init(configuration: TelemetryClientConfiguration) async
    func start()
    func stop()
    func startSpan(name: String, type: SpanType, attributes: [String: Any]) -> SpanID?
    func startSpan(
        name: String,
        type: SpanType,
        attributes: [String: Any],
        fromContext context: [String: String]
    ) -> SpanID?
    func stopSpan(spanID: SpanID, errorMessage: String?)
    func updateContext(withSpanID spanID: SpanID) -> [String: String]
}

extension TelemetryClient {
    func stopSpan(spanID: SpanID) {
        stopSpan(spanID: spanID, errorMessage: nil)
    }
}

typealias SpanID = String

enum SpanType {
    case consumer, producer
}
