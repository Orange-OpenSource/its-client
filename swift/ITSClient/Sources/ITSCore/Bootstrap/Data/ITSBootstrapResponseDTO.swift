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

struct ITSBootstrapResponseDTO: Decodable {
    let identifier: String
    let user: String
    let password: String
    let services: ITSBootstrapServiceDTO

    enum CodingKeys: String, CodingKey {
        case identifier = "iot3_id"
        case user = "psk_iot3_id"
        case password = "psk_iot3_secret"
        case services
    }
}

struct ITSBootstrapServiceDTO: Codable {
    let messages: [ITSBootstrapMQTTItemDTO]
    let telemetry: [ITSBootstrapTelemetryItemDTO]?

    enum CodingKeys: String, CodingKey {
        case messages = "message"
        case telemetry
    }
}

struct ITSBootstrapMQTTItemDTO: Codable {
    let payload: ITSBootstrapMQTTPayloadDTO
    let url: URL
    let rootTopic: String

    enum CodingKeys: String, CodingKey {
        case payload
        case url = "uri"
        case rootTopic = "topic_root"
    }
}

struct ITSBootstrapTelemetryItemDTO: Codable {
    let payload: ITSBootstrapTelemetryPayloadDTO
    let url: URL

    enum CodingKeys: String, CodingKey {
        case payload
        case url = "uri"
    }
}

enum ITSBootstrapMQTTPayloadDTO: String, Codable {
    case json
    case binary
}

enum ITSBootstrapTelemetryPayloadDTO: String, Codable {
    case otlpJSON = "otlp/json"
    case otlpGRPC = "otlp/grpc"
}
