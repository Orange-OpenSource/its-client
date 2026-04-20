/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EtsiConverterTest {

    private static final double DELTA = 1e-9;

    // -------------------------------------------------------------------------
    // Time
    // -------------------------------------------------------------------------

    @Test
    void generationDeltaTimeEtsiWrapsAt65536() {
        assertEquals(0, EtsiConverter.generationDeltaTimeEtsi(0L));
        assertEquals(1000, EtsiConverter.generationDeltaTimeEtsi(1000L));
        assertEquals(65535, EtsiConverter.generationDeltaTimeEtsi(65535L));
        assertEquals(0, EtsiConverter.generationDeltaTimeEtsi(65536L));
        assertEquals(4, EtsiConverter.generationDeltaTimeEtsi(65540L));
    }

    @Test
    void unixToEtsiTimestampMsShiftsBy2004Epoch() {
        // 2004-01-01 in UNIX ms is DELTA_1970_2004_MILLISEC; ETSI value must be 0
        assertEquals(0L, EtsiConverter.unixToEtsiTimestampMs(EtsiConverter.DELTA_1970_2004_MILLISEC));
        assertEquals(1000L, EtsiConverter.unixToEtsiTimestampMs(EtsiConverter.DELTA_1970_2004_MILLISEC + 1000L));
    }

    @Test
    void epochMillisToSecondsConvertsCorrectly() {
        assertEquals(1.0, EtsiConverter.epochMillisToSeconds(1000L), DELTA);
        assertEquals(0.5, EtsiConverter.epochMillisToSeconds(500L), DELTA);
    }

    // -------------------------------------------------------------------------
    // Latitude
    // -------------------------------------------------------------------------

    @Test
    void latitudeRoundTrip() {
        double lat = 48.8566; // Paris
        assertEquals(lat, EtsiConverter.latitudeDegrees(EtsiConverter.latitudeEtsi(lat)), 1e-7);
    }

    @Test
    void latitudeEtsiEncodesCorrectly() {
        assertEquals(488566000, EtsiConverter.latitudeEtsi(48.8566));
        assertEquals(0, EtsiConverter.latitudeEtsi(0.0));
        assertEquals(-900000000, EtsiConverter.latitudeEtsi(-90.0));
        assertEquals(900000000, EtsiConverter.latitudeEtsi(90.0));
    }

    @Test
    void latitudeEtsiClampsAboveMax() {
        assertEquals(900000001, EtsiConverter.latitudeEtsi(91.0));
    }

    @Test
    void latitudeEtsiClampsBelowMin() {
        assertEquals(-900000000, EtsiConverter.latitudeEtsi(-91.0));
    }

    @Test
    void latitudeDegreesConvertsCorrectly() {
        assertEquals(48.8566, EtsiConverter.latitudeDegrees(488566000), DELTA);
        assertEquals(0.0, EtsiConverter.latitudeDegrees(0), DELTA);
        assertEquals(-90.0, EtsiConverter.latitudeDegrees(-900000000), DELTA);
    }

    // -------------------------------------------------------------------------
    // Longitude
    // -------------------------------------------------------------------------

    @Test
    void longitudeRoundTrip() {
        double lon = 2.3522; // Paris
        assertEquals(lon, EtsiConverter.longitudeDegrees(EtsiConverter.longitudeEtsi(lon)), 1e-7);
    }

    @Test
    void longitudeEtsiEncodesCorrectly() {
        assertEquals(23522000, EtsiConverter.longitudeEtsi(2.3522));
        assertEquals(0, EtsiConverter.longitudeEtsi(0.0));
        assertEquals(-1800000000, EtsiConverter.longitudeEtsi(-180.0));
        assertEquals(1800000000, EtsiConverter.longitudeEtsi(180.0));
    }

    @Test
    void longitudeEtsiClampsOutOfRange() {
        assertEquals(1800000001, EtsiConverter.longitudeEtsi(181.0));
        assertEquals(-1800000000, EtsiConverter.longitudeEtsi(-181.0));
    }

    // -------------------------------------------------------------------------
    // Altitude
    // -------------------------------------------------------------------------

    @Test
    void altitudeRoundTrip() {
        double altMeters = 150.0;
        assertEquals(altMeters, EtsiConverter.altitudeMeters(EtsiConverter.altitudeEtsi(altMeters)), 1e-2);
    }

    @Test
    void altitudeEtsiEncodesCorrectly() {
        assertEquals(15000, EtsiConverter.altitudeEtsi(150.0));
        assertEquals(0, EtsiConverter.altitudeEtsi(0.0));
        assertEquals(800001, EtsiConverter.altitudeEtsi(8000.01));
    }

    @Test
    void altitudeEtsiClampsOutOfRange() {
        assertEquals(0, EtsiConverter.altitudeEtsi(-1.0));
        assertEquals(800001, EtsiConverter.altitudeEtsi(99999.0));
    }

    @Test
    void altitudeMetersConvertsCorrectly() {
        assertEquals(150.0, EtsiConverter.altitudeMeters(15000), DELTA);
        assertEquals(0.0, EtsiConverter.altitudeMeters(0), DELTA);
    }

    // -------------------------------------------------------------------------
    // Speed
    // -------------------------------------------------------------------------

    @Test
    void speedRoundTripMetersPerSecond() {
        double mps = 13.89; // ~50 km/h
        assertEquals(mps, EtsiConverter.speedMetersPerSecond(EtsiConverter.speedEtsiFromMetersPerSecond(mps)), 1e-2);
    }

    @Test
    void speedEtsiFromMetersPerSecondEncodesCorrectly() {
        assertEquals(0, EtsiConverter.speedEtsiFromMetersPerSecond(0.0));
        assertEquals(1389, EtsiConverter.speedEtsiFromMetersPerSecond(13.89));
        assertEquals(16383, EtsiConverter.speedEtsiFromMetersPerSecond(163.83));
    }

    @Test
    void speedEtsiClampsOutOfRange() {
        assertEquals(0, EtsiConverter.speedEtsiFromMetersPerSecond(-1.0));
        assertEquals(16383, EtsiConverter.speedEtsiFromMetersPerSecond(999.0));
    }

    @Test
    void speedKilometersPerHourConvertsCorrectly() {
        // 0 m/s -> 0 km/h
        assertEquals(0.0, EtsiConverter.speedKilometersPerHour(0), DELTA);
        // 100 ETSI = 1 m/s = 3.6 km/h
        assertEquals(3.6, EtsiConverter.speedKilometersPerHour(100), 1e-9);
    }

    @Test
    void speedRoundTripKilometersPerHour() {
        double kmh = 90.0;
        int etsi = EtsiConverter.speedEtsiFromKilometersPerHour(kmh);
        assertEquals(kmh, EtsiConverter.speedKilometersPerHour(etsi), 0.05); // ~0.05 km/h tolerance
    }

    @Test
    void metersPerSecondToKilometersPerHourAndBack() {
        double mps = 10.0;
        double kmh = EtsiConverter.metersPerSecondToKilometersPerHour(mps);
        assertEquals(36.0, kmh, DELTA);
        assertEquals(mps, EtsiConverter.kilometersPerHourToMetersPerSecond(kmh), DELTA);
    }

    // -------------------------------------------------------------------------
    // Heading
    // -------------------------------------------------------------------------

    @Test
    void headingRoundTripDegrees() {
        double deg = 90.0;
        assertEquals(deg, EtsiConverter.headingDegrees(EtsiConverter.headingEtsiFromDegrees(deg)), 1e-1);
    }

    @Test
    void headingEtsiFromDegreesEncodesCorrectly() {
        assertEquals(0, EtsiConverter.headingEtsiFromDegrees(0.0));
        assertEquals(900, EtsiConverter.headingEtsiFromDegrees(90.0));
        assertEquals(1800, EtsiConverter.headingEtsiFromDegrees(180.0));
        assertEquals(2700, EtsiConverter.headingEtsiFromDegrees(270.0));
    }

    @Test
    void headingNormalizesNegativeAngles() {
        // -90° normalizes to 270°
        assertEquals(2700, EtsiConverter.headingEtsiFromDegrees(-90.0));
    }

    @Test
    void headingNormalizesOver360() {
        // 360° normalizes to 0°
        assertEquals(0, EtsiConverter.headingEtsiFromDegrees(360.0));
        // 450° normalizes to 90°
        assertEquals(900, EtsiConverter.headingEtsiFromDegrees(450.0));
    }

    @Test
    void headingNaNReturnsUnavailableSentinel() {
        assertEquals(3601, EtsiConverter.headingEtsiFromDegrees(Double.NaN));
        assertEquals(3601, EtsiConverter.headingEtsiFromRadians(Double.NaN));
    }

    @Test
    void headingDegreesReturnsSentinelAsNaN() {
        assertTrue(Double.isNaN(EtsiConverter.headingDegrees(3600)));
        assertTrue(Double.isNaN(EtsiConverter.headingDegrees(3601)));
    }

    @Test
    void headingDegreesConvertsNormalValues() {
        assertEquals(90.0, EtsiConverter.headingDegrees(900), DELTA);
        assertEquals(0.0, EtsiConverter.headingDegrees(0), DELTA);
    }

    @Test
    void headingRadiansRoundTrip() {
        double rad = Math.PI / 2; // 90°
        assertEquals(rad, EtsiConverter.headingRadians(EtsiConverter.headingEtsiFromRadians(rad)), 1e-3);
    }

    @Test
    void headingRadiansReturnNaNForSentinel() {
        assertTrue(Double.isNaN(EtsiConverter.headingRadians(3600)));
    }

    // -------------------------------------------------------------------------
    // Vehicle dimensions
    // -------------------------------------------------------------------------

    @Test
    void vehicleLengthRoundTrip() {
        double meters = 4.5;
        assertEquals(meters, EtsiConverter.vehicleLengthMeters(EtsiConverter.vehicleLengthEtsi(meters)), 1e-1);
    }

    @Test
    void vehicleLengthEtsiEncodesCorrectly() {
        assertEquals(45, EtsiConverter.vehicleLengthEtsi(4.5));
        assertEquals(0, EtsiConverter.vehicleLengthEtsi(0.0));
        assertEquals(1023, EtsiConverter.vehicleLengthEtsi(102.3));
    }

    @Test
    void vehicleLengthEtsiClampsOutOfRange() {
        assertEquals(0, EtsiConverter.vehicleLengthEtsi(-1.0));
        assertEquals(1023, EtsiConverter.vehicleLengthEtsi(999.0));
    }

    @Test
    void vehicleWidthRoundTrip() {
        double meters = 2.0;
        assertEquals(meters, EtsiConverter.vehicleWidthMeters(EtsiConverter.vehicleWidthEtsi(meters)), 1e-1);
    }

    @Test
    void vehicleWidthEtsiClampsOutOfRange() {
        assertEquals(0, EtsiConverter.vehicleWidthEtsi(-1.0));
        assertEquals(62, EtsiConverter.vehicleWidthEtsi(999.0));
    }

    // -------------------------------------------------------------------------
    // Acceleration
    // -------------------------------------------------------------------------

    @Test
    void accelerationRoundTrip() {
        double mps2 = 3.5;
        assertEquals(mps2, EtsiConverter.accelerationMetersPerSecondSquared(EtsiConverter.accelerationEtsi(mps2)), 1e-1);
    }

    @Test
    void accelerationEtsiEncodesCorrectly() {
        assertEquals(0, EtsiConverter.accelerationEtsi(0.0));
        assertEquals(35, EtsiConverter.accelerationEtsi(3.5));
        assertEquals(-35, EtsiConverter.accelerationEtsi(-3.5));
    }

    @Test
    void accelerationEtsiClampsOutOfRange() {
        assertEquals(161, EtsiConverter.accelerationEtsi(99.0));
        assertEquals(-160, EtsiConverter.accelerationEtsi(-99.0));
    }

    // -------------------------------------------------------------------------
    // Curvature
    // -------------------------------------------------------------------------

    @Test
    void curvatureRoundTrip() {
        double perMeter = 0.01;
        assertEquals(perMeter, EtsiConverter.curvaturePerMeter(EtsiConverter.curvatureEtsi(perMeter)), 1e-5);
    }

    @Test
    void curvatureEtsiClampsOutOfRange() {
        assertEquals(1023, EtsiConverter.curvatureEtsi(99.0));
        assertEquals(-1023, EtsiConverter.curvatureEtsi(-99.0));
    }

    // -------------------------------------------------------------------------
    // Yaw rate
    // -------------------------------------------------------------------------

    @Test
    void yawRateRoundTripDegPerSec() {
        double degPerSec = 5.0;
        int etsi = EtsiConverter.yawRateEtsiFromDegreesPerSecond(degPerSec);
        assertEquals(degPerSec, Math.toDegrees(EtsiConverter.yawRateRadiansPerSecond(etsi)), 1e-2);
    }

    @Test
    void yawRateEtsiClampsOutOfRange() {
        assertEquals(32767, EtsiConverter.yawRateEtsiFromDegreesPerSecond(99999.0));
        assertEquals(-32766, EtsiConverter.yawRateEtsiFromDegreesPerSecond(-99999.0));
    }

    @Test
    void yawRateRoundTripRadiansPerSec() {
        double radPerSec = 0.1;
        int etsi = EtsiConverter.yawRateEtsiFromRadiansPerSecond(radPerSec);
        assertEquals(radPerSec, EtsiConverter.yawRateRadiansPerSecond(etsi), 1e-3);
    }

    // -------------------------------------------------------------------------
    // Delta positions
    // -------------------------------------------------------------------------

    @Test
    void deltaPositionRoundTrip() {
        double meters = 50.0;
        assertEquals(meters, EtsiConverter.deltaPositionMeters(EtsiConverter.deltaPositionEtsi(meters)), 1e-1);
    }

    @Test
    void deltaPositionEtsiClampsOutOfRange() {
        assertEquals(131072, EtsiConverter.deltaPositionEtsi(99999.0));
        assertEquals(-131071, EtsiConverter.deltaPositionEtsi(-99999.0));
    }

    @Test
    void deltaAltitudeRoundTrip() {
        double meters = 10.0;
        assertEquals(meters, EtsiConverter.deltaAltitudeMeters(EtsiConverter.deltaAltitudeEtsi(meters)), 1e-1);
    }

    @Test
    void deltaAltitudeEtsiClampsOutOfRange() {
        assertEquals(12800, EtsiConverter.deltaAltitudeEtsi(99999.0));
        assertEquals(-12700, EtsiConverter.deltaAltitudeEtsi(-99999.0));
    }

    // -------------------------------------------------------------------------
    // Generic helpers
    // -------------------------------------------------------------------------

    @Test
    void degreesToRadiansAndBack() {
        assertEquals(Math.PI / 2, EtsiConverter.degreesToRadians(90.0), DELTA);
        assertEquals(90.0, EtsiConverter.radiansToDegrees(Math.PI / 2), DELTA);
    }

    // -------------------------------------------------------------------------
    // CPM-specific helpers
    // -------------------------------------------------------------------------

    @Test
    void cpmDistanceRoundTrip() {
        double meters = 25.5;
        assertEquals(meters, EtsiConverter.cpmDistanceMeters(EtsiConverter.cpmDistanceEtsi(meters)), 1e-2);
    }

    @Test
    void cpmDistanceEtsiClampsOutOfRange() {
        assertEquals(132767, EtsiConverter.cpmDistanceEtsi(99999.0));
        assertEquals(-132768, EtsiConverter.cpmDistanceEtsi(-99999.0));
    }

    @Test
    void cpmSpeedRoundTrip() {
        double mps = 5.0;
        assertEquals(mps, EtsiConverter.cpmSpeedMetersPerSecond(EtsiConverter.cpmSpeedEtsiFromMetersPerSecond(mps)), 1e-2);
    }

    @Test
    void cpmSpeedEtsiClampsOutOfRange() {
        assertEquals(16383, EtsiConverter.cpmSpeedEtsiFromMetersPerSecond(999.0));
        assertEquals(-16383, EtsiConverter.cpmSpeedEtsiFromMetersPerSecond(-999.0));
    }

    @Test
    void cpmDerivedSpeedFromComponents() {
        // xSpeed=300, ySpeed=400 → speed=5 m/s (3-4-5 triangle)
        assertEquals(5.0, EtsiConverter.cpmDerivedSpeedMetersPerSecond(300, 400), 1e-9);
    }

    @Test
    void cpmDerivedHeadingFromComponents() {
        // xSpeed=100, ySpeed=0 → East → 90°
        assertEquals(90.0, EtsiConverter.cpmDerivedHeadingDegrees(100, 0), 1e-9);
        // xSpeed=0, ySpeed=100 → North → 0° (or 360°)
        double heading = EtsiConverter.cpmDerivedHeadingDegrees(0, 100);
        // heading = (90 - 90) % 360 = 0
        assertEquals(0.0, heading % 360, 1e-9);
    }

    @Test
    void cpmAngleRoundTrip() {
        double deg = 45.0;
        assertEquals(deg, EtsiConverter.cpmAngleDegrees(EtsiConverter.cpmAngleEtsiFromDegrees(deg)), 1e-1);
    }

    @Test
    void cpmAngleSentinelHandling() {
        assertEquals(3601, EtsiConverter.cpmAngleEtsiFromDegrees(Double.NaN));
        assertTrue(Double.isNaN(EtsiConverter.cpmAngleDegrees(3601)));
    }

    @Test
    void cpmAngularRateRoundTrip() {
        double degPerSec = 2.5;
        int etsi = EtsiConverter.cpmAngularRateEtsiFromDegreesPerSecond(degPerSec);
        assertEquals(degPerSec, EtsiConverter.cpmAngularRateDegreesPerSecond(etsi), 1e-2);
    }

    @Test
    void cpmAngularRateRadiansRoundTrip() {
        double radPerSec = 0.05;
        int etsi = EtsiConverter.cpmAngularRateEtsiFromRadiansPerSecond(radPerSec);
        assertEquals(radPerSec, EtsiConverter.cpmAngularRateRadiansPerSecond(etsi), 1e-3);
    }

    @Test
    void cpmAngularAccelerationRoundTrip() {
        double degPerSec2 = 1.0;
        int etsi = EtsiConverter.cpmAngularAccelerationEtsiFromDegreesPerSecondSquared(degPerSec2);
        assertEquals(degPerSec2, EtsiConverter.cpmAngularAccelerationDegreesPerSecondSquared(etsi), 1e-2);
    }

    @Test
    void cpmObjectDimensionRoundTrip() {
        double meters = 3.0;
        assertEquals(meters, EtsiConverter.cpmObjectDimensionMeters(EtsiConverter.cpmObjectDimensionEtsi(meters)), 1e-1);
    }

    @Test
    void cpmObjectDimensionEtsiClampsOutOfRange() {
        assertEquals(0, EtsiConverter.cpmObjectDimensionEtsi(-1.0));
        assertEquals(1023, EtsiConverter.cpmObjectDimensionEtsi(9999.0));
    }

    @Test
    void cpmLanePositionRoundTrip() {
        double meters = 2.5;
        assertEquals(meters, EtsiConverter.cpmLanePositionMeters(EtsiConverter.cpmLanePositionEtsi(meters)), 1e-1);
    }

    @Test
    void cpmOffsetRoundTrip() {
        double meters = -15.0;
        assertEquals(meters, EtsiConverter.cpmOffsetMeters(EtsiConverter.cpmOffsetEtsi(meters)), 1e-2);
    }

    @Test
    void cpmOffsetEtsiClampsOutOfRange() {
        assertEquals(32767, EtsiConverter.cpmOffsetEtsi(99999.0));
        assertEquals(-32768, EtsiConverter.cpmOffsetEtsi(-99999.0));
    }

    @Test
    void cpmRangeRoundTrip() {
        double meters = 500.0;
        assertEquals(meters, EtsiConverter.cpmRangeMeters(EtsiConverter.cpmRangeEtsi(meters)), 1e-1);
    }

    @Test
    void cpmRangeEtsiClampsOutOfRange() {
        assertEquals(0, EtsiConverter.cpmRangeEtsi(-1.0));
        assertEquals(10000, EtsiConverter.cpmRangeEtsi(99999.0));
    }

    @Test
    void cpmOpeningAngleRoundTrip() {
        double deg = 120.0;
        assertEquals(deg, EtsiConverter.cpmOpeningAngleDegrees(EtsiConverter.cpmOpeningAngleEtsiFromDegrees(deg)), 1e-1);
    }

    @Test
    void cpmOpeningAngleSentinelHandling() {
        assertEquals(3601, EtsiConverter.cpmOpeningAngleEtsiFromDegrees(Double.NaN));
        assertTrue(Double.isNaN(EtsiConverter.cpmOpeningAngleDegrees(3601)));
    }

    @Test
    void cpmCorrelationRoundTrip() {
        double coeff = 0.75;
        assertEquals(coeff, EtsiConverter.cpmCorrelationCoefficient(EtsiConverter.cpmCorrelationEtsiFromCoefficient(coeff)), 1e-2);
    }

    @Test
    void cpmCorrelationEtsiClampsOutOfRange() {
        assertEquals(100, EtsiConverter.cpmCorrelationEtsiFromCoefficient(2.0));
        assertEquals(-100, EtsiConverter.cpmCorrelationEtsiFromCoefficient(-2.0));
    }
}

