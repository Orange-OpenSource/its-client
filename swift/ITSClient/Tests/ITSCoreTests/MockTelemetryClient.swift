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
@testable import ITSCore

actor MockTelemetryClient: TelemetryClient {
    var startCallsCount: Int = 0
    var stopCallsCount: Int = 0
    var startSpanCallsCount: Int = 0
    var startSpanWithContextCallsCount: Int = 0
    var stopSpanCallsCount: Int = 0

    init(configuration: TelemetryClientConfiguration) {
    }

    func start() {
        startCallsCount += 1
    }

    func stop() {
        stopCallsCount += 1
    }

    func startSpan(name: String, type: SpanType, attributes: [String: Any]) -> SpanID? {
        startSpanCallsCount += 1
        return "span1"
    }

    func startSpan(
        name: String,
        type: SpanType,
        attributes: [String: Any],
        fromContext context: [String: String]
    ) -> SpanID? {
        startSpanWithContextCallsCount += 1
        return "span2"
    }

    func stopSpan(spanID: SpanID, errorMessage: String?) {
        stopSpanCallsCount += 1
    }

    func updateContext(withSpanID spanID: SpanID) -> [String: String] {
        [:]
    }
}
