/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.validation;

import com.orange.iot3mobility.messages.cpm.v211.model.CpmEnvelope211;
import com.orange.iot3mobility.messages.cpm.v211.model.CpmMessage211;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.Altitude;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.Angle;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.CartesianCoordinateWithConfidence;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.CartesianPosition3dWithConfidence;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.MessageRateHz;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.ReferencePosition;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.Speed;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.VelocityComponent;
import com.orange.iot3mobility.messages.cpm.v211.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.cpm.v211.model.managementcontainer.MessageRateRange;
import com.orange.iot3mobility.messages.cpm.v211.model.managementcontainer.SegmentationInfo;
import com.orange.iot3mobility.messages.cpm.v211.model.originatingvehiclecontainer.OriginatingVehicleContainer;
import com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.CartesianVelocity;
import com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.PerceivedObject;
import com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.PerceivedObjectContainer;
import com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.PolarVelocity;
import com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.Velocity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CpmValidator211Test {

    @Test
    void validateEnvelopeAcceptsMinimalValid() {
        CpmValidator211.validateEnvelope(validEnvelope());
    }

    @Test
    void validateEnvelopeAcceptsFullyPopulatedMessage() {
        ManagementContainer management = ManagementContainer.builder()
                .referenceTime(0)
                .referencePosition(validReferencePosition())
                .segmentationInfo(new SegmentationInfo(1, 1))
                .messageRateRange(new MessageRateRange(new MessageRateHz(1, 0), new MessageRateHz(10, 0)))
                .build();

        OriginatingVehicleContainer ovc = OriginatingVehicleContainer.builder()
                .orientationAngle(new Angle(0, 1))
                .build();

        CartesianPosition3dWithConfidence position = new CartesianPosition3dWithConfidence(
                new CartesianCoordinateWithConfidence(0, 1),
                new CartesianCoordinateWithConfidence(0, 1),
                null);
        PerceivedObject obj = PerceivedObject.builder()
                .measurementDeltaTime(0)
                .position(position)
                .objectId(1)
                .build();
        PerceivedObjectContainer perceivedObjects = PerceivedObjectContainer.builder()
                .perceivedObjects(java.util.List.of(obj))
                .build();

        CpmMessage211 message = CpmMessage211.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(management)
                .originatingVehicleContainer(ovc)
                .perceivedObjectContainer(perceivedObjects)
                .build();

        CpmEnvelope211 envelope = CpmEnvelope211.builder()
                .sourceUuid("com_application_42")
                .timestamp(1514764800000L)
                .objectIdRotationCount(10)
                .message(message)
                .build();

        CpmValidator211.validateEnvelope(envelope);
    }

    @Test
    void validateEnvelopeRejectsTimestampOutOfRange() {
        CpmEnvelope211 envelope = CpmEnvelope211.builder()
                .sourceUuid("com_application_42")
                .timestamp(1L)
                .message(validMessage())
                .build();

        assertThrows(CpmValidationException.class, () -> CpmValidator211.validateEnvelope(envelope));
    }

    @Test
    void validateEnvelopeRejectsObjectIdRotationCountOutOfRange() {
        CpmEnvelope211 envelope = CpmEnvelope211.builder()
                .sourceUuid("com_application_42")
                .timestamp(1514764800000L)
                .objectIdRotationCount(300)
                .message(validMessage())
                .build();

        assertThrows(CpmValidationException.class, () -> CpmValidator211.validateEnvelope(envelope));
    }

    @Test
    void validatePerceivedObjectRejectsVelocityWithBothOptions() {
        CartesianPosition3dWithConfidence position = new CartesianPosition3dWithConfidence(
                new CartesianCoordinateWithConfidence(0, 1),
                new CartesianCoordinateWithConfidence(0, 1),
                null);
        PolarVelocity polar = new PolarVelocity(new Speed(0, 1), new Angle(0, 1), new VelocityComponent(0, 1));
        CartesianVelocity cartesian = new CartesianVelocity(
                new VelocityComponent(0, 1),
                new VelocityComponent(0, 1),
                null);
        Velocity velocity = new Velocity(polar, cartesian);
        PerceivedObject object = PerceivedObject.builder()
                .measurementDeltaTime(0)
                .position(position)
                .velocity(velocity)
                .build();
        PerceivedObjectContainer perceivedObjectContainer = PerceivedObjectContainer.builder()
                .perceivedObjects(java.util.List.of(object))
                .build();

        CpmMessage211 message = CpmMessage211.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(validManagement())
                .perceivedObjectContainer(perceivedObjectContainer)
                .build();
        CpmEnvelope211 envelope = CpmEnvelope211.builder()
                .sourceUuid("com_application_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(CpmValidationException.class, () -> CpmValidator211.validateEnvelope(envelope));
    }

    @Test
    void validateManagementRejectsSegmentationInfoOutOfRange() {
        ManagementContainer management = ManagementContainer.builder()
                .referenceTime(0)
                .referencePosition(validReferencePosition())
                .segmentationInfo(new SegmentationInfo(0, 1))
                .build();
        CpmMessage211 message = CpmMessage211.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(management)
                .build();
        CpmEnvelope211 envelope = CpmEnvelope211.builder()
                .sourceUuid("com_application_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(CpmValidationException.class, () -> CpmValidator211.validateEnvelope(envelope));
    }

    @Test
    void validateManagementRejectsReferenceTimeOutOfRange() {
        ManagementContainer management = ManagementContainer.builder()
                .referenceTime(4398046511104L)
                .referencePosition(validReferencePosition())
                .build();
        CpmMessage211 message = CpmMessage211.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(management)
                .build();
        CpmEnvelope211 envelope = CpmEnvelope211.builder()
                .sourceUuid("com_application_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(CpmValidationException.class, () -> CpmValidator211.validateEnvelope(envelope));
    }

    @Test
    void validateManagementRejectsReferenceLatitudeOutOfRange() {
        ReferencePosition reference = ReferencePosition.builder()
                .latitudeLongitude(900000002, 0)
                .positionConfidenceEllipse(new PositionConfidenceEllipse(0, 0, 0))
                .altitude(new Altitude(0, 0))
                .build();
        ManagementContainer management = ManagementContainer.builder()
                .referenceTime(0)
                .referencePosition(reference)
                .build();
        CpmMessage211 message = CpmMessage211.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(management)
                .build();
        CpmEnvelope211 envelope = CpmEnvelope211.builder()
                .sourceUuid("com_application_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(CpmValidationException.class, () -> CpmValidator211.validateEnvelope(envelope));
    }

    @Test
    void validateManagementRejectsReferenceLongitudeOutOfRange() {
        ReferencePosition reference = ReferencePosition.builder()
                .latitudeLongitude(0, 1800000002)
                .positionConfidenceEllipse(new PositionConfidenceEllipse(0, 0, 0))
                .altitude(new Altitude(0, 0))
                .build();
        ManagementContainer management = ManagementContainer.builder()
                .referenceTime(0)
                .referencePosition(reference)
                .build();
        CpmMessage211 message = CpmMessage211.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(management)
                .build();
        CpmEnvelope211 envelope = CpmEnvelope211.builder()
                .sourceUuid("com_application_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(CpmValidationException.class, () -> CpmValidator211.validateEnvelope(envelope));
    }

    private static CpmEnvelope211 validEnvelope() {
        return CpmEnvelope211.builder()
                .sourceUuid("com_application_42")
                .timestamp(1514764800000L)
                .message(validMessage())
                .build();
    }

    private static CpmMessage211 validMessage() {
        return CpmMessage211.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(validManagement())
                .build();
    }

    private static ManagementContainer validManagement() {
        return ManagementContainer.builder()
                .referenceTime(0)
                .referencePosition(validReferencePosition())
                .build();
    }

    private static ReferencePosition validReferencePosition() {
        PositionConfidenceEllipse ellipse = new PositionConfidenceEllipse(0, 0, 0);
        Altitude altitude = new Altitude(0, 0);
        return ReferencePosition.builder()
                .latitudeLongitude(0, 0)
                .positionConfidenceEllipse(ellipse)
                .altitude(altitude)
                .build();
    }
}
