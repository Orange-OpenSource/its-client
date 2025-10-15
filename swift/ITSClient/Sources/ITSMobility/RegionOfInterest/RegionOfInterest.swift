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

struct RegionOfInterest: Equatable {
    private(set)var quadkeys: [String]

    mutating func addQuadkey(_ quadkey: String) {
        if !quadkeys.contains(quadkey) {
            quadkeys.append(quadkey)
        }
    }

    mutating func removeQuadkey(_ quadkey: String) {
        if let index = quadkeys.firstIndex(where: { quadkey == $0 }) {
            quadkeys.remove(at: index)
        }
    }
}
