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

final class ITSBootstrapRepository: BootstrapRepository {
    enum MQTTScheme: String {
        case mqtt
        case mqtts
        case mqttWS = "mqtt+ws"
        case mqttWSS = "mqtt+wss"
    }

    enum TelemetryScheme: String {
        case http
        case https
    }

    private let url: URL
    private let urlSessionConfiguration: URLSessionConfiguration

    init(url: URL, urlSessionConfiguration: URLSessionConfiguration = .default) {
        self.url = url
        self.urlSessionConfiguration = urlSessionConfiguration
    }

    func bootstrap(bootstrapConfiguration: BootstrapConfiguration) async throws -> Bootstrap? {
        var urlRequest = URLRequest(url: url)
        urlRequest.httpMethod = "POST"
        let jsonMimeType = "application/json"
        urlRequest.setValue(jsonMimeType, forHTTPHeaderField: "Accept")
        urlRequest.setValue(jsonMimeType, forHTTPHeaderField: "Content-Type")
        let authorizationData = Data("\(bootstrapConfiguration.user):\(bootstrapConfiguration.password)".utf8)
        urlRequest.addValue("Basic \(authorizationData.base64EncodedString())", forHTTPHeaderField: "Authorization")
        let requestDTO = ITSBootstrapRequestDTO(identifier: bootstrapConfiguration.identifier,
                                                user: bootstrapConfiguration.user,
                                                password: bootstrapConfiguration.password,
                                                role: bootstrapConfiguration.role)
        urlRequest.httpBody = try? JSONEncoder().encode(requestDTO)

        let httpClient = HTTPClient()
        let bootstrapDTO: ITSBootstrapResponseDTO = try await httpClient.requestThenDecode(
            urlRequest: urlRequest,
            urlSessionConfiguration: urlSessionConfiguration)

        return bootstrap(from: bootstrapDTO)
    }

    private func bootstrap(from bootstrapDTO: ITSBootstrapResponseDTO) -> Bootstrap? {
        guard let (mqttURL, mqttScheme, rootTopic) = mqttURL(from: bootstrapDTO) else { return nil }

        return Bootstrap(identifier: bootstrapDTO.identifier,
                         user: bootstrapDTO.user,
                         password: bootstrapDTO.password,
                         mqttURL: mqttURL,
                         useMQTTSSL: mqttScheme == .mqtts || mqttScheme == .mqttWSS,
                         useMQTTWebSockets: mqttScheme == .mqttWS || mqttScheme == .mqttWSS,
                         mqttRootTopic: rootTopic.removingSuffix("/"),
                         telemetryURL: telemetryURL(from: bootstrapDTO))
    }

    private func mqttURL(from bootstrapDTO: ITSBootstrapResponseDTO) -> (URL, MQTTScheme, String)? {
        let mqttSchemes = [MQTTScheme.mqtts, MQTTScheme.mqttWSS, MQTTScheme.mqtt, MQTTScheme.mqttWS]
        for mqttScheme in mqttSchemes {
            // Use the more secure protocol available and a json payload type.
            if let mqttItem = bootstrapDTO.services.messages.first(where: { $0.url.scheme == mqttScheme.rawValue }),
               mqttItem.payload == .json {
                return (mqttItem.url, mqttScheme, mqttItem.rootTopic)
            }
        }

        return nil
    }

    private func telemetryURL(from bootstrapDTO: ITSBootstrapResponseDTO) -> URL? {
        for telemetryScheme in [TelemetryScheme.https, TelemetryScheme.http] {
            let telemetryItem = bootstrapDTO.services.telemetry?.first(where: {
                $0.url.scheme == telemetryScheme.rawValue
            })
            if let telemetryItem {
                return telemetryItem.url
            }
        }

        return nil
    }
}
