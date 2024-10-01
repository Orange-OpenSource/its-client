/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.cpm;

import static com.orange.iot3mobility.its.json.JsonUtil.UNKNOWN;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class VehicleSensor {

    private final JSONObject json = new JSONObject();

    /**
     * Increasing counter of the trailer reference point (corresponding to the hitch point).
     */
    private final int refPointId;

    /**
     * Unit: 0.01 meter. Mounting position of sensor in negative x-direction from Reference Point
     * indicated by the ref point id.
     *
     * negativeZeroPointZeroOneMeter(-1), negativeOneMeter(-100), negativeOutOfRange(-3094),
     * positiveOneMeter(100),positiveOutOfRange(1001).
     */
    private final int xSensorOffset;

    /**
     * Unit: 0.01 meter. Mounting position of sensor in y-direction from Reference Point
     * indicated by the ref point id.
     *
     * zeroPointZeroOneMeter(1), oneMeter(100).
     */
    private final int ySensorOffset;

    /**
     * Unit: 0.01 meter. Mounting position of sensor in z-direction from Reference Point
     * indicated by the ref point id.
     *
     * zeroPointZeroOneMeter(1), oneMeter(100).
     */
    private final int zSensorOffset;

    /**
     * List of information for individual sensor(s) which are mounted to a vehicle.
     */
    private final ArrayList<VehicleSensorProperty> vehicleSensorPropertyList;

    public VehicleSensor(
            final int refPointId,
            final int xSensorOffset,
            final int ySensorOffset,
            final int zSensorOffset,
            final ArrayList<VehicleSensorProperty> vehicleSensorPropertyList
    ) throws IllegalArgumentException {
        if(refPointId == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM VehicleSensor refPointId is missing");
        } else if(CPM.isStrictMode() && (refPointId > 255 || refPointId < 0)) {
            throw new IllegalArgumentException("CPM VehicleSensor refPointId should be in the range of [0 - 255]."
                    + " Value: " + refPointId);
        }
        this.refPointId = refPointId;
        if(xSensorOffset == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM VehicleSensor xSensorOffset is missing");
        } else if(CPM.isStrictMode() && (xSensorOffset > 0 || xSensorOffset < -5000)) {
            throw new IllegalArgumentException("CPM VehicleSensor xSensorOffset should be in the range of [-5000 - 0]."
                    + " Value: " + xSensorOffset);
        }
        this.xSensorOffset = xSensorOffset;
        if(ySensorOffset == UNKNOWN && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM VehicleSensor ySensorOffset is missing");
        } else if(CPM.isStrictMode() && (ySensorOffset > 1000 || ySensorOffset < -1000)) {
            throw new IllegalArgumentException("CPM VehicleSensor ySensorOffset should be in the range of [-1000 - 1000]."
                    + " Value: " + ySensorOffset);
        }
        this.ySensorOffset = ySensorOffset;
        if(zSensorOffset != UNKNOWN && CPM.isStrictMode() && (zSensorOffset > 1000 || zSensorOffset < 0)) {
            throw new IllegalArgumentException("CPM VehicleSensor zSensorOffset should be in the range of [0 - 1000]."
                    + " Value: " + zSensorOffset);
        }
        this.zSensorOffset = zSensorOffset;
        if(vehicleSensorPropertyList == null && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM VehicleSensor vehicleSensorPropertyList is missing");
        }
        this.vehicleSensorPropertyList = vehicleSensorPropertyList;

        createJson();
    }

    private void createJson() {
        try {
            json.put(JsonCpmKey.VehicleSensor.REF_POINT_ID.key(), refPointId);
            json.put(JsonCpmKey.VehicleSensor.X_SENSOR_OFFSET.key(), xSensorOffset);
            json.put(JsonCpmKey.VehicleSensor.Y_SENSOR_OFFSET.key(), ySensorOffset);
            if(zSensorOffset != UNKNOWN)
                json.put(JsonCpmKey.VehicleSensor.Z_SENSOR_OFFSET.key(), zSensorOffset);
            JSONArray jsonVehicleSensorPropertyArray = new JSONArray();
            for(VehicleSensorProperty vehicleSensorProperty: vehicleSensorPropertyList) {
                jsonVehicleSensorPropertyArray.put(vehicleSensorProperty.getJson());
            }
            json.put(JsonCpmKey.VehicleSensor.VEHICLE_SENSOR_PROPERTY_LIST.key(), jsonVehicleSensorPropertyArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJson() {
        return json;
    }

    public int getRefPointId() {
        return refPointId;
    }

    public int getxSensorOffset() {
        return xSensorOffset;
    }

    public int getySensorOffset() {
        return ySensorOffset;
    }

    public int getzSensorOffset() {
        return zSensorOffset;
    }

    public ArrayList<VehicleSensorProperty> getVehicleSensorPropertyList() {
        return vehicleSensorPropertyList;
    }

    public static VehicleSensor jsonParser(JSONObject json) {
        if(json == null || json.length() == 0) return null;
        try {
            int refPointId = json.getInt(JsonCpmKey.VehicleSensor.REF_POINT_ID.key());
            int xSensorOffset = json.getInt(JsonCpmKey.VehicleSensor.X_SENSOR_OFFSET.key());
            int ySensorOffset = json.getInt(JsonCpmKey.VehicleSensor.Y_SENSOR_OFFSET.key());
            int zSensorOffset = json.optInt(JsonCpmKey.VehicleSensor.Z_SENSOR_OFFSET.key(), UNKNOWN);
            ArrayList<VehicleSensorProperty> vehicleSensorPropertyList = new ArrayList<>();
            JSONArray jsonVehicleSensorPropertyArray = json.getJSONArray(JsonCpmKey.VehicleSensor.VEHICLE_SENSOR_PROPERTY_LIST.key());
            for(int i = 0; i < jsonVehicleSensorPropertyArray.length(); i++) {
                VehicleSensorProperty vehicleSensorProperty = VehicleSensorProperty.jsonParser(jsonVehicleSensorPropertyArray.getJSONObject(i));
                vehicleSensorPropertyList.add(vehicleSensorProperty);
            }

            return new VehicleSensor(refPointId,
                    xSensorOffset,
                    ySensorOffset,
                    zSensorOffset,
                    vehicleSensorPropertyList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
