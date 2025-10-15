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
import ITSCore

/// The errors thrown by the mobility.
public enum MobilityError: Error, Equatable {
    /// The mobilty start failed.
    case startFailed(CoreError)
    /// The mobility must be started before performing this action.
    case notStarted
    /// The payload encoding failed.
    case payloadEncodingFailed
    /// The payload publishing failed.
    case payloadPublishingFailed(CoreError)
}
