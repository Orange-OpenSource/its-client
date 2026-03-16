package com.orange;

import com.orange.iot3core.clients.lwm2m.model.Lwm2mConfig;
import com.orange.iot3core.clients.lwm2m.model.Lwm2mDevice;
import com.orange.iot3core.clients.lwm2m.model.Lwm2mServer;
import com.orange.iot3mobility.IoT3Mobility;
import com.orange.iot3mobility.IoT3MobilityCallback;
import com.orange.iot3mobility.TrueTime;
import com.orange.iot3mobility.Utils;
import com.orange.iot3mobility.messages.EtsiConverter;
import com.orange.iot3mobility.messages.cam.core.CamCodec;
import com.orange.iot3mobility.messages.cam.core.CamVersion;
import com.orange.iot3mobility.messages.cam.v113.model.CamEnvelope113;
import com.orange.iot3mobility.messages.cam.v230.model.CamEnvelope230;
import com.orange.iot3mobility.messages.cpm.core.CpmCodec;
import com.orange.iot3mobility.messages.cpm.core.CpmVersion;
import com.orange.iot3mobility.messages.cpm.v121.model.CpmEnvelope121;
import com.orange.iot3mobility.messages.cpm.v121.model.CpmMessage121;
import com.orange.iot3mobility.messages.cpm.v121.model.Origin;
import com.orange.iot3mobility.messages.cpm.v121.model.defs.AreaCircular;
import com.orange.iot3mobility.messages.cpm.v121.model.defs.Offset;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.ManagementConfidence;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.ManagementContainer;
import com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer.ReferencePosition;
import com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer.*;
import com.orange.iot3mobility.messages.cpm.v121.model.sensorinformationcontainer.DetectionArea;
import com.orange.iot3mobility.messages.cpm.v121.model.sensorinformationcontainer.SensorInformation;
import com.orange.iot3mobility.messages.cpm.v121.model.sensorinformationcontainer.SensorInformationContainer;
import com.orange.iot3mobility.messages.cpm.v121.model.stationdatacontainer.OriginatingRsuContainer;
import com.orange.iot3mobility.messages.cpm.v121.model.stationdatacontainer.StationDataContainer;
import com.orange.iot3mobility.messages.cpm.v211.model.CpmEnvelope211;
import com.orange.iot3mobility.roadobjects.HazardType;
import com.orange.iot3mobility.its.StationType;
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

import java.io.IOException;
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
            EXAMPLE_LWM2M_SERVER,
            false
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
            public void camArrived(CamCodec.CamFrame<?> camFrame) {
                // if you want to directly process the raw CAM messages
                if(camFrame.version().equals(CamVersion.V1_1_3)) {
                    CamEnvelope113 camEnvelope113 = (CamEnvelope113) camFrame.envelope();
                    System.out.println("Raw CAM v1.1.3: " + camEnvelope113);
                } else if(camFrame.version().equals(CamVersion.V2_3_0)) {
                    CamEnvelope230 camEnvelope230 = (CamEnvelope230) camFrame.envelope();
                    System.out.println("Raw CAM v2.3.0: " + camEnvelope230);
                }
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
            public void cpmArrived(CpmCodec.CpmFrame<?> cpmFrame) {
                if(cpmFrame.version() == CpmVersion.V1_2_1) {
                    CpmEnvelope121 cpm121 = (CpmEnvelope121) cpmFrame.envelope();
                    System.out.println("Raw CPM v1.2.1: " + cpm121);
                } else if(cpmFrame.version() == CpmVersion.V2_1_1) {
                    CpmEnvelope211 cpm211 = (CpmEnvelope211) cpmFrame.envelope();
                    System.out.println("Raw CPM v2.1.1: " + cpm211);
                }
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
        try {
            ioT3Mobility.sendPosition(StationType.PASSENGER_CAR, position, 0, 0, 0, 0, 0, CamVersion.V1_1_3);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void sendTestDenm() {
        LatLng position = new LatLng(48.626059, 2.247904); // planar area of UTAC TEQMO
        ioT3Mobility.sendHazard(HazardType.ACCIDENT_NO_SUBCAUSE, position, 10, 7, StationType.PASSENGER_CAR);
    }

    private static void sendTestCpm() {
        LatLng position = new LatLng(48.625152, 2.240349); // city area of UTAC TEQMO

        PerceivedObject pedestrianPo = PerceivedObject.builder()
                .objectId(15)
                .timeOfMeasurement(0)
                .objectAge(1500)
                .xDistance(-1800 + Utils.randomBetween(-10, 10))
                .yDistance(200 + Utils.randomBetween(-10, 10))
                .xSpeed(0)
                .ySpeed(0)
                .planarObjectDimension1(10)
                .planarObjectDimension2(10)
                .verticalObjectDimension(20)
                .classification(List.of(new ObjectClassification(
                        new ObjectClass(null,
                                new ObjectClassVru(1, null, null, null),
                                null,
                                null),
                        100)))
                .sensorIdList(List.of(123))
                .confidence(
                        PerceivedObjectConfidence.builder()
                                .object(15)
                                .planarObjectDimension1(0)
                                .planarObjectDimension2(0)
                                .xSpeed(0)
                                .ySpeed(0)
                                .build())
                .build();

        PerceivedObject bicyclePo = PerceivedObject.builder()
                .objectId(34)
                .timeOfMeasurement(0)
                .objectAge(1500)
                .xDistance(1500 + Utils.randomBetween(-10, 10))
                .yDistance(100 + Utils.randomBetween(-10, 10))
                .xSpeed(0)
                .ySpeed(0)
                .planarObjectDimension1(20)
                .planarObjectDimension2(20)
                .verticalObjectDimension(15)
                .classification(List.of(new ObjectClassification(
                        new ObjectClass(null,
                                new ObjectClassVru(null, 1, null, null),
                                null,
                                null),
                        100)))
                .sensorIdList(List.of(123))
                .confidence(
                        PerceivedObjectConfidence.builder()
                                .object(15)
                                .planarObjectDimension1(0)
                                .planarObjectDimension2(0)
                                .xSpeed(0)
                                .ySpeed(0)
                                .build())
                .build();

        CpmEnvelope121 cpmEnvelope121 = CpmEnvelope121.builder()
                .origin(Origin.SELF.value)
                .sourceUuid(EXAMPLE_UUID)
                .timestamp(TrueTime.getAccurateTime())
                .message(CpmMessage121.builder()
                        .protocolVersion(2)
                        .stationId(123456)
                        .generationDeltaTime(EtsiConverter.generationDeltaTimeEtsi(TrueTime.getAccurateETSITime()))
                        .managementContainer(ManagementContainer.builder()
                                .stationType(com.orange.iot3mobility.messages.StationType.ROAD_SIDE_UNIT.value)
                                .referencePosition(new ReferencePosition(
                                        EtsiConverter.latitudeEtsi(position.getLatitude()),
                                        EtsiConverter.longitudeEtsi(position.getLongitude()),
                                        0))
                                .confidence(new ManagementConfidence(
                                        new com.orange.iot3mobility.messages.cpm.v121.model.managementcontainer
                                                .PositionConfidenceEllipse(4095, 4095, 3601),
                                        15))
                                .build())
                        .stationDataContainer(StationDataContainer.builder()
                                .originatingRsuContainer(new OriginatingRsuContainer(123, 123, 123))
                                .build())
                        .sensorInformationContainer(new SensorInformationContainer(
                                List.of(new SensorInformation(
                                        123,
                                        4,
                                        DetectionArea.builder()
                                                .stationarySensorCircular(new AreaCircular(
                                                        new Offset(0, 0, 0), 200))
                                                .build()))))
                        .perceivedObjectContainer(new PerceivedObjectContainer(List.of(pedestrianPo, bicyclePo)))
                        .build())
                .build();

        try {
            ioT3Mobility.sendCpm(cpmEnvelope121);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void sendTestConnStat() {
        lwm2mConnectivityStatistics.addTx(8L, true);
        lwm2mConnectivityStatistics.addRx(16L, true);
        lwm2mConnectivityStatistics.update();
    }

}
