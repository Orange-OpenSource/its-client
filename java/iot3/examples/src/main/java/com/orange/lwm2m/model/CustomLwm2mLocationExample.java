/*
 Copyright 2016-2025 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @authors
    Zbigniew Krawczyk  <zbigniew2.krawczyk@orange.com>
*/
package com.orange.lwm2m.model;

import com.orange.iot3core.clients.lwm2m.model.LocationUpdate;
import com.orange.iot3core.clients.lwm2m.model.Lwm2mLocation;
import com.orange.iot3mobility.quadkey.LatLng;

/**
 * A wrapper class for the LwM2M Location Object (ID: 6)
 * This implementation handles location data updates and provides
 * an easy-to-use interface for IoT3 Core users.
 */
public class CustomLwm2mLocationExample extends Lwm2mLocation {

    /**
     * Updates the location object with new location parameters.
     * This method updates all relevant resources and notifies the LwM2M server
     * about the changes.
     *
     * @param latLng The LatLng object containing the new location parameters
     */
    public void setLatLng(LatLng latLng) {
        LocationUpdate locationUpdate = new LocationUpdate.Builder(
                (float) latLng.getLatitude(),
                (float) latLng.getLongitude()
        ).build();
        update(locationUpdate);
    }

}