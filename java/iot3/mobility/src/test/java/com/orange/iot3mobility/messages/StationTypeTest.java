/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StationTypeTest {

    @Test
    void fromValueReturnsCorrectEnumForKnownValues() {
        assertEquals(StationType.UNKNOWN, StationType.fromValue(0));
        assertEquals(StationType.PEDESTRIAN, StationType.fromValue(1));
        assertEquals(StationType.CYCLIST, StationType.fromValue(2));
        assertEquals(StationType.MOPED, StationType.fromValue(3));
        assertEquals(StationType.MOTORCYCLE, StationType.fromValue(4));
        assertEquals(StationType.PASSENGER_CAR, StationType.fromValue(5));
        assertEquals(StationType.BUS, StationType.fromValue(6));
        assertEquals(StationType.LIGHT_TRUCK, StationType.fromValue(7));
        assertEquals(StationType.HEAVY_TRUCK, StationType.fromValue(8));
        assertEquals(StationType.TRAILER, StationType.fromValue(9));
        assertEquals(StationType.SPECIAL_VEHICLES, StationType.fromValue(10));
        assertEquals(StationType.TRAM, StationType.fromValue(11));
        assertEquals(StationType.ROAD_SIDE_UNIT, StationType.fromValue(15));
    }

    @Test
    void fromValueReturnsUnknownForUnrecognizedValue() {
        assertEquals(StationType.UNKNOWN, StationType.fromValue(99));
        assertEquals(StationType.UNKNOWN, StationType.fromValue(-1));
        assertEquals(StationType.UNKNOWN, StationType.fromValue(255));
    }

    @Test
    void intValueMatchesEtsiStandardCodes() {
        assertEquals(0, StationType.UNKNOWN.value);
        assertEquals(5, StationType.PASSENGER_CAR.value);
        assertEquals(15, StationType.ROAD_SIDE_UNIT.value);
    }

    @Test
    void fromValueRoundTrip() {
        for (StationType type : StationType.values()) {
            assertEquals(type, StationType.fromValue(type.value));
        }
    }
}

