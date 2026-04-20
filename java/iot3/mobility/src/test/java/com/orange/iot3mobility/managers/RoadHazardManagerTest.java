/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.managers;

import com.orange.iot3mobility.messages.denm.DenmHelper;
import com.orange.iot3mobility.messages.denm.v113.model.DenmEnvelope113;
import com.orange.iot3mobility.messages.denm.v113.model.DenmMessage113;
import com.orange.iot3mobility.messages.denm.v113.model.managementcontainer.ActionId;
import com.orange.iot3mobility.messages.denm.v113.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.denm.v113.model.managementcontainer.ReferencePosition;
import com.orange.iot3mobility.messages.denm.v113.model.situationcontainer.EventType;
import com.orange.iot3mobility.messages.denm.v113.model.situationcontainer.SituationContainer;
import com.orange.iot3mobility.messages.denm.v220.model.DenmEnvelope220;
import com.orange.iot3mobility.messages.denm.v220.model.DenmMessage220;
import com.orange.iot3mobility.messages.denm.v220.model.defs.Altitude;
import com.orange.iot3mobility.messages.denm.v220.model.defs.PositionConfidenceEllipse;
import com.orange.iot3mobility.messages.denm.v220.model.situationcontainer.CauseCode;
import com.orange.iot3mobility.roadobjects.RoadHazard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoadHazardManagerTest {

    private IoT3RoadHazardCallback mockCallback;
    private final DenmHelper denmHelper = new DenmHelper();

    // Valid far-future timestamp so hazard is not expired
    private static final long VALID_TIMESTAMP = System.currentTimeMillis();

    @BeforeEach
    void resetStaticState() throws Exception {
        mockCallback = mock(IoT3RoadHazardCallback.class);

        Field hazardsField = RoadHazardManager.class.getDeclaredField("ROAD_HAZARDS");
        hazardsField.setAccessible(true);
        ((ArrayList<?>) hazardsField.get(null)).clear();

        Field mapField = RoadHazardManager.class.getDeclaredField("ROAD_HAZARD_MAP");
        mapField.setAccessible(true);
        ((HashMap<?, ?>) mapField.get(null)).clear();

        RoadHazardManager.init(mockCallback);
    }

    // -------------------------------------------------------------------------
    // Helper factories
    // -------------------------------------------------------------------------

    /** Build a v1.1.3 DENM JSON with given sequenceNumber and validityDuration.
     *  termination=null means active hazard. */
    private String denm113Json(int originatingStationId, int sequenceNumber,
                                int validityDurationSec, Integer termination) throws Exception {
        ReferencePosition pos = new ReferencePosition(488566000, 23522000, 0);
        ManagementContainer mgmt = ManagementContainer.builder()
                .actionId(new ActionId(originatingStationId, sequenceNumber))
                .detectionTime(0)
                .referenceTime(0)
                .eventPosition(pos)
                .validityDuration(validityDurationSec)
                .termination(termination)
                .build();
        SituationContainer situation = SituationContainer.builder()
                .eventType(new EventType(1, 5)) // TRAFFIC_STATIONARY
                .build();
        DenmMessage113 message = DenmMessage113.builder()
                .protocolVersion(1)
                .stationId(originatingStationId)
                .managementContainer(mgmt)
                .situationContainer(situation)
                .build();
        DenmEnvelope113 envelope = DenmEnvelope113.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(VALID_TIMESTAMP)
                .message(message)
                .build();
        return denmHelper.toJson(envelope);
    }

    /** Build a v2.2.0 DENM JSON with given sequenceNumber and validityDuration. */
    private String denm220Json(int originatingStationId, int sequenceNumber,
                                int validityDurationSec, Integer termination) throws Exception {
        com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ReferencePosition pos =
                com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ReferencePosition.builder()
                        .latitude(488566000)
                        .longitude(23522000)
                        .positionConfidenceEllipse(new PositionConfidenceEllipse(0, 0, 0))
                        .altitude(new Altitude(0, 0))
                        .build();
        com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ManagementContainer mgmt =
                com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ManagementContainer.builder()
                        .actionId(new com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ActionId(
                                originatingStationId, sequenceNumber))
                        .detectionTime(0)
                        .referenceTime(0)
                        .eventPosition(pos)
                        .validityDuration(validityDurationSec)
                        .termination(termination)
                        .stationType(5)
                        .build();
        com.orange.iot3mobility.messages.denm.v220.model.situationcontainer.SituationContainer situation =
                com.orange.iot3mobility.messages.denm.v220.model.situationcontainer.SituationContainer.builder()
                        .informationQuality(7)
                        .eventType(new CauseCode(1, 5))
                        .build();
        DenmMessage220 message = DenmMessage220.builder()
                .protocolVersion(1)
                .stationId(originatingStationId)
                .managementContainer(mgmt)
                .situationContainer(situation)
                .build();
        DenmEnvelope220 envelope = DenmEnvelope220.builder()
                .sourceUuid("com_application_42")
                .timestamp(VALID_TIMESTAMP)
                .message(message)
                .build();
        return denmHelper.toJson(envelope);
    }

    // -------------------------------------------------------------------------
    // v1.1.3 tests
    // -------------------------------------------------------------------------

    @Test
    void processDenm113FirstMessageTriggersNewRoadHazard() throws Exception {
        String json = denm113Json(1, 1, 600, null);
        RoadHazardManager.processDenm(json, denmHelper);

        verify(mockCallback, times(1)).newRoadHazard(any(RoadHazard.class));
        verify(mockCallback, never()).roadHazardUpdate(any());
    }

    @Test
    void processDenm113DenmArrivedCallbackIsAlwaysFired() throws Exception {
        String json = denm113Json(1, 10, 600, null);
        RoadHazardManager.processDenm(json, denmHelper);
        RoadHazardManager.processDenm(json, denmHelper);

        verify(mockCallback, times(2)).denmArrived(any());
    }

    @Test
    void processDenm113SecondMessageWithSameIdTriggersUpdate() throws Exception {
        String json = denm113Json(1, 20, 600, null);
        RoadHazardManager.processDenm(json, denmHelper);
        RoadHazardManager.processDenm(json, denmHelper);

        verify(mockCallback, times(1)).newRoadHazard(any());
        verify(mockCallback, times(1)).roadHazardUpdate(any());
    }

    @Test
    void processDenm113TerminationRemovesExistingHazard() throws Exception {
        RoadHazardManager.processDenm(denm113Json(1, 30, 600, null), denmHelper);
        RoadHazardManager.processDenm(denm113Json(1, 30, 600, 0 /* termination */), denmHelper);

        verify(mockCallback, times(1)).newRoadHazard(any());
        verify(mockCallback, times(1)).roadHazardExpired(any());
        verify(mockCallback, never()).roadHazardUpdate(any());
        assertEquals(0, RoadHazardManager.getRoadHazards().size());
    }

    @Test
    void processDenm113NewHazardHasCorrectPosition() throws Exception {
        RoadHazardManager.processDenm(denm113Json(1, 50, 600, null), denmHelper);

        ArgumentCaptor<RoadHazard> captor = ArgumentCaptor.forClass(RoadHazard.class);
        verify(mockCallback).newRoadHazard(captor.capture());
        assertEquals(48.8566, captor.getValue().getPosition().getLatitude(), 1e-4);
        assertEquals(2.3522, captor.getValue().getPosition().getLongitude(), 1e-4);
    }

    @Test
    void processDenm113TwoDifferentHazardsAreCreated() throws Exception {
        RoadHazardManager.processDenm(denm113Json(1, 60, 600, null), denmHelper);
        RoadHazardManager.processDenm(denm113Json(1, 61, 600, null), denmHelper);

        verify(mockCallback, times(2)).newRoadHazard(any());
        assertEquals(2, RoadHazardManager.getRoadHazards().size());
    }

    // -------------------------------------------------------------------------
    // v2.2.0 tests
    // -------------------------------------------------------------------------

    @Test
    void processDenm220FirstMessageTriggersNewRoadHazard() throws Exception {
        String json = denm220Json(2, 1, 600, null);
        RoadHazardManager.processDenm(json, denmHelper);

        verify(mockCallback, times(1)).newRoadHazard(any(RoadHazard.class));
    }

    @Test
    void processDenm220SecondMessageTriggersUpdate() throws Exception {
        String json = denm220Json(2, 2, 600, null);
        RoadHazardManager.processDenm(json, denmHelper);
        RoadHazardManager.processDenm(json, denmHelper);

        verify(mockCallback, times(1)).newRoadHazard(any());
        verify(mockCallback, times(1)).roadHazardUpdate(any());
    }

    @Test
    void processDenm220TerminationRemovesHazard() throws Exception {
        RoadHazardManager.processDenm(denm220Json(2, 3, 600, null), denmHelper);
        RoadHazardManager.processDenm(denm220Json(2, 3, 600, 0), denmHelper);

        verify(mockCallback, times(1)).roadHazardExpired(any());
        assertEquals(0, RoadHazardManager.getRoadHazards().size());
    }

    // -------------------------------------------------------------------------
    // Edge cases
    // -------------------------------------------------------------------------

    @Test
    void processDenmInvalidJsonDoesNotCrash() {
        assertDoesNotThrow(() ->
                RoadHazardManager.processDenm("{invalid}", denmHelper));
    }

    @Test
    void getRoadHazardsReturnsUnmodifiableList() {
        assertThrows(UnsupportedOperationException.class,
                () -> RoadHazardManager.getRoadHazards().add(null));
    }
}

