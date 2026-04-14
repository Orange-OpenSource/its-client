/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.
 */
package com.orange.iot3mobility.messages.cpm;

import com.fasterxml.jackson.core.JsonFactory;
import com.orange.iot3mobility.messages.cpm.core.CpmCodec;
import com.orange.iot3mobility.messages.cpm.core.CpmException;
import com.orange.iot3mobility.messages.cpm.core.CpmVersion;
import com.orange.iot3mobility.messages.cpm.v121.model.CpmEnvelope121;
import com.orange.iot3mobility.messages.cpm.v121.model.CpmMessage121;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.ManagementConfidence;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.ReferencePosition;
import com.orange.iot3mobility.messages.cpm.v211.model.CpmEnvelope211;
import com.orange.iot3mobility.messages.cpm.v211.model.CpmMessage211;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.Altitude;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class CpmCodecTest {

    @Test
    void writeRead121RoundTrip() throws Exception {
        CpmEnvelope121 envelope = validEnvelope121();
        CpmCodec codec = new CpmCodec(new JsonFactory());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(CpmVersion.V1_2_1, envelope, out);

        CpmCodec.CpmFrame<?> frame = codec.read(new ByteArrayInputStream(out.toByteArray()));
        assertEquals(CpmVersion.V1_2_1, frame.version());
        assertTrue(frame.envelope() instanceof CpmEnvelope121);
        CpmEnvelope121 parsed = (CpmEnvelope121) frame.envelope();
        assertEquals(envelope.sourceUuid(), parsed.sourceUuid());
        assertEquals(envelope.timestamp(), parsed.timestamp());
    }

    @Test
    void writeRead211RoundTrip() throws Exception {
        CpmEnvelope211 envelope = validEnvelope211();
        CpmCodec codec = new CpmCodec(new JsonFactory());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(CpmVersion.V2_1_1, envelope, out);

        CpmCodec.CpmFrame<?> frame = codec.read(new ByteArrayInputStream(out.toByteArray()));
        assertEquals(CpmVersion.V2_1_1, frame.version());
        assertTrue(frame.envelope() instanceof CpmEnvelope211);
        CpmEnvelope211 parsed = (CpmEnvelope211) frame.envelope();
        assertEquals(envelope.sourceUuid(), parsed.sourceUuid());
        assertEquals(envelope.timestamp(), parsed.timestamp());
    }

    @Test
    void readRejectsMissingVersion() {
        CpmCodec codec = new CpmCodec(new JsonFactory());
        String json = "{\"type\":\"cpm\"}";
        assertThrows(CpmException.class, () -> codec.read(json));
    }

    private static CpmEnvelope121 validEnvelope121() {
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

    private static CpmEnvelope211 validEnvelope211() {
        com.orange.iot3mobility.messages.cpm.v211.model.defs.PositionConfidenceEllipse ellipse =
                new com.orange.iot3mobility.messages.cpm.v211.model.defs.PositionConfidenceEllipse(0, 0, 0);
        Altitude altitude = new Altitude(0, 0);
        com.orange.iot3mobility.messages.cpm.v211.model.defs.ReferencePosition reference =
                com.orange.iot3mobility.messages.cpm.v211.model.defs.ReferencePosition.builder()
                        .latitudeLongitude(0, 0)
                        .positionConfidenceEllipse(ellipse)
                        .altitude(altitude)
                        .build();
        com.orange.iot3mobility.messages.cpm.v211.model.managementcontainer.ManagementContainer management =
                com.orange.iot3mobility.messages.cpm.v211.model.managementcontainer.ManagementContainer.builder()
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
}
