/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.cpm;

import static com.orange.iot3mobility.its.json.JsonUtil.UNKNOWN;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ObjectClassVehicle {

    private static final Logger LOGGER = Logger.getLogger(ObjectClassVehicle.class.getName());

    private final JSONObject json = new JSONObject();

    /**
     * Describes the subclass of a detected object for class vehicle.
     *
     * unknown(0) the type of vehicle is unknown,
     * passengerCar(1) the detected object is a small passenger car,
     * bus(2) the detected object is a large passenger vehicle,
     * lightTruck(3) the detected object is a light goods vehicle,
     * heavyTruck(4) the detected object is a heavy goods vehicle,
     * trailer(5) the detected object is an unpowered vehicle that is intended to be towed by
     * a powered vehicle,
     * specialVehicles(6) the detected object is a vehicle which has a special purpose other than
     * the above (e.g. moving road works vehicle),
     * tram(7) the detected object is a vehicle running on tracks along public streets,
     * emergencyVehicle(8) the detected object is a vehicle used in an emergency situation,
     * such as an ambulance, police car or fire engine,
     * agricultural(9) the detected object is a vehicle used for agricultural purposes.
     */
    private final int subclass;

    public ObjectClassVehicle(final int subclass) throws IllegalArgumentException {
        if(CPM.isStrictMode() && (subclass > 255 || subclass < 0)) {
            throw new IllegalArgumentException("CPM Vehicle Object subclass should be in the range of [0 - 255]."
                    + " Value: " + subclass);
        }
        this.subclass = subclass;

        createJson();
    }

    private void createJson() {
        try {
            json.put(JsonCpmKey.ObjectClass.VEHICLE.key(), subclass);
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "CPM ObjectClassVehicle JSON build error", "Error: " + e);
        }
    }

    public JSONObject getJson() {
        return json;
    }

    public int getSubclass() {
        return subclass;
    }

    public static ObjectClassVehicle jsonParser(JSONObject json) {
        if(json == null || json.isEmpty()) return null;
        int subclass = json.optInt(JsonCpmKey.ObjectClass.VEHICLE.key(), UNKNOWN);
        if(subclass != UNKNOWN) return new ObjectClassVehicle(subclass);
        else return null;
    }

}
