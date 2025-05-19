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

#if os(macOS)

import Foundation

/// A structure to enable or disable network only on mac OS.
struct NetworkManager {
    func enableNetwork() {
        let (_, disabledServices) = retrieveNetworkServices()
        for service in disabledServices {
            shell("networksetup -setnetworkserviceenabled '\(service)' on")
        }
    }

    func disableNetwork() {
        let (enabledServices, _) = retrieveNetworkServices()
        for service in enabledServices {
            shell("networksetup -setnetworkserviceenabled '\(service)' off")
        }
    }

    private func retrieveNetworkServices() -> (enabledServices: [String], disableServices: [String]) {
        let services = shell("networksetup -listallnetworkservices")
        var enabledServices = [String]()
        var disabledServices = [String]()
        services.enumerateLines { line, _ in
            let explanationLine = "An asterisk (*) denotes that a network service is disabled."
            guard !line.hasPrefix(explanationLine) else { return }

            if line.hasPrefix("*") {
                disabledServices.append(String(line.dropFirst()))
            } else {
                enabledServices.append(line)
            }
        }

        return (enabledServices, disabledServices)
    }

    @discardableResult
    private func shell(_ command: String) -> String {
        let task = Process()
        let pipe = Pipe()

        task.standardOutput = pipe
        task.standardError = pipe
        task.arguments = ["-c", command]
        task.launchPath = "/bin/zsh"
        task.standardInput = nil
        task.launch()

        let data = pipe.fileHandleForReading.readDataToEndOfFile()
        let output = #require(String(data: data, encoding: .utf8))

        return output
    }
}

#endif
