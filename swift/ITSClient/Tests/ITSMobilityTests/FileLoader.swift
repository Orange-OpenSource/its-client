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

enum FileLoader {
    static func loadJSONFile(_ filename: String) throws -> Data {
        guard let fileURL = Bundle.module.url(forResource: "Data/\(filename)", withExtension: "json") else {
            fatalError("File \(filename).json not found")
        }
        return try Data(contentsOf: fileURL)
    }
}
