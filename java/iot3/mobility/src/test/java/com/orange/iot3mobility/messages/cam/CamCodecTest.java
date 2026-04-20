/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cam;

import com.fasterxml.jackson.core.JsonFactory;
import com.orange.iot3mobility.messages.cam.core.CamCodec;
import com.orange.iot3mobility.messages.cam.core.CamException;
import com.orange.iot3mobility.messages.cam.core.CamVersion;
import com.orange.iot3mobility.messages.cam.v113.model.CamEnvelope113;
import com.orange.iot3mobility.messages.cam.v113.model.CamMessage113;
import com.orange.iot3mobility.messages.cam.v113.model.HighFrequencyContainer;
import com.orange.iot3mobility.messages.cam.v113.model.ReferencePosition;
import com.orange.iot3mobility.messages.cam.v230.model.CamEnvelope230;
import com.orange.iot3mobility.messages.cam.v230.model.CamStructuredData;
import com.orange.iot3mobility.messages.cam.v230.model.basiccontainer.Altitude;
import com.orange.iot3mobility.messages.cam.v230.model.basiccontainer.BasicContainer;
import com.orange.iot3mobility.messages.cam.v230.model.basiccontainer.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.cam.v230.model.highfrequencycontainer.AccelerationComponent;
import com.orange.iot3mobility.messages.cam.v230.model.highfrequencycontainer.BasicVehicleContainerHighFrequency;
import com.orange.iot3mobility.messages.cam.v230.model.highfrequencycontainer.Curvature;
import com.orange.iot3mobility.messages.cam.v230.model.highfrequencycontainer.Heading;
import com.orange.iot3mobility.messages.cam.v230.model.highfrequencycontainer.Speed;
import com.orange.iot3mobility.messages.cam.v230.model.highfrequencycontainer.VehicleLength;
import com.orange.iot3mobility.messages.cam.v230.model.highfrequencycontainer.YawRate;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class CamCodecTest {

    @Test
    void writeRead113RoundTrip() throws Exception {
        CamEnvelope113 envelope = validEnvelope113();
        CamCodec codec = new CamCodec(new JsonFactory());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(CamVersion.V1_1_3, envelope, out);

        CamCodec.CamFrame<?> frame = codec.read(new ByteArrayInputStream(out.toByteArray()));
        assertEquals(CamVersion.V1_1_3, frame.version());
        assertTrue(frame.envelope() instanceof CamEnvelope113);
        CamEnvelope113 parsed = (CamEnvelope113) frame.envelope();
        assertEquals(envelope.sourceUuid(), parsed.sourceUuid());
        assertEquals(envelope.timestamp(), parsed.timestamp());
    }

    @Test
    void writeRead230RoundTrip() throws Exception {
        CamEnvelope230 envelope = validEnvelope230();
        CamCodec codec = new CamCodec(new JsonFactory());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(CamVersion.V2_3_0, envelope, out);

        CamCodec.CamFrame<?> frame = codec.read(new ByteArrayInputStream(out.toByteArray()));
        assertEquals(CamVersion.V2_3_0, frame.version());
        assertTrue(frame.envelope() instanceof CamEnvelope230);
        CamEnvelope230 parsed = (CamEnvelope230) frame.envelope();
        assertEquals(envelope.sourceUuid(), parsed.sourceUuid());
        assertEquals(envelope.timestamp(), parsed.timestamp());
    }

    @Test
    void readRejectsMissingVersion() {
        CamCodec codec = new CamCodec(new JsonFactory());
        String json = "{\"type\":\"cam\"}";
        assertThrows(CamException.class, () -> codec.read(json));
    }

    private static CamEnvelope113 validEnvelope113() {
        com.orange.iot3mobility.messages.cam.v113.model.BasicContainer basic =
                com.orange.iot3mobility.messages.cam.v113.model.BasicContainer.builder()
                 .stationType(5)
                 .referencePosition(new ReferencePosition(0, 0, 0))
                 .build();
        HighFrequencyContainer high = HighFrequencyContainer.builder().build();
        CamMessage113 message = CamMessage113.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .basicContainer(basic)
                .highFrequencyContainer(high)
                .build();

        return CamEnvelope113.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(1514764800000L)
                .message(message)
                .build();
    }

    private static CamEnvelope230 validEnvelope230() {
        com.orange.iot3mobility.messages.cam.v230.model.basiccontainer.ReferencePosition reference =
                com.orange.iot3mobility.messages.cam.v230.model.basiccontainer.ReferencePosition.builder()
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

        return CamEnvelope230.builder()
                .messageFormat("json/raw")
                .sourceUuid("com_car_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();
    }
}
