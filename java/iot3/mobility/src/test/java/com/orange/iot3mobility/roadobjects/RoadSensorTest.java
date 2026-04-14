/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.
 */
package com.orange.iot3mobility.roadobjects;

import com.orange.iot3mobility.managers.IoT3RoadSensorCallback;
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
import com.orange.iot3mobility.quadkey.LatLng;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoadSensorTest {

    private IoT3RoadSensorCallback mockCallback;
    private static final LatLng SENSOR_POSITION = new LatLng(48.8566, 2.3522);

    @BeforeEach
    void setUp() {
        mockCallback = mock(IoT3RoadSensorCallback.class);
    }

    // -------------------------------------------------------------------------
    // Helpers to build CPM v1.2.1 frames
    // -------------------------------------------------------------------------

    private static CpmCodec.CpmFrame<?> buildCpm121Frame(List<PerceivedObject> objects) {
        ReferencePosition ref = new ReferencePosition(488566000, 23522000, 0);
        PositionConfidenceEllipse ellipse = new PositionConfidenceEllipse(0, 0, 0);
        ManagementConfidence confidence = new ManagementConfidence(ellipse, 0);
        ManagementContainer management = ManagementContainer.builder()
                .stationType(5)
                .referencePosition(ref)
                .confidence(confidence)
                .build();

        CpmMessage121 message = CpmMessage121.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .managementContainer(management)
                .perceivedObjectContainer(new PerceivedObjectContainer(objects))
                .build();

        CpmEnvelope121 envelope = CpmEnvelope121.builder()
                .origin("self")
                .sourceUuid("sensor_CCU6")
                .timestamp(System.currentTimeMillis())
                .message(message)
                .build();

        return new CpmCodec.CpmFrame<>(CpmVersion.V1_2_1, envelope);
    }

    private static PerceivedObject makePerceivedObject(int id, int xDist, int yDist,
                                                        int xSpeed, int ySpeed) {
        PerceivedObjectConfidence conf = PerceivedObjectConfidence.builder()
                .distance(1, 1)
                .speed(0, 0)
                .object(7)
                .build();
        return PerceivedObject.builder()
                .objectId(id)
                .timeOfMeasurement(0)
                .distance(xDist, yDist)
                .speed(xSpeed, ySpeed)
                .objectAge(0)
                .confidence(conf)
                .build();
    }

    // -------------------------------------------------------------------------
    // Basic state tests
    // -------------------------------------------------------------------------

    @Test
    void getUuidReturnsConstructedValue() {
        CpmCodec.CpmFrame<?> frame = buildCpm121Frame(List.of());
        RoadSensor sensor = new RoadSensor("sensor-uuid", SENSOR_POSITION, frame, mockCallback);
        assertEquals("sensor-uuid", sensor.getUuid());
    }

    @Test
    void getPositionReturnsConstructedValue() {
        CpmCodec.CpmFrame<?> frame = buildCpm121Frame(List.of());
        RoadSensor sensor = new RoadSensor("sensor-uuid", SENSOR_POSITION, frame, mockCallback);
        assertEquals(48.8566, sensor.getPosition().getLatitude(), 1e-9);
        assertEquals(2.3522, sensor.getPosition().getLongitude(), 1e-9);
    }

    @Test
    void stillLivingIsTrueImmediatelyAfterCreation() {
        CpmCodec.CpmFrame<?> frame = buildCpm121Frame(List.of());
        RoadSensor sensor = new RoadSensor("sensor-uuid", SENSOR_POSITION, frame, mockCallback);
        assertTrue(sensor.stillLiving(), "A freshly created RoadSensor must be living");
    }

    @Test
    void updateTimestampResetsLivingTimer() {
        CpmCodec.CpmFrame<?> frame = buildCpm121Frame(List.of());
        RoadSensor sensor = new RoadSensor("sensor-uuid", SENSOR_POSITION, frame, mockCallback);
        sensor.updateTimestamp();
        assertTrue(sensor.stillLiving());
    }

    // -------------------------------------------------------------------------
    // New sensor object creation (v1.2.1)
    // -------------------------------------------------------------------------

    @Test
    void constructorWithOnePerceivedObjectCallsNewSensorObject() {
        PerceivedObject obj = makePerceivedObject(0, 100, 200, 0, 0);
        CpmCodec.CpmFrame<?> frame = buildCpm121Frame(List.of(obj));

        new RoadSensor("s1", SENSOR_POSITION, frame, mockCallback);

        verify(mockCallback, times(1)).newSensorObject(any(SensorObject.class));
        verify(mockCallback, never()).sensorObjectUpdate(any());
    }

    @Test
    void constructorWithTwoPerceivedObjectsCallsNewSensorObjectTwice() {
        PerceivedObject obj1 = makePerceivedObject(0, 100, 200, 0, 0);
        PerceivedObject obj2 = makePerceivedObject(1, 300, 400, 0, 0);
        CpmCodec.CpmFrame<?> frame = buildCpm121Frame(List.of(obj1, obj2));

        new RoadSensor("s2", SENSOR_POSITION, frame, mockCallback);

        verify(mockCallback, times(2)).newSensorObject(any(SensorObject.class));
    }

    @Test
    void newSensorObjectHasCorrectUuidPattern() {
        PerceivedObject obj = makePerceivedObject(7, 100, 200, 0, 0);
        CpmCodec.CpmFrame<?> frame = buildCpm121Frame(List.of(obj));

        new RoadSensor("s3", SENSOR_POSITION, frame, mockCallback);

        ArgumentCaptor<SensorObject> captor = ArgumentCaptor.forClass(SensorObject.class);
        verify(mockCallback).newSensorObject(captor.capture());
        // UUID pattern: sensorUuid_objectId
        assertEquals("s3_7", captor.getValue().getUuid());
    }

    @Test
    void newSensorObjectIsStillLiving() {
        PerceivedObject obj = makePerceivedObject(0, 100, 200, 0, 0);
        CpmCodec.CpmFrame<?> frame = buildCpm121Frame(List.of(obj));

        new RoadSensor("s4", SENSOR_POSITION, frame, mockCallback);

        ArgumentCaptor<SensorObject> captor = ArgumentCaptor.forClass(SensorObject.class);
        verify(mockCallback).newSensorObject(captor.capture());
        assertTrue(captor.getValue().stillLiving());
    }

    // -------------------------------------------------------------------------
    // Sensor object update on setCpmFrame (v1.2.1)
    // -------------------------------------------------------------------------

    @Test
    void setCpmFrameWithSameObjectIdCallsSensorObjectUpdate() {
        PerceivedObject obj = makePerceivedObject(0, 100, 200, 0, 0);
        CpmCodec.CpmFrame<?> frame = buildCpm121Frame(List.of(obj));
        RoadSensor sensor = new RoadSensor("s5", SENSOR_POSITION, frame, mockCallback);

        // Same object id → update
        PerceivedObject updatedObj = makePerceivedObject(0, 150, 250, 100, 0);
        CpmCodec.CpmFrame<?> frame2 = buildCpm121Frame(List.of(updatedObj));
        sensor.setCpmFrame(frame2);

        verify(mockCallback, times(1)).newSensorObject(any());
        verify(mockCallback, times(1)).sensorObjectUpdate(any());
    }

    @Test
    void setCpmFrameWithNewObjectIdCallsNewSensorObjectAgain() {
        PerceivedObject obj = makePerceivedObject(0, 100, 200, 0, 0);
        CpmCodec.CpmFrame<?> frame = buildCpm121Frame(List.of(obj));
        RoadSensor sensor = new RoadSensor("s6", SENSOR_POSITION, frame, mockCallback);

        // Different object id → new
        PerceivedObject newObj = makePerceivedObject(99, 500, 600, 0, 0);
        CpmCodec.CpmFrame<?> frame2 = buildCpm121Frame(List.of(newObj));
        sensor.setCpmFrame(frame2);

        // 2 newSensorObject calls (obj 0 first, then obj 99)
        verify(mockCallback, times(2)).newSensorObject(any());
    }

    // -------------------------------------------------------------------------
    // Empty perceived object list triggers no callback on first call
    // -------------------------------------------------------------------------

    @Test
    void emptyPerceivedObjectsTriggersNoNewSensorObjectCallback() {
        CpmCodec.CpmFrame<?> frame = buildCpm121Frame(List.of());
        new RoadSensor("s7", SENSOR_POSITION, frame, mockCallback);
        verify(mockCallback, never()).newSensorObject(any());
    }

    // -------------------------------------------------------------------------
    // getSensorObjects returns current snapshot
    // -------------------------------------------------------------------------

    @Test
    void getSensorObjectsContainsCreatedObjects() {
        PerceivedObject obj1 = makePerceivedObject(0, 100, 200, 0, 0);
        PerceivedObject obj2 = makePerceivedObject(1, 300, 400, 0, 0);
        CpmCodec.CpmFrame<?> frame = buildCpm121Frame(List.of(obj1, obj2));

        RoadSensor sensor = new RoadSensor("s8", SENSOR_POSITION, frame, mockCallback);

        assertEquals(2, sensor.getSensorObjects().size());
    }
}

