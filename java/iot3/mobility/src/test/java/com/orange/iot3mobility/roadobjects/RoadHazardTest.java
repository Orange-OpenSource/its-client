/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.roadobjects;

import com.orange.iot3mobility.messages.EtsiConverter;
import com.orange.iot3mobility.messages.denm.core.DenmCodec;
import com.orange.iot3mobility.messages.denm.core.DenmVersion;
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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoadHazardTest {

    // Paris ETSI-encoded coordinates
    private static final int PARIS_LAT_ETSI = EtsiConverter.latitudeEtsi(48.8566);
    private static final int PARIS_LON_ETSI = EtsiConverter.longitudeEtsi(2.3522);

    // -------------------------------------------------------------------------
    // v1.1.3 DENM frame
    // -------------------------------------------------------------------------

    private static DenmCodec.DenmFrame<?> buildFrame113(int validityDurationSec,
                                                         int causeCause, int subcause) {
        ReferencePosition eventPosition = new ReferencePosition(PARIS_LAT_ETSI, PARIS_LON_ETSI, 0);

        ManagementContainer management = ManagementContainer.builder()
                .actionId(new ActionId(1, 1))
                .detectionTime(0)
                .referenceTime(0)
                .eventPosition(eventPosition)
                .validityDuration(validityDurationSec)
                .build();

        SituationContainer situation = SituationContainer.builder()
                .eventType(new EventType(causeCause, subcause))
                .build();

        DenmMessage113 message = DenmMessage113.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(management)
                .situationContainer(situation)
                .build();

        DenmEnvelope113 envelope = DenmEnvelope113.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(System.currentTimeMillis())
                .message(message)
                .build();

        return new DenmCodec.DenmFrame<>(DenmVersion.V1_1_3, envelope);
    }

    @Test
    void roadHazard113ExtractsPositionCorrectly() {
        RoadHazard hazard = new RoadHazard("h1", buildFrame113(600, 1, 5));
        assertNotNull(hazard.getPosition());
        assertEquals(48.8566, hazard.getPosition().getLatitude(), 1e-4);
        assertEquals(2.3522, hazard.getPosition().getLongitude(), 1e-4);
    }

    @Test
    void roadHazard113ExtractsHazardTypeCorrectly() {
        // cause=1 (TRAFFIC_CONDITION), subcause=5 → TRAFFIC_STATIONARY
        RoadHazard hazard = new RoadHazard("h2", buildFrame113(600, 1, 5));
        assertEquals(HazardType.TRAFFIC_STATIONARY, hazard.getType());
    }

    @Test
    void roadHazard113StillLivingWithPositiveLifetime() {
        // 600 s lifetime → should be alive immediately
        RoadHazard hazard = new RoadHazard("h3", buildFrame113(600, 1, 0));
        assertTrue(hazard.stillLiving(),
                "Hazard with 600s lifetime should still be living right after creation");
    }

    @Test
    void roadHazard113GetUuidReturnsConstructedValue() {
        RoadHazard hazard = new RoadHazard("my-uuid", buildFrame113(600, 1, 0));
        assertEquals("my-uuid", hazard.getUuid());
    }

    @Test
    void roadHazard113TimestampIsSet() {
        long before = System.currentTimeMillis();
        RoadHazard hazard = new RoadHazard("h4", buildFrame113(600, 1, 0));
        long after = System.currentTimeMillis();
        // timestamp comes from the envelope, which was built using System.currentTimeMillis()
        assertTrue(hazard.getTimestamp() >= before - 10 && hazard.getTimestamp() <= after + 10,
                "Hazard timestamp should be close to now");
    }

    // -------------------------------------------------------------------------
    // v2.2.0 DENM frame
    // -------------------------------------------------------------------------

    private static DenmCodec.DenmFrame<?> buildFrame220(int validityDurationSec,
                                                         int causeCode, int subcause) {
        com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ReferencePosition position =
                com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ReferencePosition.builder()
                        .latitude(PARIS_LAT_ETSI)
                        .longitude(PARIS_LON_ETSI)
                        .positionConfidenceEllipse(new PositionConfidenceEllipse(0, 0, 0))
                        .altitude(new Altitude(0, 0))
                        .build();

        com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ManagementContainer management =
                com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ManagementContainer.builder()
                        .actionId(new com.orange.iot3mobility.messages.denm.v220.model.managementcontainer.ActionId(1, 1))
                        .detectionTime(0)
                        .referenceTime(0)
                        .eventPosition(position)
                        .validityDuration(validityDurationSec)
                        .stationType(5)
                        .build();

        com.orange.iot3mobility.messages.denm.v220.model.situationcontainer.SituationContainer situation =
                com.orange.iot3mobility.messages.denm.v220.model.situationcontainer.SituationContainer.builder()
                        .informationQuality(7)
                        .eventType(new CauseCode(causeCode, subcause))
                        .build();

        DenmMessage220 message = DenmMessage220.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(management)
                .situationContainer(situation)
                .build();

        DenmEnvelope220 envelope = DenmEnvelope220.builder()
                .sourceUuid("com_application_42")
                .timestamp(System.currentTimeMillis())
                .message(message)
                .build();

        return new DenmCodec.DenmFrame<>(DenmVersion.V2_2_0, envelope);
    }

    @Test
    void roadHazard220ExtractsPositionCorrectly() {
        RoadHazard hazard = new RoadHazard("h5", buildFrame220(600, 2, 1));
        assertNotNull(hazard.getPosition());
        assertEquals(48.8566, hazard.getPosition().getLatitude(), 1e-4);
        assertEquals(2.3522, hazard.getPosition().getLongitude(), 1e-4);
    }

    @Test
    void roadHazard220ExtractsHazardTypeCorrectly() {
        // cause=2 (ACCIDENT), subcause=1 → MULTI_VEHICLE_ACCIDENT
        RoadHazard hazard = new RoadHazard("h6", buildFrame220(600, 2, 1));
        assertEquals(HazardType.MULTI_VEHICLE_ACCIDENT, hazard.getType());
    }

    @Test
    void roadHazard220StillLivingWithPositiveLifetime() {
        RoadHazard hazard = new RoadHazard("h7", buildFrame220(600, 1, 0));
        assertTrue(hazard.stillLiving());
    }

    // -------------------------------------------------------------------------
    // setDenmFrame updates the hazard state
    // -------------------------------------------------------------------------

    @Test
    void setDenmFrameUpdatesPosition() {
        RoadHazard hazard = new RoadHazard("h8", buildFrame113(600, 1, 0));

        // Build a new frame with a different position (London)
        int londonLatEtsi = EtsiConverter.latitudeEtsi(51.5074);
        int londonLonEtsi = EtsiConverter.longitudeEtsi(-0.1278);

        ReferencePosition newPosition = new ReferencePosition(londonLatEtsi, londonLonEtsi, 0);
        ManagementContainer management = ManagementContainer.builder()
                .actionId(new ActionId(1, 1))
                .detectionTime(0)
                .referenceTime(0)
                .eventPosition(newPosition)
                .validityDuration(600)
                .build();

        SituationContainer situation = SituationContainer.builder()
                .eventType(new EventType(3, 0))
                .build();

        DenmMessage113 updatedMessage = DenmMessage113.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(management)
                .situationContainer(situation)
                .build();

        DenmEnvelope113 updatedEnvelope = DenmEnvelope113.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(System.currentTimeMillis())
                .message(updatedMessage)
                .build();

        hazard.setDenmFrame(new DenmCodec.DenmFrame<>(DenmVersion.V1_1_3, updatedEnvelope));

        assertEquals(51.5074, hazard.getPosition().getLatitude(), 1e-4);
        assertEquals(-0.1278, hazard.getPosition().getLongitude(), 1e-4);
    }

    @Test
    void setDenmFrameUpdatesHazardType() {
        RoadHazard hazard = new RoadHazard("h9", buildFrame113(600, 1, 5));
        assertEquals(HazardType.TRAFFIC_STATIONARY, hazard.getType());

        // Update to ROADWORKS
        ReferencePosition pos = new ReferencePosition(PARIS_LAT_ETSI, PARIS_LON_ETSI, 0);
        ManagementContainer mgmt = ManagementContainer.builder()
                .actionId(new ActionId(1, 1))
                .detectionTime(0)
                .referenceTime(0)
                .eventPosition(pos)
                .validityDuration(600)
                .build();

        SituationContainer newSituation = SituationContainer.builder()
                .eventType(new EventType(3, 0)) // ROADWORKS_NO_SUBCAUSE
                .build();

        DenmMessage113 newMessage = DenmMessage113.builder()
                .protocolVersion(1)
                .stationId(42)
                .managementContainer(mgmt)
                .situationContainer(newSituation)
                .build();

        DenmEnvelope113 newEnvelope = DenmEnvelope113.builder()
                .origin("self")
                .sourceUuid("CCU6")
                .timestamp(System.currentTimeMillis())
                .message(newMessage)
                .build();

        hazard.setDenmFrame(new DenmCodec.DenmFrame<>(DenmVersion.V1_1_3, newEnvelope));
        assertEquals(HazardType.ROADWORKS_NO_SUBCAUSE, hazard.getType());
    }
}

