/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.cpm;

import com.orange.iot3mobility.its.StationType;

import org.json.JSONException;
import org.json.JSONObject;

public class ClassificationItem {

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
            e.printStackTrace();
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
            if(singleVru instanceof ObjectVruPedestrian) {
                // raw ID + 10
                ObjectVruPedestrian vruPedestrian = (ObjectVruPedestrian) singleVru;
                return vruPedestrian.getSubclass() + 10;
            } else if(singleVru instanceof ObjectVruBicyclist) {
                // raw ID + 20
                ObjectVruBicyclist vruBicyclist = (ObjectVruBicyclist) singleVru;
                return vruBicyclist.getSubclass() + 20;
            } else if(singleVru instanceof ObjectVruMotorcyclist) {
                // raw ID + 30
                ObjectVruMotorcyclist vruMotorcyclist = (ObjectVruMotorcyclist) singleVru;
                return vruMotorcyclist.getSubclass() + 30;
            } else if(singleVru instanceof ObjectVruAnimal) {
                // raw ID + 40
                ObjectVruAnimal vruAnimal = (ObjectVruAnimal) singleVru;
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
            switch (objectClassVehicle.getSubclass()) {
                default:
                case 0:
                    return "unknown";
                case 1:
                    return "passenger-car";
                case 2:
                    return "bus";
                case 3:
                    return "light-truck";
                case 4:
                    return "heavy-truck";
                case 5:
                    return "trailer-truck";
                case 6:
                    return "special-vehicle";
                case 7:
                    return "tram";
                case 8:
                    return "emergency-vehicle";
            }
        } else if(objectClassSingleVru != null) {
            Object singleVru = objectClassSingleVru.getVruObject();
            if(singleVru instanceof ObjectVruPedestrian) {
                ObjectVruPedestrian vruPedestrian = (ObjectVruPedestrian) singleVru;
                switch (vruPedestrian.getSubclass()) {
                    default:
                    case 0:
                    case 1:
                        return "pedestrian";
                    case 2:
                        return "pedestrian-road-worker";
                    case 3:
                        return "pedestrian-first-responder";
                }
            } else if(singleVru instanceof ObjectVruBicyclist) {
                ObjectVruBicyclist vruBicyclist = (ObjectVruBicyclist) singleVru;
                switch (vruBicyclist.getSubclass()) {
                    default:
                    case 0:
                    case 1:
                        return "bicycle";
                    case 2:
                        return "wheelchair-user_bicycle";
                    case 3:
                        return "horseRider_bicycle";
                    case 4:
                        return "rollerskater_bicycle";
                    case 5:
                        return "e-scooter_bicycle";
                    case 6:
                        return "personnal-transporter_bicycle";
                    case 7:
                        return "pedelec_bicycle";
                    case 8:
                        return "speed-pedelec_bicycle";
                }
            } else if(singleVru instanceof ObjectVruMotorcyclist) {
                ObjectVruMotorcyclist vruMotorcyclist = (ObjectVruMotorcyclist) singleVru;
                switch (vruMotorcyclist.getSubclass()) {
                    default:
                    case 0:
                    case 2:
                        return "motorcycle";
                    case 1:
                        return "moped";
                    case 3:
                        return "sidecar-right";
                    case 4:
                        return "sidecar-left";
                }
            } else if(singleVru instanceof ObjectVruAnimal) {
                ObjectVruAnimal vruAnimal = (ObjectVruAnimal) singleVru;
                switch (vruAnimal.getSubclass()) {
                    default:
                    case 0:
                        return "animal";
                    case 1:
                        return "wild-animal";
                    case 2:
                        return "farm-animal";
                    case 3:
                        return "service-animal";
                }
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
            switch (objectClassVehicle.getSubclass()) {
                default:
                case 0:
                    return StationType.UNKNOWN.getId();
                case 1:
                    return StationType.PASSENGER_CAR.getId();
                case 2:
                    return StationType.BUS.getId();
                case 3:
                    return StationType.LIGHT_TRUCK.getId();
                case 4:
                    return StationType.HEAVY_TRUCK.getId();
                case 5:
                    return StationType.TRAILER.getId();
                case 6:
                case 8:
                    return StationType.SPECIAL_VEHICLES.getId();
                case 7:
                    return StationType.TRAM.getId();
            }
        } else if(objectClassSingleVru != null) {
            Object singleVru = objectClassSingleVru.getVruObject();
            if(singleVru instanceof ObjectVruPedestrian) {
                return StationType.PEDESTRIAN.getId();
            } else if(singleVru instanceof ObjectVruBicyclist) {
                return StationType.CYCLIST.getId();
            } else if(singleVru instanceof ObjectVruMotorcyclist) {
                ObjectVruMotorcyclist vruMotorcyclist = (ObjectVruMotorcyclist) singleVru;
                switch (vruMotorcyclist.getSubclass()) {
                    default:
                    case 0:
                    case 2:
                    case 3:
                    case 4:
                        return StationType.MOTORCYCLE.getId();
                    case 1:
                        return StationType.MOPED.getId();
                }
            } else return StationType.UNKNOWN.getId();
        }
        return StationType.UNKNOWN.getId();
    }

    public int getConfidence() {
        return confidence;
    }

    public static ClassificationItem jsonParser(JSONObject json) {
        if(json == null || json.length() == 0) return null;
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
            e.printStackTrace();
        }
        return null;
    }

}
