/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.managers;

import com.orange.iot3mobility.TrueTime;
import com.orange.iot3mobility.messages.denm.DenmHelper;
import com.orange.iot3mobility.messages.denm.core.DenmCodec;
import com.orange.iot3mobility.messages.denm.core.DenmVersion;
import com.orange.iot3mobility.messages.denm.v113.model.DenmEnvelope113;
import com.orange.iot3mobility.messages.denm.v113.model.DenmMessage113;
import com.orange.iot3mobility.messages.denm.v220.model.DenmEnvelope220;
import com.orange.iot3mobility.messages.denm.v220.model.DenmMessage220;
import com.orange.iot3mobility.roadobjects.RoadHazard;

import org.json.JSONException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RoadHazardManager {

    private static final Logger LOGGER = Logger.getLogger(RoadHazardManager.class.getName());

    private static final String TAG = "IoT3Mobility.RoadHazardManager";

    private static final ArrayList<RoadHazard> ROAD_HAZARDS = new ArrayList<>();
    private static final HashMap<String, RoadHazard> ROAD_HAZARD_MAP = new HashMap<>();
    private static IoT3RoadHazardCallback ioT3RoadHazardCallback;

    private static ScheduledExecutorService scheduler = null;

    public static void init(IoT3RoadHazardCallback ioT3RoadHazardCallback) {
        RoadHazardManager.ioT3RoadHazardCallback = ioT3RoadHazardCallback;
        startExpirationCheck();
    }

    public static void processDenm(String message, DenmHelper denmHelper) {
        if(ioT3RoadHazardCallback != null) {
            try {
                DenmCodec.DenmFrame<?> denmFrame = denmHelper.parse(message);
                ioT3RoadHazardCallback.denmArrived(denmFrame);

                String uuid;
                int lifetime;
                long timestamp;
                boolean expired;
                boolean terminate;

                if(denmFrame.version().equals(DenmVersion.V1_1_3)) {
                    DenmEnvelope113 denmEnvelope113 = (DenmEnvelope113) denmFrame.envelope();
                    DenmMessage113 denm113 = denmEnvelope113.message();
                    timestamp = denmEnvelope113.timestamp();
                    uuid = denm113.managementContainer().actionId().originatingStationId() + "_"
                            + denm113.managementContainer().actionId().sequenceNumber();
                    lifetime = denm113.managementContainer().validityDuration() * 1000;
                    expired = TrueTime.getAccurateTime() - timestamp > lifetime;
                    terminate = denm113.managementContainer().termination() != null;
                    createOrUpdateRoadHazard(uuid, expired, terminate, denmFrame);
                } else if(denmFrame.version().equals(DenmVersion.V2_2_0)) {
                    DenmEnvelope220 denmEnvelope220 = (DenmEnvelope220) denmFrame.envelope();
                    DenmMessage220 denm220 = denmEnvelope220.message();
                    timestamp = denmEnvelope220.timestamp();
                    uuid = denm220.managementContainer().actionId().originatingStationId() + "_"
                            + denm220.managementContainer().actionId().sequenceNumber();
                    lifetime = denm220.managementContainer().validityDuration() * 1000;
                    expired = TrueTime.getAccurateTime() - timestamp > lifetime;
                    terminate = denm220.managementContainer().termination() != null;
                    createOrUpdateRoadHazard(uuid, expired, terminate, denmFrame);
                }
            } catch (JSONException | IOException e) {
                LOGGER.log(Level.WARNING, TAG, "DENM parsing error: " + e);
            }
        }
    }

    private static void createOrUpdateRoadHazard(String uuid, boolean expired, boolean terminate,
                                                 DenmCodec.DenmFrame<?> denmFrame) {
        if(ROAD_HAZARD_MAP.containsKey(uuid)) {
            synchronized (ROAD_HAZARD_MAP) {
                RoadHazard roadHazard = ROAD_HAZARD_MAP.get(uuid);
                if(roadHazard != null) {
                    if(terminate) {
                        ROAD_HAZARD_MAP.values().remove(roadHazard);
                        synchronized (ROAD_HAZARDS) {
                            ROAD_HAZARDS.remove(roadHazard);
                        }
                        ioT3RoadHazardCallback.roadHazardExpired(roadHazard);
                    } else {
                        roadHazard.setDenmFrame(denmFrame);
                        ioT3RoadHazardCallback.roadHazardUpdate(roadHazard);
                    }
                }
            }
        } else if(!terminate && !expired) {
            RoadHazard roadHazard = new RoadHazard(uuid, denmFrame);
            addRoadHazard(uuid, roadHazard);
            ioT3RoadHazardCallback.newRoadHazard(roadHazard);
        }
    }

    private static void addRoadHazard(String key, RoadHazard roadHazard) {
        synchronized (ROAD_HAZARDS) {
            ROAD_HAZARDS.add(roadHazard);
        }
        synchronized (ROAD_HAZARD_MAP) {
            ROAD_HAZARD_MAP.put(key, roadHazard);
        }
    }

    private static void checkAndRemoveExpiredObjects() {
        synchronized (ROAD_HAZARDS) {
            Iterator<RoadHazard> iterator = ROAD_HAZARDS.iterator();
            while (iterator.hasNext()) {
                RoadHazard roadHazard = iterator.next();
                if (!roadHazard.stillLiving()) {
                    iterator.remove();
                    synchronized (ROAD_HAZARD_MAP) {
                        // Remove by value
                        ROAD_HAZARD_MAP.values().remove(roadHazard);
                    }
                    ioT3RoadHazardCallback.roadHazardExpired(roadHazard);
                }
            }
        }
    }

    private static synchronized void startExpirationCheck() {
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleWithFixedDelay(RoadHazardManager::checkAndRemoveExpiredObjects,
                    1, 1, TimeUnit.SECONDS);
        }
    }

    /**
     * Retrieve a read-only list of the Road Hazards in the vicinity.
     *
     * @return the read-only list of {@link com.orange.iot3mobility.roadobjects.RoadHazard} objects
     */
    public static List<RoadHazard> getRoadHazards() {
        return Collections.unmodifiableList(ROAD_HAZARDS);
    }

}
