/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.mapem.v200.model.lane.enums;

/**
 * Marker interface for all lane-type-specific attribute flag enums.
 * <p>
 * Implemented by {@link VehicleLaneAttribute}, {@link CrosswalkAttribute},
 * {@link BikeLaneAttribute}, {@link SidewalkAttribute}, {@link MedianAttribute},
 * {@link StripingAttribute}, {@link TrackedVehicleAttribute}, and {@link ParkingAttribute}.
 * <p>
 * SDK consumers should use the concrete enum type for construction and this interface
 * for generic handling of lane attribute flags (e.g. in {@code LaneGeometry}).
 */
public interface LaneTypeFlag {

    /**
     * Returns the JSON string value as defined in the ETSI TS 103 301 schema.
     *
     * @return the exact string value used in MAPEM JSON payloads
     */
    String value();
}

