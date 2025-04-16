/*
 Copyright 2016-2025 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

@authors
    Mathieu LEFEBVRE   <mathieu1.lefebvre@orange.com>
    Maciej Ćmiel       <maciej.cmiel@orange.com>
    Zbigniew Krawczyk  <zbigniew2.krawczyk@orange.com>
*/
package com.orange.iot3mobility;

import com.orange.iot3core.IoT3Core;
import com.orange.iot3core.IoT3CoreCallback;
import com.orange.iot3core.bootstrap.BootstrapConfig;
import com.orange.iot3core.clients.lwm2m.model.*;
import com.orange.iot3mobility.its.EtsiUtils;
import com.orange.iot3mobility.its.HazardType;
import com.orange.iot3mobility.its.StationType;
import com.orange.iot3mobility.its.json.JsonValue;
import com.orange.iot3mobility.its.json.Position;
import com.orange.iot3mobility.its.json.cam.BasicContainer;
import com.orange.iot3mobility.its.json.cam.CAM;
import com.orange.iot3mobility.its.json.cam.HighFrequencyContainer;
import com.orange.iot3mobility.its.json.cpm.CPM;
import com.orange.iot3mobility.its.json.denm.*;
import com.orange.iot3mobility.managers.*;
import com.orange.iot3mobility.quadkey.LatLng;
import com.orange.iot3mobility.quadkey.QuadTileHelper;
import com.orange.iot3mobility.roadobjects.RoadHazard;
import com.orange.iot3mobility.roadobjects.RoadSensor;
import com.orange.iot3mobility.roadobjects.RoadUser;

import java.net.URI;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

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
    private final String context;
    private final int stationId;

    private IoT3RawMessageCallback ioT3RawMessageCallback;
    private Lwm2mLocation lwm2mLocation;

    /**
     * Instantiate the IoT3.0 Mobility SDK.
     *
     * @param uuid unique user identifier
     * @param context specific project or client name
     * @param mqttHost MQTT broker address
     * @param mqttPort port of the MQTT broker
     * @param mqttUsername MQTT username
     * @param mqttPassword MQTT password
     * @param mqttUseTls use TLS for a secure connection with the MQTT broker
     * @param ioT3MobilityCallback callback to retrieve connection status
     * @param telemetryScheme Open Telemetry scheme (e.g. http, https)
     * @param telemetryHost Open Telemetry server address
     * @param telemetryPort port of the Open Telemetry server
     * @param telemetryEndpoint endpoint of the Open Telemetry server URL
     * @param telemetryUsername Open Telemetry username
     * @param telemetryPassword Open Telemetry password
     */
    private IoT3Mobility(String uuid,
                        String context,
                        String mqttHost,
                        int mqttPort,
                        String mqttUsername,
                        String mqttPassword,
                        boolean mqttUseTls,
                        IoT3MobilityCallback ioT3MobilityCallback,
                        String telemetryScheme,
                        String telemetryHost,
                        int telemetryPort,
                        String telemetryEndpoint,
                        String telemetryUsername,
                        String telemetryPassword,
                        Lwm2mConfig lwm2mConfig,
                        Lwm2mDevice lwm2mDevice,
                        Lwm2mInstance[] lwm2mInstances
    ) {
        this.uuid = uuid;
        this.context = context;
        // random stationId at the moment, will be an option to set it later on
        this.stationId = Utils.randomBetween(999, 99999999);

        IoT3CoreCallback ioT3CoreCallback = new IoT3CoreCallback() {
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
        };

        IoT3Core.IoT3CoreBuilder ioT3CoreBuilder = new IoT3Core.IoT3CoreBuilder()
                .mqttParams(mqttHost,
                        mqttPort,
                        mqttUsername,
                        mqttPassword,
                        uuid,
                        mqttUseTls)
                .lwm2mParams(
                        lwm2mConfig,
                        lwm2mDevice,
                        initLwm2mInstances(lwm2mInstances))
                .callback(ioT3CoreCallback);

        if(telemetryHost != null) {
            ioT3CoreBuilder.telemetryParams(telemetryScheme,
                    telemetryHost,
                    telemetryPort,
                    telemetryEndpoint,
                    telemetryUsername,
                    telemetryPassword);
        }

        ioT3Core = ioT3CoreBuilder.build();

        roIManager = new RoIManager(ioT3Core, uuid, context);

        TrueTime.initTrueTime();
    }

    private Lwm2mInstance[] initLwm2mInstances(Lwm2mInstance[] lwm2mInstances) {
        ArrayList<Lwm2mInstance> lwm2mInstancesMutable = new ArrayList<>(Arrays.asList(lwm2mInstances));

        // init lwm2mLocation
        Lwm2mInstance lwm2mLocationInstance = lwm2mInstancesMutable.stream()
                .filter(lwm2mInstance -> lwm2mInstance instanceof Lwm2mLocation)
                .findFirst()
                .orElse(null);

        if (lwm2mLocationInstance instanceof Lwm2mLocation) {
            lwm2mLocation = (Lwm2mLocation) lwm2mLocationInstance;
        } else {
            lwm2mLocation = new Lwm2mLocation();
            lwm2mInstancesMutable.add(lwm2mLocation);
        }

        // return result
        return lwm2mInstancesMutable.toArray(new Lwm2mInstance[0]);
    }

    /**
     * Check that the connection is established.
     */
    public boolean isConnected() {
        if(ioT3Core != null) return ioT3Core.isMqttConnected();
        else return false;
    }

    /**
     * Retrieve the IoT3Core instance powering IoT3Mobility.
     */
    public IoT3Core getIoT3Core() {
        return ioT3Core;
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

    /**
     * Set up the raw message callback to be informed of any message being received.
     *
     * @param ioT3RawMessageCallback the callback to be informed upon message reception, before treatment.
     */
    public void setRawMessageCallback(IoT3RawMessageCallback ioT3RawMessageCallback) {
        this.ioT3RawMessageCallback = ioT3RawMessageCallback;
    }

    private void processMessage(String topic, String message) {
        if(ioT3RawMessageCallback != null) ioT3RawMessageCallback.messageArrived(message);
        if(topic.contains("/cam/")) RoadUserManager.processCam(message);
        else if(topic.contains("/cpm/")) RoadSensorManager.processCpm(message);
        else if(topic.contains("/denm/")) RoadHazardManager.processDenm(message);
    }

    /**
     * Share your position and dynamic parameters with other road users.
     * Builds a CAM and uses {@link #sendCam(CAM)}
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

        LocationUpdate locationUpdate = new LocationUpdate.Builder(
                position.getLatitude(),
                position.getLongitude()
        ).altitude((double) altitude)
                .speed((double) speed)
                .build();
        lwm2mLocation.update(locationUpdate);

        // build the CAM
        CAM cam = new CAM.CAMBuilder()
                .header(
                        JsonValue.Origin.SELF.value(),
                        JsonValue.Version.CURRENT_CAM.value(),
                        uuid,
                        TrueTime.getAccurateTime())
                .pduHeader(
                        2,
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
                        new HighFrequencyContainer.HighFrequencyContainerBuilder()
                                .heading((int) (heading * 10))
                                .speed((int) (speed * 100))
                                .longitudinalAcceleration((int) (acceleration * 10))
                                .yawRate((int) (yawRate * 10))
                                .build())
                .build();

        sendCam(cam);
    }

    /**
     * Send a CAM - Cooperative Awareness Message
     *
     * @param cam the CAM representing your current state
     */
    public void sendCam(CAM cam) {
        // build the topic
        String quadkey = QuadTileHelper.latLngToQuadKey(cam.getBasicContainer().getPosition().getLatitudeDegree(),
                cam.getBasicContainer().getPosition().getLongitudeDegree(), 22);
        String geoExtension = QuadTileHelper.quadKeyToQuadTopic(quadkey);
        String topic = context + "/inQueue/v2x/cam/" + uuid + geoExtension;

        // send the message only if the client is connected
        if(isConnected()) ioT3Core.mqttPublish(topic, cam.getJsonCAM().toString());
    }

    /**
     * Inform other road users of a road hazard.
     * Builds a DENM and uses {@link #sendDenm(DENM)}
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
                        JsonValue.Version.CURRENT_DENM.value(),
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
                                        (long)(position.getLongitude() * EtsiUtils.ETSI_COORDINATES_FACTOR),
                                        0),
                                lifetime,
                                stationType.getId()))
                .situationContainer(
                        new SituationContainer(
                                infoQuality,
                                new EventType(
                                        hazardType.getCause(),
                                        hazardType.getSubcause())))
                .build();

        sendDenm(denm);
    }

    /**
     * Send a DENM - Decentralized Environment Notification Message
     *
     * @param denm the DENM representing a road event
     */
    public void sendDenm(DENM denm) {
        // build the topic
        String quadkey = QuadTileHelper.latLngToQuadKey(
                denm.getManagementContainer().getEventPosition().getLatitudeDegree(),
                denm.getManagementContainer().getEventPosition().getLongitudeDegree(),
                22);
        String geoExtension = QuadTileHelper.quadKeyToQuadTopic(quadkey);
        String topic = context + "/inQueue/v2x/denm/" + uuid + geoExtension;

        // send the message even if the client is disconnected, so it will be queued
        if(ioT3Core != null) ioT3Core.mqttPublish(topic, denm.getJsonDENM().toString());
    }

    /**
     * Send a CPM - Cooperative Perception Message
     *
     * @param cpm the CPM representing your sensors and their perceived objects
     */
    public void sendCpm(CPM cpm) {
        // build the topic
        String quadkey = QuadTileHelper.latLngToQuadKey(
                cpm.getManagementContainer().getReferencePosition().getLatitudeDegree(),
                cpm.getManagementContainer().getReferencePosition().getLongitudeDegree(),
                22);
        String geoExtension = QuadTileHelper.quadKeyToQuadTopic(quadkey);
        String topic = context + "/inQueue/v2x/cpm/" + uuid + geoExtension;

        // send the message only if the client is connected
        if(isConnected()) ioT3Core.mqttPublish(topic, cpm.getJson().toString());
    }

    /**
     * Retrieve a read-only list of the Road Users in the vicinity.
     *
     * @return the read-only list of {@link com.orange.iot3mobility.roadobjects.RoadUser} objects
     */
    public static List<RoadUser> getRoadUsers() {
        return RoadUserManager.getRoadUsers();
    }

    /**
     * Retrieve a read-only list of the Road Hazards in the vicinity.
     *
     * @return the read-only list of {@link com.orange.iot3mobility.roadobjects.RoadHazard} objects
     */
    public static List<RoadHazard> getRoadHazards() {
        return RoadHazardManager.getRoadHazards();
    }

    /**
     * Retrieve a read-only list of the Road Sensors in the vicinity.
     *
     * @return the read-only list of {@link com.orange.iot3mobility.roadobjects.RoadSensor} objects
     */
    public static List<RoadSensor> getRoadSensors() {
        return RoadSensorManager.getRoadSensors();
    }

    /**
     * Build an instance of IoT3Mobility.
     */
    public static class IoT3MobilityBuilder {
        private final String uuid;
        private final String context;
        private String mqttHost;
        private int mqttPort;
        private String mqttUsername;
        private String mqttPassword;
        private boolean mqttUseTls;
        private IoT3MobilityCallback ioT3MobilityCallback;
        private String telemetryScheme;
        private String telemetryHost = null; // will remain null if not initialized
        private int telemetryPort;
        private String telemetryEndpoint;
        private String telemetryUsername;
        private String telemetryPassword;
        private Lwm2mConfig lwm2mConfig;
        private Lwm2mDevice lwm2mDevice;
        private Lwm2mInstance[] lwm2mInstances;

        /**
         * Start building an instance of IoT3Mobility.
         *
         * @param uuid unique user identifier
         * @param context specific project or client name
         */
        public IoT3MobilityBuilder(String uuid, String context) {
            this.uuid = uuid;
            this.context = context;
        }

        /**
         * Set the MQTT parameters of your IoT3Mobility instance.
         *
         * @param mqttHost the host or IP address of the MQTT broker
         * @param mqttPort the port of the MQTT broker
         * @param mqttUsername the username for authentication with the MQTT broker
         * @param mqttPassword the password for authentication with the MQTT broker
         * @param mqttUseTls use TLS for a secure connection with the MQTT broker
         */
        public IoT3Mobility.IoT3MobilityBuilder mqttParams(String mqttHost,
                                                   int mqttPort,
                                                   String mqttUsername,
                                                   String mqttPassword,
                                                   boolean mqttUseTls) {
            this.mqttHost = mqttHost;
            this.mqttPort = mqttPort;
            this.mqttUsername = mqttUsername;
            this.mqttPassword = mqttPassword;
            this.mqttUseTls = mqttUseTls;
            return this;
        }

        /**
         * Optional. Set the OpenTelemetry parameters of your IoT3Mobility instance.
         *
         * @param telemetryScheme the scheme of the OpenTelemetry server (e.g. http, https)
         * @param telemetryHost the host or IP address of the OpenTelemetry server, must not be null
         * @param telemetryPort the port of the OpenTelemetry server
         * @param telemetryEndpoint the endpoint of the OpenTelemetry server (e.g. /endpoint/example)
         * @param telemetryUsername the username for authentication with the OpenTelemetry server
         * @param telemetryPassword the password for authentication with the OpenTelemetry server
         */
        public IoT3Mobility.IoT3MobilityBuilder telemetryParams(String telemetryScheme,
                                                                String telemetryHost,
                                                                int telemetryPort,
                                                                String telemetryEndpoint,
                                                                String telemetryUsername,
                                                                String telemetryPassword) {
            if(telemetryHost == null) throw new IllegalArgumentException("telemetryHost cannot be null");
            this.telemetryScheme = telemetryScheme;
            this.telemetryHost = telemetryHost;
            this.telemetryPort = telemetryPort;
            this.telemetryEndpoint = telemetryEndpoint;
            this.telemetryUsername = telemetryUsername;
            this.telemetryPassword = telemetryPassword;
            return this;
        }

        /**
         * Automatically set the MQTT and OpenTelemetry parameters of your IoT3Mobility instance with parameters from
         * the bootstrap configuration.
         * <p>
         * Use instead of {@link #mqttParams(String, int, String, String, boolean)}
         * and {@link #telemetryParams(String, String, int, String, String, String)}.
         *
         * @param bootstrapConfig the bootstrap configuration object you get from the
         * @param enableTelemetry enable telemetry for performance measurements
         * {@link com.orange.iot3core.bootstrap.BootstrapHelper} bootstrap sequence
         */
        public IoT3Mobility.IoT3MobilityBuilder bootstrapConfig(BootstrapConfig bootstrapConfig,
                                                                boolean enableTelemetry) {
            URI mqttUri = bootstrapConfig.getServiceUri(BootstrapConfig.Service.MQTT);
            this.mqttHost = mqttUri.getHost();
            this.mqttPort = mqttUri.getPort();
            this.mqttUsername = bootstrapConfig.getPskRunLogin();
            this.mqttPassword = bootstrapConfig.getPskRunPassword();
            this.mqttUseTls = bootstrapConfig.isServiceSecured(BootstrapConfig.Service.MQTT);
            if(enableTelemetry) {
                URI telemetryUri = bootstrapConfig.getServiceUri(BootstrapConfig.Service.OPEN_TELEMETRY);
                this.telemetryScheme = telemetryUri.getScheme();
                this.telemetryHost = telemetryUri.getHost();
                this.telemetryPort = telemetryUri.getPort();
                this.telemetryEndpoint = telemetryUri.getPath();
                this.telemetryUsername = bootstrapConfig.getPskRunLogin();
                this.telemetryPassword = bootstrapConfig.getPskRunPassword();
            }
            return this;
        }

        /**
         * Set the LwM2M configuration for your IoT3Mobility instance.
         *
         * @param lwm2mConfig An instance of {@link Lwm2mConfig}, containing the endpoint name, server URI,
         *                    security credentials, and optional parameters depending on the configuration type.
         *                    - Use {@link Lwm2mConfig.Lwm2mBootstrapConfig} for bootstrap setup.
         *                    - Use {@link Lwm2mConfig.Lwm2mClassicConfig} for direct PSK setup.
         * @param lwm2mDevice represents the device's details [LwM2M Device (3) object]
         * @param lwm2mInstances represents the instance details of the LwM2M's object
         *                       [an object that conforms to the LwM2M specification]
         * @return The current IoT3CoreBuilder instance with the updated LwM2M configuration.
         * @throws IllegalArgumentException If the provided {@link Lwm2mConfig} is null or incomplete.
         */
        public IoT3Mobility.IoT3MobilityBuilder lwm2mParams(
                Lwm2mConfig lwm2mConfig,
                Lwm2mDevice lwm2mDevice,
                Lwm2mInstance... lwm2mInstances
        ) {
            if (lwm2mConfig == null) {
                throw new IllegalArgumentException("Lwm2mConfig cannot be null.");
            }
            if (lwm2mDevice == null) {
                throw new IllegalArgumentException("Lwm2mDevice cannot be null.");
            }
            this.lwm2mConfig = lwm2mConfig;
            this.lwm2mDevice = lwm2mDevice;
            this.lwm2mInstances = lwm2mInstances;
            return this;
        }

        /**
         * Set the callback of your IoT3Mobility instance.
         *
         * @param ioT3MobilityCallback callback to be notified of mainly MQTT-related events, e.g. message reception
         *                         or connection status
         */
        public IoT3Mobility.IoT3MobilityBuilder callback(IoT3MobilityCallback ioT3MobilityCallback) {
            this.ioT3MobilityCallback = ioT3MobilityCallback;
            return this;
        }

        /**
         * Build the IoT3Mobility instance.
         *
         * @return Iot3Mobility instance
         */
        public IoT3Mobility build() {
            return new IoT3Mobility(
                    uuid,
                    context,
                    mqttHost,
                    mqttPort,
                    mqttUsername,
                    mqttPassword,
                    mqttUseTls,
                    ioT3MobilityCallback,
                    telemetryScheme,
                    telemetryHost,
                    telemetryPort,
                    telemetryEndpoint,
                    telemetryUsername,
                    telemetryPassword,
                    lwm2mConfig,
                    lwm2mDevice,
                    lwm2mInstances
            );
        }
    }

}
