/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.message;

/**
 * ITS Station Type
 * <p>
 * unknown(0), pedestrian(1), cyclist(2), moped(3), motorcycle(4), passengerCar(5), bus(6), lightTruck(7),
 * heavyTruck(8), trailer(9), specialVehicles(10), tram(11), roadSideUnit(15)
 */
public enum StationType {
    UNKNOWN(0),
    PEDESTRIAN(1),
    CYCLIST(2),
    MOPED(3),
    MOTORCYCLE(4),
    PASSENGER_CAR(5),
    BUS(6),
    LIGHT_TRUCK(7),
    HEAVY_TRUCK(8),
    TRAILER(9),
    SPECIAL_VEHICLES(10),
    TRAM(11),
    ROAD_SIDE_UNIT(15);

    public final int value;

    StationType(int value) {
        this.value = value;
    }
}
