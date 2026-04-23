/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.denm;

import com.fasterxml.jackson.core.JsonFactory;
import com.orange.iot3mobility.messages.denm.core.DenmCodec;
import com.orange.iot3mobility.messages.denm.core.DenmException;
import com.orange.iot3mobility.messages.denm.core.DenmVersion;
import com.orange.iot3mobility.messages.denm.v113.model.DenmEnvelope113;
import com.orange.iot3mobility.messages.denm.v113.model.DenmMessage113;
import com.orange.iot3mobility.messages.denm.v113.model.managementcontainer.ActionId;
import com.orange.iot3mobility.messages.denm.v113.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.denm.v113.model.managementcontainer.ReferencePosition;
import com.orange.iot3mobility.messages.denm.v220.model.DenmEnvelope220;
import com.orange.iot3mobility.messages.denm.v220.model.DenmMessage220;
import com.orange.iot3mobility.messages.denm.v220.model.defs.Altitude;
import com.orange.iot3mobility.messages.denm.v220.model.defs.PositionConfidenceEllipse;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DenmCodecTest {

    @Test
    void writeRead113RoundTrip() throws Exception {
        DenmEnvelope113 envelope = validEnvelope113();
        DenmCodec codec = new DenmCodec(new JsonFactory());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(DenmVersion.V1_1_3, envelope, out);

        DenmCodec.DenmFrame<?> frame = codec.read(new ByteArrayInputStream(out.toByteArray()));
        assertEquals(DenmVersion.V1_1_3, frame.version());
        assertTrue(frame.envelope() instanceof DenmEnvelope113);
        DenmEnvelope113 parsed = (DenmEnvelope113) frame.envelope();
        assertEquals(envelope.sourceUuid(), parsed.sourceUuid());
        assertEquals(envelope.timestamp(), parsed.timestamp());
    }

    @Test
    void writeRead220RoundTrip() throws Exception {
        DenmEnvelope220 envelope = validEnvelope220();
        DenmCodec codec = new DenmCodec(new JsonFactory());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(DenmVersion.V2_2_0, envelope, out);

        DenmCodec.DenmFrame<?> frame = codec.read(new ByteArrayInputStream(out.toByteArray()));
        assertEquals(DenmVersion.V2_2_0, frame.version());
        assertTrue(frame.envelope() instanceof DenmEnvelope220);
        DenmEnvelope220 parsed = (DenmEnvelope220) frame.envelope();
        assertEquals(envelope.sourceUuid(), parsed.sourceUuid());
        assertEquals(envelope.timestamp(), parsed.timestamp());
    }

    @Test
    void readRejectsMissingVersion() {
        DenmCodec codec = new DenmCodec(new JsonFactory());
        String json = "{\"type\":\"denm\"}";
        assertThrows(DenmException.class, () -> codec.read(json));
    }

    private static DenmEnvelope113 validEnvelope113() {
        ReferencePosition position = new ReferencePosition(0, 0, 0);
        ManagementContainer management = ManagementContainer.builder()
                .actionId(new ActionId(1, 1))
                .detectionTime(0)
                .referenceTime(0)
                .eventPosition(position)
                .build();

        DenmMessage113 message = DenmMessage113.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(management)
                .build();

        return DenmEnvelope113.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1514764800000L)
                .message(message)
                .build();
    }

    private static DenmEnvelope220 validEnvelope220() {
        com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ReferencePosition position =
                com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ReferencePosition.builder()
                        .latitude(0)
                        .longitude(0)
                        .positionConfidenceEllipse(new PositionConfidenceEllipse(0, 0, 0))
                        .altitude(new Altitude(0, 0))
                        .build();

        com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ManagementContainer management =
                com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ManagementContainer.builder()
                        .actionId(new com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ActionId(1, 1))
                        .detectionTime(0)
                        .referenceTime(0)
                        .eventPosition(position)
                        .stationType(5)
                        .build();

        DenmMessage220 message = DenmMessage220.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(management)
                .build();

        return DenmEnvelope220.builder()
                .sourceUuid("com_application_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();
    }
}
