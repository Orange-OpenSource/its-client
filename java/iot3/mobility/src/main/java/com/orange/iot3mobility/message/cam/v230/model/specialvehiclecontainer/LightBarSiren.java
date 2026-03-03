package com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer;

/**
 * LightBarSiren v2.3.0
 * <p>
 * Status of light bar and any sort of audible alarm system besides the horn.
 *
 * @param lightBarActivated When the light bar is activated
 * @param sirenActivated When the siren is activated
 */
public record LightBarSiren(
        boolean lightBarActivated,
        boolean sirenActivated) {}
