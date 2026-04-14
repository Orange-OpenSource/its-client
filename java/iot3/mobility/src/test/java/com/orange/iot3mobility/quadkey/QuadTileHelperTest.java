/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.
 */
package com.orange.iot3mobility.quadkey;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuadTileHelperTest {

    // -------------------------------------------------------------------------
    // tileXYToQuadKey
    // -------------------------------------------------------------------------

    @Test
    void tileXYToQuadKeyZoom1NW() {
        // Tile (0,0) at zoom 1 = NW quadrant
        assertEquals("0", QuadTileHelper.tileXYToQuadKey(new TileXY(0, 0), 1));
    }

    @Test
    void tileXYToQuadKeyZoom1NE() {
        // Tile (1,0) at zoom 1 = NE quadrant
        assertEquals("1", QuadTileHelper.tileXYToQuadKey(new TileXY(1, 0), 1));
    }

    @Test
    void tileXYToQuadKeyZoom1SW() {
        // Tile (0,1) at zoom 1 = SW quadrant
        assertEquals("2", QuadTileHelper.tileXYToQuadKey(new TileXY(0, 1), 1));
    }

    @Test
    void tileXYToQuadKeyZoom1SE() {
        // Tile (1,1) at zoom 1 = SE quadrant
        assertEquals("3", QuadTileHelper.tileXYToQuadKey(new TileXY(1, 1), 1));
    }

    @Test
    void tileXYToQuadKeyZoom2SubtilesOf0() {
        // The four sub-tiles of "0" are "00", "01", "02", "03"
        assertEquals("00", QuadTileHelper.tileXYToQuadKey(new TileXY(0, 0), 2));
        assertEquals("01", QuadTileHelper.tileXYToQuadKey(new TileXY(1, 0), 2));
        assertEquals("02", QuadTileHelper.tileXYToQuadKey(new TileXY(0, 1), 2));
        assertEquals("03", QuadTileHelper.tileXYToQuadKey(new TileXY(1, 1), 2));
    }

    @Test
    void tileXYToQuadKeyLengthEqualsZoomLevel() {
        for (int zoom = 1; zoom <= 8; zoom++) {
            String key = QuadTileHelper.tileXYToQuadKey(new TileXY(0, 0), zoom);
            assertEquals(zoom, key.length());
        }
    }

    @Test
    void tileXYToQuadKeyOnlyContainsValidDigits() {
        String key = QuadTileHelper.tileXYToQuadKey(new TileXY(12345, 6789), 14);
        assertTrue(key.matches("[0-3]+"), "QuadKey must only contain digits 0-3");
    }

    // -------------------------------------------------------------------------
    // quadKeyToTileXY
    // -------------------------------------------------------------------------

    @Test
    void quadKeyToTileXYRoundTrip() {
        String[] keys = {"0", "1", "2", "3", "00", "13", "23", "031", "0213"};
        for (String key : keys) {
            TileXY tile = QuadTileHelper.quadKeyToTileXY(key);
            assertEquals(key, QuadTileHelper.tileXYToQuadKey(tile, key.length()),
                    "Round-trip failed for key: " + key);
        }
    }

    @Test
    void quadKeyToTileXYKnownValues() {
        TileXY tile0 = QuadTileHelper.quadKeyToTileXY("0");
        assertEquals(0, tile0.getTileX());
        assertEquals(0, tile0.getTileY());

        TileXY tile3 = QuadTileHelper.quadKeyToTileXY("3");
        assertEquals(1, tile3.getTileX());
        assertEquals(1, tile3.getTileY());

        TileXY tile1 = QuadTileHelper.quadKeyToTileXY("1");
        assertEquals(1, tile1.getTileX());
        assertEquals(0, tile1.getTileY());

        TileXY tile2 = QuadTileHelper.quadKeyToTileXY("2");
        assertEquals(0, tile2.getTileX());
        assertEquals(1, tile2.getTileY());
    }

    // -------------------------------------------------------------------------
    // latLngToQuadKey
    // -------------------------------------------------------------------------

    @Test
    void latLngToQuadKeyOriginIsInSEQuadrantAtZoom1() {
        // (0, 0) falls in tile (1,1) → "3"
        assertEquals("3", QuadTileHelper.latLngToQuadKey(0.0, 0.0, 1));
    }

    @Test
    void latLngToQuadKeySWQuadrantAtZoom1() {
        // (-45, -90) falls in SW tile → "2"
        assertEquals("2", QuadTileHelper.latLngToQuadKey(-45.0, -90.0, 1));
    }

    @Test
    void latLngToQuadKeyNWQuadrantAtZoom1() {
        // (45, -90) falls in NW tile → "0"
        assertEquals("0", QuadTileHelper.latLngToQuadKey(45.0, -90.0, 1));
    }

    @Test
    void latLngToQuadKeyNEQuadrantAtZoom1() {
        // (45, 90) falls in NE tile → "1"
        assertEquals("1", QuadTileHelper.latLngToQuadKey(45.0, 90.0, 1));
    }

    @Test
    void latLngToQuadKeyLengthEqualsZoomLevel() {
        // Paris
        String key14 = QuadTileHelper.latLngToQuadKey(48.8566, 2.3522, 14);
        assertEquals(14, key14.length());
        assertTrue(key14.matches("[0-3]+"));

        String key8 = QuadTileHelper.latLngToQuadKey(48.8566, 2.3522, 8);
        assertEquals(8, key8.length());
    }

    @Test
    void latLngToQuadKeyHigherZoomIsPrefixedByLowerZoom() {
        // The zoom-14 key for Paris must start with the zoom-8 key for Paris
        String key8 = QuadTileHelper.latLngToQuadKey(48.8566, 2.3522, 8);
        String key14 = QuadTileHelper.latLngToQuadKey(48.8566, 2.3522, 14);
        assertTrue(key14.startsWith(key8),
                "Zoom-14 key must start with zoom-8 key for the same location");
    }

    @Test
    void latLngToQuadKeyClipsOutOfRangeCoordinates() {
        // Should not throw, clips to valid range
        assertDoesNotThrow(() -> QuadTileHelper.latLngToQuadKey(91.0, 181.0, 8));
        assertDoesNotThrow(() -> QuadTileHelper.latLngToQuadKey(-91.0, -181.0, 8));
    }

    // -------------------------------------------------------------------------
    // Pixel / LatLng round-trip
    // -------------------------------------------------------------------------

    @Test
    void latLongToPixelXYToLatLongRoundTrip() {
        double lat = 48.8566;
        double lon = 2.3522;
        int zoom = 14;

        PixelXY pixel = QuadTileHelper.latLongToPixelXY(lat, lon, zoom);
        LatLng recovered = QuadTileHelper.pixelXYToLatLong(pixel, zoom);

        assertEquals(lat, recovered.getLatitude(), 0.01);
        assertEquals(lon, recovered.getLongitude(), 0.01);
    }

    @Test
    void pixelXYToTileXYAndBack() {
        PixelXY pixel = new PixelXY(512, 768);
        TileXY tile = QuadTileHelper.pixelXYToTileXY(pixel);
        assertEquals(2, tile.getTileX());
        assertEquals(3, tile.getTileY());
    }

    @Test
    void tileXYToPixelXYCorners() {
        TileXY tile = new TileXY(2, 3);

        PixelXY upLeft = QuadTileHelper.tileXYToPixelXYUpLeft(tile);
        assertEquals(512, upLeft.getPixelX());
        assertEquals(768, upLeft.getPixelY());

        PixelXY lowRight = QuadTileHelper.tileXYToPixelXYLowRight(tile);
        assertEquals(768, lowRight.getPixelX());
        assertEquals(1024, lowRight.getPixelY());

        PixelXY upRight = QuadTileHelper.tileXYToPixelXYUpRight(tile);
        assertEquals(768, upRight.getPixelX());
        assertEquals(768, upRight.getPixelY());

        PixelXY lowLeft = QuadTileHelper.tileXYToPixelXYLowLeft(tile);
        assertEquals(512, lowLeft.getPixelX());
        assertEquals(1024, lowLeft.getPixelY());
    }

    // -------------------------------------------------------------------------
    // getNeighborQuadKeys
    // -------------------------------------------------------------------------

    @Test
    void getNeighborQuadKeysReturnsExactlyEight() {
        ArrayList<String> neighbors = QuadTileHelper.getNeighborQuadKeys("0213");
        assertEquals(8, neighbors.size());
    }

    @Test
    void getNeighborQuadKeysDoesNotContainCenter() {
        String center = "0213";
        ArrayList<String> neighbors = QuadTileHelper.getNeighborQuadKeys(center);
        assertFalse(neighbors.contains(center));
    }

    @Test
    void getNeighborQuadKeysSameLengthAsInput() {
        ArrayList<String> neighbors = QuadTileHelper.getNeighborQuadKeys("12030");
        for (String neighbor : neighbors) {
            assertEquals(5, neighbor.length());
        }
    }

    @Test
    void getNeighborQuadKeysAreSymmetric() {
        // If B is a neighbor of A, then A must be a neighbor of B
        String center = "0213";
        ArrayList<String> neighbors = QuadTileHelper.getNeighborQuadKeys(center);
        for (String neighbor : neighbors) {
            ArrayList<String> neighborOfNeighbor = QuadTileHelper.getNeighborQuadKeys(neighbor);
            assertTrue(neighborOfNeighbor.contains(center),
                    center + " should be a neighbor of " + neighbor);
        }
    }

    // -------------------------------------------------------------------------
    // substractKeys
    // -------------------------------------------------------------------------

    @Test
    void substractKeysNoOverlapKeepsAllTargetKeys() {
        ArrayList<String> target = new ArrayList<>(List.of("0", "1", "2", "3"));
        ArrayList<String> extract = new ArrayList<>(); // empty
        ArrayList<String> result = QuadTileHelper.substractKeys(target, extract);
        assertEquals(4, result.size());
        assertTrue(result.containsAll(target));
    }

    @Test
    void substractKeysExactMatchRemovesKey() {
        ArrayList<String> target = new ArrayList<>(List.of("0"));
        ArrayList<String> extract = new ArrayList<>(List.of("0"));
        ArrayList<String> result = QuadTileHelper.substractKeys(target, extract);
        assertTrue(result.isEmpty());
    }

    @Test
    void substractKeysSubKeyTriggersSubdivision() {
        // target = ["0"], extract = ["00"] → "0" is split; "01","02","03" survive, "00" is removed
        ArrayList<String> target = new ArrayList<>(List.of("0"));
        ArrayList<String> extract = new ArrayList<>(List.of("00"));
        ArrayList<String> result = QuadTileHelper.substractKeys(target, extract);
        assertEquals(3, result.size());
        assertTrue(result.contains("01"));
        assertTrue(result.contains("02"));
        assertTrue(result.contains("03"));
        assertFalse(result.contains("00"));
    }

    @Test
    void substractKeysDeepSubdivision() {
        // target = ["0"], extract = ["000"] → splits down 2 levels
        // Level 1: "0" splits → "00","01","02","03"
        // "00" is still a prefix of "000", splits → "000","001","002","003"
        // "000" is removed; "001","002","003" survive
        // Final: "001","002","003","01","02","03"
        ArrayList<String> target = new ArrayList<>(List.of("0"));
        ArrayList<String> extract = new ArrayList<>(List.of("000"));
        ArrayList<String> result = QuadTileHelper.substractKeys(target, extract);
        assertEquals(6, result.size());
        assertTrue(result.contains("001"));
        assertTrue(result.contains("002"));
        assertTrue(result.contains("003"));
        assertTrue(result.contains("01"));
        assertTrue(result.contains("02"));
        assertTrue(result.contains("03"));
        assertFalse(result.contains("000"));
        assertFalse(result.contains("0"));
    }

    @Test
    void substractKeysNonOverlappingExtractKeepsAll() {
        ArrayList<String> target = new ArrayList<>(List.of("0", "1"));
        ArrayList<String> extract = new ArrayList<>(List.of("2", "3"));
        ArrayList<String> result = QuadTileHelper.substractKeys(target, extract);
        assertEquals(2, result.size());
        assertTrue(result.contains("0"));
        assertTrue(result.contains("1"));
    }

    // -------------------------------------------------------------------------
    // quadKeyToQuadTopic
    // -------------------------------------------------------------------------

    @Test
    void quadKeyToQuadTopicFormatsCorrectly() {
        assertEquals("/0", QuadTileHelper.quadKeyToQuadTopic("0"));
        assertEquals("/1/2/3", QuadTileHelper.quadKeyToQuadTopic("123"));
        assertEquals("/0/2/1/3", QuadTileHelper.quadKeyToQuadTopic("0213"));
    }

    @Test
    void quadKeyToQuadTopicEmptyKeyProducesEmptyTopic() {
        assertEquals("", QuadTileHelper.quadKeyToQuadTopic(""));
    }
}

