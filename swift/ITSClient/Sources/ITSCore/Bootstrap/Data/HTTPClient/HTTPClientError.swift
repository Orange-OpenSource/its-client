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

enum HTTPClientError: Error {
    case badURL
    case resourceNotLoaded(Int?)
    case resourceNotParseable
    case cancelled
    case resourceNotReachable(Error)
}
