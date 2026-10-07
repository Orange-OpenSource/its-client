/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
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
import com.orange.iot3mobility.messages.cpm.v121.model.stationdatacontainer.OriginatingVehicleContainer;
import com.orange.iot3mobility.messages.cpm.v121.model.stationdatacontainer.StationDataContainer;
import com.orange.iot3mobility.messages.cpm.v121.model.stationdatacontainer.VehicleConfidence;
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
        return buildCpm121Frame(null, objects);
    }

    private static CpmCodec.CpmFrame<?> buildCpm121Frame(StationDataContainer stationDataContainer, List<PerceivedObject> objects) {
        ReferencePosition ref = new ReferencePosition(488566000, 23522000, 0);
        PositionConfidenceEllipse ellipse = new PositionConfidenceEllipse(0, 0, 0);
        ManagementConfidence confidence = new ManagementConfidence(ellipse, 0);
        ManagementContainer management = ManagementContainer.builder()
                .stationType(5)
                .referencePosition(ref)
                .confidence(confidence)
                .build();

        CpmMessage121.Builder messageBuilder = CpmMessage121.builder()
                .protocolVersion(1)
                .stationId(42)
                .generationDeltaTime(1)
                .managementContainer(management)
                .perceivedObjectContainer(new PerceivedObjectContainer(objects));
        if (stationDataContainer != null) {
            messageBuilder.stationDataContainer(stationDataContainer);
        }

        CpmEnvelope121 envelope = CpmEnvelope121.builder()
                .origin("self")
                .sourceUuid("sensor_CCU6")
                .timestamp(System.currentTimeMillis())
                .message(messageBuilder.build())
                .build();

        return new CpmCodec.CpmFrame<>(CpmVersion.V1_2_1, envelope);
    }

    /** Builds a {@link StationDataContainer} for a vehicle-origin CPM v1.2.1, with the given heading (ETSI, 0.1°). */
    private static StationDataContainer buildVehicleStationData(int headingEtsi) {
        VehicleConfidence confidence = VehicleConfidence.builder().heading(1).speed(1).build();
        OriginatingVehicleContainer ovc = OriginatingVehicleContainer.builder()
                .heading(headingEtsi)
                .speed(0)
                .confidence(confidence)
                .build();
        return StationDataContainer.builder().originatingVehicleContainer(ovc).build();
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
    // Helpers to build CPM v2.1.1 frames
    // -------------------------------------------------------------------------

    private static CpmCodec.CpmFrame<?> buildCpm211Frame(
            Integer vehicleHeadingEtsi,
            List<com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.PerceivedObject> objects) {
        var ref = com.orange.iot3mobility.messages.cpm.v211.model.defs.ReferencePosition.builder()
                .latitudeLongitude(488566000, 23522000)
                .positionConfidenceEllipse(new com.orange.iot3mobility.messages.cpm.v211.model.defs.PositionConfidenceEllipse(4095, 4095, 3601))
                .altitude(new com.orange.iot3mobility.messages.cpm.v211.model.defs.Altitude(800001, 15))
                .build();

        var management = com.orange.iot3mobility.messages.cpm.v211.model.managementcontainer.ManagementContainer.builder()
                .referenceTime(0L)
                .referencePosition(ref)
                .build();

        var messageBuilder = com.orange.iot3mobility.messages.cpm.v211.model.CpmMessage211.builder()
                .protocolVersion(2)
                .stationId(42)
                .managementContainer(management)
                .perceivedObjectContainer(
                        new com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.PerceivedObjectContainer(objects));
        if (vehicleHeadingEtsi != null) {
            var ovc = com.orange.iot3mobility.messages.cpm.v211.model.originatingvehiclecontainer.OriginatingVehicleContainer.builder()
                    .orientationAngle(new com.orange.iot3mobility.messages.cpm.v211.model.defs.Angle(vehicleHeadingEtsi, 127))
                    .build();
            messageBuilder.originatingVehicleContainer(ovc);
        }

        var envelope = com.orange.iot3mobility.messages.cpm.v211.model.CpmEnvelope211.builder()
                .sourceUuid("sensor_CCU6")
                .timestamp(System.currentTimeMillis())
                .message(messageBuilder.build())
                .build();

        return new CpmCodec.CpmFrame<>(CpmVersion.V2_1_1, envelope);
    }

    private static com.orange.iot3mobility.messages.cpm.v211.model.defs.CartesianPosition3dWithConfidence position211(int xDist, int yDist) {
        return new com.orange.iot3mobility.messages.cpm.v211.model.defs.CartesianPosition3dWithConfidence(
                new com.orange.iot3mobility.messages.cpm.v211.model.defs.CartesianCoordinateWithConfidence(xDist, 100),
                new com.orange.iot3mobility.messages.cpm.v211.model.defs.CartesianCoordinateWithConfidence(yDist, 100),
                null);
    }

    private static com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.PerceivedObject
            makePerceivedObject211(int id, int xDist, int yDist) {
        return com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.PerceivedObject.builder()
                .measurementDeltaTime(0)
                .position(position211(xDist, yDist))
                .objectId(id)
                .build();
    }

    private static com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.PerceivedObject
            makePerceivedObject211WithCartesianVelocity(int id, int xDist, int yDist, int xSpeed, int ySpeed) {
        var velocity = com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.Velocity.cartesian(
                new com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.CartesianVelocity(
                        new com.orange.iot3mobility.messages.cpm.v211.model.defs.VelocityComponent(xSpeed, 1),
                        new com.orange.iot3mobility.messages.cpm.v211.model.defs.VelocityComponent(ySpeed, 1),
                        null));
        return com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.PerceivedObject.builder()
                .measurementDeltaTime(0)
                .position(position211(xDist, yDist))
                .objectId(id)
                .velocity(velocity)
                .build();
    }

    private static com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.PerceivedObject
            makePerceivedObject211WithPolarVelocity(int id, int xDist, int yDist, int velocityDirectionEtsi) {
        var velocity = com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.Velocity.polar(
                new com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.PolarVelocity(
                        new com.orange.iot3mobility.messages.cpm.v211.model.defs.Speed(100, 1),
                        new com.orange.iot3mobility.messages.cpm.v211.model.defs.Angle(velocityDirectionEtsi, 1),
                        null));
        return com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.PerceivedObject.builder()
                .measurementDeltaTime(0)
                .position(position211(xDist, yDist))
                .objectId(id)
                .velocity(velocity)
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

    // -------------------------------------------------------------------------
    // Heading-relative position offsets (vehicle-origin CPM): x_distance/y_distance (and velocity
    // components) are reported in a body-fixed (ISO 8855) coordinate system when the sender is a
    // vehicle, and must be rotated by the vehicle's own heading to resolve an absolute geographic
    // position. See AGENTS.md / ETSI CDD TS 102 894-2 CartesianPosition3d convention.
    // -------------------------------------------------------------------------

    @Test
    void vehicleHeadingRotatesPerceivedObjectPositionV121() {
        StationDataContainer vehicleStationData = buildVehicleStationData(900); // 90° = East
        PerceivedObject obj = makePerceivedObject(0, 0, 1000, 0, 0); // 10 m "forward" in body frame
        CpmCodec.CpmFrame<?> frame = buildCpm121Frame(vehicleStationData, List.of(obj));

        new RoadSensor("veh121", SENSOR_POSITION, frame, mockCallback);

        ArgumentCaptor<SensorObject> captor = ArgumentCaptor.forClass(SensorObject.class);
        verify(mockCallback).newSensorObject(captor.capture());
        LatLng result = captor.getValue().getPosition();

        // A 90° (East) heading rotates the "forward" (y) offset to due east: longitude increases,
        // latitude is unaffected.
        assertTrue(result.getLongitude() > SENSOR_POSITION.getLongitude());
        assertEquals(SENSOR_POSITION.getLatitude(), result.getLatitude(), 1e-6);
    }

    @Test
    void perceivedObjectPositionUnrotatedWithoutVehicleContainerV121() {
        // No stationDataContainer → RSU/stationary convention (WGS84 north/east-aligned), unchanged behavior.
        PerceivedObject obj = makePerceivedObject(0, 0, 1000, 0, 0);
        CpmCodec.CpmFrame<?> frame = buildCpm121Frame(List.of(obj));

        new RoadSensor("rsu121", SENSOR_POSITION, frame, mockCallback);

        ArgumentCaptor<SensorObject> captor = ArgumentCaptor.forClass(SensorObject.class);
        verify(mockCallback).newSensorObject(captor.capture());
        LatLng result = captor.getValue().getPosition();

        assertTrue(result.getLatitude() > SENSOR_POSITION.getLatitude());
        assertEquals(SENSOR_POSITION.getLongitude(), result.getLongitude(), 1e-6);
    }

    @Test
    void vehicleHeadingRotatesPerceivedObjectPositionV211() {
        var obj = makePerceivedObject211(0, 0, 1000); // 10 m "forward" in body frame
        CpmCodec.CpmFrame<?> frame = buildCpm211Frame(1800, List.of(obj)); // 180° = South

        new RoadSensor("veh211", SENSOR_POSITION, frame, mockCallback);

        ArgumentCaptor<SensorObject> captor = ArgumentCaptor.forClass(SensorObject.class);
        verify(mockCallback).newSensorObject(captor.capture());
        LatLng result = captor.getValue().getPosition();

        assertTrue(result.getLatitude() < SENSOR_POSITION.getLatitude());
        assertEquals(SENSOR_POSITION.getLongitude(), result.getLongitude(), 1e-6);
    }

    @Test
    void vehicleHeadingRotatesCartesianVelocityHeadingV211() {
        // Moving "forward" (y > 0) in the vehicle's body frame.
        var obj = makePerceivedObject211WithCartesianVelocity(0, 0, 1000, 0, 1000);
        CpmCodec.CpmFrame<?> frame = buildCpm211Frame(900, List.of(obj)); // 90° = East

        new RoadSensor("veh211v", SENSOR_POSITION, frame, mockCallback);

        ArgumentCaptor<SensorObject> captor = ArgumentCaptor.forClass(SensorObject.class);
        verify(mockCallback).newSensorObject(captor.capture());

        // Forward motion rotated by a 90° vehicle heading becomes an absolute East bearing.
        assertEquals(90.0, captor.getValue().getBearing(), 1e-6);
    }

    @Test
    void vehicleHeadingDoesNotAffectPolarVelocityDirectionV211() {
        // polar_velocity.velocity_direction is an "angle" DE: always WGS84-absolute, unlike cartesian offsets.
        var obj = makePerceivedObject211WithPolarVelocity(0, 0, 1000, 450); // 45° absolute
        CpmCodec.CpmFrame<?> frame = buildCpm211Frame(900, List.of(obj)); // 90° vehicle heading, must be ignored here

        new RoadSensor("veh211p", SENSOR_POSITION, frame, mockCallback);

        ArgumentCaptor<SensorObject> captor = ArgumentCaptor.forClass(SensorObject.class);
        verify(mockCallback).newSensorObject(captor.capture());

        assertEquals(45.0, captor.getValue().getBearing(), 1e-6);
    }
}

