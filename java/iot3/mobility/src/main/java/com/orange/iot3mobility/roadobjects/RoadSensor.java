package com.orange.iot3mobility.roadobjects;

import com.orange.iot3mobility.its.json.cpm.CPM;
import com.orange.iot3mobility.its.json.cpm.ClassificationItem;
import com.orange.iot3mobility.its.json.cpm.PerceivedObject;
import com.orange.iot3mobility.managers.IoT3RoadSensorCallback;
import com.orange.iot3mobility.quadkey.LatLng;
import com.orange.iot3mobility.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class RoadSensor {

    private static final int LIFETIME = 1500; // 1.5 seconds

    private final String uuid;
    private final ArrayList<SensorObject> sensorObjects;
    private final HashMap<String, SensorObject> sensorObjectMap;
    private LatLng position;
    private long timestamp;
    private long zeroObjectsTimestamp;
    private CPM cpm;
    private final IoT3RoadSensorCallback ioT3RoadSensorCallback;

    public RoadSensor(String uuid, LatLng position, CPM cpm, IoT3RoadSensorCallback ioT3RoadSensorCallback) {
        this.uuid = uuid;
        this.position = position;
        this.cpm = cpm;
        this.sensorObjects = new ArrayList<>();
        this.sensorObjectMap = new HashMap<>();
        this.ioT3RoadSensorCallback = ioT3RoadSensorCallback;
        updateTimestamp();
    }

    public String getUuid() {
        return uuid;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public void setCpm(CPM cpm) {
        this.cpm = cpm;
    }

    public CPM getCpm() {
        return cpm;
    }

    public void updateSensorObjects(ArrayList<PerceivedObject> perceivedObjects) {
        if(!perceivedObjects.isEmpty()) {
            for(PerceivedObject perceivedObject: perceivedObjects) {
                String objectId = uuid + "_" + perceivedObject.getObjectId();

                float xOffsetMeters = perceivedObject.getDistanceX() / 100f;
                float yOffsetMeters = perceivedObject.getDistanceY() / 100f;

                LatLng objectPosition = Utils.pointFromPosition(position, 0, yOffsetMeters);
                objectPosition = Utils.pointFromPosition(objectPosition, (90 + 360) % 360, xOffsetMeters);

                int objectType = -1;
                if(perceivedObject.getClassification() != null) {
                    for(ClassificationItem classificationItem: perceivedObject.getClassification()) {
                        if(classificationItem != null) {
                            objectType = classificationItem.getObjectStationType();
                        }
                    }
                }

                float objectSpeed = perceivedObject.getSpeedMs();
                float objectHeading = perceivedObject.getHeadingDegree();
                int infoQuality = 3;

                if(perceivedObject.getClassification() != null
                        && !perceivedObject.getClassification().isEmpty()) {
                    infoQuality = perceivedObject.getClassification().get(0).getConfidence();
                }

                SensorObject sensorObject;
                if(sensorObjectMap.containsKey(objectId)) {
                    // update existing SensorObject
                    synchronized (sensorObjectMap) {
                        sensorObject = sensorObjectMap.get(objectId);
                        if(sensorObject != null) {
                            sensorObject.updateTimestamp();
                            sensorObject.setPosition(objectPosition);
                            sensorObject.setType(objectType);
                            sensorObject.setSpeed(objectSpeed);
                            sensorObject.setHeading(objectHeading);
                            sensorObject.setInfoQuality(infoQuality);
                            ioT3RoadSensorCallback.sensorObjectUpdate(sensorObject);
                        }
                    }
                } else {
                    // create new SensorObject
                    sensorObject = new SensorObject(objectId, objectType, objectPosition, objectSpeed, objectHeading, infoQuality);
                    synchronized (sensorObjects) {
                        sensorObjects.add(sensorObject);
                    }
                    synchronized (sensorObjectMap) {
                        sensorObjectMap.put(objectId, sensorObject);
                    }
                    ioT3RoadSensorCallback.newSensorObject(sensorObject);
                }

                if(perceivedObjects.size() != sensorObjects.size()
                        && !sensorObjects.isEmpty()) {
                    // remove objects that have not been tracked / detected again
                    checkAndRemoveExpiredObjects(false);
                }
                zeroObjectsTimestamp = 0;
            }
        } else { // clear all objects if nothing is received for 1.5 seconds
            if(zeroObjectsTimestamp == 0) zeroObjectsTimestamp = System.currentTimeMillis();
            else if(System.currentTimeMillis() - zeroObjectsTimestamp > LIFETIME) checkAndRemoveExpiredObjects(true);
        }
    }

    private void checkAndRemoveExpiredObjects(boolean forceDelete) {
        synchronized (sensorObjects) {
            Iterator<SensorObject> iterator = sensorObjects.iterator();
            while (iterator.hasNext()) {
                SensorObject sensorObject = iterator.next();
                if (!sensorObject.stillLiving() || forceDelete) {
                    iterator.remove();
                    synchronized (sensorObjectMap) {
                        // Remove by value
                        sensorObjectMap.values().remove(sensorObject);
                    }
                    ioT3RoadSensorCallback.sensorObjectExpired(sensorObject);
                }
            }
        }
    }

    public ArrayList<SensorObject> getSensorObjects() {
        return sensorObjects;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void updateTimestamp() {
        this.timestamp = System.currentTimeMillis();
    }

    public boolean stillLiving() {
        return System.currentTimeMillis() - timestamp < LIFETIME;
    }

}
