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

struct RegionOfInterest {
    let quadkey: String
    let neighborQuadkeys: [String]

    var allQuadkeys: [String] {
        [quadkey] + neighborQuadkeys
    }
}
