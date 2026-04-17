/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.managers;

import com.orange.iot3mobility.messages.spatem.SpatemHelper;
import com.orange.iot3mobility.messages.spatem.core.SpatemCodec;
import com.orange.iot3mobility.messages.spatem.core.SpatemVersion;
import com.orange.iot3mobility.messages.spatem.v200.model.SpatemEnvelope200;
import com.orange.iot3mobility.messages.spatem.v200.model.intersection.IntersectionState;
import com.orange.iot3mobility.roadobjects.RoadIntersection;
import com.orange.iot3mobility.roadobjects.SignalController;
import com.orange.iot3mobility.roadobjects.SignalGroup;
import com.orange.iot3mobility.roadobjects.SignalGroupUpdateType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages {@link SignalController} objects derived from received SPATEM messages.
 * <p>
 * One {@link SignalController} object is maintained per unique intersection, identified by the
 * composite key {@code {sourceUuid}_{regionId}_{intersectionId}}.
 * A {@link SignalController} is removed when all its {@link SignalGroup} entries have expired
 * (all phase deadlines passed) <em>and</em> no SPATEM has been received within
 * {@link SignalController#MAX_STALENESS_MS} ms. This dual-condition expiry supports both
 * periodic and event-based SPATEM publishing.
 * The {@link IoT3SignalControllerCallback#signalControllerExpired(SignalController)} callback
 * is fired on removal. Call {@link #clear()} to remove all entries immediately when leaving
 * a geographic area.
 * <p>
 * State is static — there is one shared store per JVM process.
 */
public class SignalControllerManager {

    private static final Logger LOGGER = Logger.getLogger(SignalControllerManager.class.getName());
    private static final String TAG = "IoT3Mobility.SignalControllerManager";

    private static final ArrayList<SignalController> SIGNAL_CONTROLLERS = new ArrayList<>();
    private static final HashMap<String, SignalController> SIGNAL_CONTROLLER_MAP = new HashMap<>();

    private static IoT3SignalControllerCallback ioT3SignalControllerCallback;
    private static ScheduledExecutorService scheduler;

    public static void init(IoT3SignalControllerCallback callback) {
        SignalControllerManager.ioT3SignalControllerCallback = callback;
        startExpirationCheck();
    }

    // -------------------------------------------------------------------------
    // SPATEM processing
    // -------------------------------------------------------------------------

    public static void processSpatem(String message, SpatemHelper spatemHelper) {
        if (ioT3SignalControllerCallback == null) return;
        try {
            SpatemCodec.SpatemFrame<?> frame = spatemHelper.parse(message);
            ioT3SignalControllerCallback.spatemArrived(frame);

            if (frame.version() == SpatemVersion.V2_0_0) {
                SpatemEnvelope200 envelope = (SpatemEnvelope200) frame.envelope();

                synchronized (SIGNAL_CONTROLLER_MAP) {
                    for (IntersectionState intersectionState : envelope.message().intersections()) {
                        int regionId = intersectionState.id().region() != null ? intersectionState.id().region() : 0;
                        int intersectionId = intersectionState.id().id();
                        String uuid = envelope.sourceUuid() + "_" + regionId + "_" + intersectionId;

                        SignalController signalController = SIGNAL_CONTROLLER_MAP.get(uuid);
                        boolean isNew = signalController == null;

                        if (isNew) {
                            signalController = new SignalController(uuid, regionId, intersectionId, frame);
                            SIGNAL_CONTROLLER_MAP.put(uuid, signalController);
                            synchronized (SIGNAL_CONTROLLERS) {
                                SIGNAL_CONTROLLERS.add(signalController);
                            }
                            // Try to resolve positions from already-received MAPEM data
                            tryResolveFromMapem(signalController, regionId, intersectionId);
                            ioT3SignalControllerCallback.newSignalController(signalController);
                            for (SignalGroup signalGroup : signalController.getSignalGroups()) {
                                ioT3SignalControllerCallback.newSignalGroup(signalController, signalGroup);
                            }
                        } else {
                            Set<Integer> knownGroupIds = new HashSet<>();
                            for (SignalGroup signalGroup : signalController.getSignalGroups()) {
                                knownGroupIds.add(signalGroup.getId());
                            }
                            Map<SignalGroup, SignalGroupUpdateType> signalGroupChanges =
                                    signalController.updateWithChanges(frame);
                            ioT3SignalControllerCallback.signalControllerUpdated(signalController);
                            // This second iteration goes through the updated state, so it can detect new groups that
                            // were not present before (edge case)
                            for (SignalGroup signalGroup : signalController.getSignalGroups()) {
                                if (!knownGroupIds.contains(signalGroup.getId())) {
                                    ioT3SignalControllerCallback.newSignalGroup(signalController, signalGroup);
                                }
                            }
                            for (Map.Entry<SignalGroup, SignalGroupUpdateType> signalGroupChange
                                    : signalGroupChanges.entrySet()) {
                                ioT3SignalControllerCallback.signalGroupUpdated(
                                        signalController,
                                        signalGroupChange.getKey(),
                                        signalGroupChange.getValue());
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, TAG + " SPATEM parsing error: " + e);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, TAG + " SPATEM processing error: " + e);
        }
    }

    // -------------------------------------------------------------------------
    // MAPEM-driven position resolution
    // -------------------------------------------------------------------------

    /**
     * Called by {@link RoadGeometryManager} whenever a {@link RoadIntersection} is created or
     * updated. Finds any existing {@link SignalController} matching the same region + intersection ID
     * and resolves the stop-line positions of its signal groups.
     *
     * @param roadIntersection the newly created or updated intersection
     */
    public static void tryResolvePositions(RoadIntersection roadIntersection) {
        synchronized (SIGNAL_CONTROLLER_MAP) {
            for (SignalController signalController : SIGNAL_CONTROLLERS) {
                if (signalController.getRegionId() == roadIntersection.getRegionId()
                        && signalController.getIntersectionId() == roadIntersection.getIntersectionId()) {
                    List<SignalGroup> newlyResolved = signalController.resolvePositions(roadIntersection);
                    if (ioT3SignalControllerCallback != null) {
                        for (SignalGroup signalGroup : newlyResolved) {
                            ioT3SignalControllerCallback.signalGroupUpdated(
                                    signalController, signalGroup, SignalGroupUpdateType.POSITION);
                        }
                    }
                }
            }
        }
    }

    /** Looks up the matching RoadIntersection from RoadGeometryManager and resolves positions. */
    private static void tryResolveFromMapem(SignalController signalController, int regionId, int intersectionId) {
        for (RoadIntersection roadIntersection : RoadGeometryManager.getRoadIntersections()) {
            if (roadIntersection.getRegionId() == regionId && roadIntersection.getIntersectionId() == intersectionId) {
                signalController.resolvePositions(roadIntersection);
                return;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Expiry
    // -------------------------------------------------------------------------

    private static void checkAndRemoveExpiredObjects() {
        synchronized (SIGNAL_CONTROLLERS) {
            Iterator<SignalController> iterator = SIGNAL_CONTROLLERS.iterator();
            while (iterator.hasNext()) {
                SignalController signalController = iterator.next();

                // Check individual signal group expiry (phase deadline passed)
                List<SignalGroup> newlyExpiredGroups = signalController.checkAndExpireSignalGroups();
                for (SignalGroup signalGroup : newlyExpiredGroups) {
                    if (ioT3SignalControllerCallback != null) {
                        ioT3SignalControllerCallback.signalGroupExpired(signalController, signalGroup);
                    }
                }

                // Check controller expiry: all groups expired AND RSU silent beyond MAX_STALENESS_MS
                if (!signalController.stillLiving()) {
                    // Force-expire all signal groups not yet expired, then fire SignalControllerExpired
                    List<SignalGroup> remainingGroups = signalController.forceExpireAllSignalGroups();
                    for (SignalGroup signalGroup : remainingGroups) {
                        if (ioT3SignalControllerCallback != null) {
                            ioT3SignalControllerCallback.signalGroupExpired(signalController, signalGroup);
                        }
                    }
                    iterator.remove();
                    synchronized (SIGNAL_CONTROLLER_MAP) {
                        SIGNAL_CONTROLLER_MAP.values().remove(signalController);
                    }
                    if (ioT3SignalControllerCallback != null) {
                        ioT3SignalControllerCallback.signalControllerExpired(signalController);
                    }
                }
            }
        }
    }

    private static synchronized void startExpirationCheck() {
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleWithFixedDelay(SignalControllerManager::checkAndRemoveExpiredObjects,
                    1, 1, TimeUnit.SECONDS);
        }
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Retrieve a read-only list of all known signal controllers (one per intersection per SPATEM source).
     */
    public static List<SignalController> getSignalControllers() {
        return Collections.unmodifiableList(SIGNAL_CONTROLLERS);
    }

    /**
     * Remove all stored signal controller data. Use when leaving a geographic area or resetting state.
     */
    public static void clear() {
        synchronized (SIGNAL_CONTROLLER_MAP) {
            SIGNAL_CONTROLLERS.clear();
            SIGNAL_CONTROLLER_MAP.clear();
        }
    }
}
