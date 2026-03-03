package com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer;

/**
 * DangerousGoodsContainer v2.3.0
 *
 * If the vehicleRole component is set to dangerousGoods(3) this container shall be present.
 *
 * @param dangerousGoodsBasic Type of the dangerous goods being carried by a heavy vehicle. explosives1 (0),
 *                            explosives2 (1), explosives3 (2), explosives4…(15), infectiousSubstances (16),
 *                            radioactiveMaterial (17), corrosiveSubstances (18), miscellaneousDangerousSubstances (19)
 */
public record DangerousGoodsContainer(
        int dangerousGoodsBasic) implements SpecialVehiclePayload {}
