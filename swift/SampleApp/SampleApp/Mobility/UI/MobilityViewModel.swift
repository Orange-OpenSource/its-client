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

import SwiftUI

@MainActor
final class MobilityViewModel: ObservableObject {
    @Published var isStarted = false
    @Published var error: MobilityError?
    private let mobilityService: MobilityService

    init(mobilityService: MobilityService) {
        self.mobilityService = mobilityService
    }

    func start() async {
        do {
            try await mobilityService.start()
            isStarted = true
            error = nil
        } catch {
            self.error = error
        }
    }

    func stop() async {
        do {
            try await mobilityService.stop()
            isStarted = false
            error = nil
        } catch {
            self.error = error
        }
    }
}
