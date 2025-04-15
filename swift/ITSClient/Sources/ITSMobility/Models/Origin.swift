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

/// The entity responsible for this message.
public enum Origin: String, Codable {
    case globalApplication = "global_application"
    case mecApplication = "mec_application"
    case onBoardApplication = "on_board_application"
    case originSelf = "self"
}
