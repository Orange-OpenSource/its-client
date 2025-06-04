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
import Testing

struct MockResponse {
    let httpCode: Int
    let data: Data
    let headers: [String: String]

    init(httpCode: Int, data: Data, headers: [String: String] = ["Content-Type": "application/json"]) {
        self.httpCode = httpCode
        self.data = data
        self.headers = headers
    }
}

// swiftlint:disable static_over_final_class
final class MockURLProtocol: URLProtocol {
    typealias MockHandler = (_ request: URLRequest) -> MockResponse

    nonisolated(unsafe) static var mockHandlers: [String: MockHandler]?

    override class func canInit(with request: URLRequest) -> Bool {
        true
    }

    override class func canonicalRequest(for request: URLRequest) -> URLRequest {
        request
    }

    override func stopLoading() {}

    override func startLoading() {
        guard let url = request.url else { return }

        do {
            guard let mockResponse = mockResponse(from: url)?(request) else {
                fatalError("Mock response not set")
            }

            let urlResponse = try #require(HTTPURLResponse(url: url,
                                                           statusCode: mockResponse.httpCode,
                                                           httpVersion: "2.0",
                                                           headerFields: mockResponse.headers))
            client?.urlProtocol(self, didReceive: urlResponse, cacheStoragePolicy: .notAllowed)
            client?.urlProtocol(self, didLoad: mockResponse.data)
            client?.urlProtocolDidFinishLoading(self)
        } catch {
            let error = NSError(domain: NSURLErrorDomain, code: NSURLErrorCannotParseResponse)
            client?.urlProtocol(self, didFailWithError: error)
        }
    }

    private func mockResponse(from url: URL) -> MockHandler? {
        guard let mockHandlers = Self.mockHandlers,
              let urlComponents = URLComponents(url: url, resolvingAgainstBaseURL: true) else { return nil }

        // 1 request, no need to check the path
        return mockHandlers.count == 1 ? mockHandlers.first?.value : mockHandlers[urlComponents.path]
    }

    class func setMock(mockHandler: @escaping MockHandler) {
        // Just one request, fake path to keep the API simple
        self.mockHandlers = ["Mock": mockHandler]
    }

    class func setMocks(mockHandlers: [String: MockHandler]) {
        self.mockHandlers = mockHandlers
    }
}
// swiftlint:enable static_over_final_class
