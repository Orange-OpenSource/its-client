package com.orange.iot3core.clients.lwm2m.model;

import org.eclipse.leshan.client.object.Device;

/**
 * A simple Lwm2mDevice for the Device (3) object.
 */
public class Lwm2mDevice {

    private final Device device;

    public Lwm2mDevice(String manufacturer, String modelNumber, String serialNumber, String supportedBinding) {
        this.device = new Device(manufacturer, modelNumber, serialNumber, supportedBinding);
    }

    public Device getDevice() {
        return device;
    }

}