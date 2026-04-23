/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.managers;

import com.orange.iot3mobility.messages.cam.CamHelper;
import com.orange.iot3mobility.messages.cam.v113.model.CamEnvelope113;
import com.orange.iot3mobility.messages.cam.v113.model.CamMessage113;
import com.orange.iot3mobility.messages.cam.v230.model.CamEnvelope230;
import com.orange.iot3mobility.messages.cam.v230.model.CamStructuredData;
import com.orange.iot3mobility.messages.cam.v230.model.basiccontainer.Altitude;
import com.orange.iot3mobility.messages.cam.v230.model.basiccontainer.BasicContainer;
import com.orange.iot3mobility.messages.cam.v230.model.basiccontainer.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.cam.v230.model.basiccontainer.ReferencePosition;
import com.orange.iot3mobility.roadobjects.RoadUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoadUserManagerTest {

    private IoT3RoadUserCallback mockCallback;
    private final CamHelper camHelper = new CamHelper();

    @BeforeEach
    void resetStaticState() throws Exception {
        mockCallback = mock(IoT3RoadUserCallback.class);

        // Clear static collections to avoid cross-test contamination
        Field usersField = RoadUserManager.class.getDeclaredField("ROAD_USERS");
        usersField.setAccessible(true);
        ((ArrayList<?>) usersField.get(null)).clear();

        Field mapField = RoadUserManager.class.getDeclaredField("ROAD_USER_MAP");
        mapField.setAccessible(true);
        ((HashMap<?, ?>) mapField.get(null)).clear();

        RoadUserManager.init(mockCallback);
    }

    // -------------------------------------------------------------------------
    // Helper factories
    // -------------------------------------------------------------------------

    /** Build a minimal valid v1.1.3 CAM JSON string. */
    private String cam113Json(String sourceUuid, long stationId) throws Exception {
        com.orange.iot3mobility.messages.cam.v113.model.BasicContainer basic =
                com.orange.iot3mobility.messages.cam.v113.model.BasicContainer.builder()
                        .stationType(5)
                        .referencePosition(
                                new com.orange.iot3mobility.messages.cam.v113.model.ReferencePosition(
                                        488566000, 23522000, 0))
                        .build();
        com.orange.iot3mobility.messages.cam.v113.model.HighFrequencyContainer hf113 =
                com.orange.iot3mobility.messages.cam.v113.model.HighFrequencyContainer.builder()
                        .heading(900)
                        .speed(1389)
                        .build();
        CamMessage113 message = CamMessage113.builder()
                .protocolVersion(1)
                .stationId((int) stationId)
                .generationDeltaTime(1)
                .basicContainer(basic)
                .highFrequencyContainer(hf113)
                .build();
        CamEnvelope113 envelope = CamEnvelope113.builder()
                .origin("self")
                .sourceUuid(sourceUuid)
                .timestamp(1514764800000L)
                .message(message)
                .build();
        return camHelper.toJson(envelope);
    }

    /** Build a minimal valid v2.3.0 CAM JSON string (all v230 HF types fully qualified). */
    private String cam230Json(String sourceUuid, long stationId) throws Exception {
        ReferencePosition ref = ReferencePosition.builder()
                .latitudeLongitude(488566000, 23522000)
                .positionConfidenceEllipse(new PositionConfidenceEllipse(0, 0, 0))
                .altitude(new Altitude(0, 0))
                .build();
        BasicContainer basic = BasicContainer.builder()
                .stationType(5)
                .referencePosition(ref)
                .build();
        com.orange.iot3mobility.messages.cam.v230.model.highfrequencycontainer.BasicVehicleContainerHighFrequency hf =
                com.orange.iot3mobility.messages.cam.v230.model.highfrequencycontainer.BasicVehicleContainerHighFrequency.builder()
                        .heading(new com.orange.iot3mobility.messages.cam.v230.model.highfrequencycontainer.Heading(900, 1))
                        .speed(new com.orange.iot3mobility.messages.cam.v230.model.highfrequencycontainer.Speed(1389, 1))
                        .driveDirection(0)
                        .vehicleLength(new com.orange.iot3mobility.messages.cam.v230.model.highfrequencycontainer.VehicleLength(45, 0))
                        .vehicleWidth(20)
                        .longitudinalAcceleration(new com.orange.iot3mobility.messages.cam.v230.model.highfrequencycontainer.AccelerationComponent(0, 1))
                        .curvature(new com.orange.iot3mobility.messages.cam.v230.model.highfrequencycontainer.Curvature(0, 0))
                        .curvatureCalculationMode(0)
                        .yawRate(new com.orange.iot3mobility.messages.cam.v230.model.highfrequencycontainer.YawRate(0, 0))
                        .build();
        CamStructuredData message = CamStructuredData.builder()
                .protocolVersion(1)
                .stationId((int) stationId)
                .generationDeltaTime(1)
                .basicContainer(basic)
                .highFrequencyContainer(hf)
                .build();
        CamEnvelope230 envelope = CamEnvelope230.builder()
                .messageFormat("json/raw")
                .sourceUuid(sourceUuid)
                .timestamp(1514764800000L)
                .message(message)
                .build();
        return camHelper.toJson(envelope);
    }

    // -------------------------------------------------------------------------
    // v1.1.3 tests
    // -------------------------------------------------------------------------

    @Test
    void processCam113FirstMessageTriggersNewRoadUser() throws Exception {
        String json = cam113Json("vehicle_A", 1001);
        RoadUserManager.processCam(json, camHelper);

        verify(mockCallback, times(1)).newRoadUser(any(RoadUser.class));
        verify(mockCallback, never()).roadUserUpdate(any());
    }

    @Test
    void processCam113NewRoadUserHasCorrectPosition() throws Exception {
        String json = cam113Json("vehicle_A", 1001);
        RoadUserManager.processCam(json, camHelper);

        ArgumentCaptor<RoadUser> captor = ArgumentCaptor.forClass(RoadUser.class);
        verify(mockCallback).newRoadUser(captor.capture());
        RoadUser user = captor.getValue();
        assertEquals(48.8566, user.getPosition().getLatitude(), 1e-4);
        assertEquals(2.3522, user.getPosition().getLongitude(), 1e-4);
    }

    @Test
    void processCam113SecondMessageTriggersRoadUserUpdate() throws Exception {
        String json = cam113Json("vehicle_B", 2002);
        RoadUserManager.processCam(json, camHelper);
        RoadUserManager.processCam(json, camHelper);

        verify(mockCallback, times(1)).newRoadUser(any());
        verify(mockCallback, times(1)).roadUserUpdate(any());
    }

    @Test
    void processCam113CamArrivedCallbackIsAlwaysFired() throws Exception {
        String json = cam113Json("vehicle_C", 3003);
        RoadUserManager.processCam(json, camHelper);
        RoadUserManager.processCam(json, camHelper);

        verify(mockCallback, times(2)).camArrived(any());
    }

    @Test
    void processCam113TwoDifferentVehiclesBothCreate() throws Exception {
        RoadUserManager.processCam(cam113Json("veh_X", 101), camHelper);
        RoadUserManager.processCam(cam113Json("veh_Y", 102), camHelper);

        verify(mockCallback, times(2)).newRoadUser(any());
        assertEquals(2, RoadUserManager.getRoadUsers().size());
    }

    @Test
    void processCamInvalidJsonDoesNotCrash() {
        assertDoesNotThrow(() ->
                RoadUserManager.processCam("{not valid json at all}", camHelper));
    }

    // -------------------------------------------------------------------------
    // v2.3.0 tests
    // -------------------------------------------------------------------------

    @Test
    void processCam230FirstMessageTriggersNewRoadUser() throws Exception {
        String json = cam230Json("com_car_42", 42);
        RoadUserManager.processCam(json, camHelper);

        verify(mockCallback, times(1)).newRoadUser(any(RoadUser.class));
    }

    @Test
    void processCam230SecondMessageTriggersRoadUserUpdate() throws Exception {
        String json = cam230Json("com_car_43", 43);
        RoadUserManager.processCam(json, camHelper);
        RoadUserManager.processCam(json, camHelper);

        verify(mockCallback, times(1)).newRoadUser(any());
        verify(mockCallback, times(1)).roadUserUpdate(any());
    }

    @Test
    void getRoadUsersReturnsUnmodifiableList() {
        assertThrows(UnsupportedOperationException.class,
                () -> RoadUserManager.getRoadUsers().add(null));
    }
}

