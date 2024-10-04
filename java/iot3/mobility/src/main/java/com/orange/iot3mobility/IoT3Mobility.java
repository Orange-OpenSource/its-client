/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility;

import com.orange.iot3core.IoT3Core;
import com.orange.iot3core.IoT3CoreCallback;
import com.orange.iot3mobility.its.EtsiUtils;
import com.orange.iot3mobility.its.HazardType;
import com.orange.iot3mobility.its.StationType;
import com.orange.iot3mobility.its.json.JsonValue;
import com.orange.iot3mobility.its.json.Position;
import com.orange.iot3mobility.its.json.cam.BasicContainer;
import com.orange.iot3mobility.its.json.cam.CAM;
import com.orange.iot3mobility.its.json.cam.HighFrequencyContainer;
import com.orange.iot3mobility.its.json.denm.*;
import com.orange.iot3mobility.managers.IoT3RoadHazardCallback;
import com.orange.iot3mobility.managers.IoT3RoadSensorCallback;
import com.orange.iot3mobility.managers.IoT3RoadUserCallback;
import com.orange.iot3mobility.managers.RoIManager;
import com.orange.iot3mobility.managers.RoadHazardManager;
import com.orange.iot3mobility.managers.RoadSensorManager;
import com.orange.iot3mobility.managers.RoadUserManager;
import com.orange.iot3mobility.quadkey.LatLng;
import com.orange.iot3mobility.quadkey.QuadTileHelper;

/**
 * Mobility SDK based on the Orange IoT3.0 platform.
 * <br>IoT3Mobility takes advantage of the IoT3Core to propose:
 * <ul>
 * <li>Transparent management of V2X messages (road users, road hazards, road sensors),</li>
 * <li>Share your location and see other road users around you,</li>
 * <li>Be alerted of road hazards in your vicinity.</li>
 * </ul>
 */
public class IoT3Mobility {

    private final IoT3Core ioT3Core;
    private final RoIManager roIManager;

    private final String uuid;
    private final int stationId;

    /**
     * Instantiate the IoT3.0 Mobility SDK.
     *
     * @param host server address, provided by Orange
     * @param username username, provided by Orange
     * @param password password, provided by Orange
     * @param uuid your unique user ID, usually in the form com_userType_id (e.g. ora_car_123)
     * @param context can be a specific project name or can be provided by Orange
     * @param ioT3MobilityCallback callback to retrieve connection status
     */
    public IoT3Mobility(String host,
                        String username,
                        String password,
                        String uuid,
                        String context,
                        IoT3MobilityCallback ioT3MobilityCallback,
                        String telemetryHost) {
        this.uuid = uuid;
        // random stationId at the moment, will be an option to set it later on
        this.stationId = Utils.randomBetween(999, 99999999);
        ioT3Core = new IoT3Core(
                host,
                username,
                password,
                uuid,
                new IoT3CoreCallback() {
                    @Override
                    public void mqttConnectionLost(Throwable throwable) {
                        ioT3MobilityCallback.connectionLost(throwable);
                    }

                    @Override
                    public void mqttMessageArrived(String topic, String message) {
                        processMessage(topic, message);
                    }

                    @Override
                    public void mqttConnectComplete(boolean reconnect, String serverURI) {
                        ioT3MobilityCallback.connectComplete(reconnect, serverURI);
                    }

                    @Override
                    public void mqttMessagePublished(Throwable publishFailure) {

                    }

                    @Override
                    public void mqttSubscriptionComplete(Throwable subscribeFailure) {

                    }

                    @Override
                    public void mqttUnsubscriptionComplete(Throwable unsubscribeFailure) {

                    }
                },
                telemetryHost);

        roIManager = new RoIManager(ioT3Core, uuid, context);

        TrueTime.initTrueTime();
    }

    /**
     * Disconnect from the server.
     */
    public void disconnect() {
        if(ioT3Core != null) ioT3Core.disconnectAll();
    }

    /**
     * Sets the Region of Interest (RoI) for road users based on a specific
     * geographical position and zoom level.
     *
     * @param position the LatLng representing the target geographical position for the RoI.
     * @param level the zoom level for the RoI, which should be between 1 and 22. This level
     *              determines the granularity of the quadtree tiles computed around the target
     *              position.
     * @param withNeighborTiles include neighboring tiles around the computed target tile,
     *                          i.e. the RoI will be 1 tile or 9 tiles (recommended for clients
     *                          changing position such as vehicles or vulnerable users)
     */
    public void setRoadUserRoI(LatLng position, int level, boolean withNeighborTiles) {
        if(roIManager != null) roIManager.setRoadUserRoI(position, level, withNeighborTiles);
    }

    /**
     * Set up the road user callback to be informed of road users updates in the RoI you have
     * defined with {@link #setRoadUserRoI(LatLng, int, boolean)}
     *
     * @param ioT3RoadUserCallback the callback to be informed of new road users,
     *                             existing user updates, users who have timed out,
     *                             and raw CAM reception
     */
    public void setRoadUserCallback(IoT3RoadUserCallback ioT3RoadUserCallback) {
        RoadUserManager.init(ioT3RoadUserCallback);
    }

    /**
     * Sets the Region of Interest (RoI) for road hazards based on a specific
     * geographical position and zoom level.
     *
     * @param position the LatLng representing the target geographical position for the RoI.
     * @param level the zoom level for the RoI, which should be between 1 and 22. This level
     *              determines the granularity of the quadtree tiles computed around the target
     *              position.
     * @param withNeighborTiles include neighboring tiles around the computed target tile,
     *                          i.e. the RoI will be 1 tile or 9 tiles (recommended for clients
     *                          changing position such as vehicles or vulnerable users)
     */
    public void setRoadHazardRoI(LatLng position, int level, boolean withNeighborTiles) {
        if(roIManager != null) roIManager.setRoadHazardRoI(position, level, withNeighborTiles);
    }

    /**
     * Set up the road hazard callback to be informed of road hazards updates in the RoI you have
     * defined with {@link #setRoadHazardRoI(LatLng, int, boolean)}
     *
     * @param ioT3RoadHazardCallback the callback to be informed of new road hazards,
     *                               existing hazard updates,
     *                               hazards that have timed out, and raw DENM reception
     */
    public void setRoadHazardCallback(IoT3RoadHazardCallback ioT3RoadHazardCallback) {
        RoadHazardManager.init(ioT3RoadHazardCallback);
    }

    /**
     * Sets the Region of Interest (RoI) for road sensors based on a specific
     * geographical position and zoom level.
     *
     * @param position the LatLng representing the target geographical position for the RoI.
     * @param level the zoom level for the RoI, which should be between 1 and 22. This level
     *              determines the granularity of the quadtree tiles computed around the target
     *              position.
     * @param withNeighborTiles include neighboring tiles around the computed target tile,
     *                          i.e. the RoI will be 1 tile or 9 tiles (recommended for clients
     *                          changing position such as vehicles or vulnerable users)
     */
    public void setRoadSensorRoI(LatLng position, int level, boolean withNeighborTiles) {
        if(roIManager != null) roIManager.setRoadSensorRoI(position, level, withNeighborTiles);
    }

    /**
     * Set up the road sensor callback to be informed of road sensors updates in the RoI you have
     * defined with {@link #setRoadSensorRoI(LatLng, int, boolean)}
     *
     * @param ioT3RoadSensorCallback the callback to be informed of new road sensors,
     *                               existing sensors updates and the objects they detect,
     *                               sensors and detected objects that have timed out,
     *                               and raw CPM reception
     */
    public void setRoadSensorCallback(IoT3RoadSensorCallback ioT3RoadSensorCallback) {
        RoadSensorManager.init(ioT3RoadSensorCallback);
    }

    private void processMessage(String topic, String message) {
        if(topic.contains("/cam/")) RoadUserManager.processCam(message);
        else if(topic.contains("/cpm/")) RoadSensorManager.processCpm(message);
        else if(topic.contains("/denm/")) RoadHazardManager.processDenm(message);
    }

    /**
     * Share your position and dynamic parameters with other road users
     *
     * @param stationType your road user type
     * @param position your position (latitude, longitude in degrees)
     * @param altitude your altitude in meters [-1000 - 8000]
     * @param heading your heading in degrees [0 - 360]
     * @param speed your speed in meters per second [0 - 163]
     * @param acceleration your longitudinal acceleration in m/s² [-16 - 16]
     * @param yawRate your rotational acceleration in deg/s² [-327 - 327]
     */
    public void sendPosition(StationType stationType, LatLng position, float altitude,
                             float heading, float speed, float acceleration, float yawRate) {
        // check for out of scope values before building the CAM
        altitude = Utils.clamp(altitude, -1000, 8000);
        heading = Utils.normalizeAngle(heading);
        speed = Utils.clamp(speed, 0, 163);
        acceleration = Utils.clamp(acceleration, -16, 16);
        yawRate = Utils.clamp(yawRate, -327, 327);

        // build the CAM
        CAM cam = new CAM.CAMBuilder()
                .header(
                        JsonValue.Origin.SELF.value(),
                        JsonValue.Version.CURRENT.value(),
                        uuid,
                        TrueTime.getAccurateTime())
                .pduHeader(
                        1,
                        stationId,
                        (int) (TrueTime.getAccurateETSITime() % 65536))
                .basicContainer(
                        new BasicContainer(
                                stationType.getId(),
                                new Position(
                                        (long) (position.getLatitude() * EtsiUtils.ETSI_COORDINATES_FACTOR),
                                        (long) (position.getLongitude() * EtsiUtils.ETSI_COORDINATES_FACTOR),
                                        (int) (altitude * 100))))
                .highFreqContainer(
                        new HighFrequencyContainer(
                                (int) (heading * 10),
                                (int) (speed * 100),
                                (int) (acceleration * 10),
                                (int) (yawRate * 10)))
                .build();

        // build the topic
        String quadkey = QuadTileHelper.latLngToQuadKey(position.latitude, position.longitude, 22);
        String geoExtension = QuadTileHelper.quadKeyToQuadTopic(quadkey);
        String topic = "SWR/inQueue/v2x/cam/" + uuid + geoExtension;

        // send the message
        if(ioT3Core != null) ioT3Core.mqttPublish(topic, cam.getJsonCAM().toString());
    }

    /**
     * Inform other road users of a road hazard
     *
     * @param hazardType the type of the reported hazard
     * @param position the hazard's position (latitude, longitude in degrees)
     * @param lifetime the lifetimes of this hazard in seconds [0 - 86400]
     * @param infoQuality the quality of this hazard information [0 - 7]
     * @param stationType your road user type
     */
    public void sendHazard(HazardType hazardType, LatLng position, int lifetime, int infoQuality,
                           StationType stationType) {
        // check for out of scope values before building the DENM
        lifetime = (int) Utils.clamp(lifetime, 0, 86400);
        infoQuality = (int) Utils.clamp(infoQuality, 0, 7);

        // build the DENM
        DENM denm = new DENM.DENMBuilder()
                .header(
                        JsonValue.Origin.SELF.value(),
                        JsonValue.Version.CURRENT.value(),
                        uuid,
                        TrueTime.getAccurateTime())
                .pduHeader(
                        2,
                        stationId)
                .managementContainer(
                        new ManagementContainer(
                                new ActionId(
                                        stationId,
                                        EtsiUtils.getNextSequenceNumber()),
                                TrueTime.getAccurateETSITime(),
                                TrueTime.getAccurateETSITime(),
                                new Position(
                                        (long)(position.getLatitude() * EtsiUtils.ETSI_COORDINATES_FACTOR),
                                        (long)(position.getLongitude() * EtsiUtils.ETSI_COORDINATES_FACTOR)),
                                lifetime,
                                stationType.getId()))
                .situationContainer(
                        new SituationContainer(
                                infoQuality,
                                new EventType(
                                        hazardType.getCause(),
                                        hazardType.getSubcause())))
                .build();

        // build the topic
        String quadkey = QuadTileHelper.latLngToQuadKey(position.latitude, position.longitude, 22);
        String geoExtension = QuadTileHelper.quadKeyToQuadTopic(quadkey);
        String topic = "SWR/inQueue/v2x/denm/" + uuid + geoExtension;

        // send the message
        if(ioT3Core != null) ioT3Core.mqttPublish(topic, denm.getJsonDENM().toString());
    }


}
