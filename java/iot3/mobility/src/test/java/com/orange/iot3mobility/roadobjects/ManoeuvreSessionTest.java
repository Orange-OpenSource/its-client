/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author GitHub Copilot <copilot@github.com>
 @generated GitHub Copilot (Claude Haiku 4.5)
 */
package com.orange.iot3mobility.roadobjects;

import com.orange.iot3mobility.messages.mcm.core.McmCodec;
import com.orange.iot3mobility.quadkey.LatLng;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ManoeuvreSessionTest {

    private static final String TEST_UUID = "test-uuid-123";
    private static final LatLng TEST_POSITION = new LatLng(48.8566, 2.3522);
    private static final List<LatLng> TEST_TRAJECTORY = List.of(
            new LatLng(48.8566, 2.3522),
            new LatLng(48.8567, 2.3523));
    private static final List<Long> TEST_EXECUTANT_IDS = List.of(111L, 222L);
    private static final Map<Long, String> TEST_ADVISED_CHANGES = new LinkedHashMap<Long, String>() {{
        put(111L, "stay_in_lane");
        put(222L, "slow_down");
    }};

    private static McmCodec.McmFrame<?> createMockMcmFrame() {
        return Mockito.mock(McmCodec.McmFrame.class);
    }

    private static ManoeuvreSession createSession(ManoeuvreSourceType sourceType,
                                                   ManoeuvrePhase phase,
                                                   ManoeuvreConcept concept) {
        return new ManoeuvreSession(
                TEST_UUID,
                sourceType,
                phase,
                concept,
                TEST_POSITION,
                TEST_TRAJECTORY,
                TEST_EXECUTANT_IDS,
                TEST_ADVISED_CHANGES,
                createMockMcmFrame());
    }

    // -------------------------------------------------------------------------
    // Construction & Initialization
    // -------------------------------------------------------------------------

    @Test
    void testConstructorInitializesAllFields() {
        ManoeuvreSession session = createSession(ManoeuvreSourceType.VEHICLE,
                ManoeuvrePhase.INTENT,
                ManoeuvreConcept.AGREEMENT_SEEKING);

        assertEquals(TEST_UUID, session.getUuid());
        assertEquals(ManoeuvreSourceType.VEHICLE, session.getSourceType());
        assertEquals(ManoeuvrePhase.INTENT, session.getPhase());
        assertEquals(ManoeuvreConcept.AGREEMENT_SEEKING, session.getConcept());
        assertEquals(TEST_POSITION.getLatitude(), session.getPosition().getLatitude(), 1e-9);
        assertEquals(TEST_POSITION.getLongitude(), session.getPosition().getLongitude(), 1e-9);
        assertEquals(2, session.getPlannedTrajectory().size());
        assertEquals(2, session.getExecutantIds().size());
    }

    @Test
    void testConstructorWithNullTrajectory() {
        ManoeuvreSession session = new ManoeuvreSession(
                TEST_UUID,
                ManoeuvreSourceType.VEHICLE,
                ManoeuvrePhase.REQUEST,
                ManoeuvreConcept.PRESCRIPTIVE,
                TEST_POSITION,
                null,
                TEST_EXECUTANT_IDS,
                TEST_ADVISED_CHANGES,
                createMockMcmFrame());

        assertNull(session.getPlannedTrajectory());
    }

    @Test
    void testConstructorWithNullExecutantIds() {
        ManoeuvreSession session = new ManoeuvreSession(
                TEST_UUID,
                ManoeuvreSourceType.ROADSIDE_UNIT,
                ManoeuvrePhase.OFFER,
                null,
                TEST_POSITION,
                TEST_TRAJECTORY,
                null,
                null,
                createMockMcmFrame());

        assertNull(session.getExecutantIds());
        assertNull(session.getAdvisedChanges());
    }

    // -------------------------------------------------------------------------
    // Getters & Immutability
    // -------------------------------------------------------------------------

    @Test
    void testGettersReturnCorrectValues() {
        ManoeuvreSession session = createSession(ManoeuvreSourceType.VRU,
                ManoeuvrePhase.RESPONSE,
                ManoeuvreConcept.AGREEMENT_SEEKING);

        assertEquals(TEST_UUID, session.getUuid());
        assertEquals(ManoeuvreSourceType.VRU, session.getSourceType());
        assertEquals(ManoeuvrePhase.RESPONSE, session.getPhase());
        assertEquals(ManoeuvreConcept.AGREEMENT_SEEKING, session.getConcept());
        assertNotNull(session.getPosition());
        assertNotNull(session.getPlannedTrajectory());
        assertNotNull(session.getExecutantIds());
        assertNotNull(session.getAdvisedChanges());
        assertNotNull(session.getMcmFrame());
    }

    @Test
    void testGetPlannedTrajectoryReturnsUnmodifiableList() {
        ManoeuvreSession session = createSession(ManoeuvreSourceType.VEHICLE,
                ManoeuvrePhase.INTENT,
                ManoeuvreConcept.AGREEMENT_SEEKING);

        List<LatLng> trajectory = session.getPlannedTrajectory();
        assertThrows(UnsupportedOperationException.class, () -> trajectory.add(new LatLng(0, 0)));
    }

    @Test
    void testGetExecutantIdsReturnsUnmodifiableList() {
        ManoeuvreSession session = createSession(ManoeuvreSourceType.VEHICLE,
                ManoeuvrePhase.INTENT,
                ManoeuvreConcept.AGREEMENT_SEEKING);

        List<Long> ids = session.getExecutantIds();
        assertThrows(UnsupportedOperationException.class, () -> ids.add(999L));
    }

    @Test
    void testGetAdvisedChangesReturnsUnmodifiableMap() {
        ManoeuvreSession session = createSession(ManoeuvreSourceType.VEHICLE,
                ManoeuvrePhase.INTENT,
                ManoeuvreConcept.AGREEMENT_SEEKING);

        Map<Long, String> changes = session.getAdvisedChanges();
        assertThrows(UnsupportedOperationException.class, () -> changes.put(999L, "test"));
    }

    @Test
    void testGettersReturnNullWhenFieldsNull() {
        ManoeuvreSession session = new ManoeuvreSession(
                TEST_UUID,
                ManoeuvreSourceType.CENTRAL_STATION,
                ManoeuvrePhase.OFFER,
                null,
                TEST_POSITION,
                null,
                null,
                null,
                createMockMcmFrame());

        assertNull(session.getConcept());
        assertNull(session.getPlannedTrajectory());
        assertNull(session.getExecutantIds());
        assertNull(session.getAdvisedChanges());
    }

    // -------------------------------------------------------------------------
    // Update Method
    // -------------------------------------------------------------------------

    @Test
    void testUpdateModifiesAllFields() {
        ManoeuvreSession session = createSession(ManoeuvreSourceType.VEHICLE,
                ManoeuvrePhase.INTENT,
                ManoeuvreConcept.AGREEMENT_SEEKING);

        LatLng newPosition = new LatLng(49.0, 3.0);
        List<LatLng> newTrajectory = List.of(new LatLng(49.0, 3.0));
        List<Long> newExecutantIds = List.of(333L);
        Map<Long, String> newAdvisedChanges = Map.of(333L, "accelerate");

        session.update(
                ManoeuvrePhase.RESPONSE,
                ManoeuvreConcept.PRESCRIPTIVE,
                newPosition,
                newTrajectory,
                newExecutantIds,
                newAdvisedChanges,
                createMockMcmFrame());

        assertEquals(ManoeuvrePhase.RESPONSE, session.getPhase());
        assertEquals(ManoeuvreConcept.PRESCRIPTIVE, session.getConcept());
        assertEquals(49.0, session.getPosition().getLatitude(), 1e-9);
        assertEquals(3.0, session.getPosition().getLongitude(), 1e-9);
        assertEquals(1, session.getPlannedTrajectory().size());
        assertEquals(1, session.getExecutantIds().size());
    }

    @Test
    void testUpdateRefreshesTimestamp() {
        ManoeuvreSession session = createSession(ManoeuvreSourceType.VEHICLE,
                ManoeuvrePhase.INTENT,
                ManoeuvreConcept.AGREEMENT_SEEKING);

        long initialTimestamp = session.getTimestamp();
        session.backdateTimestampForTesting(100);
        long backDatedTimestamp = session.getTimestamp();

        assertTrue(backDatedTimestamp < initialTimestamp);

        session.update(ManoeuvrePhase.REQUEST, ManoeuvreConcept.AGREEMENT_SEEKING,
                TEST_POSITION, TEST_TRAJECTORY, TEST_EXECUTANT_IDS,
                TEST_ADVISED_CHANGES, createMockMcmFrame());

        long updatedTimestamp = session.getTimestamp();
        assertTrue(updatedTimestamp > backDatedTimestamp);
    }

    @Test
    void testUpdateWithNullValues() {
        ManoeuvreSession session = createSession(ManoeuvreSourceType.VEHICLE,
                ManoeuvrePhase.INTENT,
                ManoeuvreConcept.AGREEMENT_SEEKING);

        session.update(ManoeuvrePhase.OFFER, null, TEST_POSITION, null, null, null, createMockMcmFrame());

        assertNull(session.getConcept());
        assertNull(session.getPlannedTrajectory());
        assertNull(session.getExecutantIds());
        assertNull(session.getAdvisedChanges());
    }

    // -------------------------------------------------------------------------
    // Lifetime & Expiry Logic (Mobile)
    // -------------------------------------------------------------------------

    @Test
    void testStillLivingReturnsTrueWithinMobileTimeout() {
        ManoeuvreSession session = createSession(ManoeuvreSourceType.VEHICLE,
                ManoeuvrePhase.INTENT,
                ManoeuvreConcept.AGREEMENT_SEEKING);

        assertTrue(session.stillLiving());
    }

    @Test
    void testStillLivingReturnsFalseAfterMobileTimeout() throws InterruptedException {
        ManoeuvreSession session = createSession(ManoeuvreSourceType.VEHICLE,
                ManoeuvrePhase.INTENT,
                ManoeuvreConcept.AGREEMENT_SEEKING);

        session.backdateTimestampForTesting(ManoeuvreSession.MOBILE_LIFETIME_MS + 100);
        assertFalse(session.stillLiving());
    }

    @Test
    void testStillLivingDetectsVRUAsMobile() {
        ManoeuvreSession session = createSession(ManoeuvreSourceType.VRU,
                ManoeuvrePhase.INTENT,
                ManoeuvreConcept.AGREEMENT_SEEKING);

        session.backdateTimestampForTesting(ManoeuvreSession.MOBILE_LIFETIME_MS + 100);
        assertFalse(session.stillLiving());
    }

    // -------------------------------------------------------------------------
    // Lifetime & Expiry Logic (Infrastructure)
    // -------------------------------------------------------------------------

    @Test
    void testStillLivingReturnsTrueWithinInfrastructureTimeout() {
        ManoeuvreSession session = createSession(ManoeuvreSourceType.ROADSIDE_UNIT,
                ManoeuvrePhase.INTENT,
                ManoeuvreConcept.AGREEMENT_SEEKING);

        assertTrue(session.stillLiving());
    }

    @Test
    void testStillLivingReturnsFalseAfterInfrastructureTimeout() {
        ManoeuvreSession session = createSession(ManoeuvreSourceType.ROADSIDE_UNIT,
                ManoeuvrePhase.INTENT,
                ManoeuvreConcept.AGREEMENT_SEEKING);

        session.backdateTimestampForTesting(ManoeuvreSession.INFRASTRUCTURE_LIFETIME_MS + 100);
        assertFalse(session.stillLiving());
    }

    @Test
    void testStillLivingDetectsRSUAsInfrastructure() {
        ManoeuvreSession session = createSession(ManoeuvreSourceType.ROADSIDE_UNIT,
                ManoeuvrePhase.INTENT,
                ManoeuvreConcept.AGREEMENT_SEEKING);

        // Just before infrastructure timeout (5000ms + 100ms)
        session.backdateTimestampForTesting(ManoeuvreSession.INFRASTRUCTURE_LIFETIME_MS - 100);
        assertTrue(session.stillLiving());

        // After infrastructure timeout
        session.backdateTimestampForTesting(ManoeuvreSession.INFRASTRUCTURE_LIFETIME_MS + 100);
        assertFalse(session.stillLiving());
    }

    @Test
    void testStillLivingDetectsCentralStationAsInfrastructure() {
        ManoeuvreSession session = createSession(ManoeuvreSourceType.CENTRAL_STATION,
                ManoeuvrePhase.OFFER,
                ManoeuvreConcept.PRESCRIPTIVE);

        session.backdateTimestampForTesting(ManoeuvreSession.INFRASTRUCTURE_LIFETIME_MS + 100);
        assertFalse(session.stillLiving());
    }

    // -------------------------------------------------------------------------
    // Timestamp Manipulation (Test Utilities)
    // -------------------------------------------------------------------------

    @Test
    void testBackdateTimestampForTesting() {
        ManoeuvreSession session = createSession(ManoeuvreSourceType.VEHICLE,
                ManoeuvrePhase.INTENT,
                ManoeuvreConcept.AGREEMENT_SEEKING);

        long originalTimestamp = session.getTimestamp();
        session.backdateTimestampForTesting(500);
        long backDatedTimestamp = session.getTimestamp();

        assertEquals(500, originalTimestamp - backDatedTimestamp);
    }

    @Test
    void testUpdateTimestampRefreshesToNow() {
        ManoeuvreSession session = createSession(ManoeuvreSourceType.VEHICLE,
                ManoeuvrePhase.INTENT,
                ManoeuvreConcept.AGREEMENT_SEEKING);

        long initialTimestamp = session.getTimestamp();
        session.backdateTimestampForTesting(1000);

        assertTrue(session.getTimestamp() < initialTimestamp);

        session.updateTimestamp();
        long refreshedTimestamp = session.getTimestamp();

        assertTrue(refreshedTimestamp >= initialTimestamp);
    }

    // -------------------------------------------------------------------------
    // Phase & Concept Transitions
    // -------------------------------------------------------------------------

    @Test
    void testUpdateCanChangePhaseAndConcept() {
        ManoeuvreSession session = createSession(ManoeuvreSourceType.VEHICLE,
                ManoeuvrePhase.INTENT,
                ManoeuvreConcept.AGREEMENT_SEEKING);

        assertEquals(ManoeuvrePhase.INTENT, session.getPhase());
        assertEquals(ManoeuvreConcept.AGREEMENT_SEEKING, session.getConcept());

        session.update(ManoeuvrePhase.RESPONSE, ManoeuvreConcept.PRESCRIPTIVE,
                TEST_POSITION, TEST_TRAJECTORY, TEST_EXECUTANT_IDS,
                TEST_ADVISED_CHANGES, createMockMcmFrame());

        assertEquals(ManoeuvrePhase.RESPONSE, session.getPhase());
        assertEquals(ManoeuvreConcept.PRESCRIPTIVE, session.getConcept());
    }

    @Test
    void testUpdateHandlesNullConcept() {
        ManoeuvreSession session = createSession(ManoeuvreSourceType.VEHICLE,
                ManoeuvrePhase.INTENT,
                ManoeuvreConcept.AGREEMENT_SEEKING);

        assertNotNull(session.getConcept());

        session.update(ManoeuvrePhase.OFFER, null, TEST_POSITION, null, null, null, createMockMcmFrame());

        assertNull(session.getConcept());
    }
}

