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

struct MobilityView: View {
    @StateObject private var viewModel: MobilityViewModel
    @State private var showAlert = false

    init(mobileService: MobilityService) {
        _viewModel = .init(wrappedValue: .init(mobilityService: mobileService))
    }

    var body: some View {
        VStack {
            Button(viewModel.isStarted ? "Stop" : "Start") {
                Task {
                    if viewModel.isStarted {
                        await viewModel.stop()
                    } else {
                        showAlert = await !viewModel.start()
                    }
                }
            }
            .buttonStyle(.borderedProminent)
        }
        .alert("Error", isPresented: $showAlert, actions: {}, message: {
            Text("Start mobility failed")
        })
    }
}

#Preview {
    MobilityView(mobileService: ITSMobilityService())
}
