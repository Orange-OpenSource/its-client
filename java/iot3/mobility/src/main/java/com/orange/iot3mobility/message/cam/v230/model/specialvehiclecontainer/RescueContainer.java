package com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer;

/**
 * RescueContainer v2.3.0
 * <p>
 * If the vehicleRole component is set to rescue(5) this container shall be present.
 *
 * @param lightBarSirenInUse {@link LightBarSiren}
 */
public record RescueContainer(
        LightBarSiren lightBarSirenInUse) implements SpecialVehiclePayload {}
