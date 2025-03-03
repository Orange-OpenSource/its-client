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
import Network

final class NetworkMonitor: Sendable {
    private let networkPathMonitor: NWPathMonitor

    init() {
        networkPathMonitor = NWPathMonitor()
    }

    func start() -> AsyncStream<NetworkStatus> {
        AsyncStream { continuation in
            networkPathMonitor.pathUpdateHandler = { [weak self] path in
                guard let self else { return }

                let networkStatus = networkStatus(from: path)
                continuation.yield(networkStatus)
            }

            continuation.onTermination = { @Sendable [weak self] _ in
                self?.stop()
            }

            networkPathMonitor.start(queue: DispatchQueue(label: "itsclient.networkmonitor"))
        }
    }

    func stop() {
        networkPathMonitor.cancel()
    }

    private func networkStatus(from path: NWPath) -> NetworkStatus {
        if path.status == .satisfied {
            return .connected(networkType(from: path))
        } else {
            return .disconnected
        }
    }

    private func networkType(from path: NWPath) -> NetworkType {
        if path.usesInterfaceType(.wifi) {
            return .wifi
        } else if path.usesInterfaceType(.cellular) {
            return .cellular
        } else if path.usesInterfaceType(.wiredEthernet) {
            return .ethernet
        }

        return .other
    }
}
