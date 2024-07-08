package com.orange.iot3mobility.managers;

import com.orange.iot3mobility.its.StationType;
import com.orange.iot3mobility.its.json.cam.CAM;
import com.orange.iot3mobility.its.json.cpm.CPM;
import com.orange.iot3mobility.quadkey.LatLng;
import com.orange.iot3mobility.roadobjects.RoadUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RoadUserManager {

    private static final Logger LOGGER = Logger.getLogger(RoadUserManager.class.getName());

    private static final String TAG = "IoT3Mobility.RoadUserManager";

    private static final ArrayList<RoadUser> ROAD_USERS = new ArrayList<>();
    private static final HashMap<String, RoadUser> ROAD_USER_MAP = new HashMap<>();
    private static IoT3RoadUserCallback ioT3RoadUserCallback;

    private static ScheduledExecutorService scheduler = null;

    public static void init(IoT3RoadUserCallback ioT3RoadUserCallback) {
        RoadUserManager.ioT3RoadUserCallback = ioT3RoadUserCallback;
        startExpirationCheck();
    }

    public static void processCam(String message) {
        if(ioT3RoadUserCallback != null) {
            try {
                CAM cam = CAM.jsonParser(new JSONObject(message));
                ioT3RoadUserCallback.camArrived(cam);

                //associate the received CAM to a RoadUser object
                String uuid = cam.getSourceUuid() + "_" + cam.getStationId();
                StationType stationType = StationType.fromId(cam.getBasicContainer().getStationType());
                LatLng position = new LatLng(
                        cam.getBasicContainer().getPosition().getLatitudeDegree(),
                        cam.getBasicContainer().getPosition().getLongitudeDegree());
                float speed = cam.getHighFrequencyContainer().getSpeedMs();
                float heading = cam.getHighFrequencyContainer().getHeadingDegree();
                if(ROAD_USER_MAP.containsKey(uuid)) {
                    synchronized (ROAD_USER_MAP) {
                        RoadUser roadUser = ROAD_USER_MAP.get(uuid);
                        if(roadUser != null) {
                            roadUser.updateTimestamp();
                            roadUser.setStationType(stationType);
                            roadUser.setPosition(position);
                            roadUser.setSpeed(speed);
                            roadUser.setHeading(heading);
                            roadUser.setCam(cam);
                            ioT3RoadUserCallback.roadUserUpdate(roadUser);
                        }
                    }
                } else {
                    RoadUser roadUser = new RoadUser(uuid, stationType, position, speed, heading, cam);
                    addRoadUser(uuid, roadUser);
                    ioT3RoadUserCallback.newRoadUser(roadUser);
                }
            } catch (JSONException e) {
                LOGGER.log(Level.WARNING, TAG, "CAM parsing error: " + e);
            }
        }
    }

    private static void addRoadUser(String key, RoadUser roadUser) {
        synchronized (ROAD_USERS) {
            ROAD_USERS.add(roadUser);
        }
        synchronized (ROAD_USER_MAP) {
            ROAD_USER_MAP.put(key, roadUser);
        }
    }

    private static void checkAndRemoveExpiredObjects() {
        synchronized (ROAD_USERS) {
            Iterator<RoadUser> iterator = ROAD_USERS.iterator();
            while (iterator.hasNext()) {
                RoadUser roadUser = iterator.next();
                if (!roadUser.stillLiving()) {
                    iterator.remove();
                    synchronized (ROAD_USER_MAP) {
                        // Remove by value
                        ROAD_USER_MAP.values().remove(roadUser);
                    }
                    ioT3RoadUserCallback.roadUserExpired(roadUser);
                }
            }
        }
    }

    private static synchronized void startExpirationCheck() {
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleWithFixedDelay(RoadUserManager::checkAndRemoveExpiredObjects,
                    1, 1, TimeUnit.SECONDS);
        }
    }

}
