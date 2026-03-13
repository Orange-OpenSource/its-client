/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.managers;

import com.orange.iot3mobility.messages.EtsiConverter;
import com.orange.iot3mobility.messages.cpm.CpmHelper;
import com.orange.iot3mobility.messages.cpm.core.CpmCodec;
import com.orange.iot3mobility.messages.cpm.core.CpmVersion;
import com.orange.iot3mobility.messages.cpm.v121.model.CpmEnvelope121;
import com.orange.iot3mobility.messages.cpm.v121.model.CpmMessage121;
import com.orange.iot3mobility.messages.cpm.v211.model.CpmEnvelope211;
import com.orange.iot3mobility.messages.cpm.v211.model.CpmMessage211;
import com.orange.iot3mobility.quadkey.LatLng;
import com.orange.iot3mobility.roadobjects.RoadSensor;

import java.io.IOException;
import java.util.*;
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

    public static void processCpm(String message, CpmHelper cpmHelper) {
        if(ioT3RoadSensorCallback != null) {
            try {
                CpmCodec.CpmFrame<?> cpmFrame = cpmHelper.parse(message);
                ioT3RoadSensorCallback.cpmArrived(cpmFrame);

                String uuid;
                LatLng position;

                if(cpmFrame.version() == CpmVersion.V1_2_1) {
                    CpmEnvelope121 cpmEnvelope121 = (CpmEnvelope121) cpmFrame.envelope();
                    CpmMessage121 cpm121 = cpmEnvelope121.message();
                    uuid = cpmEnvelope121.sourceUuid() + "_" + cpm121.stationId();
                    position = new LatLng(
                            EtsiConverter.latitudeDegrees(cpm121.managementContainer().referencePosition().latitude()),
                            EtsiConverter.longitudeDegrees(cpm121.managementContainer().referencePosition().longitude()));
                    updateRoadSensor(uuid, position, cpmFrame);
                } else if(cpmFrame.version() == CpmVersion.V2_1_1) {
                    CpmEnvelope211 cpmEnvelope211 = (CpmEnvelope211) cpmFrame.envelope();
                    CpmMessage211 cpm211 = cpmEnvelope211.message();
                    uuid = cpmEnvelope211.sourceUuid() + "_" + cpm211.stationId();
                    position = new LatLng(
                            EtsiConverter.latitudeDegrees(cpm211.managementContainer().referencePosition().latitude()),
                            EtsiConverter.longitudeDegrees(cpm211.managementContainer().referencePosition().longitude()));
                    updateRoadSensor(uuid, position, cpmFrame);
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, TAG, "CPM parsing error: " + e);
            }
        }
    }

    private static void updateRoadSensor(String uuid, LatLng position, CpmCodec.CpmFrame<?> cpmFrame) {
        if(ROAD_SENSOR_MAP.containsKey(uuid)) {
            synchronized (ROAD_SENSOR_MAP) {
                RoadSensor roadSensor = ROAD_SENSOR_MAP.get(uuid);
                if(roadSensor != null) {
                    roadSensor.updateTimestamp();
                    roadSensor.setCpmFrame(cpmFrame);
                    ioT3RoadSensorCallback.roadSensorUpdate(roadSensor);
                }
            }
        } else {
            RoadSensor roadSensor = new RoadSensor(uuid, position, cpmFrame, ioT3RoadSensorCallback);
            addRoadSensor(uuid, roadSensor);
            ioT3RoadSensorCallback.newRoadSensor(roadSensor);
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

    /**
     * Retrieve a read-only list of the Road Sensors in the vicinity.
     *
     * @return the read-only list of {@link RoadSensor} objects
     */
    public static List<RoadSensor> getRoadSensors() {
        return Collections.unmodifiableList(ROAD_SENSORS);
    }

}
