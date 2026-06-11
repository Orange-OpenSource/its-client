/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.managers;

import com.orange.iot3mobility.messages.EtsiConverter;
import com.orange.iot3mobility.messages.StationType;
import com.orange.iot3mobility.messages.cam.CamHelper;
import com.orange.iot3mobility.messages.cam.core.CamCodec;
import com.orange.iot3mobility.messages.cam.core.CamVersion;
import com.orange.iot3mobility.messages.cam.v113.model.CamEnvelope113;
import com.orange.iot3mobility.messages.cam.v240.model.CamEnvelope240;
import com.orange.iot3mobility.messages.cam.v240.model.CamStructuredData;
import com.orange.iot3mobility.messages.cam.v240.model.highfrequencycontainer.BasicVehicleContainerHighFrequency;
import com.orange.iot3mobility.quadkey.LatLng;
import com.orange.iot3mobility.roadobjects.RoadUser;

import java.io.IOException;
import java.util.*;
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

    public static void processCam(String message, CamHelper camHelper) {
        if(ioT3RoadUserCallback != null) {
            try {
                CamCodec.CamFrame<?> camFrame = camHelper.parse(message);
                ioT3RoadUserCallback.camArrived(camFrame);

                String uuid;
                StationType stationType;
                LatLng position;
                double speed;
                double heading;

                if(camFrame.version().equals(CamVersion.V1_1_3)) {
                    CamEnvelope113 camEnvelope113 = (CamEnvelope113) camFrame.envelope();
                    uuid = camEnvelope113.sourceUuid() + "_" + camEnvelope113.message().stationId();
                    stationType = StationType.fromValue(camEnvelope113.message().basicContainer().stationType());
                    position = new LatLng(
                            EtsiConverter.latitudeDegrees(camEnvelope113.message().basicContainer().referencePosition().latitude()),
                            EtsiConverter.longitudeDegrees(camEnvelope113.message().basicContainer().referencePosition().longitude()));
                    speed = EtsiConverter.speedMetersPerSecond(camEnvelope113.message().highFrequencyContainer().speed());
                    heading = EtsiConverter.headingDegrees(camEnvelope113.message().highFrequencyContainer().heading());
                    createOrUpdateRoadUser(uuid, stationType, position, speed, heading, null, camFrame);
                } else if(camFrame.version().equals(CamVersion.V2_4_0)) {
                    CamEnvelope240 camEnvelope240 = (CamEnvelope240) camFrame.envelope();
                    if(camEnvelope240.message() instanceof CamStructuredData cam) {
                        uuid = camEnvelope240.sourceUuid() + "_" + cam.stationId();
                        stationType = StationType.fromValue(cam.basicContainer().stationType());
                        position = new LatLng(
                                EtsiConverter.latitudeDegrees(cam.basicContainer().referencePosition().latitude()),
                                EtsiConverter.longitudeDegrees(cam.basicContainer().referencePosition().longitude()));
                        if(cam.highFrequencyContainer() instanceof BasicVehicleContainerHighFrequency vehicleContainerHighFrequency) {
                            speed = EtsiConverter.speedMetersPerSecond(vehicleContainerHighFrequency.speed().value());
                            heading = EtsiConverter.headingDegrees(vehicleContainerHighFrequency.heading().value());
                        } else { // road-side unit
                            speed = 0;
                            heading = 0;
                        }
                        createOrUpdateRoadUser(uuid, stationType, position, speed, heading,
                                camEnvelope240.linkedStationId(), camFrame);
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, TAG + " CAM parsing error: " + e);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, TAG + " CAM processing error: " + e);
            }
        }
    }

    private static void createOrUpdateRoadUser(String uuid, StationType stationType, LatLng position, double speed,
                                               double heading, Long linkedStationId, CamCodec.CamFrame<?> camFrame) {
        if(ROAD_USER_MAP.containsKey(uuid)) {
            synchronized (ROAD_USER_MAP) {
                RoadUser roadUser = ROAD_USER_MAP.get(uuid);
                if(roadUser != null) {
                    roadUser.updateTimestamp();
                    roadUser.setStationType(stationType);
                    roadUser.setPosition(position);
                    roadUser.setSpeed(speed);
                    roadUser.setHeading(heading);
                    roadUser.setLinkedStationId(linkedStationId);
                    roadUser.setCamFrame(camFrame);
                    ioT3RoadUserCallback.roadUserUpdate(roadUser);
                }
            }
        } else {
            RoadUser roadUser = new RoadUser(uuid, stationType, position, speed, heading, linkedStationId, camFrame);
            addRoadUser(uuid, roadUser);
            ioT3RoadUserCallback.newRoadUser(roadUser);
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

    /**
     * Retrieve a read-only list of the Road Users in the vicinity.
     *
     * @return the read-only list of {@link RoadUser} objects
     */
    public static List<RoadUser> getRoadUsers() {
        return Collections.unmodifiableList(ROAD_USERS);
    }

    /**
     * Returns the {@link RoadUser} whose station identifier matches the
     * {@link RoadUser#getLinkedStationId()} of the given road user, or
     * {@code null} if the road user is not linked or the linked object
     * is not currently known.
     *
     * @param roadUser the road user to resolve the linked object for
     * @return the linked {@link RoadUser}, or {@code null}
     */
    public static RoadUser getLinkedObject(RoadUser roadUser) {
        if (roadUser == null || roadUser.getLinkedStationId() == null) {
            return null;
        }
        String suffix = "_" + roadUser.getLinkedStationId();
        synchronized (ROAD_USERS) {
            for (RoadUser candidate : ROAD_USERS) {
                if (candidate.getUuid().endsWith(suffix)) {
                    return candidate;
                }
            }
        }
        return null;
    }

}
