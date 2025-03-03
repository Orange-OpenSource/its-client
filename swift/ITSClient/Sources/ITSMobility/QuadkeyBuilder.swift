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

struct QuadkeyBuilder {
    private struct Tile {
        let x: Int
        let y: Int
    }

    func quadkeyFrom(latitude: Double, longitude: Double, zoomLevel: Int, separator: String = "") -> String {
        let x = Int(floor((longitude + 180) / 360.0 * pow(2.0, Double(zoomLevel))))
        let y = Int(floor((1 - log(tan( latitude * Double.pi / 180.0 ) + 1 / cos( latitude * Double.pi / 180.0 )) / Double.pi ) / 2 * pow(2.0, Double(zoomLevel))))

        return quadkey(from: Tile(x: x, y: y), zoomLevel: zoomLevel, separator: separator)
    }

    func neighborQuadkeys(for quadkey: String) -> [String] {
        var quadkeys = [String]()
        let tile = tile(from: quadkey)
        let zoomLevel = quadkey.count
        let maxTileValue = Int(pow(2.0, Double(zoomLevel))) - 1

        // North-west
        if tile.y > 0 && tile.x > 0 {
            quadkeys.append(self.quadkey(from: Tile(x: tile.x - 1, y: tile.y - 1), zoomLevel: zoomLevel))
        }
        // North
        if tile.y > 0 {
            quadkeys.append(self.quadkey(from: Tile(x: tile.x, y: tile.y - 1), zoomLevel: zoomLevel))
        }
        // North-east
        if tile.y > 0 && tile.x < maxTileValue {
            quadkeys.append(self.quadkey(from: Tile(x: tile.x + 1, y: tile.y - 1), zoomLevel: zoomLevel))
        }
        // West
        if tile.x > 0 {
            quadkeys.append(self.quadkey(from: Tile(x: tile.x - 1, y: tile.y), zoomLevel: zoomLevel))
        }
        // East
        if tile.x < maxTileValue {
            quadkeys.append(self.quadkey(from: Tile(x: tile.x + 1, y: tile.y), zoomLevel: zoomLevel))
        }
        // South-west
        if tile.y < maxTileValue && tile.x > 0 {
            quadkeys.append(self.quadkey(from: Tile(x: tile.x - 1, y: tile.y + 1), zoomLevel: zoomLevel))
        }
        // South
        if tile.y < maxTileValue {
            quadkeys.append(self.quadkey(from: Tile(x: tile.x, y: tile.y + 1), zoomLevel: zoomLevel))
        }
        // South-east
        if tile.y < maxTileValue && tile.x < maxTileValue {
            quadkeys.append(self.quadkey(from: Tile(x: tile.x + 1, y: tile.y + 1), zoomLevel: zoomLevel))
        }

        return quadkeys
    }

    private func quadkey(from tile: Tile, zoomLevel: Int, separator: String = "") -> String {
        var quadkey = ""

        for i in stride(from: zoomLevel, to: 0, by: -1) {
            var digit = 0
            let mask = 1 << (i - 1)
            if (tile.x & mask) != 0 {
                digit += 1
            }
            if (tile.y & mask) != 0 {
                digit += 2
            }
            quadkey += String(digit)
        }

        return separator + quadkey.map { String($0) }.joined(separator: separator)
    }

    private func tile(from quadkey: String) -> Tile {
        var x = 0
        var y = 0
        let zoomLevel = quadkey.count

        for i in stride(from: zoomLevel, to: 0, by: -1) {
            let mask = 1 << (i - 1)
            let index = quadkey.index(quadkey.startIndex, offsetBy: zoomLevel - i)
            let digit = quadkey[index].wholeNumberValue
            switch digit {
            case 1:
                x |= mask
                break
            case 2:
                y |= mask
            case 3:
                x |= mask
                y |= mask
            default:
                break
            }
        }

        return Tile(x: x, y: y)
    }
}
