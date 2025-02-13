package com.orange;

import com.orange.iot3core.bootstrap.BootstrapCallback;
import com.orange.iot3core.bootstrap.BootstrapConfig;
import com.orange.iot3core.bootstrap.BootstrapHelper;
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

import java.net.URI;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Iot3MobilityBootstrapExample {

    private static final String EXAMPLE_UUID = "uuid";
    private static final String EXAMPLE_CONTEXT = "context";
    // Bootstrap parameters
    private static final String BOOTSTRAP_ID = "bootstrap_id";
    private static final String BOOTSTRAP_LOGIN = "boostrap_login";
    private static final String BOOTSTRAP_PASSWORD = "bootstrap_password";
    private static final BootstrapHelper.Role BOOTSTRAP_ROLE = BootstrapHelper.Role.EXTERNAL_APP;
    private static final String BOOTSTRAP_URI = "bootstrap.uri.com";
    private static final boolean ENABLE_TELEMETRY = true;

    private static IoT3Mobility ioT3Mobility;

    public static void main(String[] args) {
        // bootstrap sequence
        BootstrapHelper.bootstrap(BOOTSTRAP_ID,
                BOOTSTRAP_LOGIN,
                BOOTSTRAP_PASSWORD,
                BOOTSTRAP_ROLE,
                BOOTSTRAP_URI,
                new BootstrapCallback() {
                    @Override
                    public void boostrapSuccess(BootstrapConfig bootstrapConfig) {
                        System.out.println("Bootstrap success");
                        System.out.println("IoT3 ID: " + bootstrapConfig.getIot3Id());
                        System.out.println("LOGIN: " + bootstrapConfig.getPskRunLogin());
                        System.out.println("PASSWORD: " + bootstrapConfig.getPskRunPassword());

                        URI mqttUri = bootstrapConfig.getServiceUri(BootstrapConfig.Service.MQTT);
                        URI telemetryUri = bootstrapConfig.getServiceUri(BootstrapConfig.Service.OPEN_TELEMETRY);
                        System.out.println("MQTT URI: " + mqttUri);
                        System.out.println("TELEMETRY URI: " + telemetryUri);

                        // init IoT3Mobility with boostrap config
                        initIoT3Mobility(bootstrapConfig);
                    }

                    @Override
                    public void boostrapError(Throwable bootstrapError) {
                        System.out.println("Bootstrap error: " + bootstrapError);
                    }
                });
    }

    private static void initIoT3Mobility(BootstrapConfig bootstrapConfig) {
        // instantiate IoT3Mobility and its callback
        ioT3Mobility = new IoT3Mobility.IoT3MobilityBuilder(EXAMPLE_UUID, EXAMPLE_CONTEXT)
                .bootstrapConfig(bootstrapConfig, ENABLE_TELEMETRY)
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
        messageScheduler.scheduleWithFixedDelay(Iot3MobilityBootstrapExample::sendTestCam, 1, 1, TimeUnit.SECONDS);
        messageScheduler.scheduleWithFixedDelay(Iot3MobilityBootstrapExample::sendTestDenm, 1, 10, TimeUnit.SECONDS);
        messageScheduler.scheduleWithFixedDelay(Iot3MobilityBootstrapExample::sendTestCpm, 1, 1, TimeUnit.SECONDS);
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
                        JsonValue.Version.CURRENT.value(),
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
                                        (long) (position.getLongitude() * EtsiUtils.ETSI_COORDINATES_FACTOR)),
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

}
