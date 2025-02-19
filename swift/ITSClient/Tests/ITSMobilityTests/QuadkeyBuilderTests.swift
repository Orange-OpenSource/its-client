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

import Testing
@testable import ITSMobility

struct QuadkeyBuilderTests {
    @Test("Quadkey must be generated from coordinates and zoom level")
    func quadkey_must_be_generated_from_coordinates_and_zoom_level() {
        // Given
        let builder = QuadkeyBuilder()

        // When
        let quadKey = builder.quadkeyFrom(latitude: 43.63516355648167,
                                          longitude: 1.3744570239910097,
                                          zoomLevel: 22)

        // Then
        #expect(quadKey == "1202220213331030003321")
    }

    @Test("Quadkey must be generated from coordinates and zoom level with separator")
    func quadkey_must_be_generated_from_coordinates_and_zoom_level_with_separator() {
        // Given
        let builder = QuadkeyBuilder()

        // When
        let quadKey = builder.quadkeyFrom(latitude: 43.63516355648167,
                                          longitude: 1.3744570239910097,
                                          zoomLevel: 22,
                                          separator: "/")

        // Then
        #expect(quadKey == "1/2/0/2/2/2/0/2/1/3/3/3/1/0/3/0/0/0/3/3/2/1")
    }


    @Test("Quadkey must have 8 neighbors if not in a edge")
    func quadkey_must_have_neighbors_if_not_in_a_edge() {
        // Given
        let builder = QuadkeyBuilder()
        let quadkey = "12030213"

        // When
        let neighborQuadkeys = builder.neighborQuadkeys(for: quadkey)

        // Then
        #expect(neighborQuadkeys.count == 8)
        #expect(neighborQuadkeys.contains("12030210"))
        #expect(neighborQuadkeys.contains("12030211"))
        #expect(neighborQuadkeys.contains("12030300"))
        #expect(neighborQuadkeys.contains("12030212"))
        #expect(neighborQuadkeys.contains("12030302"))
        #expect(neighborQuadkeys.contains("12030230"))
        #expect(neighborQuadkeys.contains("12030231"))
        #expect(neighborQuadkeys.contains("12030320"))
    }

    @Test("Quadkey must have 3 neighbors if it is at the top left edge")
    func quadkey_must_have_3_neighbors_if_top_left_edge() {
        // Given
        let builder = QuadkeyBuilder()
        let quadkey = "00"

        // When
        let neighborQuadkeys = builder.neighborQuadkeys(for: quadkey)

        // Then
        #expect(neighborQuadkeys.count == 3)
        #expect(neighborQuadkeys.contains("01"))
        #expect(neighborQuadkeys.contains("02"))
        #expect(neighborQuadkeys.contains("03"))
    }

    @Test("Quadkey must have 5 neighbors if it is at the top edge")
    func quadkey_must_have_5_neighbors_if_top_edge() {
        // Given
        let builder = QuadkeyBuilder()
        let quadkey = "01"

        // When
        let neighborQuadkeys = builder.neighborQuadkeys(for: quadkey)

        // Then
        #expect(neighborQuadkeys.count == 5)
        #expect(neighborQuadkeys.contains("00"))
        #expect(neighborQuadkeys.contains("10"))
        #expect(neighborQuadkeys.contains("02"))
        #expect(neighborQuadkeys.contains("03"))
        #expect(neighborQuadkeys.contains("12"))
    }

    @Test("Quadkey must have 3 neighbors if it is at the top right edge")
    func quadkey_must_have_3_neighbors_if_top_right_edge() {
        // Given
        let builder = QuadkeyBuilder()
        let quadkey = "11"

        // When
        let neighborQuadkeys = builder.neighborQuadkeys(for: quadkey)

        // Then
        #expect(neighborQuadkeys.count == 3)
        #expect(neighborQuadkeys.contains("10"))
        #expect(neighborQuadkeys.contains("12"))
        #expect(neighborQuadkeys.contains("13"))
    }

    @Test("Quadkey must have 3 neighbors if it is at the bottom left edge")
    func quadkey_must_have_3_neighbors_if_bottom_left_edge() {
        // Given
        let builder = QuadkeyBuilder()
        let quadkey = "22"

        // When
        let neighborQuadkeys = builder.neighborQuadkeys(for: quadkey)

        // Then
        #expect(neighborQuadkeys.count == 3)
        #expect(neighborQuadkeys.contains("20"))
        #expect(neighborQuadkeys.contains("21"))
        #expect(neighborQuadkeys.contains("23"))
    }

    @Test("Quadkey must have 5 neighbors if it is at the bottom edge")
    func quadkey_must_have_5_neighbors_if_bottom_edge() {
        // Given
        let builder = QuadkeyBuilder()
        let quadkey = "23"

        // When
        let neighborQuadkeys = builder.neighborQuadkeys(for: quadkey)

        // Then
        #expect(neighborQuadkeys.count == 5)
        #expect(neighborQuadkeys.contains("20"))
        #expect(neighborQuadkeys.contains("21"))
        #expect(neighborQuadkeys.contains("30"))
        #expect(neighborQuadkeys.contains("22"))
        #expect(neighborQuadkeys.contains("32"))
    }

    @Test("Quadkey must have 3 neighbors if it is at the bottom right edge")
    func quadkey_must_have_3_neighbors_if_bottom_right_edge() {
        // Given
        let builder = QuadkeyBuilder()
        let quadkey = "33"

        // When
        let neighborQuadkeys = builder.neighborQuadkeys(for: quadkey)

        // Then
        #expect(neighborQuadkeys.count == 3)
        #expect(neighborQuadkeys.contains("30"))
        #expect(neighborQuadkeys.contains("31"))
        #expect(neighborQuadkeys.contains("32"))
    }
}


