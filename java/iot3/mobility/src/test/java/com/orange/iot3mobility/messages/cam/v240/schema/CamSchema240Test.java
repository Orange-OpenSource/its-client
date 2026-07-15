/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.messages.cam.v240.schema;

import com.fasterxml.jackson.core.JsonFactory;
import com.networknt.schema.JsonSchema;
import com.orange.iot3mobility.messages.SchemaTestUtils;
import com.orange.iot3mobility.messages.cam.core.CamCodec;
import com.orange.iot3mobility.messages.cam.core.CamVersion;
import com.orange.iot3mobility.messages.cam.v240.model.CamEnvelope240;
import com.orange.iot3mobility.messages.cam.v240.model.CamStructuredData;
import com.orange.iot3mobility.messages.cam.v240.model.basiccontainer.Altitude;
import com.orange.iot3mobility.messages.cam.v240.model.basiccontainer.BasicContainer;
import com.orange.iot3mobility.messages.cam.v240.model.basiccontainer.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.cam.v240.model.basiccontainer.ReferencePosition;
import com.orange.iot3mobility.messages.cam.v240.model.highfrequencycontainer.AccelerationComponent;
import com.orange.iot3mobility.messages.cam.v240.model.highfrequencycontainer.AccelerationControl;
import com.orange.iot3mobility.messages.cam.v240.model.highfrequencycontainer.BasicVehicleContainerHighFrequency;
import com.orange.iot3mobility.messages.cam.v240.model.highfrequencycontainer.CenDsrcTollingZone;
import com.orange.iot3mobility.messages.cam.v240.model.highfrequencycontainer.Curvature;
import com.orange.iot3mobility.messages.cam.v240.model.highfrequencycontainer.Heading;
import com.orange.iot3mobility.messages.cam.v240.model.highfrequencycontainer.Speed;
import com.orange.iot3mobility.messages.cam.v240.model.highfrequencycontainer.SteeringWheelAngle;
import com.orange.iot3mobility.messages.cam.v240.model.highfrequencycontainer.VehicleLength;
import com.orange.iot3mobility.messages.cam.v240.model.highfrequencycontainer.YawRate;
import com.orange.iot3mobility.messages.cam.v240.model.lowfrequencycontainer.BasicVehicleContainerLowFrequency;
import com.orange.iot3mobility.messages.cam.v240.model.lowfrequencycontainer.DeltaReferencePosition;
import com.orange.iot3mobility.messages.cam.v240.model.lowfrequencycontainer.ExteriorLights;
import com.orange.iot3mobility.messages.cam.v240.model.lowfrequencycontainer.LowFrequencyContainer;
import com.orange.iot3mobility.messages.cam.v240.model.lowfrequencycontainer.PathPoint;
import com.orange.iot3mobility.messages.cam.v240.model.specialvehiclecontainer.CauseCode;
import com.orange.iot3mobility.messages.cam.v240.model.specialvehiclecontainer.EmergencyContainer;
import com.orange.iot3mobility.messages.cam.v240.model.specialvehiclecontainer.EmergencyPriority;
import com.orange.iot3mobility.messages.cam.v240.model.specialvehiclecontainer.IncidentIndication;
import com.orange.iot3mobility.messages.cam.v240.model.specialvehiclecontainer.LightBarSiren;
import com.orange.iot3mobility.messages.cam.v240.model.specialvehiclecontainer.SpecialVehicleContainer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.List;

class CamSchema240Test {

    private static final JsonSchema SCHEMA;

    static {
        try {
            SCHEMA = SchemaTestUtils.loadSchema("cam/cam_schema_2-4-0.json");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void write_minimalValidEnvelope_conformsToSchema() throws Exception {
        CamEnvelope240 envelope = minimalEnvelope();
        CamCodec codec = new CamCodec(new JsonFactory());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(CamVersion.V2_4_0, envelope, out);

        SchemaTestUtils.assertConformsToSchema(SCHEMA, out.toByteArray());
    }

    @Test
    void write_envelopeWithLowFrequencyContainer_conformsToSchema() throws Exception {
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

        CamEnvelope240 envelope = CamEnvelope240.builder()
                .messageFormat("json/raw")
                .sourceUuid("com_car_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        CamCodec codec = new CamCodec(new JsonFactory());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(CamVersion.V2_4_0, envelope, out);

        SchemaTestUtils.assertConformsToSchema(SCHEMA, out.toByteArray());
    }

    @Test
    void write_fullyPopulatedEnvelope_conformsToSchema() throws Exception {
        BasicVehicleContainerHighFrequency hf = BasicVehicleContainerHighFrequency.builder()
                .heading(new Heading(900, 1))
                .speed(new Speed(1400, 1))
                .driveDirection(0)
                .vehicleLength(new VehicleLength(45, 0))
                .vehicleWidth(18)
                .longitudinalAcceleration(new AccelerationComponent(20, 1))
                .curvature(new Curvature(100, 0))
                .curvatureCalculationMode(0)
                .yawRate(new YawRate(500, 1))
                .accelerationControl(new AccelerationControl(true, false, false, false, false, true, false))
                .lanePosition(1)
                .steeringWheelAngle(new SteeringWheelAngle(10, 1))
                .lateralAcceleration(new AccelerationComponent(5, 1))
                .verticalAcceleration(new AccelerationComponent(3, 1))
                .performanceClass(1)
                .cenDsrcTollingZone(new CenDsrcTollingZone(480000000, 160000000, 42))
                .build();

        LowFrequencyContainer low = new LowFrequencyContainer(
                new BasicVehicleContainerLowFrequency(
                        6,
                        new ExteriorLights(true, false, false, true, false, false, false, true),
                        List.of(
                                new PathPoint(new DeltaReferencePosition(10, 20, 0), 100),
                                new PathPoint(new DeltaReferencePosition(20, 40, 0), 200))));

        EmergencyContainer emergency = EmergencyContainer.builder()
                .lightBarSirenInUse(new LightBarSiren(true, true))
                .incidentIndication(new IncidentIndication(new CauseCode(95, 1)))
                .emergencyPriority(new EmergencyPriority(true, false))
                .build();

        CamStructuredData message = CamStructuredData.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(65000)
                .basicContainer(validBasicContainer())
                .highFrequencyContainer(hf)
                .lowFrequencyContainer(low)
                .specialVehicleContainer(new SpecialVehicleContainer(emergency))
                .build();

        CamEnvelope240 envelope = CamEnvelope240.builder()
                .messageFormat("json/raw")
                .sourceUuid("com_car_42")
                .timestamp(1514764800000L)
                .message(message)
                .build();

        CamCodec codec = new CamCodec(new JsonFactory());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        codec.write(CamVersion.V2_4_0, envelope, out);

        SchemaTestUtils.assertConformsToSchema(SCHEMA, out.toByteArray());
    }

    private static CamEnvelope240 minimalEnvelope() {
        CamStructuredData message = CamStructuredData.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .basicContainer(validBasicContainer())
                .highFrequencyContainer(validHighFrequencyContainer())
                .build();
        return CamEnvelope240.builder()
                .messageFormat("json/raw")
                .sourceUuid("com_car_42")
                .timestamp(1514764800000L)
                .message(message)
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
