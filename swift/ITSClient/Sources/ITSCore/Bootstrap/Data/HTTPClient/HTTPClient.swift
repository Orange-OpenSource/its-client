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

struct HTTPClient {
    func requestThenDecode<T: Decodable>(
        urlRequest: URLRequest?,
        urlSessionConfiguration: URLSessionConfiguration = .default
    ) async throws(HTTPClientError) -> T {
        guard let urlRequest else { throw .badURL }

        let httpResult = try await request(for: urlRequest, urlSessionConfiguration: urlSessionConfiguration)

        let httpURLResponse = httpResult.1 as? HTTPURLResponse

        guard httpURLResponse?.statusCode == 200 else {
            throw .resourceNotLoaded(httpURLResponse?.statusCode)
        }

        do {
            return try JSONDecoder().decode(T.self, from: httpResult.0)
        } catch {
            throw .resourceNotParseable
        }
    }

    private func request(
        for urlRequest: URLRequest,
        urlSessionConfiguration: URLSessionConfiguration
    ) async throws(HTTPClientError) -> (Data, URLResponse) {
        let urlSession = URLSession(configuration: urlSessionConfiguration)

        do {
            return try await urlSession.data(for: urlRequest)
        } catch {
            if let error = error as? URLError, error.code == .cancelled {
                throw .cancelled
            } else {
                throw .resourceNotReachable(error)
            }
        }
    }
}
