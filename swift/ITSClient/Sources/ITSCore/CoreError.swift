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

/// The errors thrown by the core.
public enum CoreError: Error, Equatable {
    /// The core must be started before performing this action.
    case notStarted
    /// A MQTT error occured.
    case mqttError(EquatableError)
}
