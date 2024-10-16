/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.cpm;

import com.orange.iot3mobility.its.StationType;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ClassificationItem {

    private static final Logger LOGGER = Logger.getLogger(ClassificationItem.class.getName());

    private final JSONObject json = new JSONObject();

    /**
     * The class that best describes the detected object.
     *
     * The object can be classified into one of three main categories: vehicle, VRU and other.
     */
    private final ObjectClassVehicle objectClassVehicle;
    private final ObjectClassSingleVru objectClassSingleVru;
    private final ObjectClassOther objectClassOther;

    /**
     * Describes the confidence value for the type of a detected object.
     *
     * unknown(0) in case the confidence value is unknown but the reported classification
     * is still valid, onePercent(1), oneHundredPercent(100), unavailable(101)
     * in case the class confidence value computation is not available for this object,
     * indicates that the class assignment is invalid
     */
    private final int confidence;

    public ClassificationItem(final ObjectClassVehicle objectClassVehicle,
                              final int confidence) throws IllegalArgumentException {
        if(objectClassVehicle == null && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM PerceivedObject objectClass is missing");
        }
        this.objectClassVehicle = objectClassVehicle;
        this.objectClassSingleVru = null;
        this.objectClassOther = null;
        if(CPM.isStrictMode() && (confidence > 101 || confidence < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObject confidence should be in the range of [0 - 101]."
                    + " Value: " + confidence);
        }
        this.confidence = confidence;

        createJson();
    }

    public ClassificationItem(final ObjectClassSingleVru objectClassSingleVru,
                              final int confidence) throws IllegalArgumentException {
        if(objectClassSingleVru == null && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM PerceivedObject objectClass is missing");
        }
        this.objectClassVehicle = null;
        this.objectClassSingleVru = objectClassSingleVru;
        this.objectClassOther = null;
        if(CPM.isStrictMode() && (confidence > 101 || confidence < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObject confidence should be in the range of [0 - 101]."
                    + " Value: " + confidence);
        }
        this.confidence = confidence;

        createJson();
    }

    public ClassificationItem(final ObjectClassOther objectClassOther,
                              final int confidence) throws IllegalArgumentException {
        if(objectClassOther == null && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM PerceivedObject objectClass is missing");
        }
        this.objectClassSingleVru = null;
        this.objectClassVehicle = null;
        this.objectClassOther = objectClassOther;
        if(CPM.isStrictMode() && (confidence > 101 || confidence < 0)) {
            throw new IllegalArgumentException("CPM PerceivedObject confidence should be in the range of [0 - 101]."
                    + " Value: " + confidence);
        }
        this.confidence = confidence;

        createJson();
    }

    private void createJson() {
        try {
            if(objectClassVehicle != null)
                json.put(JsonCpmKey.Classification.OBJECT_CLASS.key(), objectClassVehicle.getJson());
            else if(objectClassSingleVru != null)
                json.put(JsonCpmKey.Classification.OBJECT_CLASS.key(), objectClassSingleVru.getJson());
            else if(objectClassOther != null)
                json.put(JsonCpmKey.Classification.OBJECT_CLASS.key(), objectClassOther.getJson());
            json.put(JsonCpmKey.Classification.CONFIDENCE.key(), confidence);
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "CPM ClassificationItem JSON build error", "Error: " + e);
        }
    }

    public JSONObject getJson() {
        return json;
    }

    public Object getObjectClass() {
        if(objectClassVehicle != null) return objectClassVehicle;
        if(objectClassSingleVru != null) return objectClassSingleVru;
        return objectClassOther;
    }

    public boolean isObjectPedestrian() {
        if(objectClassSingleVru != null) {
            Object singleVru = objectClassSingleVru.getVruObject();
            return singleVru instanceof ObjectVruPedestrian;
        }
        return false;
    }

    public int getObjectClassId() {
        if(objectClassVehicle != null) {
            // raw ID
            return objectClassVehicle.getSubclass();
        } else if(objectClassSingleVru != null) {
            Object singleVru = objectClassSingleVru.getVruObject();
            if(singleVru instanceof ObjectVruPedestrian vruPedestrian) {
                // raw ID + 10
                return vruPedestrian.getSubclass() + 10;
            } else if(singleVru instanceof ObjectVruBicyclist vruBicyclist) {
                // raw ID + 20
                return vruBicyclist.getSubclass() + 20;
            } else if(singleVru instanceof ObjectVruMotorcyclist vruMotorcyclist) {
                // raw ID + 30
                return vruMotorcyclist.getSubclass() + 30;
            } else if(singleVru instanceof ObjectVruAnimal vruAnimal) {
                // raw ID + 40
                return vruAnimal.getSubclass() + 40;
            } else return -1;
        } else if (objectClassOther != null) {
            if(objectClassOther.getSubclass() == 1)
                return 51; // raw ID + 50
            else
                return -1;
        }
        return -1;
    }

    public String getObjectClassStr() {
        if(objectClassVehicle != null) {
            return switch (objectClassVehicle.getSubclass()) {
                case 1 -> "passenger-car";
                case 2 -> "bus";
                case 3 -> "light-truck";
                case 4 -> "heavy-truck";
                case 5 -> "trailer-truck";
                case 6 -> "special-vehicle";
                case 7 -> "tram";
                case 8 -> "emergency-vehicle";
                default -> "unknown";
            };
        } else if(objectClassSingleVru != null) {
            Object singleVru = objectClassSingleVru.getVruObject();
            if(singleVru instanceof ObjectVruPedestrian vruPedestrian) {
                return switch (vruPedestrian.getSubclass()) {
                    case 2 -> "pedestrian-road-worker";
                    case 3 -> "pedestrian-first-responder";
                    default -> "pedestrian";
                };
            } else if(singleVru instanceof ObjectVruBicyclist vruBicyclist) {
                return switch (vruBicyclist.getSubclass()) {
                    case 2 -> "wheelchair-user_bicycle";
                    case 3 -> "horseRider_bicycle";
                    case 4 -> "rollerskater_bicycle";
                    case 5 -> "e-scooter_bicycle";
                    case 6 -> "personnal-transporter_bicycle";
                    case 7 -> "pedelec_bicycle";
                    case 8 -> "speed-pedelec_bicycle";
                    default -> "bicycle";
                };
            } else if(singleVru instanceof ObjectVruMotorcyclist vruMotorcyclist) {
                return switch (vruMotorcyclist.getSubclass()) {
                    case 1 -> "moped";
                    case 3 -> "sidecar-right";
                    case 4 -> "sidecar-left";
                    default -> "motorcycle";
                };
            } else if(singleVru instanceof ObjectVruAnimal vruAnimal) {
                return switch (vruAnimal.getSubclass()) {
                    case 1 -> "wild-animal";
                    case 2 -> "farm-animal";
                    case 3 -> "service-animal";
                    default -> "animal";
                };
            } else return "unknown";
        } else if (objectClassOther != null) {
            if(objectClassOther.getSubclass() == 1)
                return "roadSideUnit";
            else
                return "other";
        }
        return "unknown";
    }

    public int getObjectStationType() {
        if(objectClassVehicle != null) {
            return switch (objectClassVehicle.getSubclass()) {
                case 1 -> StationType.PASSENGER_CAR.getId();
                case 2 -> StationType.BUS.getId();
                case 3 -> StationType.LIGHT_TRUCK.getId();
                case 4 -> StationType.HEAVY_TRUCK.getId();
                case 5 -> StationType.TRAILER.getId();
                case 6, 8 -> StationType.SPECIAL_VEHICLES.getId();
                case 7 -> StationType.TRAM.getId();
                default -> StationType.UNKNOWN.getId();
            };
        } else if(objectClassSingleVru != null) {
            Object singleVru = objectClassSingleVru.getVruObject();
            if(singleVru instanceof ObjectVruPedestrian) {
                return StationType.PEDESTRIAN.getId();
            } else if(singleVru instanceof ObjectVruBicyclist) {
                return StationType.CYCLIST.getId();
            } else if(singleVru instanceof ObjectVruMotorcyclist vruMotorcyclist) {
                if(vruMotorcyclist.getSubclass() == 1) return StationType.MOPED.getId();
                else return StationType.MOTORCYCLE.getId();
            } else return StationType.UNKNOWN.getId();
        }
        return StationType.UNKNOWN.getId();
    }

    public int getConfidence() {
        return confidence;
    }

    public static ClassificationItem jsonParser(JSONObject json) {
        if(json == null || json.isEmpty()) return null;
        try {
            JSONObject jsonObjectClass = json.getJSONObject(JsonCpmKey.Classification.OBJECT_CLASS.key());
            ObjectClassVehicle objectClassVehicle = ObjectClassVehicle.jsonParser(jsonObjectClass);
            ObjectClassSingleVru objectClassSingleVru = ObjectClassSingleVru.jsonParser(jsonObjectClass);
            ObjectClassOther objectClassOther = ObjectClassOther.jsonParser(jsonObjectClass);
            int confidence = json.optInt(JsonCpmKey.Classification.CONFIDENCE.key(), 0);

            if(objectClassVehicle != null) return new ClassificationItem(objectClassVehicle, confidence);
            if(objectClassSingleVru != null) return new ClassificationItem(objectClassSingleVru, confidence);
            if(objectClassOther != null) return new ClassificationItem(objectClassOther, confidence);
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "CPM ClassificationItem JSON parsing error", "Error: " + e);
        }
        return null;
    }

}
