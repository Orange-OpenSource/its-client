/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3mobility.managers;

import com.orange.iot3mobility.messages.EtsiConverter;
import com.orange.iot3mobility.messages.mcm.McmHelper;
import com.orange.iot3mobility.messages.mcm.core.McmCodec;
import com.orange.iot3mobility.messages.mcm.core.McmVersion;
import com.orange.iot3mobility.messages.mcm.v200.model.McmData;
import com.orange.iot3mobility.messages.mcm.v200.model.McmEnvelope200;
import com.orange.iot3mobility.messages.mcm.v200.model.McmMessage200;
import com.orange.iot3mobility.messages.mcm.v200.model.defs.WayPoint;
import com.orange.iot3mobility.messages.mcm.v200.model.manoeuvreadvice.ManoeuvreAdvice;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.Submanoeuvre;
import com.orange.iot3mobility.messages.mcm.v200.model.vehiclemanoeuvrecontainer.VehicleManoeuvreContainer;
import com.orange.iot3mobility.quadkey.LatLng;
import com.orange.iot3mobility.roadobjects.ManoeuvreConcept;
import com.orange.iot3mobility.roadobjects.ManoeuvrePhase;
import com.orange.iot3mobility.roadobjects.ManoeuvreSession;
import com.orange.iot3mobility.roadobjects.ManoeuvreSourceType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages {@link ManoeuvreSession} objects derived from received MCM messages.
 * <p>
 * One {@link ManoeuvreSession} is maintained per active MCS session, identified by:
 * <ul>
 *   <li>{@code {sourceUuid}_{stationId}_{manoeuvreId}} — for vehicle / VRU originators</li>
 *   <li>{@code {sourceUuid}_{stationId}_advised} — for orchestrator-only sessions</li>
 * </ul>
 * Objects expire on a rolling timeout (1 500 ms for mobile sources, 5 000 ms for infrastructure)
 * or immediately when a TERMINATION or CANCELLATION phase is received.
 * <p>
 * State is static — there is one shared store per JVM process.
 */
public class ManoeuvreSessionManager {

    private static final Logger LOGGER = Logger.getLogger(ManoeuvreSessionManager.class.getName());
    private static final String TAG = "IoT3Mobility.ManoeuvreSessionManager";

    private static final ArrayList<ManoeuvreSession> MANOEUVRE_SESSIONS = new ArrayList<>();
    private static final HashMap<String, ManoeuvreSession> MANOEUVRE_SESSION_MAP = new HashMap<>();

    private static IoT3ManoeuvreCallback ioT3ManoeuvreCallback;
    private static ScheduledExecutorService scheduler;

    // -------------------------------------------------------------------------
    // Init
    // -------------------------------------------------------------------------

    /**
     * Registers the callback and starts the expiry scheduler (idempotent).
     *
     * @param callback the callback to receive lifecycle events
     */
    public static void init(IoT3ManoeuvreCallback callback) {
        ManoeuvreSessionManager.ioT3ManoeuvreCallback = callback;
        startExpirationCheck();
    }

    // -------------------------------------------------------------------------
    // MCM processing
    // -------------------------------------------------------------------------

    /**
     * Decodes a raw MCM JSON payload and updates the manoeuvre session store.
     * Fires the appropriate callback ({@code newManoeuvreSession},
     * {@code manoeuvreSessionUpdated}, or {@code manoeuvreSessionExpired}).
     *
     * @param message   raw JSON string received from the broker
     * @param mcmHelper helper used to decode the payload
     */
    public static void processMcm(String message, McmHelper mcmHelper) {
        if (ioT3ManoeuvreCallback == null) return;
        try {
            McmCodec.McmFrame<?> mcmFrame = mcmHelper.parse(message);
            ioT3ManoeuvreCallback.mcmArrived(mcmFrame);

            if (mcmFrame.version() == McmVersion.V2_0_0) {
                McmEnvelope200 envelope = (McmEnvelope200) mcmFrame.envelope();
                processMcmEnvelope200(envelope, mcmFrame);
            }
        } catch (IOException ioException) {
            LOGGER.log(Level.WARNING, TAG + " MCM parsing error: " + ioException);
        } catch (RuntimeException runtimeException) {
            LOGGER.log(Level.WARNING, TAG + " MCM processing error: " + runtimeException);
        }
    }

    private static void processMcmEnvelope200(McmEnvelope200 envelope, McmCodec.McmFrame<?> mcmFrame) {
        McmMessage200 mcmMessage = envelope.message();
        String sourceUuid = envelope.sourceUuid();
        long stationId = mcmMessage.stationId();
        ManoeuvreSourceType sourceType = ManoeuvreSourceType.fromStationType(mcmMessage.stationType());
        McmData mcmData = mcmMessage.mcmData();

        // Decode position (always present)
        LatLng position = new LatLng(
                EtsiConverter.latitudeDegrees(mcmMessage.position().latitude()),
                EtsiConverter.longitudeDegrees(mcmMessage.position().longitude()));

        if (mcmData.vehicleManoeuvreContainer() != null) {
            processVehicleContainer(sourceUuid, stationId, sourceType, position,
                    mcmData.vehicleManoeuvreContainer(), mcmFrame);
        } else if (mcmData.advisedManoeuvreContainer() != null) {
            processAdvisedContainer(sourceUuid, stationId, sourceType, position,
                    mcmData.advisedManoeuvreContainer(), mcmFrame);
        }
    }

    // ---- vehicle_manoeuvre_container ----

    private static void processVehicleContainer(String sourceUuid,
                                                long stationId,
                                                ManoeuvreSourceType sourceType,
                                                LatLng position,
                                                VehicleManoeuvreContainer vmc,
                                                McmCodec.McmFrame<?> mcmFrame) {
        int manoeuvreId = vmc.mcmGenericCurrentStateContainer().manoeuvreId();
        ManoeuvrePhase phase = ManoeuvrePhase.fromMcmType(
                vmc.mcmGenericCurrentStateContainer().mcmType());
        ManoeuvreConcept concept = ManoeuvreConcept.fromValue(
                vmc.mcmGenericCurrentStateContainer().concept());

        // Planned trajectory from first submanoeuvre
        List<LatLng> plannedTrajectory = null;
        if (vmc.submaneuvres() != null && !vmc.submaneuvres().isEmpty()) {
            Submanoeuvre firstSubmanoeuvre = vmc.submaneuvres().get(0);
            if (firstSubmanoeuvre.referenceTrajectory() != null
                    && !firstSubmanoeuvre.referenceTrajectory().isEmpty()) {
                plannedTrajectory = decodeTrajectory(firstSubmanoeuvre.referenceTrajectory());
            }
        }

        // Optional advice within vehicle container
        List<Long> executantIds = null;
        Map<Long, String> advisedChanges = null;
        if (vmc.manoeuvreAdvice() != null && !vmc.manoeuvreAdvice().isEmpty()) {
            executantIds = new ArrayList<>();
            advisedChanges = new LinkedHashMap<>();
            for (ManoeuvreAdvice advice : vmc.manoeuvreAdvice()) {
                executantIds.add(advice.executantId());
                advisedChanges.put(advice.executantId(), advice.currentStateAdvisedChange());
            }
        }

        String sessionKey = buildVehicleKey(sourceUuid, stationId, manoeuvreId);
        createOrUpdate(sessionKey, sourceType, phase, concept, position, plannedTrajectory, executantIds,
                advisedChanges, mcmFrame);
    }

    // ---- advised_manoeuvre_container ----

    private static void processAdvisedContainer(String sourceUuid,
                                                long stationId,
                                                ManoeuvreSourceType sourceType,
                                                LatLng position,
                                                List<ManoeuvreAdvice> adviceList,
                                                McmCodec.McmFrame<?> mcmFrame) {
        List<Long> executantIds = new ArrayList<>();
        Map<Long, String> advisedChanges = new LinkedHashMap<>();
        for (ManoeuvreAdvice advice : adviceList) {
            executantIds.add(advice.executantId());
            advisedChanges.put(advice.executantId(), advice.currentStateAdvisedChange());
        }

        // Phase = OFFER for orchestrator-only MCMs (no vehicle container)
        String sessionKey = buildAdvisedKey(sourceUuid, stationId);
        createOrUpdate(sessionKey, sourceType, ManoeuvrePhase.OFFER, null, position, null,
                executantIds, advisedChanges, mcmFrame);
    }

    // ---- shared create / update logic ----

    private static void createOrUpdate(String sessionKey,
                                       ManoeuvreSourceType sourceType,
                                       ManoeuvrePhase phase,
                                       ManoeuvreConcept concept,
                                       LatLng position,
                                       List<LatLng> plannedTrajectory,
                                       List<Long> executantIds,
                                       Map<Long, String> advisedChanges,
                                       McmCodec.McmFrame<?> mcmFrame) {

        // TERMINATION / CANCELLATION: expire any existing session immediately
        if (phase.causesImmediateExpiry()) {
            synchronized (MANOEUVRE_SESSION_MAP) {
                ManoeuvreSession existing = MANOEUVRE_SESSION_MAP.get(sessionKey);
                if (existing != null) {
                    existing.update(phase, concept, position, plannedTrajectory,
                            executantIds, advisedChanges, mcmFrame);
                    removeSession(existing);
                    ioT3ManoeuvreCallback.manoeuvreSessionExpired(existing);
                }
            }
            return;
        }

        synchronized (MANOEUVRE_SESSION_MAP) {
            if (MANOEUVRE_SESSION_MAP.containsKey(sessionKey)) {
                ManoeuvreSession existing = MANOEUVRE_SESSION_MAP.get(sessionKey);
                if (existing != null) {
                    existing.update(phase, concept, position, plannedTrajectory,
                            executantIds, advisedChanges, mcmFrame);
                    ioT3ManoeuvreCallback.manoeuvreSessionUpdated(existing);
                }
            } else {
                ManoeuvreSession newSession = new ManoeuvreSession(
                        sessionKey, sourceType, phase, concept, position, plannedTrajectory,
                        executantIds, advisedChanges, mcmFrame);
                addSession(sessionKey, newSession);
                ioT3ManoeuvreCallback.newManoeuvreSession(newSession);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Collection management
    // -------------------------------------------------------------------------

    private static void addSession(String key, ManoeuvreSession session) {
        synchronized (MANOEUVRE_SESSIONS) {
            MANOEUVRE_SESSIONS.add(session);
        }
        MANOEUVRE_SESSION_MAP.put(key, session);
    }

    private static void removeSession(ManoeuvreSession session) {
        synchronized (MANOEUVRE_SESSIONS) {
            MANOEUVRE_SESSIONS.remove(session);
        }
        MANOEUVRE_SESSION_MAP.values().remove(session);
    }

    private static void checkAndRemoveExpiredSessions() {
        synchronized (MANOEUVRE_SESSIONS) {
            Iterator<ManoeuvreSession> iterator = MANOEUVRE_SESSIONS.iterator();
            while (iterator.hasNext()) {
                ManoeuvreSession session = iterator.next();
                if (!session.stillLiving()) {
                    iterator.remove();
                    synchronized (MANOEUVRE_SESSION_MAP) {
                        MANOEUVRE_SESSION_MAP.values().remove(session);
                    }
                    ioT3ManoeuvreCallback.manoeuvreSessionExpired(session);
                }
            }
        }
    }

    private static synchronized void startExpirationCheck() {
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleWithFixedDelay(
                    ManoeuvreSessionManager::checkAndRemoveExpiredSessions,
                    1, 1, TimeUnit.SECONDS);
        }
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Returns a read-only snapshot of all currently active manoeuvre sessions.
     */
    public static List<ManoeuvreSession> getManoeuvreSessions() {
        synchronized (MANOEUVRE_SESSIONS) {
            return Collections.unmodifiableList(new ArrayList<>(MANOEUVRE_SESSIONS));
        }
    }

    /**
     * Removes all stored manoeuvre sessions immediately without firing expiry callbacks.
     * Call this when leaving a geographic area to avoid unbounded memory growth.
     */
    public static void clear() {
        synchronized (MANOEUVRE_SESSIONS) {
            MANOEUVRE_SESSIONS.clear();
        }
        synchronized (MANOEUVRE_SESSION_MAP) {
            MANOEUVRE_SESSION_MAP.clear();
        }
    }

    // -------------------------------------------------------------------------
    // Key builders
    // -------------------------------------------------------------------------

    /** Key for vehicle/VRU originated sessions: {@code {sourceUuid}_{stationId}_{manoeuvreId}}. */
    static String buildVehicleKey(String sourceUuid, long stationId, int manoeuvreId) {
        return sourceUuid + "_" + stationId + "_" + manoeuvreId;
    }

    /** Key for orchestrator-only (advised) sessions: {@code {sourceUuid}_{stationId}_advised}. */
    static String buildAdvisedKey(String sourceUuid, long stationId) {
        return sourceUuid + "_" + stationId + "_advised";
    }

    // -------------------------------------------------------------------------
    // Testing Support (package-private)
    // -------------------------------------------------------------------------

    /**
     * Package-private: returns the registered callback for testing purposes.
     */
    static IoT3ManoeuvreCallback getCallbackForTesting() {
        return ioT3ManoeuvreCallback;
    }

    /**
     * Package-private: exposes buildVehicleKey for test access.
     */
    static String buildVehicleKeyForTesting(String sourceUuid, long stationId, int manoeuvreId) {
        return buildVehicleKey(sourceUuid, stationId, manoeuvreId);
    }

    /**
     * Package-private: exposes buildAdvisedKey for test access.
     */
    static String buildAdvisedKeyForTesting(String sourceUuid, long stationId) {
        return buildAdvisedKey(sourceUuid, stationId);
    }

    /**
     * Package-private: resets all static state (sessions, callback, scheduler).
     * Must be called in test {@code @BeforeEach} / {@code @AfterEach} to avoid
     * cross-test contamination of static fields.
     */
    static synchronized void resetForTesting() {
        synchronized (MANOEUVRE_SESSIONS) {
            MANOEUVRE_SESSIONS.clear();
        }
        synchronized (MANOEUVRE_SESSION_MAP) {
            MANOEUVRE_SESSION_MAP.clear();
        }
        ioT3ManoeuvreCallback = null;
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    /**
     * Package-private: triggers the expiry scan synchronously, without waiting
     * for the background scheduler. Use in tests together with
     * {@link com.orange.iot3mobility.roadobjects.ManoeuvreSession#backdateTimestampForTesting}
     * to simulate a timeout without {@code Thread.sleep()}.
     */
    static void checkAndRemoveExpiredSessionsForTesting() {
        checkAndRemoveExpiredSessions();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Converts a list of ETSI-encoded {@link WayPoint} to a list of degree-based {@link LatLng}.
     */
    private static List<LatLng> decodeTrajectory(List<WayPoint> wayPoints) {
        List<LatLng> latLngList = new ArrayList<>(wayPoints.size());
        for (WayPoint wayPoint : wayPoints) {
            latLngList.add(new LatLng(
                    EtsiConverter.latitudeDegrees(wayPoint.latitude()),
                    EtsiConverter.longitudeDegrees(wayPoint.longitude())));
        }
        return latLngList;
    }
}

