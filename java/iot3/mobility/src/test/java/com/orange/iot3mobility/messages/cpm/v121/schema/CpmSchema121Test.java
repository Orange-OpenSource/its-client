/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.cpm.v121.schema;

import com.fasterxml.jackson.core.JsonFactory;
import com.networknt.schema.JsonSchema;
import com.orange.iot3mobility.messages.SchemaTestUtils;
import com.orange.iot3mobility.messages.cpm.core.CpmCodec;
import com.orange.iot3mobility.messages.cpm.core.CpmVersion;
import com.orange.iot3mobility.messages.cpm.v121.model.CpmEnvelope121;
import com.orange.iot3mobility.messages.cpm.v121.model.CpmMessage121;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.ManagementConfidence;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.ReferencePosition;
import com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer.PerceivedObject;
import com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer.PerceivedObjectConfidence;
import com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer.PerceivedObjectContainer;
import com.orange.iot3mobility.messages.cpm.v121.model.stationdatacontainer.OriginatingVehicleContainer;
import com.orange.iot3mobility.messages.cpm.v121.model.stationdatacontainer.StationDataContainer;
import com.orange.iot3mobility.messages.cpm.v121.model.stationdatacontainer.VehicleConfidence;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.List;

class CpmSchema121Test {

    private static final JsonSchema SCHEMA;

    static {
        try {
            SCHEMA = SchemaTestUtils.loadSchema("cpm/cpm_schema_1-2-1.json");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void write_minimalValidEnvelope_conformsToSchema() throws Exception {
        CpmEnvelope121 envelope = minimalEnvelope();
        CpmCodec codec = new CpmCodec(new JsonFactory());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(CpmVersion.V1_2_1, envelope, out);

        SchemaTestUtils.assertConformsToSchema(SCHEMA, out.toByteArray());
    }

    @Test
    void write_fullyPopulatedEnvelope_conformsToSchema() throws Exception {
        CpmEnvelope121 envelope = fullyPopulatedEnvelope();

        CpmCodec codec = new CpmCodec(new JsonFactory());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(CpmVersion.V1_2_1, envelope, out);

        SchemaTestUtils.assertConformsToSchema(SCHEMA, out.toByteArray());
    }

    private static ManagementContainer validManagement() {
        ReferencePosition referencePosition = new ReferencePosition(0, 0, 0);
        PositionConfidenceEllipse ellipse = new PositionConfidenceEllipse(0, 0, 0);
        ManagementConfidence confidence = new ManagementConfidence(ellipse, 0);
        return ManagementContainer.builder()
                .stationType(5)
                .referencePosition(referencePosition)
                .confidence(confidence)
                .build();
    }

    private static CpmEnvelope121 minimalEnvelope() {
        ReferencePosition referencePosition = new ReferencePosition(0, 0, 0);
        PositionConfidenceEllipse ellipse = new PositionConfidenceEllipse(0, 0, 0);
        ManagementConfidence confidence = new ManagementConfidence(ellipse, 0);
        ManagementContainer management = ManagementContainer.builder()
                .stationType(5)
                .referencePosition(referencePosition)
                .confidence(confidence)
                .build();
        CpmMessage121 message = CpmMessage121.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .managementContainer(management)
                .build();
        return CpmEnvelope121.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1514764800000L)
                .message(message)
                .build();
    }

    private static CpmEnvelope121 fullyPopulatedEnvelope() {
        VehicleConfidence vehicleConf = VehicleConfidence.builder()
                .heading(1).speed(1).build();
        OriginatingVehicleContainer vehicle = OriginatingVehicleContainer.builder()
                .heading(0)
                .speed(0)
                .confidence(vehicleConf)
                .driveDirection(0)
                .vehicleLength(10)
                .vehicleWidth(10)
                .longitudinalAcceleration(0)
                .yawRate(0)
                .build();
        StationDataContainer stationData = StationDataContainer.builder()
                .originatingVehicleContainer(vehicle)
                .build();

        PerceivedObjectConfidence objConf = PerceivedObjectConfidence.builder()
                .distance(1, 1)
                .speed(0, 0)
                .object(0)
                .build();
        PerceivedObject obj = PerceivedObject.builder()
                .objectId(0)
                .timeOfMeasurement(0)
                .distance(0, 0)
                .speed(0, 0)
                .objectAge(0)
                .confidence(objConf)
                .build();
        PerceivedObjectContainer perceivedObjects = new PerceivedObjectContainer(List.of(obj));

        CpmMessage121 message = CpmMessage121.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .managementContainer(validManagement())
                .stationDataContainer(stationData)
                .perceivedObjectContainer(perceivedObjects)
                .build();

        return CpmEnvelope121.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1514764800000L)
                .message(message)
                .build();
    }
}
