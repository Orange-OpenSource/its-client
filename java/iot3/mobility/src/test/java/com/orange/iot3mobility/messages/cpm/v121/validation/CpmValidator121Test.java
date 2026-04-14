/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.
 */
package com.orange.iot3mobility.messages.cpm.v121.validation;

import com.orange.iot3mobility.messages.cpm.v121.model.CpmEnvelope121;
import com.orange.iot3mobility.messages.cpm.v121.model.CpmMessage121;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.ManagementConfidence;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.ReferencePosition;
import com.orange.iot3mobility.messages.cpm.v121.model.stationdatacontainer.OriginatingRsuContainer;
import com.orange.iot3mobility.messages.cpm.v121.model.stationdatacontainer.OriginatingVehicleContainer;
import com.orange.iot3mobility.messages.cpm.v121.model.stationdatacontainer.StationDataContainer;
import com.orange.iot3mobility.messages.cpm.v121.model.stationdatacontainer.VehicleConfidence;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CpmValidator121Test {

    @Test
    void validateEnvelopeAcceptsMinimalValid() {
        CpmValidator121.validateEnvelope(validEnvelope());
    }

    @Test
    void validateEnvelopeRejectsTimestampOutOfRange() {
        CpmEnvelope121 envelope = CpmEnvelope121.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1L)
                .message(validMessage())
                .build();

        assertThrows(CpmValidationException.class, () -> CpmValidator121.validateEnvelope(envelope));
    }

    @Test
    void validateStationDataContainerRejectsBothPresent() {
        VehicleConfidence confidence = VehicleConfidence.builder()
                .heading(1)
                .speed(1)
                .build();
        OriginatingVehicleContainer vehicle = OriginatingVehicleContainer.builder()
                .heading(0)
                .speed(0)
                .confidence(confidence)
                .build();
        OriginatingRsuContainer rsu = new OriginatingRsuContainer(1, 2, 3);
        StationDataContainer stationData = StationDataContainer.builder()
                .originatingVehicleContainer(vehicle)
                .originatingRsuContainer(rsu)
                .build();

        CpmMessage121 message = CpmMessage121.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .managementContainer(validManagement())
                .stationDataContainer(stationData)
                .build();
        CpmEnvelope121 envelope = CpmEnvelope121.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(CpmValidationException.class, () -> CpmValidator121.validateEnvelope(envelope));
    }

    @Test
    void validateManagementRejectsStationTypeOutOfRange() {
        ManagementContainer management = ManagementContainer.builder()
                .stationType(300)
                .referencePosition(new ReferencePosition(0, 0, 0))
                .confidence(new ManagementConfidence(new PositionConfidenceEllipse(0, 0, 0), 0))
                .build();
        CpmMessage121 message = CpmMessage121.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .managementContainer(management)
                .build();

        CpmEnvelope121 envelope = CpmEnvelope121.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(CpmValidationException.class, () -> CpmValidator121.validateEnvelope(envelope));
    }

    @Test
    void validateManagementRejectsReferenceLongitudeOutOfRange() {
        ManagementContainer management = ManagementContainer.builder()
                .stationType(5)
                .referencePosition(new ReferencePosition(0, 1800000002, 0))
                .confidence(new ManagementConfidence(new PositionConfidenceEllipse(0, 0, 0), 0))
                .build();
        CpmMessage121 message = CpmMessage121.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .managementContainer(management)
                .build();

        CpmEnvelope121 envelope = CpmEnvelope121.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(CpmValidationException.class, () -> CpmValidator121.validateEnvelope(envelope));
    }

    @Test
    void validateManagementRejectsReferenceLatitudeOutOfRange() {
        ManagementContainer management = ManagementContainer.builder()
                .stationType(5)
                .referencePosition(new ReferencePosition(900000002, 0, 0))
                .confidence(new ManagementConfidence(new PositionConfidenceEllipse(0, 0, 0), 0))
                .build();
        CpmMessage121 message = CpmMessage121.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .managementContainer(management)
                .build();

        CpmEnvelope121 envelope = CpmEnvelope121.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        assertThrows(CpmValidationException.class, () -> CpmValidator121.validateEnvelope(envelope));
    }

    private static CpmEnvelope121 validEnvelope() {
        return CpmEnvelope121.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1514764800000L)
                .message(validMessage())
                .build();
    }

    private static CpmMessage121 validMessage() {
        return CpmMessage121.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .managementContainer(validManagement())
                .build();
    }

    private static ManagementContainer validManagement() {
        ReferencePosition reference = new ReferencePosition(0, 0, 0);
        PositionConfidenceEllipse ellipse = new PositionConfidenceEllipse(0, 0, 0);
        ManagementConfidence confidence = new ManagementConfidence(ellipse, 0);
        return ManagementContainer.builder()
                .stationType(5)
                .referencePosition(reference)
                .confidence(confidence)
                .build();
    }
}
