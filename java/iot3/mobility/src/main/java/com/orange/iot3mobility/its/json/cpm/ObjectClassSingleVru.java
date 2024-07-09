/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.cpm;

import org.json.JSONException;
import org.json.JSONObject;

public class ObjectClassSingleVru {

    private final JSONObject json = new JSONObject();

    /**
     * If the single VRU is a pedestrian.
     */
    private final ObjectVruPedestrian objectVruPedestrian;

    /**
     * If the single VRU is a bicyclist.
     */
    private final ObjectVruBicyclist objectVruBicyclist;

    /**
     * If the single VRU is a motorcyclist.
     */
    private final ObjectVruMotorcyclist objectVruMotorcyclist;

    /**
     * If the single VRU is an animal.
     */
    private final ObjectVruAnimal objectVruAnimal;

    public ObjectClassSingleVru(final ObjectVruPedestrian objectVruPedestrian)
            throws IllegalArgumentException {
        if(objectVruPedestrian == null && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM objectClass VRU is missing");
        }
        this.objectVruPedestrian = objectVruPedestrian;
        this.objectVruBicyclist = null;
        this.objectVruMotorcyclist = null;
        this.objectVruAnimal = null;

        createJson();
    }

    public ObjectClassSingleVru(final ObjectVruBicyclist objectVruBicyclist) {
        if(objectVruBicyclist == null && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM objectClass VRU is missing");
        }
        this.objectVruPedestrian = null;
        this.objectVruBicyclist = objectVruBicyclist;
        this.objectVruMotorcyclist = null;
        this.objectVruAnimal = null;

        createJson();
    }

    public ObjectClassSingleVru(final ObjectVruMotorcyclist objectVruMotorcyclist) {
        if(objectVruMotorcyclist == null && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM objectClass VRU is missing");
        }
        this.objectVruPedestrian = null;
        this.objectVruBicyclist = null;
        this.objectVruMotorcyclist = objectVruMotorcyclist;
        this.objectVruAnimal = null;

        createJson();
    }

    public ObjectClassSingleVru(final ObjectVruAnimal objectVruAnimal) {
        if(objectVruAnimal == null && CPM.isStrictMode()) {
            throw new IllegalArgumentException("CPM objectClass VRU is missing");
        }
        this.objectVruPedestrian = null;
        this.objectVruBicyclist = null;
        this.objectVruMotorcyclist = null;
        this.objectVruAnimal = objectVruAnimal;

        createJson();
    }

    private void createJson() {
        try {
            if(objectVruPedestrian != null)
                json.put(JsonCpmKey.ObjectClass.SINGLE_VRU.key(), objectVruPedestrian.getJson());
            if(objectVruBicyclist != null)
                json.put(JsonCpmKey.ObjectClass.SINGLE_VRU.key(), objectVruBicyclist.getJson());
            if(objectVruMotorcyclist != null)
                json.put(JsonCpmKey.ObjectClass.SINGLE_VRU.key(), objectVruMotorcyclist.getJson());
            if(objectVruAnimal != null)
                json.put(JsonCpmKey.ObjectClass.SINGLE_VRU.key(), objectVruAnimal.getJson());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJson() {
        return json;
    }

    public Object getVruObject() {
        if(objectVruPedestrian != null) return objectVruPedestrian;
        if(objectVruBicyclist != null) return objectVruBicyclist;
        if(objectVruMotorcyclist != null) return objectVruMotorcyclist;
        return objectVruAnimal;
    }

    public static ObjectClassSingleVru jsonParser(JSONObject json) {
        if(json == null || json.length() == 0) return null;
        JSONObject jsonSingleVru = json.optJSONObject(JsonCpmKey.ObjectClass.SINGLE_VRU.key());
        if(jsonSingleVru != null) {
            ObjectVruPedestrian objectVruPedestrian = ObjectVruPedestrian.jsonParser(jsonSingleVru);
            ObjectVruBicyclist objectVruBicyclist = ObjectVruBicyclist.jsonParser(jsonSingleVru);
            ObjectVruMotorcyclist objectVruMotorcyclist = ObjectVruMotorcyclist.jsonParser(jsonSingleVru);
            ObjectVruAnimal objectVruAnimal = ObjectVruAnimal.jsonParser(jsonSingleVru);

            if(objectVruPedestrian != null) return new ObjectClassSingleVru(objectVruPedestrian);
            if(objectVruBicyclist != null) return new ObjectClassSingleVru(objectVruBicyclist);
            if(objectVruMotorcyclist != null) return new ObjectClassSingleVru(objectVruMotorcyclist);
            if(objectVruAnimal != null) return new ObjectClassSingleVru(objectVruAnimal);
        }
        return null;
    }

}
