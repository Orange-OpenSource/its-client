package com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer;

/**
 * SpecialTransportContainer v2.3.0
 * <p>
 * If the vehicleRole component is set to specialTransport(2) this container shall be present.
 *
 * @param specialTransportType {@link SpecialTransportType}
 * @param lightBarSirenInUse {@link LightBarSiren}
 */
public record SpecialTransportContainer(
        SpecialTransportType specialTransportType,
        LightBarSiren lightBarSirenInUse) implements SpecialVehiclePayload {}
