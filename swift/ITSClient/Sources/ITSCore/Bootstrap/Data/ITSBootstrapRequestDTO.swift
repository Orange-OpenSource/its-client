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

struct ITSBootstrapRequestDTO: Codable {
    let identifier: String
    let user: String
    let password: String
    let role: String

    enum CodingKeys: String, CodingKey {
        case identifier = "ue_id"
        case user = "psk_login"
        case password = "psk_password"
        case role
    }
}
