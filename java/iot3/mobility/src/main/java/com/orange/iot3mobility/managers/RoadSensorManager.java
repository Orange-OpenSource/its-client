/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.managers;

import com.orange.iot3mobility.its.json.cpm.CPM;
import com.orange.iot3mobility.quadkey.LatLng;
import com.orange.iot3mobility.roadobjects.RoadSensor;

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

public class RoadSensorManager {

    private static final Logger LOGGER = Logger.getLogger(RoadSensorManager.class.getName());

    private static final String TAG = "IoT3Mobility.RoadSensorManager";

    private static final ArrayList<RoadSensor> ROAD_SENSORS = new ArrayList<>();
    private static final HashMap<String, RoadSensor> ROAD_SENSOR_MAP = new HashMap<>();
    private static IoT3RoadSensorCallback ioT3RoadSensorCallback;

    private static ScheduledExecutorService scheduler = null;

    public static void init(IoT3RoadSensorCallback ioT3RoadSensorCallback) {
        RoadSensorManager.ioT3RoadSensorCallback = ioT3RoadSensorCallback;
        startExpirationCheck();
    }

    public static void processCpm(String message) {
        if(ioT3RoadSensorCallback != null) {
            try {
                CPM cpm = CPM.jsonParser(new JSONObject(message));
                ioT3RoadSensorCallback.cpmArrived(cpm);

                //associate the received CPM to a RoadSensor object
                String uuid = cpm.getSourceUuid() + "_" + cpm.getStationId();
                LatLng position = new LatLng(
                        cpm.getManagementContainer().getReferencePosition().getLatitudeDegree(),
                        cpm.getManagementContainer().getReferencePosition().getLongitudeDegree());
                if(ROAD_SENSOR_MAP.containsKey(uuid)) {
                    synchronized (ROAD_SENSOR_MAP) {
                        RoadSensor roadSensor = ROAD_SENSOR_MAP.get(uuid);
                        if(roadSensor != null) {
                            roadSensor.updateTimestamp();
                            roadSensor.setCpm(cpm);
                            if(cpm.getPerceivedObjectContainer() != null)
                                roadSensor.updateSensorObjects(cpm.getPerceivedObjectContainer().getPerceivedObjects());
                            ioT3RoadSensorCallback.roadSensorUpdate(roadSensor);
                        }
                    }
                } else {
                    RoadSensor roadSensor = new RoadSensor(uuid, position, cpm, ioT3RoadSensorCallback);
                    if(cpm.getPerceivedObjectContainer() != null)
                        roadSensor.updateSensorObjects(cpm.getPerceivedObjectContainer().getPerceivedObjects());
                    addRoadSensor(uuid, roadSensor);
                    ioT3RoadSensorCallback.newRoadSensor(roadSensor);
                }
            } catch (JSONException e) {
                LOGGER.log(Level.WARNING, TAG, "CPM parsing error: " + e);
            }
        }
    }

    private static void addRoadSensor(String key, RoadSensor roadSensor) {
        synchronized (ROAD_SENSORS) {
            ROAD_SENSORS.add(roadSensor);
        }
        synchronized (ROAD_SENSOR_MAP) {
            ROAD_SENSOR_MAP.put(key, roadSensor);
        }
    }

    private static void checkAndRemoveExpiredObjects() {
        synchronized (ROAD_SENSORS) {
            Iterator<RoadSensor> iterator = ROAD_SENSORS.iterator();
            while (iterator.hasNext()) {
                RoadSensor roadSensor = iterator.next();
                if (!roadSensor.stillLiving()) {
                    iterator.remove();
                    synchronized (ROAD_SENSOR_MAP) {
                        // Remove by value
                        ROAD_SENSOR_MAP.values().remove(roadSensor);
                    }
                    ioT3RoadSensorCallback.roadSensorExpired(roadSensor);
                }
            }
        }
    }

    private static synchronized void startExpirationCheck() {
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleWithFixedDelay(RoadSensorManager::checkAndRemoveExpiredObjects,
                    1, 1, TimeUnit.SECONDS);
        }
    }

}
