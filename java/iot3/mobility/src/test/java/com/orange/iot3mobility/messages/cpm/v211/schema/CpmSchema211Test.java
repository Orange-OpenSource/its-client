/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.cpm.v211.schema;

import com.fasterxml.jackson.core.JsonFactory;
import com.networknt.schema.JsonSchema;
import com.orange.iot3mobility.messages.SchemaTestUtils;
import com.orange.iot3mobility.messages.cpm.core.CpmCodec;
import com.orange.iot3mobility.messages.cpm.core.CpmVersion;
import com.orange.iot3mobility.messages.cpm.v211.model.CpmEnvelope211;
import com.orange.iot3mobility.messages.cpm.v211.model.CpmMessage211;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.Altitude;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.ReferencePosition;
import com.orange.iot3mobility.messages.cpm.v211.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.Angle;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.CartesianCoordinateWithConfidence;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.CartesianPosition3dWithConfidence;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.MessageRateHz;
import com.orange.iot3mobility.messages.cpm.v211.model.managementcontainer.MessageRateRange;
import com.orange.iot3mobility.messages.cpm.v211.model.managementcontainer.SegmentationInfo;
import com.orange.iot3mobility.messages.cpm.v211.model.originatingvehiclecontainer.OriginatingVehicleContainer;
import com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.PerceivedObject;
import com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.PerceivedObjectContainer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.List;

class CpmSchema211Test {

    private static final JsonSchema SCHEMA;

    static {
        try {
            SCHEMA = SchemaTestUtils.loadSchema("cpm/cpm_schema_2-1-1.json");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void write_minimalValidEnvelope_conformsToSchema() throws Exception {
        CpmEnvelope211 envelope = minimalEnvelope();
        CpmCodec codec = new CpmCodec(new JsonFactory());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(CpmVersion.V2_1_1, envelope, out);

        SchemaTestUtils.assertConformsToSchema(SCHEMA, out.toByteArray());
    }

    @Test
    void write_fullyPopulatedEnvelope_conformsToSchema() throws Exception {
        CpmEnvelope211 envelope = fullyPopulatedEnvelope();

        CpmCodec codec = new CpmCodec(new JsonFactory());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(CpmVersion.V2_1_1, envelope, out);

        SchemaTestUtils.assertConformsToSchema(SCHEMA, out.toByteArray());
    }

    private static ReferencePosition validReferencePosition() {
        return ReferencePosition.builder()
                .latitudeLongitude(0, 0)
                .positionConfidenceEllipse(new PositionConfidenceEllipse(0, 0, 0))
                .altitude(new Altitude(0, 0))
                .build();
    }

    private static CpmEnvelope211 minimalEnvelope() {
        ReferencePosition reference = ReferencePosition.builder()
                .latitudeLongitude(0, 0)
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
        return CpmEnvelope211.builder()
                .sourceUuid("com_application_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();
    }

    private static CpmEnvelope211 fullyPopulatedEnvelope() {
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
                .perceivedObjects(List.of(obj))
                .build();

        CpmMessage211 message = CpmMessage211.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(management)
                .originatingVehicleContainer(ovc)
                .perceivedObjectContainer(perceivedObjects)
                .build();

        return CpmEnvelope211.builder()
                .sourceUuid("com_application_42")
                .timestamp(1514764800000L)
                .objectIdRotationCount(10)
                .message(message)
                .build();
    }
}
