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
                        await viewModel.start()
                    }
                }
            }
            .buttonStyle(.borderedProminent)
        }
        .onChange(of: viewModel.error) { newValue in
            showAlert = newValue != nil
        }
        .alert("Error", isPresented: $showAlert, actions: {}, message: {
            errorView
        })
    }

    @ViewBuilder
    private var errorView: some View {
        switch viewModel.error {
        case .startFailed:
            Text("Start mobility failed")
        case .stopFailed:
            Text("Stop mobility failed")
        default:
            EmptyView()
        }
    }
}

#Preview {
    MobilityView(mobileService: ITSMobilityService())
}
