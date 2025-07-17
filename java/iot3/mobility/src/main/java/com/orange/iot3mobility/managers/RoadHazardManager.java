/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.managers;

import com.orange.iot3mobility.TrueTime;
import com.orange.iot3mobility.its.json.JsonUtil;
import com.orange.iot3mobility.its.json.denm.DENM;
import com.orange.iot3mobility.roadobjects.RoadHazard;

import org.json.JSONException;
import org.json.JSONObject;

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

    public static void processDenm(String message) {
        if(ioT3RoadHazardCallback != null) {
            try {
                DENM denm = DENM.jsonParser(new JSONObject(message));
                if(denm != null) {
                    ioT3RoadHazardCallback.denmArrived(denm);
                    //associate the received DENM to a RoadHazard object
                    String uuid = denm.getManagementContainer().getActionId().getOriginatingStationId()
                            + "_" + denm.getManagementContainer().getActionId().getSequenceNumber();
                    int lifetime = denm.getManagementContainer().getValidityDuration() * 1000; // to ms
                    long timestamp = denm.getTimestamp();
                    boolean expired = TrueTime.getAccurateTime() - timestamp > lifetime;
                    boolean terminate = denm.getManagementContainer().getTermination() != JsonUtil.UNKNOWN;
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
                                    roadHazard.setDenm(denm);
                                    ioT3RoadHazardCallback.roadHazardUpdate(roadHazard);
                                }
                            }
                        }
                    } else if(!terminate && !expired) {
                        RoadHazard roadHazard = new RoadHazard(uuid, denm);
                        addRoadHazard(uuid, roadHazard);
                        ioT3RoadHazardCallback.newRoadHazard(roadHazard);
                    }
                }
            } catch (JSONException e) {
                LOGGER.log(Level.WARNING, TAG, "DENM parsing error: " + e);
            }
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
