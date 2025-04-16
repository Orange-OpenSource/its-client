package com.orange;

import com.orange.iot3core.clients.lwm2m.model.Lwm2mConfig;
import com.orange.iot3core.clients.lwm2m.model.Lwm2mDevice;
import com.orange.iot3core.clients.lwm2m.model.Lwm2mServer;
import com.orange.iot3mobility.IoT3Mobility;
import com.orange.iot3mobility.IoT3MobilityCallback;
import com.orange.iot3mobility.TrueTime;
import com.orange.iot3mobility.Utils;
import com.orange.iot3mobility.its.EtsiUtils;
import com.orange.iot3mobility.its.HazardType;
import com.orange.iot3mobility.its.StationType;
import com.orange.iot3mobility.its.json.JsonValue;
import com.orange.iot3mobility.its.json.Position;
import com.orange.iot3mobility.its.json.PositionConfidence;
import com.orange.iot3mobility.its.json.PositionConfidenceEllipse;
import com.orange.iot3mobility.its.json.cam.CAM;
import com.orange.iot3mobility.its.json.cpm.*;
import com.orange.iot3mobility.its.json.denm.DENM;
import com.orange.iot3mobility.managers.IoT3RoadHazardCallback;
import com.orange.iot3mobility.managers.IoT3RoadSensorCallback;
import com.orange.iot3mobility.managers.IoT3RoadUserCallback;
import com.orange.iot3mobility.quadkey.LatLng;
import com.orange.iot3mobility.roadobjects.RoadHazard;
import com.orange.iot3mobility.roadobjects.RoadSensor;
import com.orange.iot3mobility.roadobjects.RoadUser;
import com.orange.iot3mobility.roadobjects.SensorObject;
import com.orange.lwm2m.model.CustomLwm2mConnectivityStatisticsExample;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Iot3MobilityExample {

    private static final String EXAMPLE_UUID = "uuid";
    private static final String EXAMPLE_CONTEXT = "context";
    // MQTT parameters
    private static final String EXAMPLE_MQTT_HOST = "mqtt_host";
    private static final int EXAMPLE_MQTT_PORT = 1883;
    private static final String EXAMPLE_MQTT_USERNAME = "mqtt_username";
    private static final String EXAMPLE_MQTT_PASSWORD = "mqtt_password";
    private static final boolean EXAMPLE_MQTT_USE_TLS = false;
    // OpenTelemetry parameters
    private static final String EXAMPLE_OTL_SCHEME = "http";
    private static final String EXAMPLE_OTL_HOST = "telemetry_host";
    private static final int EXAMPLE_OTL_PORT = 4318;
    private static final String EXAMPLE_OTL_ENDPOINT = "/telemetry/endpoint";
    private static final String EXAMPLE_OTL_USERNAME = "telemetry_username";
    private static final String EXAMPLE_OTL_PASSWORD = "telemetry_password";

    private static final int EXAMPLE_SHORT_SERVER_ID = 12345;
    private static final Lwm2mDevice EXAMPLE_LWM2M_DEVICE = new Lwm2mDevice(
            "device_manufacturer",
            "model_number",
            "serial_number",
            "U"
    );
    private static final Lwm2mServer EXAMPLE_LWM2M_SERVER = new Lwm2mServer(
            EXAMPLE_SHORT_SERVER_ID,
            5 * 60,
            "U"
    );
    private static final Lwm2mConfig EXAMPLE_LWM2M_CONFIG = new Lwm2mConfig.Lwm2mClassicConfig(
            "your_endpoint_name",
            "coaps://lwm2m.liveobjects.orange-business.com:5684",
            "your_psk_id",
            "your_private_key_in_hex",
            EXAMPLE_SHORT_SERVER_ID,
            EXAMPLE_LWM2M_SERVER
    );
    private static IoT3Mobility ioT3Mobility;
    private static final CustomLwm2mConnectivityStatisticsExample lwm2mConnectivityStatistics =
            new CustomLwm2mConnectivityStatisticsExample();

    public static void main(String[] args) {
        // instantiate IoT3Mobility and its callback
        ioT3Mobility = new IoT3Mobility.IoT3MobilityBuilder(EXAMPLE_UUID, EXAMPLE_CONTEXT)
                .mqttParams(EXAMPLE_MQTT_HOST,
                        EXAMPLE_MQTT_PORT,
                        EXAMPLE_MQTT_USERNAME,
                        EXAMPLE_MQTT_PASSWORD,
                        EXAMPLE_MQTT_USE_TLS)
                .telemetryParams(EXAMPLE_OTL_SCHEME,
                        EXAMPLE_OTL_HOST,
                        EXAMPLE_OTL_PORT,
                        EXAMPLE_OTL_ENDPOINT,
                        EXAMPLE_OTL_USERNAME,
                        EXAMPLE_OTL_PASSWORD)
                .lwm2mParams(
                        EXAMPLE_LWM2M_CONFIG,
                        EXAMPLE_LWM2M_DEVICE,
                        lwm2mConnectivityStatistics
                )
                .callback(new IoT3MobilityCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        System.out.println("MQTT Connection lost...");
                    }

                    @Override
                    public void connectComplete(boolean reconnect, String serverURI) {
                        System.out.println("MQTT connection complete: " + serverURI);
                    }
                })
                .build();

        // set the RoadHazardCallback to be informed of road hazards in the corresponding Region of Interest (RoI)
        ioT3Mobility.setRoadHazardCallback(new IoT3RoadHazardCallback() {
            @Override
            public void newRoadHazard(RoadHazard roadHazard) {
                System.out.println("New Road Hazard: " + roadHazard.getUuid());
            }

            @Override
            public void roadHazardUpdate(RoadHazard roadHazard) {
                System.out.println("Road Hazard update: " + roadHazard.getUuid());
            }

            @Override
            public void roadHazardExpired(RoadHazard roadHazard) {
                System.out.println("Road Hazard has expired: " + roadHazard.getUuid());
            }

            @Override
            public void denmArrived(DENM denm) {
                System.out.println("DENM received: " + denm.getJsonDENM());
            }
        });

        // set the RoadUserCallback to be informed of road users in the corresponding Region of Interest (RoI)
        ioT3Mobility.setRoadUserCallback(new IoT3RoadUserCallback() {
            @Override
            public void newRoadUser(RoadUser roadUser) {
                // RoadUser is a simple object provided by IoT3Mobility
                System.out.println("New Road User: " + roadUser.getUuid());
                LatLng position = roadUser.getPosition();
                System.out.println("Road User position: " + position);
                // the CAM on which this object is based can still be accessed
                CAM originalCam = roadUser.getCam();
                double latitude = originalCam.getBasicContainer().getPosition().getLatitudeDegree();
                double longitude = originalCam.getBasicContainer().getPosition().getLongitudeDegree();
                LatLng camPosition = new LatLng(latitude, longitude);
                System.out.println("CAM position: " + camPosition);
            }

            @Override
            public void roadUserUpdate(RoadUser roadUser) {
                System.out.println("Road User update: " + roadUser.getUuid()
                        + " | Position: " + roadUser.getPosition()
                        + " | Speed: " + roadUser.getSpeedKmh() + " km/h");
            }

            @Override
            public void roadUserExpired(RoadUser roadUser) {
                System.out.println("Road User has expired: " + roadUser.getUuid());
            }

            @Override
            public void camArrived(CAM cam) {
                // if you want to directly process the raw CAM messages
                System.out.println("CAM received: " + cam.getJsonCAM());
            }
        });

        // set the RoadSensorCallback to be informed of road sensors (onboard and roadside)
        // and the objects they detect in the corresponding Region of Interest (RoI)
        ioT3Mobility.setRoadSensorCallback(new IoT3RoadSensorCallback() {
            @Override
            public void newRoadSensor(RoadSensor roadSensor) {
                System.out.println("New Road Sensor: " + roadSensor.getUuid());
            }

            @Override
            public void roadSensorUpdate(RoadSensor roadSensor) {
                System.out.println("Road Sensor update: " + roadSensor.getUuid());
            }

            @Override
            public void roadSensorExpired(RoadSensor roadSensor) {
                System.out.println("Road Sensor has expired: " + roadSensor.getUuid());
            }

            @Override
            public void newSensorObject(SensorObject sensorObject) {
                System.out.println("New Sensor object: " + sensorObject.getUuid());
            }

            @Override
            public void sensorObjectUpdate(SensorObject sensorObject) {
                System.out.println("Sensor Object update: " + sensorObject.getUuid());
            }

            @Override
            public void sensorObjectExpired(SensorObject sensorObject) {
                System.out.println("Sensor Object has expired: " + sensorObject.getUuid());
            }

            @Override
            public void cpmArrived(CPM cpm) {
                System.out.println("CPM received: " + cpm.getJson());
            }
        });

        // set the RawMessageCallback to be informed of any message being received by the SDK, before treatment
        // this callback is intended for users who prefer to process messages themselves
        ioT3Mobility.setRawMessageCallback(message -> System.out.println("Raw message received: " + message));

        // let's set a Region of Interest for each object type (IoT3Mobility will handle the subscriptions)
        LatLng roiPosition = new LatLng(48.625218, 2.243448); // UTAC TEQMO test track coordinates
        setRegionOfInterest(roiPosition);

        startSendingMessages();
    }

    private static void setRegionOfInterest(LatLng roiPosition) {
        // the coordinates are translated into tiles of varying sizes, depending on the chosen zoom level:
        // zoom 22 ~ 5m x 5m at Paris latitude (max resolution)
        // zoom 1 is a quarter of the world per tile (min resolution)
        // tile area x4 with each level decrease
        ioT3Mobility.setRoadUserRoI(roiPosition, 16, true);
        ioT3Mobility.setRoadHazardRoI(roiPosition, 16, true);
        ioT3Mobility.setRoadSensorRoI(roiPosition, 16, true);
    }

    private static synchronized void startSendingMessages() {
        ScheduledExecutorService messageScheduler = Executors.newScheduledThreadPool(1);
        messageScheduler.scheduleWithFixedDelay(Iot3MobilityExample::sendTestCam, 1, 1, TimeUnit.SECONDS);
        messageScheduler.scheduleWithFixedDelay(Iot3MobilityExample::sendTestDenm, 1, 10, TimeUnit.SECONDS);
        messageScheduler.scheduleWithFixedDelay(Iot3MobilityExample::sendTestCpm, 1, 1, TimeUnit.SECONDS);
        messageScheduler.scheduleWithFixedDelay(Iot3MobilityExample::sendTestConnStat, 1, 1, TimeUnit.SECONDS);
    }

    private static void sendTestCam() {
        LatLng position = new LatLng(48.625218, 2.243448); // center point of UTAC TEQMO
        ioT3Mobility.sendPosition(StationType.PASSENGER_CAR, position, 0, 0, 0, 0, 0);
    }

    private static void sendTestDenm() {
        LatLng position = new LatLng(48.626059, 2.247904); // planar area of UTAC TEQMO
        ioT3Mobility.sendHazard(HazardType.ACCIDENT_NO_SUBCAUSE, position, 10, 7, StationType.PASSENGER_CAR);
    }

    private static void sendTestCpm() {
        LatLng position = new LatLng(48.625152, 2.240349); // city area of UTAC TEQMO

        PerceivedObject pedestrianPo = new PerceivedObject.PerceivedObjectBuilder(
                12, 0, 1500)
                .distance(-1800 + Utils.randomBetween(-10, 10),
                        200 + Utils.randomBetween(-10, 10))
                .speed(0, 0)
                .objectDimension(10, 10, 20, 0)
                .classification(List.of(new ClassificationItem(
                        new ObjectClassSingleVru(
                                new ObjectVruPedestrian(1)),
                        100)))
                .sensorIdList(List.of(123))
                .confidence(
                        new PerceivedObjectConfidence.PerceivedObjectConfidenceBuilder(15)
                                .distance(0, 0)
                                .speed(0, 0)
                                .build())
                .build();

        PerceivedObject bicyclePo = new PerceivedObject.PerceivedObjectBuilder(
                34, 0, 1500)
                .distance(1500 + Utils.randomBetween(-10, 10),
                        100 + Utils.randomBetween(-10, 10))
                .speed(0, 0)
                .objectDimension(20, 20, 15, 0)
                .classification(List.of(new ClassificationItem(
                        new ObjectClassSingleVru(
                                new ObjectVruBicyclist(1)),
                        100)))
                .sensorIdList(List.of(123))
                .confidence(
                        new PerceivedObjectConfidence.PerceivedObjectConfidenceBuilder(15)
                                .distance(0, 0)
                                .speed(0, 0)
                                .build())
                .build();

        CPM cpm = new CPM.CPMBuilder()
                .header(JsonValue.Origin.SELF.value(),
                        JsonValue.Version.CURRENT_CPM.value(),
                        EXAMPLE_UUID,
                        TrueTime.getAccurateTime())
                .pduHeader(2,
                        123456,
                        (int) (TrueTime.getAccurateETSITime() % 65536))
                .managementContainer(
                        new ManagementContainer(
                                StationType.ROAD_SIDE_UNIT.getId(),
                                new Position(
                                        (long) (position.getLatitude() * EtsiUtils.ETSI_COORDINATES_FACTOR),
                                        (long) (position.getLongitude() * EtsiUtils.ETSI_COORDINATES_FACTOR),
                                        0),
                                new PositionConfidence(
                                        new PositionConfidenceEllipse(0, 0, 0),
                                        0)))
                .stationDataContainer(
                        new StationDataContainer(
                                new OriginatingRsuContainer(
                                        123, 123, 123)))
                .sensorInformationContainer(
                        new SensorInformationContainer(
                                List.of(new SensorInformation(123, 4,
                                        new DetectionArea(
                                                new StationarySensorCircular(
                                                        new Offset(0, 0, 0),
                                                        200))))))
                .perceivedObjectContainer(
                        new PerceivedObjectContainer(
                                List.of(pedestrianPo,
                                        bicyclePo)))
                .build();

        ioT3Mobility.sendCpm(cpm);
    }

    private static void sendTestConnStat() {
        lwm2mConnectivityStatistics.addTx(8L, true);
        lwm2mConnectivityStatistics.addRx(16L, true);
        lwm2mConnectivityStatistics.update();
    }

}
