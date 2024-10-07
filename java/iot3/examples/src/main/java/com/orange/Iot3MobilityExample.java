package com.orange;

import com.orange.iot3mobility.IoT3Mobility;
import com.orange.iot3mobility.IoT3MobilityCallback;
import com.orange.iot3mobility.its.HazardType;
import com.orange.iot3mobility.its.StationType;
import com.orange.iot3mobility.its.json.cam.CAM;
import com.orange.iot3mobility.its.json.cpm.CPM;
import com.orange.iot3mobility.its.json.denm.DENM;
import com.orange.iot3mobility.managers.IoT3RoadHazardCallback;
import com.orange.iot3mobility.managers.IoT3RoadSensorCallback;
import com.orange.iot3mobility.managers.IoT3RoadUserCallback;
import com.orange.iot3mobility.quadkey.LatLng;
import com.orange.iot3mobility.roadobjects.RoadHazard;
import com.orange.iot3mobility.roadobjects.RoadSensor;
import com.orange.iot3mobility.roadobjects.RoadUser;
import com.orange.iot3mobility.roadobjects.SensorObject;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Iot3MobilityExample {

    private static final String EXAMPLE_HOST = "host";
    private static final String EXAMPLE_USERNAME = "username";
    private static final String EXAMPLE_PASSWORD = "password";
    private static final String EXAMPLE_UUID = "uuid";
    private static final String EXAMPLE_CONTEXT = "context";
    private static final String EXAMPLE_TELEMETRY_HOST = "telemetry_host";

    private static IoT3Mobility ioT3Mobility;

    public static void main(String[] args) {
        // instantiate IoT3Mobility and its callback
        ioT3Mobility = new IoT3Mobility(
                EXAMPLE_HOST,
                EXAMPLE_USERNAME,
                EXAMPLE_PASSWORD,
                EXAMPLE_UUID, // serves to identify the road user, app or infrastructure
                EXAMPLE_CONTEXT, // serves as the root of the MQTT mobility topics
                new IoT3MobilityCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        System.out.println("MQTT Connection lost...");
                    }

                    @Override
                    public void connectComplete(boolean reconnect, String serverURI) {
                        System.out.println("MQTT connection complete: " + serverURI);
                    }
                },
                EXAMPLE_TELEMETRY_HOST);

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
                System.out.println("Road User position: " + position.toString());
                // the CAM on which this object is based can still be accessed
                CAM originalCam = roadUser.getCam();
                double latitude = originalCam.getBasicContainer().getPosition().getLatitudeDegree();
                double longitude = originalCam.getBasicContainer().getPosition().getLongitudeDegree();
                LatLng camPosition = new LatLng(latitude, longitude);
                System.out.println("CAM position: " + camPosition.toString());
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
        ioT3Mobility.setRoadUserRoI(roiPosition, 17, true);
        ioT3Mobility.setRoadHazardRoI(roiPosition, 16, true);
        ioT3Mobility.setRoadSensorRoI(roiPosition, 18, true);
    }

    private static synchronized void startSendingMessages() {
        ScheduledExecutorService messageScheduler = Executors.newScheduledThreadPool(1);
        messageScheduler.scheduleWithFixedDelay(Iot3MobilityExample::sendTestCam, 1, 1, TimeUnit.SECONDS);
        messageScheduler.scheduleWithFixedDelay(Iot3MobilityExample::sendTestDenm, 1, 10, TimeUnit.SECONDS);
    }

    private static void sendTestCam() {
        LatLng position = new LatLng(48.625218, 2.243448); // center point of UTAC TEQMO
        ioT3Mobility.sendPosition(StationType.PASSENGER_CAR, position, 0, 0, 0, 0, 0);
    }

    private static void sendTestDenm() {
        LatLng position = new LatLng(48.625261, 2.243713); // Slightly east of UTAC TEQMO center point
        ioT3Mobility.sendHazard(HazardType.ACCIDENT_NO_SUBCAUSE, position, 5, 7, StationType.PASSENGER_CAR);
    }

}
