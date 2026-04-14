/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.roadobjects;

import com.orange.iot3mobility.managers.IoT3RoadSensorCallback;
import com.orange.iot3mobility.messages.EtsiConverter;
import com.orange.iot3mobility.messages.cpm.core.CpmCodec;
import com.orange.iot3mobility.messages.cpm.core.CpmVersion;
import com.orange.iot3mobility.messages.cpm.v121.model.CpmEnvelope121;
import com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer.ObjectClassification;
import com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer.PerceivedObject;
import com.orange.iot3mobility.messages.cpm.v211.model.CpmEnvelope211;
import com.orange.iot3mobility.quadkey.LatLng;
import com.orange.iot3mobility.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class RoadSensor {

    private static final int LIFETIME = 1500; // 1.5 seconds

    private final String uuid;
    private final ArrayList<SensorObject> sensorObjects;
    private final HashMap<String, SensorObject> sensorObjectMap;
    private LatLng position;
    private long timestamp;
    private long zeroObjectsTimestamp;
    private CpmCodec.CpmFrame<?> cpmFrame;
    private final IoT3RoadSensorCallback ioT3RoadSensorCallback;

    public RoadSensor(String uuid, LatLng position, CpmCodec.CpmFrame<?> cpmFrame, IoT3RoadSensorCallback ioT3RoadSensorCallback) {
        this.uuid = uuid;
        this.position = position;
        this.cpmFrame = cpmFrame;
        this.sensorObjects = new ArrayList<>();
        this.sensorObjectMap = new HashMap<>();
        this.ioT3RoadSensorCallback = ioT3RoadSensorCallback;
        updateTimestamp();
        updateSensorObjects();
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

    public CpmCodec.CpmFrame<?> getCpmFrame() {
        return cpmFrame;
    }

    public void setCpmFrame(CpmCodec.CpmFrame<?> cpmFrame) {
        this.cpmFrame = cpmFrame;
        updateSensorObjects();
    }

    public void updateSensorObjects() {
        if(cpmFrame != null && cpmFrame.version() == CpmVersion.V1_2_1) {
            CpmEnvelope121 cpmEnvelope121 = (CpmEnvelope121) cpmFrame.envelope();
            if(cpmEnvelope121.message().perceivedObjectContainer() == null) return;
            List<PerceivedObject> perceivedObjects = cpmEnvelope121.message().perceivedObjectContainer().perceivedObjects();

            if(!perceivedObjects.isEmpty()) {
                for(PerceivedObject perceivedObject: perceivedObjects) {
                    String objectId = uuid + "_" + perceivedObject.objectId();


                    double xOffsetMeters = EtsiConverter.cpmDistanceMeters(perceivedObject.xDistance());
                    double yOffsetMeters = EtsiConverter.cpmDistanceMeters(perceivedObject.yDistance());

                    LatLng objectPosition = Utils.pointFromPosition(position, 0, yOffsetMeters);
                    objectPosition = Utils.pointFromPosition(objectPosition, (90 + 360) % 360, xOffsetMeters);

                    SensorObjectType objectType = SensorObjectType.UNKNOWN;
                    if(perceivedObject.classification() != null) {
                        for(ObjectClassification classificationItem: perceivedObject.classification()) {
                            if(classificationItem != null && classificationItem.objectClass() != null) {
                                objectType = cpm121ObjectTypeFromObjectClass(classificationItem.objectClass());
                            }
                        }
                    }

                    double objectSpeed = EtsiConverter.cpmDerivedSpeedMetersPerSecond(
                            perceivedObject.xSpeed(),
                            perceivedObject.ySpeed());
                    double objectHeading = EtsiConverter.cpmDerivedHeadingDegrees(
                            perceivedObject.xSpeed(),
                            perceivedObject.ySpeed());
                    int infoQuality = 3;

                    if(perceivedObject.classification() != null
                            && !perceivedObject.classification().isEmpty()) {
                        infoQuality = perceivedObject.classification().get(0).confidence();
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
        } else if (cpmFrame != null && cpmFrame.version() == CpmVersion.V2_1_1) {
            CpmEnvelope211 cpmEnvelope211 = (CpmEnvelope211) cpmFrame.envelope();
            List<com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.PerceivedObject> perceivedObjects = new ArrayList<>();
            if (cpmEnvelope211.message().perceivedObjectContainer() != null) {
                perceivedObjects = cpmEnvelope211.message().perceivedObjectContainer().perceivedObjects();
            }

            if(!perceivedObjects.isEmpty()) {
                for (int index = 0; index < perceivedObjects.size(); index++) {
                    com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.PerceivedObject perceivedObject = perceivedObjects.get(index);
                    Integer perceivedObjectId = perceivedObject.objectId();
                    String objectId = uuid + "_" + (perceivedObjectId != null ? perceivedObjectId : index);

                    if (perceivedObject.position() == null
                            || perceivedObject.position().xCoordinate() == null
                            || perceivedObject.position().yCoordinate() == null) {
                        continue;
                    }

                    int xDistance = perceivedObject.position().xCoordinate().value();
                    int yDistance = perceivedObject.position().yCoordinate().value();
                    double xOffsetMeters = EtsiConverter.cpmDistanceMeters(xDistance);
                    double yOffsetMeters = EtsiConverter.cpmDistanceMeters(yDistance);

                    LatLng objectPosition = Utils.pointFromPosition(position, 0, yOffsetMeters);
                    objectPosition = Utils.pointFromPosition(objectPosition, (90 + 360) % 360, xOffsetMeters);

                    SensorObjectType objectType = SensorObjectType.UNKNOWN;
                    if(perceivedObject.classification() != null) {
                        for(com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.ObjectClassification classificationItem: perceivedObject.classification()) {
                            if(classificationItem != null && classificationItem.objectClass() != null) {
                                objectType = cpm211ObjectTypeFromObjectClass(classificationItem.objectClass());
                            }
                        }
                    }

                    double objectSpeed = 0.0;
                    double objectHeading = 0.0;
                    if (perceivedObject.velocity() != null) {
                        if (perceivedObject.velocity().cartesianVelocity() != null
                                && perceivedObject.velocity().cartesianVelocity().xVelocity() != null
                                && perceivedObject.velocity().cartesianVelocity().yVelocity() != null) {
                            int xSpeed = perceivedObject.velocity().cartesianVelocity().xVelocity().value();
                            int ySpeed = perceivedObject.velocity().cartesianVelocity().yVelocity().value();
                            objectSpeed = EtsiConverter.cpmDerivedSpeedMetersPerSecond(xSpeed, ySpeed);
                            objectHeading = EtsiConverter.cpmDerivedHeadingDegrees(xSpeed, ySpeed);
                        } else if (perceivedObject.velocity().polarVelocity() != null) {
                            if (perceivedObject.velocity().polarVelocity().velocityMagnitude() != null) {
                                objectSpeed = EtsiConverter.cpmSpeedMetersPerSecond(
                                        perceivedObject.velocity().polarVelocity().velocityMagnitude().value());
                            }
                            if (perceivedObject.velocity().polarVelocity().velocityDirection() != null) {
                                objectHeading = EtsiConverter.cpmAngleDegrees(
                                        perceivedObject.velocity().polarVelocity().velocityDirection().value());
                                if (Double.isNaN(objectHeading)) {
                                    objectHeading = 0.0;
                                }
                            }
                        }
                    }

                    int infoQuality = 3;
                    if(perceivedObject.classification() != null
                            && !perceivedObject.classification().isEmpty()) {
                        infoQuality = perceivedObject.classification().get(0).confidence();
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

    /* --------------------------------------------------------------------- */
    /* Helper methods                                                        */
    /* --------------------------------------------------------------------- */

    private SensorObjectType cpm121ObjectTypeFromObjectClass(
            com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer.ObjectClass objectClass) {
        if(objectClass.vehicle() != null) {
            return switch (objectClass.vehicle()) {
                case 1 -> SensorObjectType.PASSENGER_CAR;
                case 2 -> SensorObjectType.BUS;
                case 3 -> SensorObjectType.LIGHT_TRUCK;
                case 4 -> SensorObjectType.HEAVY_TRUCK;
                case 5 -> SensorObjectType.TRAILER;
                case 6, 8 -> SensorObjectType.SPECIAL_VEHICLES;
                case 7 -> SensorObjectType.TRAM;
                default -> SensorObjectType.UNKNOWN;
            };
        } else if(objectClass.singleVru() != null) {
            if(objectClass.singleVru().pedestrian() != null)
                return SensorObjectType.PEDESTRIAN;
            else if(objectClass.singleVru().bicyclist() != null)
                return SensorObjectType.CYCLIST;
            else if(objectClass.singleVru().motorcylist() != null)
                return SensorObjectType.MOTORCYCLE;
            else if(objectClass.singleVru().animal() != null)
                return SensorObjectType.ANIMAL;
            else return SensorObjectType.UNKNOWN;
        } else if(objectClass.vruGroup() != null) {
            if(objectClass.vruGroup().groupType().pedestrian())
                return SensorObjectType.PEDESTRIAN_GROUP;
            else if(objectClass.vruGroup().groupType().bicyclist())
                return SensorObjectType.CYCLIST_GROUP;
            else if(objectClass.vruGroup().groupType().motorcyclist())
                return SensorObjectType.MOTORCYCLE_GROUP;
            else if(objectClass.vruGroup().groupType().animal())
                return SensorObjectType.ANIMAL_GROUP;
            else return SensorObjectType.UNKNOWN;
        }
        return SensorObjectType.UNKNOWN;
    }

    private SensorObjectType cpm211ObjectTypeFromObjectClass(
            com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer.ObjectClass objectClass) {
        if(objectClass.vehicle() != null) {
            return switch (objectClass.vehicle()) {
                case 1 -> SensorObjectType.PASSENGER_CAR;
                case 2 -> SensorObjectType.BUS;
                case 3 -> SensorObjectType.LIGHT_TRUCK;
                case 4 -> SensorObjectType.HEAVY_TRUCK;
                case 5 -> SensorObjectType.TRAILER;
                case 6, 8 -> SensorObjectType.SPECIAL_VEHICLES;
                case 7 -> SensorObjectType.TRAM;
                default -> SensorObjectType.UNKNOWN;
            };
        } else if(objectClass.vru() != null) {
            if(objectClass.vru().pedestrian() != null)
                return SensorObjectType.PEDESTRIAN;
            else if(objectClass.vru().bicyclistAndLightVruVehicle() != null)
                return SensorObjectType.CYCLIST;
            else if(objectClass.vru().motorcylist() != null)
                return SensorObjectType.MOTORCYCLE;
            else if(objectClass.vru().animal() != null)
                return SensorObjectType.ANIMAL;
            else return SensorObjectType.UNKNOWN;
        } else if(objectClass.group() != null) {
            if(objectClass.group().clusterProfiles().pedestrian())
                return SensorObjectType.PEDESTRIAN_GROUP;
            else if(objectClass.group().clusterProfiles().bicyclist())
                return SensorObjectType.CYCLIST_GROUP;
            else if(objectClass.group().clusterProfiles().motorcyclist())
                return SensorObjectType.MOTORCYCLE_GROUP;
            else if(objectClass.group().clusterProfiles().animal())
                return SensorObjectType.ANIMAL_GROUP;
            else return SensorObjectType.UNKNOWN;
        }
        return SensorObjectType.UNKNOWN;
    }

}
