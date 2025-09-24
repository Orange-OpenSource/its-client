/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @authors
    Maciej Ćmiel       <maciej.cmiel@orange.com>
    Zbigniew Krawczyk  <zbigniew2.krawczyk@orange.com>
*/
package com.orange.iot3core.clients.lwm2m.model;

import org.eclipse.leshan.client.object.Device;
import org.eclipse.leshan.core.request.BindingMode;

import java.util.EnumSet;

/**
 * A simple Lwm2mDevice for the Device (3) object.
 */
public class Lwm2mDevice {

    private final Device device;

    public Lwm2mDevice(String manufacturer, String modelNumber, String serialNumber, String supportedBinding) {
        this.device = new Device(manufacturer, modelNumber, serialNumber, EnumSet.of(BindingMode.valueOf(supportedBinding)));
    }

    public Device getDevice() {
        return device;
    }

}
