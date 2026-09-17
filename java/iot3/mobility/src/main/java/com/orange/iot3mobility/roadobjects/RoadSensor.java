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
import com.orange.iot3mobility.messages.cpm.v121.model.defs.AreaCircular;
import com.orange.iot3mobility.messages.cpm.v121.model.defs.AreaEllipse;
import com.orange.iot3mobility.messages.cpm.v121.model.defs.AreaPolygon;
import com.orange.iot3mobility.messages.cpm.v121.model.defs.AreaRectangle;
import com.orange.iot3mobility.messages.cpm.v121.model.defs.Offset;
import com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer.ObjectClass;
import com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer.ObjectClassification;
import com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer.PerceivedObject;
import com.orange.iot3mobility.messages.cpm.v121.model.sensorinformationcontainer.DetectionArea;
import com.orange.iot3mobility.messages.cpm.v121.model.sensorinformationcontainer.SensorInformation;
import com.orange.iot3mobility.messages.cpm.v121.model.sensorinformationcontainer.StationarySensorRadial;
import com.orange.iot3mobility.messages.cpm.v121.model.sensorinformationcontainer.VehicleSensor;
import com.orange.iot3mobility.messages.cpm.v121.model.sensorinformationcontainer.VehicleSensorProperty;
import com.orange.iot3mobility.messages.cpm.v211.model.CpmEnvelope211;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.CartesianPosition3d;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.Circular;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.Elliptical;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.Polygonal;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.Radial;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.RadialShapes;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.Rectangular;
import com.orange.iot3mobility.messages.cpm.v211.model.defs.Shape;
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
    private final HashMap<Integer, SensorCoverage> sensorCoverageMap;
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
        this.sensorCoverageMap = new HashMap<>();
        this.ioT3RoadSensorCallback = ioT3RoadSensorCallback;
        updateTimestamp();
        updateSensorObjects();
        updateSensorCoverages();
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
        updateSensorCoverages();
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

                    Double objectLength = null, objectWidth = null, objectOrientation = null;
                    if(perceivedObject.planarObjectDimension1() != null
                            && perceivedObject.planarObjectDimension2() != null) {
                        objectLength = EtsiConverter.cpmObjectDimensionMeters(perceivedObject.planarObjectDimension1());
                        objectWidth = EtsiConverter.cpmObjectDimensionMeters(perceivedObject.planarObjectDimension2());
                    }
                    if(perceivedObject.yawAngle() != null) {
                        objectOrientation = EtsiConverter.cpmAngleDegrees(perceivedObject.yawAngle());
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
                                sensorObject.setBearing(objectHeading);
                                sensorObject.setInfoQuality(infoQuality);
                                sensorObject.setDimensions(objectLength, objectWidth);
                                sensorObject.setOrientation(objectOrientation);
                                ioT3RoadSensorCallback.sensorObjectUpdate(sensorObject);
                            }
                        }
                    } else {
                        // create new SensorObject
                        sensorObject = new SensorObject(objectId, objectType, objectPosition, objectSpeed,
                                objectHeading, infoQuality, objectLength, objectWidth, objectOrientation);
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

                    Double objectLength = null, objectWidth = null, objectOrientation = null;
                    if(perceivedObject.objectDimensionX() != null && perceivedObject.objectDimensionY() != null) {
                        objectLength = EtsiConverter.cpmObjectDimensionMeters(perceivedObject.objectDimensionX().value());
                        objectWidth = EtsiConverter.cpmObjectDimensionMeters(perceivedObject.objectDimensionY().value());
                    }
                    if(perceivedObject.angles() != null && perceivedObject.angles().zAngle() != null) {
                        objectOrientation = EtsiConverter.cpmAngleDegrees(perceivedObject.angles().zAngle().value());
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
                                sensorObject.setBearing(objectHeading);
                                sensorObject.setInfoQuality(infoQuality);
                                sensorObject.setDimensions(objectLength, objectWidth);
                                sensorObject.setOrientation(objectOrientation);
                                ioT3RoadSensorCallback.sensorObjectUpdate(sensorObject);
                            }
                        }
                    } else {
                        // create new SensorObject
                        sensorObject = new SensorObject(objectId, objectType, objectPosition, objectSpeed,
                                objectHeading, infoQuality, objectLength, objectWidth, objectOrientation);
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

    /**
     * Use when a RoadSensor expires or is removed.
     */
    public void flushAllSensorObjects() {
        synchronized (sensorObjects) {
            Iterator<SensorObject> iterator = sensorObjects.iterator();
            while (iterator.hasNext()) {
                SensorObject sensorObject = iterator.next();
                iterator.remove();
                synchronized (sensorObjectMap) {
                    // Remove by value
                    sensorObjectMap.values().remove(sensorObject);
                }
                ioT3RoadSensorCallback.sensorObjectExpired(sensorObject);
            }
        }
    }

    public ArrayList<SensorObject> getSensorObjects() {
        return sensorObjects;
    }

    /**
     * @return the coverage areas of this {@link RoadSensor}'s individual sensors, as a plain list.
     *         Each {@link SensorCoverage} approximates the shape of the sensor's detection area / perception
     *         region as one or more polygons.
     */
    public List<SensorCoverage> getSensorCoverageList() {
        return List.copyOf(sensorCoverageMap.values());
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
    /* Sensor coverage                                                        */
    /* --------------------------------------------------------------------- */

    /** Number of vertices used to approximate a full circle / ellipse. */
    private static final int CIRCLE_POINTS = 24;
    /** Number of vertices used to approximate the arc of a sector (radial shape). */
    private static final int SECTOR_ARC_POINTS = 16;

    /**
     * Recomputes the {@link #sensorCoverageMap} map from the current {@link #cpmFrame}, translating each
     * sensor's CPM detection area / perception region shape into one or more absolute geographic polygons.
     */
    private void updateSensorCoverages() {
        if (cpmFrame == null) return;

        if (cpmFrame.version() == CpmVersion.V1_2_1) {
            CpmEnvelope121 cpmEnvelope121 = (CpmEnvelope121) cpmFrame.envelope();
            if (cpmEnvelope121.message().sensorInformationContainer() == null) return;
            List<SensorInformation> sensorInformationList =
                    cpmEnvelope121.message().sensorInformationContainer().sensorInformation();
            if (sensorInformationList == null) return;

            Integer headingEtsi = extractHeadingV121(cpmEnvelope121);

            for (SensorInformation sensorInformation : sensorInformationList) {
                List<List<LatLng>> coverageAreas = resolveCoverageV121(sensorInformation.detectionArea(), headingEtsi);
                sensorCoverageMap.put(sensorInformation.sensorId(),
                        new SensorCoverage(sensorInformation.sensorId(), sensorInformation.type(), coverageAreas));
            }
        } else if (cpmFrame.version() == CpmVersion.V2_1_1) {
            CpmEnvelope211 cpmEnvelope211 = (CpmEnvelope211) cpmFrame.envelope();
            if (cpmEnvelope211.message().sensorInformationContainer() == null) return;
            List<com.orange.iot3mobility.messages.cpm.v211.model.sensorinformationcontainer.SensorInformation>
                    sensorInformationList = cpmEnvelope211.message().sensorInformationContainer().sensorInformation();
            if (sensorInformationList == null) return;

            for (com.orange.iot3mobility.messages.cpm.v211.model.sensorinformationcontainer.SensorInformation
                    sensorInformation : sensorInformationList) {
                List<List<LatLng>> coverageAreas = resolveCoverageV211(sensorInformation.perceptionRegionShape());
                sensorCoverageMap.put(sensorInformation.sensorId(),
                        new SensorCoverage(sensorInformation.sensorId(), sensorInformation.sensorType(), coverageAreas));
            }
        }
    }

    /**
     * @return the disseminating vehicle's heading in ETSI units (0.1 degree), or {@code null} if unavailable
     *         (e.g. RSU-originated CPM, or no station data container / originating vehicle container).
     */
    private Integer extractHeadingV121(CpmEnvelope121 cpmEnvelope121) {
        var stationDataContainer = cpmEnvelope121.message().stationDataContainer();
        if (stationDataContainer == null || stationDataContainer.originatingVehicleContainer() == null) return null;
        return stationDataContainer.originatingVehicleContainer().heading();
    }

    /**
     * @return the effective heading to use for rotating a vehicle-relative shape, in degrees (WGS84, clockwise
     *         from true north). Falls back to {@code 0} (true north) when unavailable, e.g. for fixed (non
     *         vehicular) sensors, per project convention.
     */
    private double effectiveHeadingDegrees(Integer headingEtsi) {
        if (headingEtsi == null) return 0.0;
        double headingDegrees = EtsiConverter.headingDegrees(headingEtsi);
        return Double.isNaN(headingDegrees) ? 0.0 : headingDegrees;
    }

    /**
     * Offsets {@code origin} by a local (x, y) vector expressed in meters and rotated by {@code headingDegrees}
     * (WGS84, clockwise from true north). With {@code headingDegrees == 0}, {@code x} is eastward and {@code y}
     * is northward (matching the convention already used for CPM perceived objects).
     */
    private LatLng offsetPoint(LatLng origin, double xMeters, double yMeters, double headingDegrees) {
        LatLng point = Utils.pointFromPosition(origin, headingDegrees, yMeters);
        return Utils.pointFromPosition(point, (headingDegrees + 90 + 360) % 360, xMeters);
    }

    /** Builds a polygon (ring) approximating a full circle of the given radius around {@code center}. */
    private List<LatLng> polygonFromCircle(LatLng center, double radiusMeters) {
        List<LatLng> ring = new ArrayList<>(CIRCLE_POINTS);
        for (int i = 0; i < CIRCLE_POINTS; i++) {
            double bearing = i * 360.0 / CIRCLE_POINTS;
            ring.add(Utils.pointFromPosition(center, bearing, radiusMeters));
        }
        return ring;
    }

    /**
     * Builds a polygon (ring) approximating an ellipse around {@code center}, whose major axis is oriented
     * {@code orientationDegrees} clockwise from true north.
     */
    private List<LatLng> polygonFromEllipse(LatLng center, double semiMajorMeters, double semiMinorMeters,
                                             double orientationDegrees) {
        List<LatLng> ring = new ArrayList<>(CIRCLE_POINTS);
        for (int i = 0; i < CIRCLE_POINTS; i++) {
            double angleRad = Math.toRadians(i * 360.0 / CIRCLE_POINTS);
            double localX = semiMajorMeters * Math.cos(angleRad);
            double localY = semiMinorMeters * Math.sin(angleRad);
            ring.add(offsetPoint(center, localX, localY, orientationDegrees));
        }
        return ring;
    }

    /**
     * Builds a polygon (4 corners) approximating a rectangle around {@code center}, whose length axis is
     * oriented {@code orientationDegrees} clockwise from true north.
     */
    private List<LatLng> polygonFromRectangle(LatLng center, double semiLengthMeters, double semiBreadthMeters,
                                               double orientationDegrees) {
        List<LatLng> ring = new ArrayList<>(4);
        ring.add(offsetPoint(center, semiLengthMeters, semiBreadthMeters, orientationDegrees));
        ring.add(offsetPoint(center, semiLengthMeters, -semiBreadthMeters, orientationDegrees));
        ring.add(offsetPoint(center, -semiLengthMeters, -semiBreadthMeters, orientationDegrees));
        ring.add(offsetPoint(center, -semiLengthMeters, semiBreadthMeters, orientationDegrees));
        return ring;
    }

    /**
     * Builds a "pie slice" polygon: {@code center}, followed by the arc points from {@code startDegrees} to
     * {@code endDegrees} (WGS84, clockwise from true north) at {@code rangeMeters}, closing back on {@code center}.
     */
    private List<LatLng> polygonFromSector(LatLng center, double rangeMeters, double startDegrees, double endDegrees) {
        double start = ((startDegrees % 360) + 360) % 360;
        double end = ((endDegrees % 360) + 360) % 360;
        double sweep = end - start;
        if (sweep <= 0) sweep += 360;

        List<LatLng> ring = new ArrayList<>(SECTOR_ARC_POINTS + 2);
        ring.add(center);
        for (int i = 0; i <= SECTOR_ARC_POINTS; i++) {
            double bearing = (start + sweep * i / SECTOR_ARC_POINTS) % 360;
            ring.add(Utils.pointFromPosition(center, bearing, rangeMeters));
        }
        return ring;
    }

    /**
     * Translates a CPM v1.2.1 {@link DetectionArea} into one or more absolute geographic polygons approximating
     * its shape. Height / vertical opening angles are ignored (2D projection only).
     *
     * @param detectionArea the sensor's detection area, never {@code null}
     * @param headingEtsi the disseminating vehicle's heading in ETSI units (0.1 degree), or {@code null} if
     *                    unavailable; only relevant for {@code vehicleSensor}, defaults to true north otherwise
     * @return the polygon(s) approximating the covered area
     */
    private List<List<LatLng>> resolveCoverageV121(DetectionArea detectionArea, Integer headingEtsi) {
        if (detectionArea == null) return List.of();

        if (detectionArea.stationarySensorCircular() != null) {
            AreaCircular circular = detectionArea.stationarySensorCircular();
            LatLng center = applyOffset(position, circular.nodeCenterPoint(), 0);
            double radius = EtsiConverter.cpmRangeMeters(circular.radius());
            return List.of(polygonFromCircle(center, radius));

        } else if (detectionArea.stationarySensorEllipse() != null) {
            AreaEllipse ellipse = detectionArea.stationarySensorEllipse();
            LatLng center = applyOffset(position, ellipse.nodeCenterPoint(), 0);
            double semiMajor = EtsiConverter.cpmRangeMeters(ellipse.semiMajorRangeLength());
            double semiMinor = EtsiConverter.cpmRangeMeters(ellipse.semiMinorRangeLength());
            double orientation = angleDegreesOrZero(ellipse.semiMajorRangeOrientation());
            return List.of(polygonFromEllipse(center, semiMajor, semiMinor, orientation));

        } else if (detectionArea.stationarySensorRectangle() != null) {
            AreaRectangle rectangle = detectionArea.stationarySensorRectangle();
            LatLng center = applyOffset(position, rectangle.nodeCenterPoint(), 0);
            double semiLength = EtsiConverter.cpmRangeMeters(rectangle.semiMajorRangeLength());
            double semiBreadth = EtsiConverter.cpmRangeMeters(rectangle.semiMinorRangeLength());
            double orientation = angleDegreesOrZero(rectangle.semiMajorRangeOrientation());
            return List.of(polygonFromRectangle(center, semiLength, semiBreadth, orientation));

        } else if (detectionArea.stationarySensorPolygon() != null) {
            AreaPolygon polygon = detectionArea.stationarySensorPolygon();
            List<LatLng> ring = new ArrayList<>();
            for (Offset offset : polygon.offsets()) {
                ring.add(applyOffset(position, offset, 0));
            }
            return List.of(ring);

        } else if (detectionArea.stationarySensorRadial() != null) {
            StationarySensorRadial radial = detectionArea.stationarySensorRadial();
            LatLng center = applyOffset(position, radial.sensorPositionOffset(), 0);
            double range = EtsiConverter.cpmRangeMeters(radial.range());
            double start = angleDegreesOrZero(radial.horizontalOpeningAngleStart());
            double end = angleDegreesOrZero(radial.horizontalOpeningAngleEnd());
            return List.of(polygonFromSector(center, range, start, end));

        } else if (detectionArea.vehicleSensor() != null) {
            VehicleSensor vehicleSensor = detectionArea.vehicleSensor();
            double heading = effectiveHeadingDegrees(headingEtsi);
            LatLng mountingPoint = offsetPoint(position,
                    EtsiConverter.cpmOffsetMeters(vehicleSensor.xSensorOffset()),
                    EtsiConverter.cpmOffsetMeters(vehicleSensor.ySensorOffset()),
                    heading);

            List<List<LatLng>> coverageAreas = new ArrayList<>();
            if (vehicleSensor.vehicleSensorPropertyList() != null) {
                for (VehicleSensorProperty property : vehicleSensor.vehicleSensorPropertyList()) {
                    double range = EtsiConverter.cpmRangeMeters(property.range());
                    double relativeStart = angleDegreesOrZero(property.horizontalOpeningAngleStart());
                    double relativeEnd = angleDegreesOrZero(property.horizontalOpeningAngleEnd());
                    double absoluteStart = (heading + relativeStart) % 360;
                    double absoluteEnd = (heading + relativeEnd) % 360;
                    coverageAreas.add(polygonFromSector(mountingPoint, range, absoluteStart, absoluteEnd));
                }
            }
            return coverageAreas;
        }

        return List.of();
    }

    /**
     * Translates a CPM v2.1.1 perception region {@link Shape} into one or more absolute geographic polygons
     * approximating its shape. Opening angles and orientations in this shape are always WGS84-absolute (per
     * ETSI TS 103 324), so no heading correction is applied. Height / vertical opening angles are ignored
     * (2D projection only).
     *
     * @param shape the sensor's perception region shape, may be {@code null} if not reported
     * @return the polygon(s) approximating the covered area
     */
    private List<List<LatLng>> resolveCoverageV211(Shape shape) {
        if (shape == null) return List.of();

        if (shape.rectangular() != null) {
            Rectangular rectangular = shape.rectangular();
            LatLng center = applyOffset(position, rectangular.centerPoint(), 0);
            double semiLength = EtsiConverter.cpmRangeMeters(rectangular.semiLength());
            double semiBreadth = EtsiConverter.cpmRangeMeters(rectangular.semiBreadth());
            double orientation = angleDegreesOrZero(rectangular.orientation());
            return List.of(polygonFromRectangle(center, semiLength, semiBreadth, orientation));

        } else if (shape.circular() != null) {
            Circular circular = shape.circular();
            LatLng center = applyOffset(position, circular.shapeReferencePoint(), 0);
            double radius = EtsiConverter.cpmRangeMeters(circular.radius());
            return List.of(polygonFromCircle(center, radius));

        } else if (shape.elliptical() != null) {
            Elliptical elliptical = shape.elliptical();
            LatLng center = applyOffset(position, elliptical.shapeReferencePoint(), 0);
            double semiMajor = EtsiConverter.cpmRangeMeters(elliptical.semiMajorAxisLength());
            double semiMinor = EtsiConverter.cpmRangeMeters(elliptical.semiMinorAxisLength());
            double orientation = angleDegreesOrZero(elliptical.orientation());
            return List.of(polygonFromEllipse(center, semiMajor, semiMinor, orientation));

        } else if (shape.polygonal() != null) {
            Polygonal polygonal = shape.polygonal();
            LatLng base = applyOffset(position, polygonal.shapeReferencePoint(), 0);
            List<LatLng> ring = new ArrayList<>();
            if (polygonal.polygon() != null) {
                for (CartesianPosition3d point : polygonal.polygon()) {
                    ring.add(applyOffset(base, point, 0));
                }
            }
            return List.of(ring);

        } else if (shape.radial() != null) {
            Radial radial = shape.radial();
            LatLng center = applyOffset(position, radial.shapeReferencePoint(), 0);
            double range = EtsiConverter.cpmRangeMeters(radial.range());
            double start = angleDegreesOrZero(radial.stationaryHorizontalOpeningAngleStart());
            double end = angleDegreesOrZero(radial.stationaryHorizontalOpeningAngleEnd());
            return List.of(polygonFromSector(center, range, start, end));

        } else if (shape.radialShapes() != null) {
            RadialShapes radialShapes = shape.radialShapes();
            LatLng refPoint = offsetPoint(position,
                    EtsiConverter.cpmOffsetMeters(radialShapes.xCoordinate()),
                    EtsiConverter.cpmOffsetMeters(radialShapes.yCoordinate()),
                    0);

            List<List<LatLng>> coverageAreas = new ArrayList<>();
            if (radialShapes.radialShapesList() != null) {
                for (Radial radial : radialShapes.radialShapesList()) {
                    LatLng center = radial.shapeReferencePoint() != null
                            ? applyOffset(position, radial.shapeReferencePoint(), 0)
                            : refPoint;
                    double range = EtsiConverter.cpmRangeMeters(radial.range());
                    double start = angleDegreesOrZero(radial.stationaryHorizontalOpeningAngleStart());
                    double end = angleDegreesOrZero(radial.stationaryHorizontalOpeningAngleEnd());
                    coverageAreas.add(polygonFromSector(center, range, start, end));
                }
            }
            return coverageAreas;
        }

        return List.of();
    }

    /** Applies an optional CPM v1.2.1 {@link Offset} (0.01 m units) to {@code origin}, or returns {@code origin}. */
    private LatLng applyOffset(LatLng origin, Offset offset, double headingDegrees) {
        if (offset == null) return origin;
        return offsetPoint(origin,
                EtsiConverter.cpmOffsetMeters(offset.x()),
                EtsiConverter.cpmOffsetMeters(offset.y()),
                headingDegrees);
    }

    /** Applies an optional CPM v2.1.1 {@link CartesianPosition3d} (0.01 m units) to {@code origin}. */
    private LatLng applyOffset(LatLng origin, CartesianPosition3d offset, double headingDegrees) {
        if (offset == null) return origin;
        return offsetPoint(origin,
                EtsiConverter.cpmOffsetMeters(offset.xCoordinate()),
                EtsiConverter.cpmOffsetMeters(offset.yCoordinate()),
                headingDegrees);
    }

    /** Converts a CPM 0.1-degree opening angle / orientation to degrees, defaulting to {@code 0} if unavailable. */
    private double angleDegreesOrZero(Integer angleEtsi) {
        if (angleEtsi == null) return 0.0;
        double degrees = EtsiConverter.cpmOpeningAngleDegrees(angleEtsi);
        return Double.isNaN(degrees) ? 0.0 : degrees;
    }

    private double angleDegreesOrZero(int angleEtsi) {
        double degrees = EtsiConverter.cpmOpeningAngleDegrees(angleEtsi);
        return Double.isNaN(degrees) ? 0.0 : degrees;
    }

    /* --------------------------------------------------------------------- */
    /* Helper methods                                                        */
    /* --------------------------------------------------------------------- */

    private SensorObjectType cpm121ObjectTypeFromObjectClass(
            ObjectClass objectClass) {
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
                //*** doesn't make much sense to have VRUs here - v2.1.1 spec issue? ***
                case 1 -> SensorObjectType.PEDESTRIAN;
                case 2 -> SensorObjectType.CYCLIST;
                case 3 -> SensorObjectType.MOPED;
                case 4 -> SensorObjectType.MOTORCYCLE;
                //***********************************************************************
                case 5 -> SensorObjectType.PASSENGER_CAR;
                case 6 -> SensorObjectType.BUS;
                case 7 -> SensorObjectType.LIGHT_TRUCK;
                case 8 -> SensorObjectType.HEAVY_TRUCK;
                case 9 -> SensorObjectType.TRAILER;
                case 10 -> SensorObjectType.SPECIAL_VEHICLES;
                case 11 -> SensorObjectType.TRAM;
                case 12 -> SensorObjectType.LIGHT_VRU_VEHICLE;
                case 13 -> SensorObjectType.ANIMAL;
                case 14 -> SensorObjectType.AGRICULTURAL;
                case 15 -> SensorObjectType.ROAD_SIDE_UNIT;
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
