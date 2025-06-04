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

/// The file loader.
public enum FileLoader {
    /// Load a JSON file located in Data directory for tests.
    /// - Parameters:
    ///   - filename: The filename.
    ///   - bundle: The bundle.
    /// - Returns: The contents of the JSON file.
    public static func loadJSONFile(_ filename: String, from bundle: Bundle) throws -> Data {
        guard let fileURL = bundle.url(forResource: "Data/\(filename)", withExtension: "json") else {
            fatalError("File \(filename).json not found")
        }
        return try Data(contentsOf: fileURL)
    }
}
