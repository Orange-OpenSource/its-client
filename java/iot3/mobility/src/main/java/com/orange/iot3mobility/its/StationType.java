package com.orange.iot3mobility.its;

public enum StationType {

    UNKNOWN("unknown", 0),
    PEDESTRIAN("pedestrian", 1),
    CYCLIST("cyclist", 2),
    MOPED("moped", 3),
    MOTORCYCLE("motorcycle", 4),
    PASSENGER_CAR("passengerCar", 5),
    BUS("bus", 6),
    LIGHT_TRUCK("lightTruck", 7),
    HEAVY_TRUCK("heavyTruck", 8),
    TRAILER("trailer", 9),
    SPECIAL_VEHICLES("specialVehicles", 10),
    TRAM("tram", 11),
    ROAD_SIDE_UNIT("roadSideUnit", 15);

    private String name;
    private int id;

    StationType(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public static StationType fromName(String name) {
        for (StationType stationType : StationType.values()) {
            if (stationType.getName().equals(name)) {
                return stationType;
            }
        }
        return UNKNOWN;
    }

    public static StationType fromId(int id) {
        for (StationType stationType : StationType.values()) {
            if (stationType.getId() == id) {
                return stationType;
            }
        }
        return UNKNOWN;
    }

}
