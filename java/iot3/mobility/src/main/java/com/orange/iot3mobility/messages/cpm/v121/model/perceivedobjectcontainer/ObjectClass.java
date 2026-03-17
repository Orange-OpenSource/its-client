package com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer;

/**
 * Object class.
 * <p>
 * Exactly one of the class options should be provided.
 *
 * @param vehicle Describes the subclass of a detected object for class vehicle. Value: unknown(0), passengerCar(1),
 *                bus(2), lightTruck(3), heavyTruck(4), trailer(5), specialVehicles(6), tram(7), emergencyVehicle(8),
 *                agricultural(9).
 * @param singleVru {@link ObjectClassVru}
 * @param vruGroup {@link ObjectClassGroup}
 * @param other Detected object for class other. Value: unknown(0), roadSideUnit(1).
 */
public record ObjectClass(
        Integer vehicle,
        ObjectClassVru singleVru,
        ObjectClassGroup vruGroup,
        Integer other) {}
