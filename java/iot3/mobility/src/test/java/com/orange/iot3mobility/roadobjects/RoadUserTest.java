/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.roadobjects;

import com.orange.iot3mobility.messages.StationType;
import com.orange.iot3mobility.quadkey.LatLng;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoadUserTest {

    private static RoadUser makeRoadUser(StationType stationType) {
        return new RoadUser(
                "test-uuid",
                stationType,
                new LatLng(48.8566, 2.3522),
                13.89,       // ~50 km/h in m/s
                90.0,
                null);
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    @Test
    void getUuidReturnsConstructedValue() {
        RoadUser user = makeRoadUser(StationType.PASSENGER_CAR);
        assertEquals("test-uuid", user.getUuid());
    }

    @Test
    void getStationTypeReturnsConstructedValue() {
        RoadUser user = makeRoadUser(StationType.PASSENGER_CAR);
        assertEquals(StationType.PASSENGER_CAR, user.getStationType());
    }

    @Test
    void getPositionReturnsConstructedValue() {
        RoadUser user = makeRoadUser(StationType.PASSENGER_CAR);
        assertEquals(48.8566, user.getPosition().getLatitude(), 1e-9);
        assertEquals(2.3522, user.getPosition().getLongitude(), 1e-9);
    }

    @Test
    void getSpeedReturnsConstructedValue() {
        RoadUser user = makeRoadUser(StationType.PASSENGER_CAR);
        assertEquals(13.89, user.getSpeed(), 1e-9);
    }

    @Test
    void getHeadingReturnsConstructedValue() {
        RoadUser user = makeRoadUser(StationType.PASSENGER_CAR);
        assertEquals(90.0, user.getHeading(), 1e-9);
    }

    // -------------------------------------------------------------------------
    // getSpeedKmh
    // -------------------------------------------------------------------------

    @Test
    void getSpeedKmhConvertsCorrectly() {
        // 13.89 m/s * 3.6 = 50.004 km/h
        RoadUser user = makeRoadUser(StationType.PASSENGER_CAR);
        assertEquals(13.89 * 3.6f, user.getSpeedKmh(), 0.01);
    }

    @Test
    void getSpeedKmhIsZeroWhenSpeedIsZero() {
        RoadUser user = new RoadUser("u", StationType.UNKNOWN, new LatLng(0, 0), 0.0, 0.0, null);
        assertEquals(0.0, user.getSpeedKmh(), 1e-9);
    }

    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    @Test
    void setStationTypeUpdatesValue() {
        RoadUser user = makeRoadUser(StationType.PASSENGER_CAR);
        user.setStationType(StationType.BUS);
        assertEquals(StationType.BUS, user.getStationType());
    }

    @Test
    void setPositionUpdatesValue() {
        RoadUser user = makeRoadUser(StationType.PASSENGER_CAR);
        LatLng newPos = new LatLng(51.5074, -0.1278);
        user.setPosition(newPos);
        assertEquals(51.5074, user.getPosition().getLatitude(), 1e-9);
        assertEquals(-0.1278, user.getPosition().getLongitude(), 1e-9);
    }

    @Test
    void setSpeedUpdatesValue() {
        RoadUser user = makeRoadUser(StationType.PASSENGER_CAR);
        user.setSpeed(27.78);
        assertEquals(27.78, user.getSpeed(), 1e-9);
    }

    @Test
    void setHeadingUpdatesValue() {
        RoadUser user = makeRoadUser(StationType.PASSENGER_CAR);
        user.setHeading(180.0);
        assertEquals(180.0, user.getHeading(), 1e-9);
    }

    // -------------------------------------------------------------------------
    // stillLiving
    // -------------------------------------------------------------------------

    @Test
    void stillLivingIsTrueImmediatelyAfterCreation() {
        RoadUser user = makeRoadUser(StationType.PASSENGER_CAR);
        assertTrue(user.stillLiving(), "A freshly created RoadUser must still be living");
    }

    @Test
    void updateTimestampResetsLivingTimer() {
        RoadUser user = makeRoadUser(StationType.PASSENGER_CAR);
        user.updateTimestamp();
        assertTrue(user.stillLiving());
    }

    // -------------------------------------------------------------------------
    // isVulnerable
    // -------------------------------------------------------------------------

    @Test
    void isVulnerableReturnsTrueForCyclist() {
        assertTrue(makeRoadUser(StationType.CYCLIST).isVulnerable());
    }

    @Test
    void isVulnerableReturnsTrueForPedestrian() {
        assertTrue(makeRoadUser(StationType.PEDESTRIAN).isVulnerable());
    }

    @Test
    void isVulnerableReturnsFalseForPassengerCar() {
        assertFalse(makeRoadUser(StationType.PASSENGER_CAR).isVulnerable());
    }

    @Test
    void isVulnerableReturnsFalseForBus() {
        assertFalse(makeRoadUser(StationType.BUS).isVulnerable());
    }

    @Test
    void isVulnerableReturnsFalseForUnknown() {
        assertFalse(makeRoadUser(StationType.UNKNOWN).isVulnerable());
    }
}

