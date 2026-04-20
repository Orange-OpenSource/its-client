/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.managers;

import com.orange.iot3mobility.messages.cpm.CpmHelper;
import com.orange.iot3mobility.messages.cpm.v121.model.CpmEnvelope121;
import com.orange.iot3mobility.messages.cpm.v121.model.CpmMessage121;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.ManagementConfidence;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.ReferencePosition;
import com.orange.iot3mobility.messages.cpm.v211.model.CpmEnvelope211;
import com.orange.iot3mobility.messages.cpm.v211.model.CpmMessage211;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.Altitude;
import com.orange.iot3mobility.roadobjects.RoadSensor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoadSensorManagerTest {

    private IoT3RoadSensorCallback mockCallback;
    private final CpmHelper cpmHelper = new CpmHelper();

    @BeforeEach
    void resetStaticState() throws Exception {
        mockCallback = mock(IoT3RoadSensorCallback.class);

        Field sensorsField = RoadSensorManager.class.getDeclaredField("ROAD_SENSORS");
        sensorsField.setAccessible(true);
        ((ArrayList<?>) sensorsField.get(null)).clear();

        Field mapField = RoadSensorManager.class.getDeclaredField("ROAD_SENSOR_MAP");
        mapField.setAccessible(true);
        ((HashMap<?, ?>) mapField.get(null)).clear();

        RoadSensorManager.init(mockCallback);
    }

    // -------------------------------------------------------------------------
    // Helper factories
    // -------------------------------------------------------------------------

    /** Build a minimal v1.2.1 CPM JSON string. */
    private String cpm121Json(String sourceUuid, long stationId) throws Exception {
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
                .stationId(stationId)
                .generationDeltaTime(1)
                .managementContainer(management)
                // no perceivedObjectContainer: omitted → null, avoids [1,128] size validation
                .build();
        CpmEnvelope121 envelope = CpmEnvelope121.builder()
                .origin("self")
                .sourceUuid(sourceUuid)
                .timestamp(1514764800000L)
                .message(message)
                .build();
        return cpmHelper.toJson(envelope);
    }

    /** Build a minimal v2.1.1 CPM JSON string. */
    private String cpm211Json(String sourceUuid, long stationId) throws Exception {
        com.orange.iot3mobility.messages.cpm.v211.model.defs.PositionConfidenceEllipse ellipse =
                new com.orange.iot3mobility.messages.cpm.v211.model.defs.PositionConfidenceEllipse(0, 0, 0);
        com.orange.iot3mobility.messages.cpm.v211.model.defs.ReferencePosition ref =
                com.orange.iot3mobility.messages.cpm.v211.model.defs.ReferencePosition.builder()
                        .latitudeLongitude(488566000, 23522000)
                        .positionConfidenceEllipse(ellipse)
                        .altitude(new Altitude(0, 0))
                        .build();
        com.orange.iot3mobility.messages.cpm.v211.model.managementcontainer.ManagementContainer management =
                com.orange.iot3mobility.messages.cpm.v211.model.managementcontainer.ManagementContainer.builder()
                .referenceTime(0)
                .referencePosition(ref)
                .build();
        CpmMessage211 message = CpmMessage211.builder()
                .protocolVersion(1)
                .stationId(stationId)
                .managementContainer(management)
                .build();
        CpmEnvelope211 envelope = CpmEnvelope211.builder()
                .sourceUuid(sourceUuid)
                .timestamp(1514764800000L)
                .message(message)
                .build();
        return cpmHelper.toJson(envelope);
    }

    // -------------------------------------------------------------------------
    // v1.2.1 tests
    // -------------------------------------------------------------------------

    @Test
    void processCpm121FirstMessageTriggersNewRoadSensor() throws Exception {
        RoadSensorManager.processCpm(cpm121Json("sensor_A", 1), cpmHelper);

        verify(mockCallback, times(1)).newRoadSensor(any(RoadSensor.class));
        verify(mockCallback, never()).roadSensorUpdate(any());
    }

    @Test
    void processCpm121NewSensorHasCorrectPosition() throws Exception {
        RoadSensorManager.processCpm(cpm121Json("sensor_B", 2), cpmHelper);

        ArgumentCaptor<RoadSensor> captor = ArgumentCaptor.forClass(RoadSensor.class);
        verify(mockCallback).newRoadSensor(captor.capture());
        assertEquals(48.8566, captor.getValue().getPosition().getLatitude(), 1e-4);
        assertEquals(2.3522, captor.getValue().getPosition().getLongitude(), 1e-4);
    }

    @Test
    void processCpm121SecondMessageTriggersRoadSensorUpdate() throws Exception {
        String json = cpm121Json("sensor_C", 3);
        RoadSensorManager.processCpm(json, cpmHelper);
        RoadSensorManager.processCpm(json, cpmHelper);

        verify(mockCallback, times(1)).newRoadSensor(any());
        verify(mockCallback, times(1)).roadSensorUpdate(any());
    }

    @Test
    void processCpm121CpmArrivedCallbackIsAlwaysFired() throws Exception {
        String json = cpm121Json("sensor_D", 4);
        RoadSensorManager.processCpm(json, cpmHelper);
        RoadSensorManager.processCpm(json, cpmHelper);

        verify(mockCallback, times(2)).cpmArrived(any());
    }

    @Test
    void processCpm121TwoDifferentSensorsAreCreated() throws Exception {
        RoadSensorManager.processCpm(cpm121Json("sensor_E", 5), cpmHelper);
        RoadSensorManager.processCpm(cpm121Json("sensor_F", 6), cpmHelper);

        verify(mockCallback, times(2)).newRoadSensor(any());
        assertEquals(2, RoadSensorManager.getRoadSensors().size());
    }

    // -------------------------------------------------------------------------
    // v2.1.1 tests
    // -------------------------------------------------------------------------

    @Test
    void processCpm211FirstMessageTriggersNewRoadSensor() throws Exception {
        RoadSensorManager.processCpm(cpm211Json("sensor_G", 7), cpmHelper);

        verify(mockCallback, times(1)).newRoadSensor(any(RoadSensor.class));
    }

    @Test
    void processCpm211SecondMessageTriggersUpdate() throws Exception {
        String json = cpm211Json("sensor_H", 8);
        RoadSensorManager.processCpm(json, cpmHelper);
        RoadSensorManager.processCpm(json, cpmHelper);

        verify(mockCallback, times(1)).newRoadSensor(any());
        verify(mockCallback, times(1)).roadSensorUpdate(any());
    }

    // -------------------------------------------------------------------------
    // Edge cases
    // -------------------------------------------------------------------------

    @Test
    void processCpmInvalidJsonDoesNotCrash() {
        assertDoesNotThrow(() ->
                RoadSensorManager.processCpm("{not valid json}", cpmHelper));
    }

    @Test
    void getRoadSensorsReturnsUnmodifiableList() {
        assertThrows(UnsupportedOperationException.class,
                () -> RoadSensorManager.getRoadSensors().add(null));
    }
}

