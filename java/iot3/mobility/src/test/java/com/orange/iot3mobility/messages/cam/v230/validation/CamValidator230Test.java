/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.
 */
package com.orange.iot3mobility.messages.cam.v230.validation;

import com.orange.iot3mobility.messages.cam.v230.model.CamAsn1Payload;
import com.orange.iot3mobility.messages.cam.v230.model.CamEnvelope230;
import com.orange.iot3mobility.messages.cam.v230.model.CamStructuredData;
import com.orange.iot3mobility.messages.cam.v230.model.basiccontainer.Altitude;
import com.orange.iot3mobility.messages.cam.v230.model.basiccontainer.BasicContainer;
import com.orange.iot3mobility.messages.cam.v230.model.basiccontainer.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.cam.v230.model.basiccontainer.ReferencePosition;
import com.orange.iot3mobility.messages.cam.v230.model.highfrequencycontainer.*;
import com.orange.iot3mobility.messages.cam.v230.model.lowfrequencycontainer.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CamValidator230Test {

    @Test
    void validateEnvelopeAcceptsStructuredPayload() {
        CamEnvelope230 envelope = validEnvelope();
        CamValidator230.validateEnvelope(envelope);
    }

    @Test
    void validateEnvelopeAcceptsFullyPopulatedMessage() {
        LowFrequencyContainer low = new LowFrequencyContainer(
                new BasicVehicleContainerLowFrequency(
                        0,
                        new ExteriorLights(true, false, false, false, false, false, false, false),
                        List.of(new PathPoint(new DeltaReferencePosition(0, 0, 0), 1))));

        CamStructuredData message = CamStructuredData.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .basicContainer(validBasicContainer())
                .highFrequencyContainer(validHighFrequencyContainer())
                .lowFrequencyContainer(low)
                .build();

        CamEnvelope230 envelope = CamEnvelope230.builder()
                .messageFormat("json/raw")
                .sourceUuid("com_car_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        CamValidator230.validateEnvelope(envelope);
    }

    @Test
    void validateEnvelopeRejectsMessageFormatMismatch() {
        CamEnvelope230 envelope = CamEnvelope230.builder()
                .messageFormat("asn1/uper")
                .sourceUuid("com_car_42")
                .timestamp(1514764800000L)
                .message(validStructuredMessage())
                .build();

        assertThrows(CamValidationException.class, () -> CamValidator230.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsAsn1InvalidBase64() {
        CamEnvelope230 envelope = CamEnvelope230.builder()
                .messageFormat("asn1/uper")
                .sourceUuid("com_car_42")
                .timestamp(1514764800000L)
                .message(new CamAsn1Payload("1", "not_base64"))
                .build();

        assertThrows(CamValidationException.class, () -> CamValidator230.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsEmptyRsuZones() {
        ReferencePosition reference = ReferencePosition.builder()
                .latitudeLongitude(0, 0)
                .positionConfidenceEllipse(new PositionConfidenceEllipse(0, 0, 0))
                .altitude(new Altitude(0, 0))
                .build();

        BasicContainer basic = BasicContainer.builder()
                .stationType(5)
                .referencePosition(reference)
                .build();

        RsuContainerHighFrequency rsu = new RsuContainerHighFrequency(List.of());

        CamStructuredData message = CamStructuredData.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .basicContainer(basic)
                .highFrequencyContainer(rsu)
                .build();

        CamEnvelope230 envelope = CamEnvelope230.builder()
                .messageFormat("json/raw")
                .sourceUuid("com_car_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(CamValidationException.class, () -> CamValidator230.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsReferenceAltitudeOutOfRange() {
        ReferencePosition reference = ReferencePosition.builder()
                .latitudeLongitude(0, 0)
                .positionConfidenceEllipse(new PositionConfidenceEllipse(0, 0, 0))
                .altitude(new Altitude(-100001, 0))
                .build();

        BasicContainer basic = BasicContainer.builder()
                .stationType(5)
                .referencePosition(reference)
                .build();

        BasicVehicleContainerHighFrequency high = BasicVehicleContainerHighFrequency.builder()
                .heading(new Heading(0, 1))
                .speed(new Speed(0, 1))
                .driveDirection(0)
                .vehicleLength(new VehicleLength(1, 0))
                .vehicleWidth(1)
                .longitudinalAcceleration(new AccelerationComponent(0, 1))
                .curvature(new Curvature(0, 0))
                .curvatureCalculationMode(0)
                .yawRate(new YawRate(0, 0))
                .build();

        CamStructuredData message = CamStructuredData.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .basicContainer(basic)
                .highFrequencyContainer(high)
                .build();

        CamEnvelope230 envelope = CamEnvelope230.builder()
                .messageFormat("json/raw")
                .sourceUuid("com_car_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(CamValidationException.class, () -> CamValidator230.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsHeadingOutOfRange() {
        ReferencePosition reference = ReferencePosition.builder()
                .latitudeLongitude(0, 0)
                .positionConfidenceEllipse(new PositionConfidenceEllipse(0, 0, 0))
                .altitude(new Altitude(0, 0))
                .build();

        BasicContainer basic = BasicContainer.builder()
                .stationType(5)
                .referencePosition(reference)
                .build();

        BasicVehicleContainerHighFrequency high = BasicVehicleContainerHighFrequency.builder()
                .heading(new Heading(4000, 1))
                .speed(new Speed(0, 1))
                .driveDirection(0)
                .vehicleLength(new VehicleLength(1, 0))
                .vehicleWidth(1)
                .longitudinalAcceleration(new AccelerationComponent(0, 1))
                .curvature(new Curvature(0, 0))
                .curvatureCalculationMode(0)
                .yawRate(new YawRate(0, 0))
                .build();

        CamStructuredData message = CamStructuredData.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .basicContainer(basic)
                .highFrequencyContainer(high)
                .build();

        CamEnvelope230 envelope = CamEnvelope230.builder()
                .messageFormat("json/raw")
                .sourceUuid("com_car_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(CamValidationException.class, () -> CamValidator230.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsReferenceLatitudeOutOfRange() {
        ReferencePosition reference = ReferencePosition.builder()
                .latitudeLongitude(900000002, 0)
                .positionConfidenceEllipse(new PositionConfidenceEllipse(0, 0, 0))
                .altitude(new Altitude(0, 0))
                .build();

        BasicContainer basic = BasicContainer.builder()
                .stationType(5)
                .referencePosition(reference)
                .build();

        BasicVehicleContainerHighFrequency high = BasicVehicleContainerHighFrequency.builder()
                .heading(new Heading(0, 1))
                .speed(new Speed(0, 1))
                .driveDirection(0)
                .vehicleLength(new VehicleLength(1, 0))
                .vehicleWidth(1)
                .longitudinalAcceleration(new AccelerationComponent(0, 1))
                .curvature(new Curvature(0, 0))
                .curvatureCalculationMode(0)
                .yawRate(new YawRate(0, 0))
                .build();

        CamStructuredData message = CamStructuredData.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .basicContainer(basic)
                .highFrequencyContainer(high)
                .build();

        CamEnvelope230 envelope = CamEnvelope230.builder()
                .messageFormat("json/raw")
                .sourceUuid("com_car_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(CamValidationException.class, () -> CamValidator230.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsSpeedOutOfRange() {
        ReferencePosition reference = ReferencePosition.builder()
                .latitudeLongitude(0, 0)
                .positionConfidenceEllipse(new PositionConfidenceEllipse(0, 0, 0))
                .altitude(new Altitude(0, 0))
                .build();

        BasicContainer basic = BasicContainer.builder()
                .stationType(5)
                .referencePosition(reference)
                .build();

        BasicVehicleContainerHighFrequency high = BasicVehicleContainerHighFrequency.builder()
                .heading(new Heading(0, 1))
                .speed(new Speed(20000, 1))
                .driveDirection(0)
                .vehicleLength(new VehicleLength(1, 0))
                .vehicleWidth(1)
                .longitudinalAcceleration(new AccelerationComponent(0, 1))
                .curvature(new Curvature(0, 0))
                .curvatureCalculationMode(0)
                .yawRate(new YawRate(0, 0))
                .build();

        CamStructuredData message = CamStructuredData.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .basicContainer(basic)
                .highFrequencyContainer(high)
                .build();

        CamEnvelope230 envelope = CamEnvelope230.builder()
                .messageFormat("json/raw")
                .sourceUuid("com_car_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(CamValidationException.class, () -> CamValidator230.validateEnvelope(envelope));
    }

    @Test
    void validateLowFrequencyRejectsTooManyPathPoints() {
        List<PathPoint> points = new ArrayList<>();
        for (int i = 0; i < 41; i++) {
            points.add(new PathPoint(new DeltaReferencePosition(0, 0, 0), 1));
        }

        LowFrequencyContainer low = new LowFrequencyContainer(
                new BasicVehicleContainerLowFrequency(
                        0,
                        new ExteriorLights(false, false, false, false, false, false, false, false),
                        points));

        CamStructuredData message = CamStructuredData.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .basicContainer(validBasicContainer())
                .highFrequencyContainer(validHighFrequencyContainer())
                .lowFrequencyContainer(low)
                .build();

        CamEnvelope230 envelope = CamEnvelope230.builder()
                .messageFormat("json/raw")
                .sourceUuid("com_car_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(CamValidationException.class, () -> CamValidator230.validateEnvelope(envelope));
    }

    @Test
    void validateLowFrequencyRejectsPathDeltaTimeOutOfRange() {
        LowFrequencyContainer low = new LowFrequencyContainer(
                new BasicVehicleContainerLowFrequency(
                        0,
                        new ExteriorLights(false, false, false, false, false, false, false, false),
                        List.of(new PathPoint(new DeltaReferencePosition(0, 0, 0), 0))));

        CamStructuredData message = CamStructuredData.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .basicContainer(validBasicContainer())
                .highFrequencyContainer(validHighFrequencyContainer())
                .lowFrequencyContainer(low)
                .build();

        CamEnvelope230 envelope = CamEnvelope230.builder()
                .messageFormat("json/raw")
                .sourceUuid("com_car_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(CamValidationException.class, () -> CamValidator230.validateEnvelope(envelope));
    }

    private static CamEnvelope230 validEnvelope() {
        return CamEnvelope230.builder()
                .messageFormat("json/raw")
                .sourceUuid("com_car_42")
                .timestamp(1514764800000L)
                .message(validStructuredMessage())
                .build();
    }

    private static CamStructuredData validStructuredMessage() {
        return CamStructuredData.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .basicContainer(validBasicContainer())
                .highFrequencyContainer(validHighFrequencyContainer())
                .build();
    }

    private static BasicContainer validBasicContainer() {
        ReferencePosition reference = ReferencePosition.builder()
                .latitudeLongitude(0, 0)
                .positionConfidenceEllipse(new PositionConfidenceEllipse(0, 0, 0))
                .altitude(new Altitude(0, 0))
                .build();
        return BasicContainer.builder()
                .stationType(5)
                .referencePosition(reference)
                .build();
    }

    private static BasicVehicleContainerHighFrequency validHighFrequencyContainer() {
        return BasicVehicleContainerHighFrequency.builder()
                .heading(new Heading(0, 1))
                .speed(new Speed(0, 1))
                .driveDirection(0)
                .vehicleLength(new VehicleLength(1, 0))
                .vehicleWidth(1)
                .longitudinalAcceleration(new AccelerationComponent(0, 1))
                .curvature(new Curvature(0, 0))
                .curvatureCalculationMode(0)
                .yawRate(new YawRate(0, 0))
                .build();
    }
}
