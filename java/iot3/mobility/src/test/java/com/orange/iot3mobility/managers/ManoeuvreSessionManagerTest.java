/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author GitHub Copilot <copilot@github.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.managers;

import com.orange.iot3mobility.messages.mcm.McmHelper;
import com.orange.iot3mobility.messages.mcm.core.McmCodec;
import com.orange.iot3mobility.messages.mcm.core.McmVersion;
import com.orange.iot3mobility.messages.mcm.v200.model.McmData;
import com.orange.iot3mobility.messages.mcm.v200.model.McmEnvelope200;
import com.orange.iot3mobility.messages.mcm.v200.model.McmMessage200;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.ReferencePosition;
import com.orange.iot3mobility.messages.mcm.v200.model.manoeuvreadvice.ManoeuvreAdvice;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.McmGenericCurrentStateContainer;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.VehicleManoeuvreContainer;
import com.orange.iot3mobility.roadobjects.ManoeuvrePhase;
import com.orange.iot3mobility.roadobjects.ManoeuvreSession;
import com.orange.iot3mobility.roadobjects.ManoeuvreSourceType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ManoeuvreSessionManagerTest {

    private static final String TEST_SOURCE_UUID = "test-source-uuid";
    private static final long TEST_STATION_ID = 9876543210L;
    private static final int TEST_MANOEUVRE_ID = 42;
    private static final int TEST_LAT_ETSI = 490000000;    // ~49°
    private static final int TEST_LON_ETSI = 30000000;     // ~3°

    private IoT3ManoeuvreCallback mockCallback;
    private McmHelper mockMcmHelper;

    @BeforeEach
    void setUp() {
        mockCallback = Mockito.mock(IoT3ManoeuvreCallback.class);
        mockMcmHelper = Mockito.mock(McmHelper.class);
        // resetForTesting() clears sessions, callback AND shuts down the scheduler,
        // preventing static-state contamination across tests.
        ManoeuvreSessionManager.resetForTesting();
    }

    @AfterEach
    void tearDown() {
        ManoeuvreSessionManager.resetForTesting();
    }

    @AfterAll
    static void tearDownAll() {
        // Final cleanup in case the last test left the scheduler running.
        ManoeuvreSessionManager.resetForTesting();
    }

    // -------------------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private McmCodec.McmFrame<McmEnvelope200> createMockMcmFrame() {
        McmCodec.McmFrame<McmEnvelope200> frame =
                (McmCodec.McmFrame<McmEnvelope200>) Mockito.mock(McmCodec.McmFrame.class);
        when(frame.version()).thenReturn(McmVersion.V2_0_0);
        return frame;
    }

    private ReferencePosition createMockReferencePosition(int latitude, int longitude) {
        ReferencePosition position = Mockito.mock(ReferencePosition.class);
        when(position.latitude()).thenReturn(latitude);
        when(position.longitude()).thenReturn(longitude);
        return position;
    }

    private McmMessage200 createMockMcmMessage200(int stationType, int itssRole) {
        ReferencePosition mockPosition = createMockReferencePosition(TEST_LAT_ETSI, TEST_LON_ETSI);
        McmMessage200 message = Mockito.mock(McmMessage200.class);
        when(message.stationId()).thenReturn(TEST_STATION_ID);
        when(message.stationType()).thenReturn(stationType);
        when(message.itssRole()).thenReturn(itssRole);
        when(message.position()).thenReturn(mockPosition);
        return message;
    }

    private McmEnvelope200 createMockMcmEnvelope200WithVehicleContainer(
            int mcmType, int concept) {
        McmEnvelope200 envelope = Mockito.mock(McmEnvelope200.class);
        when(envelope.sourceUuid()).thenReturn(TEST_SOURCE_UUID);

        McmGenericCurrentStateContainer genericContainer = Mockito.mock(McmGenericCurrentStateContainer.class);
        when(genericContainer.mcmType()).thenReturn(mcmType);
        when(genericContainer.manoeuvreId()).thenReturn(TEST_MANOEUVRE_ID);
        when(genericContainer.concept()).thenReturn(concept);

        VehicleManoeuvreContainer vehicleContainer = Mockito.mock(VehicleManoeuvreContainer.class);
        when(vehicleContainer.mcmGenericCurrentStateContainer()).thenReturn(genericContainer);
        when(vehicleContainer.submaneuvres()).thenReturn(new ArrayList<>());
        when(vehicleContainer.manoeuvreAdvice()).thenReturn(new ArrayList<>());

        McmData mcmData = Mockito.mock(McmData.class);
        when(mcmData.vehicleManoeuvreContainer()).thenReturn(vehicleContainer);
        when(mcmData.advisedManoeuvreContainer()).thenReturn(null);

        McmMessage200 message = createMockMcmMessage200(1, 0);
        when(message.mcmData()).thenReturn(mcmData);
        when(envelope.message()).thenReturn(message);

        return envelope;
    }

    private McmEnvelope200 createMockMcmEnvelopeWithAdvisedContainer() {
        McmEnvelope200 envelope = Mockito.mock(McmEnvelope200.class);
        when(envelope.sourceUuid()).thenReturn(TEST_SOURCE_UUID);

        ManoeuvreAdvice advice = Mockito.mock(ManoeuvreAdvice.class);
        when(advice.executantId()).thenReturn(111L);
        when(advice.currentStateAdvisedChange()).thenReturn("stay_in_lane");

        McmData mcmData = Mockito.mock(McmData.class);
        when(mcmData.vehicleManoeuvreContainer()).thenReturn(null);
        when(mcmData.advisedManoeuvreContainer()).thenReturn(List.of(advice));

        McmMessage200 message = createMockMcmMessage200(2, 1);
        when(message.mcmData()).thenReturn(mcmData);
        when(envelope.message()).thenReturn(message);

        return envelope;
    }

    // -------------------------------------------------------------------------
    // Initialization & Lifecycle
    // -------------------------------------------------------------------------

    @Test
    void testInitRegistersCallback() {
        ManoeuvreSessionManager.init(mockCallback);
        assertEquals(mockCallback, ManoeuvreSessionManager.getCallbackForTesting());
    }

    @Test
    void testClearRemovesAllSessions() throws IOException {
        ManoeuvreSessionManager.init(mockCallback);

        McmEnvelope200 envelope = createMockMcmEnvelope200WithVehicleContainer(0, 0);
        McmCodec.McmFrame<McmEnvelope200> frame = createMockMcmFrame();
        when(frame.envelope()).thenReturn(envelope);
        doReturn(frame).when(mockMcmHelper).parse(anyString());

        ManoeuvreSessionManager.processMcm("{}", mockMcmHelper);
        assertFalse(ManoeuvreSessionManager.getManoeuvreSessions().isEmpty());

        ManoeuvreSessionManager.clear();
        assertTrue(ManoeuvreSessionManager.getManoeuvreSessions().isEmpty());
    }

    // -------------------------------------------------------------------------
    // Key Builders
    // -------------------------------------------------------------------------

    @Test
    void testBuildVehicleKeyFormat() {
        String key = ManoeuvreSessionManager.buildVehicleKeyForTesting(
                "uuid123", 999L, 42);
        assertEquals("uuid123_999_42", key);
    }

    @Test
    void testBuildAdvisedKeyFormat() {
        String key = ManoeuvreSessionManager.buildAdvisedKeyForTesting(
                "uuid456", 888L);
        assertEquals("uuid456_888_advised", key);
    }

    @Test
    void testBuildVehicleKeyWithDifferentIds() {
        String key1 = ManoeuvreSessionManager.buildVehicleKeyForTesting(
                "uuid", 111L, 1);
        String key2 = ManoeuvreSessionManager.buildVehicleKeyForTesting(
                "uuid", 111L, 2);
        String key3 = ManoeuvreSessionManager.buildVehicleKeyForTesting(
                "uuid", 222L, 1);

        assertNotEquals(key1, key2);
        assertNotEquals(key1, key3);
    }

    // -------------------------------------------------------------------------
    // Vehicle Container Processing
    // -------------------------------------------------------------------------

    @Test
    void testProcessMcmCreatesNewVehicleSession() throws IOException {
        ManoeuvreSessionManager.init(mockCallback);

        McmEnvelope200 envelope = createMockMcmEnvelope200WithVehicleContainer(0, 0);
        McmCodec.McmFrame<McmEnvelope200> frame = createMockMcmFrame();
        when(frame.envelope()).thenReturn(envelope);
        doReturn(frame).when(mockMcmHelper).parse(anyString());

        ManoeuvreSessionManager.processMcm("{}", mockMcmHelper);

        assertEquals(1, ManoeuvreSessionManager.getManoeuvreSessions().size());
        verify(mockCallback, times(1)).newManoeuvreSession(any(ManoeuvreSession.class));
    }

    @Test
    void testProcessMcmUpdatesExistingVehicleSession() throws IOException {
        ManoeuvreSessionManager.init(mockCallback);

        McmEnvelope200 envelope1 = createMockMcmEnvelope200WithVehicleContainer(0, 0);
        McmCodec.McmFrame<McmEnvelope200> frame1 = createMockMcmFrame();
        when(frame1.envelope()).thenReturn(envelope1);

        McmEnvelope200 envelope2 = createMockMcmEnvelope200WithVehicleContainer(1, 0);
        McmCodec.McmFrame<McmEnvelope200> frame2 = createMockMcmFrame();
        when(frame2.envelope()).thenReturn(envelope2);

        doReturn(frame1).doReturn(frame2).when(mockMcmHelper).parse(anyString());

        ManoeuvreSessionManager.processMcm("{}", mockMcmHelper);

        verify(mockCallback, times(1)).newManoeuvreSession(any(ManoeuvreSession.class));

        // Send second MCM for same session
        ManoeuvreSessionManager.processMcm("{}", mockMcmHelper);

        assertEquals(1, ManoeuvreSessionManager.getManoeuvreSessions().size());
        verify(mockCallback, times(1)).manoeuvreSessionUpdated(any(ManoeuvreSession.class));
    }

    @Test
    void testProcessVehicleContainerCallsNewSessionCallback() throws IOException {
        ManoeuvreSessionManager.init(mockCallback);

        McmEnvelope200 envelope = createMockMcmEnvelope200WithVehicleContainer(0, 0);
        McmCodec.McmFrame<McmEnvelope200> frame = createMockMcmFrame();
        when(frame.envelope()).thenReturn(envelope);
        doReturn(frame).when(mockMcmHelper).parse(anyString());

        ManoeuvreSessionManager.processMcm("{}", mockMcmHelper);

        ArgumentCaptor<ManoeuvreSession> captor = ArgumentCaptor.forClass(ManoeuvreSession.class);
        verify(mockCallback).newManoeuvreSession(captor.capture());

        ManoeuvreSession session = captor.getValue();
        assertEquals(ManoeuvreSourceType.VEHICLE, session.getSourceType());
        assertEquals(ManoeuvrePhase.INTENT, session.getPhase());
    }

    // -------------------------------------------------------------------------
    // Advised Container Processing
    // -------------------------------------------------------------------------

    @Test
    void testProcessMcmCreatesNewAdvisedSession() throws IOException {
        ManoeuvreSessionManager.init(mockCallback);

        McmEnvelope200 envelope = createMockMcmEnvelopeWithAdvisedContainer();
        McmCodec.McmFrame<McmEnvelope200> frame = createMockMcmFrame();
        when(frame.envelope()).thenReturn(envelope);
        doReturn(frame).when(mockMcmHelper).parse(anyString());

        ManoeuvreSessionManager.processMcm("{}", mockMcmHelper);

        assertEquals(1, ManoeuvreSessionManager.getManoeuvreSessions().size());

        ArgumentCaptor<ManoeuvreSession> captor = ArgumentCaptor.forClass(ManoeuvreSession.class);
        verify(mockCallback).newManoeuvreSession(captor.capture());

        ManoeuvreSession session = captor.getValue();
        assertEquals(ManoeuvrePhase.OFFER, session.getPhase());
        assertNull(session.getConcept());
    }

    @Test
    void testProcessAdvisedContainerUsesCorrectKey() throws IOException {
        ManoeuvreSessionManager.init(mockCallback);

        McmEnvelope200 envelope = createMockMcmEnvelopeWithAdvisedContainer();
        McmCodec.McmFrame<McmEnvelope200> frame = createMockMcmFrame();
        when(frame.envelope()).thenReturn(envelope);
        doReturn(frame).when(mockMcmHelper).parse(anyString());

        ManoeuvreSessionManager.processMcm("{}", mockMcmHelper);

        ArgumentCaptor<ManoeuvreSession> captor = ArgumentCaptor.forClass(ManoeuvreSession.class);
        verify(mockCallback).newManoeuvreSession(captor.capture());

        String expectedKey = TEST_SOURCE_UUID + "_" + TEST_STATION_ID + "_advised";
        assertEquals(expectedKey, captor.getValue().getUuid());
    }

    // -------------------------------------------------------------------------
    // Immediate Expiry (TERMINATION/CANCELLATION)
    // -------------------------------------------------------------------------

    @Test
    void testTerminationPhaseExpiresSessionImmediately() throws IOException {
        ManoeuvreSessionManager.init(mockCallback);

        // Create initial session
        McmEnvelope200 envelope1 = createMockMcmEnvelope200WithVehicleContainer(0, 0);
        McmCodec.McmFrame<McmEnvelope200> frame1 = createMockMcmFrame();
        when(frame1.envelope()).thenReturn(envelope1);

        // Send TERMINATION (mcm_type = 4)
        McmEnvelope200 envelope2 = createMockMcmEnvelope200WithVehicleContainer(4, 0);
        McmCodec.McmFrame<McmEnvelope200> frame2 = createMockMcmFrame();
        when(frame2.envelope()).thenReturn(envelope2);

        doReturn(frame1).doReturn(frame2).when(mockMcmHelper).parse(anyString());

        ManoeuvreSessionManager.processMcm("{}", mockMcmHelper);
        assertEquals(1, ManoeuvreSessionManager.getManoeuvreSessions().size());

        ManoeuvreSessionManager.processMcm("{}", mockMcmHelper);

        assertEquals(0, ManoeuvreSessionManager.getManoeuvreSessions().size());
        verify(mockCallback, times(1)).manoeuvreSessionExpired(any(ManoeuvreSession.class));
    }

    @Test
    void testCancellationPhaseExpiresSessionImmediately() throws IOException {
        ManoeuvreSessionManager.init(mockCallback);

        McmEnvelope200 envelope1 = createMockMcmEnvelope200WithVehicleContainer(0, 0);
        McmCodec.McmFrame<McmEnvelope200> frame1 = createMockMcmFrame();
        when(frame1.envelope()).thenReturn(envelope1);

        // Send CANCELLATION (mcm_type = 5)
        McmEnvelope200 envelope2 = createMockMcmEnvelope200WithVehicleContainer(5, 0);
        McmCodec.McmFrame<McmEnvelope200> frame2 = createMockMcmFrame();
        when(frame2.envelope()).thenReturn(envelope2);

        doReturn(frame1).doReturn(frame2).when(mockMcmHelper).parse(anyString());

        ManoeuvreSessionManager.processMcm("{}", mockMcmHelper);

        ManoeuvreSessionManager.processMcm("{}", mockMcmHelper);

        assertEquals(0, ManoeuvreSessionManager.getManoeuvreSessions().size());
        verify(mockCallback, times(1)).manoeuvreSessionExpired(any(ManoeuvreSession.class));
    }

    // -------------------------------------------------------------------------
    // Rolling Timeout Expiry
    // -------------------------------------------------------------------------

    @Test
    void testSessionExpiresAfterRollingTimeout() throws IOException {
        ManoeuvreSessionManager.init(mockCallback);

        McmEnvelope200 envelope = createMockMcmEnvelope200WithVehicleContainer(0, 0);
        McmCodec.McmFrame<McmEnvelope200> frame = createMockMcmFrame();
        when(frame.envelope()).thenReturn(envelope);
        doReturn(frame).when(mockMcmHelper).parse(anyString());

        ManoeuvreSessionManager.processMcm("{}", mockMcmHelper);
        assertEquals(1, ManoeuvreSessionManager.getManoeuvreSessions().size());

        // Simulate silence beyond MOBILE_LIFETIME_MS without Thread.sleep()
        ManoeuvreSession session = ManoeuvreSessionManager.getManoeuvreSessions().get(0);
        session.backdateTimestampForTesting(ManoeuvreSession.MOBILE_LIFETIME_MS + 100L);

        ManoeuvreSessionManager.checkAndRemoveExpiredSessionsForTesting();

        assertEquals(0, ManoeuvreSessionManager.getManoeuvreSessions().size());
        verify(mockCallback, times(1)).manoeuvreSessionExpired(any(ManoeuvreSession.class));
    }

    @Test
    void testSessionStillAliveBeforeTimeoutElapses() throws IOException {
        ManoeuvreSessionManager.init(mockCallback);

        McmEnvelope200 envelope = createMockMcmEnvelope200WithVehicleContainer(0, 0);
        McmCodec.McmFrame<McmEnvelope200> frame = createMockMcmFrame();
        when(frame.envelope()).thenReturn(envelope);
        doReturn(frame).when(mockMcmHelper).parse(anyString());

        ManoeuvreSessionManager.processMcm("{}", mockMcmHelper);

        // Backdate by less than the lifetime: session must survive the scan
        ManoeuvreSession session = ManoeuvreSessionManager.getManoeuvreSessions().get(0);
        session.backdateTimestampForTesting(ManoeuvreSession.MOBILE_LIFETIME_MS - 100L);

        ManoeuvreSessionManager.checkAndRemoveExpiredSessionsForTesting();

        assertEquals(1, ManoeuvreSessionManager.getManoeuvreSessions().size());
        verify(mockCallback, never()).manoeuvreSessionExpired(any(ManoeuvreSession.class));
    }

    // -------------------------------------------------------------------------
    // Collection Management
    // -------------------------------------------------------------------------

    @Test
    void testGetManoeuvrSessionsReturnsSnapshot() throws IOException {
        ManoeuvreSessionManager.init(mockCallback);

        McmEnvelope200 envelope = createMockMcmEnvelope200WithVehicleContainer(0, 0);
        McmCodec.McmFrame<McmEnvelope200> frame = createMockMcmFrame();
        when(frame.envelope()).thenReturn(envelope);
        doReturn(frame).when(mockMcmHelper).parse(anyString());

        ManoeuvreSessionManager.processMcm("{}", mockMcmHelper);

        List<ManoeuvreSession> sessions = ManoeuvreSessionManager.getManoeuvreSessions();
        assertEquals(1, sessions.size());
    }

    @Test
    void testGetManoeuvrSessionsIsEmpty() {
        ManoeuvreSessionManager.init(mockCallback);
        assertTrue(ManoeuvreSessionManager.getManoeuvreSessions().isEmpty());
    }

    @Test
    void testGetManoeuvrSessionsReturnsUnmodifiableList() throws IOException {
        ManoeuvreSessionManager.init(mockCallback);

        McmEnvelope200 envelope = createMockMcmEnvelope200WithVehicleContainer(0, 0);
        McmCodec.McmFrame<McmEnvelope200> frame = createMockMcmFrame();
        when(frame.envelope()).thenReturn(envelope);
        doReturn(frame).when(mockMcmHelper).parse(anyString());

        ManoeuvreSessionManager.processMcm("{}", mockMcmHelper);

        List<ManoeuvreSession> sessions = ManoeuvreSessionManager.getManoeuvreSessions();
        assertThrows(UnsupportedOperationException.class,
                () -> sessions.add(Mockito.mock(ManoeuvreSession.class)));
    }

    // -------------------------------------------------------------------------
    // Callback Invocation
    // -------------------------------------------------------------------------

    @Test
    void testMcmArrivedCallbackIsFired() throws IOException {
        ManoeuvreSessionManager.init(mockCallback);

        McmEnvelope200 envelope = createMockMcmEnvelope200WithVehicleContainer(0, 0);
        McmCodec.McmFrame<McmEnvelope200> frame = createMockMcmFrame();
        when(frame.envelope()).thenReturn(envelope);
        doReturn(frame).when(mockMcmHelper).parse(anyString());

        ManoeuvreSessionManager.processMcm("{}", mockMcmHelper);

        verify(mockCallback, times(1)).mcmArrived(any(McmCodec.McmFrame.class));
    }

    @Test
    void testNewManoeuvreSessionCallbackOnFirstMcm() throws IOException {
        ManoeuvreSessionManager.init(mockCallback);

        McmEnvelope200 envelope = createMockMcmEnvelope200WithVehicleContainer(0, 0);
        McmCodec.McmFrame<McmEnvelope200> frame = createMockMcmFrame();
        when(frame.envelope()).thenReturn(envelope);
        doReturn(frame).when(mockMcmHelper).parse(anyString());

        ManoeuvreSessionManager.processMcm("{}", mockMcmHelper);

        verify(mockCallback, times(1)).newManoeuvreSession(any(ManoeuvreSession.class));
        verify(mockCallback, never()).manoeuvreSessionUpdated(any(ManoeuvreSession.class));
    }

    @Test
    void testManoeuvreSessionUpdatedCallbackOnSubsequentMcm() throws IOException {
        ManoeuvreSessionManager.init(mockCallback);

        McmEnvelope200 envelope1 = createMockMcmEnvelope200WithVehicleContainer(0, 0);
        McmCodec.McmFrame<McmEnvelope200> frame1 = createMockMcmFrame();
        when(frame1.envelope()).thenReturn(envelope1);

        McmEnvelope200 envelope2 = createMockMcmEnvelope200WithVehicleContainer(1, 0);
        McmCodec.McmFrame<McmEnvelope200> frame2 = createMockMcmFrame();
        when(frame2.envelope()).thenReturn(envelope2);

        doReturn(frame1).doReturn(frame2).when(mockMcmHelper).parse(anyString());

        ManoeuvreSessionManager.processMcm("{}", mockMcmHelper);

        ManoeuvreSessionManager.processMcm("{}", mockMcmHelper);

        verify(mockCallback, times(1)).manoeuvreSessionUpdated(any(ManoeuvreSession.class));
    }

    @Test
    void testCallbackNotInvokedIfCallbackIsNull() throws IOException {
        // Deliberately do NOT call init() — callback must be null after resetForTesting()
        doReturn(Mockito.mock(McmCodec.McmFrame.class)).when(mockMcmHelper).parse(anyString());

        assertDoesNotThrow(() -> ManoeuvreSessionManager.processMcm("{}", mockMcmHelper));

        // processMcm() returns early when callback is null: parse() must never be called
        verify(mockMcmHelper, never()).parse(anyString());
    }

    // -------------------------------------------------------------------------
    // Error Handling
    // -------------------------------------------------------------------------

    @Test
    void testProcessMcmHandlesInvalidJsonGracefully() throws IOException {
        ManoeuvreSessionManager.init(mockCallback);

        doThrow(new IOException("Parse error")).when(mockMcmHelper).parse(anyString());

        assertDoesNotThrow(() -> ManoeuvreSessionManager.processMcm("{invalid}", mockMcmHelper));
    }

    @Test
    void testProcessMcmHandlesRuntimeExceptionGracefully() throws IOException {
        ManoeuvreSessionManager.init(mockCallback);

        doThrow(new RuntimeException("Processing error")).when(mockMcmHelper).parse(anyString());

        assertDoesNotThrow(() -> ManoeuvreSessionManager.processMcm("{}", mockMcmHelper));
    }

    @Test
    void testSourceTypeResolution() throws IOException {
        ManoeuvreSessionManager.init(mockCallback);

        McmEnvelope200 envelope = createMockMcmEnvelope200WithVehicleContainer(0, 0);
        McmCodec.McmFrame<McmEnvelope200> frame = createMockMcmFrame();
        when(frame.envelope()).thenReturn(envelope);
        doReturn(frame).when(mockMcmHelper).parse(anyString());

        ManoeuvreSessionManager.processMcm("{}", mockMcmHelper);

        ManoeuvreSession session = ManoeuvreSessionManager.getManoeuvreSessions().get(0);
        assertEquals(ManoeuvreSourceType.VEHICLE, session.getSourceType());
    }
}

