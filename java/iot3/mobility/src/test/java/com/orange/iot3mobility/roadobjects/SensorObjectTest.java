/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.roadobjects;

import com.orange.iot3mobility.quadkey.LatLng;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SensorObjectTest {

    private static SensorObject makeSensorObject() {
        return new SensorObject(
                "sensor_42_obj1",
                SensorObjectType.PASSENGER_CAR,
                new LatLng(48.8566, 2.3522),
                13.89,
                90.0,
                5);
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    @Test
    void getUuidReturnsConstructedValue() {
        assertEquals("sensor_42_obj1", makeSensorObject().getUuid());
    }

    @Test
    void getTypeReturnsConstructedValue() {
        assertEquals(SensorObjectType.PASSENGER_CAR, makeSensorObject().getType());
    }

    @Test
    void getPositionReturnsConstructedValue() {
        SensorObject obj = makeSensorObject();
        assertEquals(48.8566, obj.getPosition().getLatitude(), 1e-9);
        assertEquals(2.3522, obj.getPosition().getLongitude(), 1e-9);
    }

    @Test
    void getSpeedReturnsConstructedValue() {
        assertEquals(13.89, makeSensorObject().getSpeed(), 1e-9);
    }

    @Test
    void getHeadingReturnsConstructedValue() {
        assertEquals(90.0, makeSensorObject().getHeading(), 1e-9);
    }

    @Test
    void getInfoQualityReturnsConstructedValue() {
        assertEquals(5, makeSensorObject().getInfoQuality());
    }

    // -------------------------------------------------------------------------
    // getSpeedKmh
    // -------------------------------------------------------------------------

    @Test
    void getSpeedKmhConvertsCorrectly() {
        // 13.89 m/s * 3.6f ≈ 50.0 km/h
        assertEquals(13.89 * 3.6f, makeSensorObject().getSpeedKmh(), 0.01);
    }

    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    @Test
    void setTypeUpdatesValue() {
        SensorObject obj = makeSensorObject();
        obj.setType(SensorObjectType.PEDESTRIAN);
        assertEquals(SensorObjectType.PEDESTRIAN, obj.getType());
    }

    @Test
    void setPositionUpdatesValue() {
        SensorObject obj = makeSensorObject();
        LatLng newPos = new LatLng(51.5074, -0.1278);
        obj.setPosition(newPos);
        assertEquals(51.5074, obj.getPosition().getLatitude(), 1e-9);
    }

    @Test
    void setSpeedUpdatesValue() {
        SensorObject obj = makeSensorObject();
        obj.setSpeed(27.78);
        assertEquals(27.78, obj.getSpeed(), 1e-9);
    }

    @Test
    void setHeadingUpdatesValue() {
        SensorObject obj = makeSensorObject();
        obj.setHeading(270.0);
        assertEquals(270.0, obj.getHeading(), 1e-9);
    }

    @Test
    void setInfoQualityUpdatesValue() {
        SensorObject obj = makeSensorObject();
        obj.setInfoQuality(7);
        assertEquals(7, obj.getInfoQuality());
    }

    // -------------------------------------------------------------------------
    // stillLiving
    // -------------------------------------------------------------------------

    @Test
    void stillLivingIsTrueImmediatelyAfterCreation() {
        assertTrue(makeSensorObject().stillLiving(),
                "A freshly created SensorObject must still be living");
    }

    @Test
    void updateTimestampResetsLivingTimer() {
        SensorObject obj = makeSensorObject();
        obj.updateTimestamp();
        assertTrue(obj.stillLiving());
    }
}

