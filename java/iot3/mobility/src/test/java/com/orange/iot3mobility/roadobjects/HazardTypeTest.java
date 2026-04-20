/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.roadobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HazardTypeTest {

    @Test
    void getHazardTypeReturnsCorrectTypeForKnownCauseAndSubcause() {
        // cause=1 (TRAFFIC_CONDITION), subcause=0 → TRAFFIC_CONDITION_NO_SUBCAUSE
        assertEquals(HazardType.TRAFFIC_CONDITION_NO_SUBCAUSE,
                HazardType.getHazardType(1, 0));

        // cause=1, subcause=5 → TRAFFIC_STATIONARY
        assertEquals(HazardType.TRAFFIC_STATIONARY,
                HazardType.getHazardType(1, 5));

        // cause=2 (ACCIDENT), subcause=1 → MULTI_VEHICLE_ACCIDENT
        assertEquals(HazardType.MULTI_VEHICLE_ACCIDENT,
                HazardType.getHazardType(2, 1));

        // cause=3 (ROADWORKS), subcause=0 → ROADWORKS_NO_SUBCAUSE
        assertEquals(HazardType.ROADWORKS_NO_SUBCAUSE,
                HazardType.getHazardType(3, 0));

        // cause=6 (ADVERSE_WEATHER_CONDITION_ADHESION), subcause=5 → ICE_ON_ROAD
        assertEquals(HazardType.ICE_ON_ROAD,
                HazardType.getHazardType(6, 5));

        // cause=91 (VEHICLE_BREAKDOWN), subcause=1 → LACK_OF_FUEL
        assertEquals(HazardType.LACK_OF_FUEL,
                HazardType.getHazardType(91, 1));
    }

    @Test
    void getHazardTypeReturnsUndefinedForUnknownCombination() {
        assertEquals(HazardType.UNDEFINED, HazardType.getHazardType(999, 999));
        assertEquals(HazardType.UNDEFINED, HazardType.getHazardType(-1, 0));
        assertEquals(HazardType.UNDEFINED, HazardType.getHazardType(0, 99));
    }

    @Test
    void getCauseReturnsCorrectEtsiCauseCode() {
        assertEquals(1, HazardType.TRAFFIC_STATIONARY.getCause());
        assertEquals(2, HazardType.HEAVY_ACCIDENT.getCause());
        assertEquals(91, HazardType.VEHICLE_BREAKDOWN_NO_SUBCAUSE.getCause());
    }

    @Test
    void getSubcauseReturnsCorrectSubcauseCode() {
        assertEquals(0, HazardType.TRAFFIC_CONDITION_NO_SUBCAUSE.getSubcause());
        assertEquals(5, HazardType.TRAFFIC_STATIONARY.getSubcause());
        assertEquals(1, HazardType.LACK_OF_FUEL.getSubcause());
    }

    @Test
    void getCategoryReturnsCorrectCategory() {
        assertEquals(HazardCategory.TRAFFIC_CONDITION,
                HazardType.TRAFFIC_STATIONARY.getCategory());
        assertEquals(HazardCategory.ACCIDENT,
                HazardType.MULTI_VEHICLE_ACCIDENT.getCategory());
        assertEquals(HazardCategory.VEHICLE_BREAKDOWN,
                HazardType.LACK_OF_FUEL.getCategory());
        assertEquals(HazardCategory.UNDEFINED,
                HazardType.UNDEFINED.getCategory());
    }

    @Test
    void getNameReturnsNonBlankString() {
        for (HazardType type : HazardType.values()) {
            assertFalse(type.getName().isBlank(),
                    "HazardType " + type + " has a blank name");
        }
    }

    @Test
    void getHazardTypeIsConsistentWithEnumValues() {
        // Every enum value must be findable by its own cause+subcause
        for (HazardType type : HazardType.values()) {
            // Skip UNDEFINED since it has cause=0/subcause=0 which may be
            // ambiguous with other entries; just verify the lookup doesn't crash
            HazardType found = HazardType.getHazardType(type.getCause(), type.getSubcause());
            assertNotNull(found);
            // The found type must have the same cause and subcause
            assertEquals(type.getCause(), found.getCause());
            assertEquals(type.getSubcause(), found.getSubcause());
        }
    }
}

