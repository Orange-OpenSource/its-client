/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.roadobjects;

import com.orange.iot3mobility.Utils;
import com.orange.iot3mobility.messages.StationType;
import com.orange.iot3mobility.messages.cam.core.CamCodec;
import com.orange.iot3mobility.messages.cam.v113.model.CamEnvelope113;
import com.orange.iot3mobility.messages.cam.v240.model.CamEnvelope240;
import com.orange.iot3mobility.messages.cam.v240.model.CamStructuredData;
import com.orange.iot3mobility.messages.cam.v240.model.highfrequencycontainer.BasicVehicleContainerHighFrequency;
import com.orange.iot3mobility.quadkey.LatLng;

import java.util.List;

public class RoadUser {

    private static final int LIFETIME = 1500; // 1.5 seconds

    private final String uuid;
    private StationType stationType;
    private LatLng position;
    private double speed; // m/s
    private double heading; // degree
    private Double length; // meter
    private Double width; // meter
    private List<LatLng> footprint;
    private long timestamp;
    private Long linkedStationId;
    private CamCodec.CamFrame<?> camFrame;

    public RoadUser(String uuid, StationType stationType, LatLng position, double speed, double heading,
                    CamCodec.CamFrame<?> camFrame) {
        this(uuid, stationType, position, speed, heading, null, null, null, camFrame);
    }

    public RoadUser(String uuid, StationType stationType, LatLng position, double speed, double heading,
                    Double length, Double width, Long linkedStationId, CamCodec.CamFrame<?> camFrame) {
        this.uuid = uuid;
        this.setStationType(stationType);
        this.position = position;
        this.speed = speed;
        this.heading = heading;
        this.length = length;
        this.width = width;
        this.linkedStationId = linkedStationId;
        this.camFrame = camFrame;
        computeFootprint();
        updateTimestamp();
    }

    public String getUuid() {
        return uuid;
    }

    public StationType getStationType() {
        return stationType;
    }

    public void setStationType(StationType stationType) {
        this.stationType = stationType;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public double getSpeed() {
        return speed;
    }

    public double getSpeedKmh() {
        return speed * 3.6;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getHeading() {
        return heading;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    public void setDimensions(Double length, Double width) {
        this.length = length;
        this.width = width;
        computeFootprint();
    }

    public Double getLength() {
        return length;
    }

    public Double getWidth() {
        return width;
    }

    private void computeFootprint() {
        if(length != null && width != null) {
            footprint = Utils.computeFootprint(position, heading, length, width);
        }
    }

    public List<LatLng> getFootprint() {
        return footprint;
    }

    public void setCamFrame(CamCodec.CamFrame<?> camFrame) {
        this.camFrame = camFrame;
    }

    public CamCodec.CamFrame<?> getCamFrame() {
        return camFrame;
    }

    /**
     * Returns the linked station identifier, or {@code null} if not linked.
     */
    public Long getLinkedStationId() {
        return linkedStationId;
    }

    public void setLinkedStationId(Long linkedStationId) {
        this.linkedStationId = linkedStationId;
    }

    /**
     * Returns {@code true} if this road user has a linked station identifier
     * (e.g. a trailer or a platooning pair).
     *
     * @see com.orange.iot3mobility.managers.RoadUserManager#getLinkedObject(RoadUser)
     */
    public boolean isLinked() {
        return linkedStationId != null;
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

    public boolean isVulnerable() {
        return stationType.equals(StationType.CYCLIST) || stationType.equals(StationType.PEDESTRIAN);
    }

    /**
     * Returns {@code true} if the last received CAM indicates that this road user is
     * driving backward (reversing), based on the {@code drive_direction} field of the
     * high frequency container.
     * <p>
     * Returns {@code false} when no CAM frame is available, when the drive direction is
     * forward or unavailable, or when it cannot be determined (e.g. RSU-originated CAM or
     * a raw ASN.1 payload).
     */
    public boolean isReversing() {
        if (camFrame == null) {
            return false;
        }
        Integer driveDirection = switch (camFrame.version()) {
            case V1_1_3 -> {
                if (camFrame.envelope() instanceof CamEnvelope113 envelope) {
                    yield envelope.message().highFrequencyContainer().driveDirection();
                }
                yield null;
            }
            case V2_4_0 -> {
                if (camFrame.envelope() instanceof CamEnvelope240 envelope
                        && envelope.message() instanceof CamStructuredData structuredData
                        && structuredData.highFrequencyContainer()
                                instanceof BasicVehicleContainerHighFrequency vehicleContainer) {
                    yield vehicleContainer.driveDirection();
                }
                yield null;
            }
        };
        return driveDirection != null && driveDirection == 1;
    }

}
