/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cam.v230.model.specialvehiclecontainer;

/**
 * DangerousGoodsContainer v2.3.0
 * <p>
 * If the vehicleRole component is set to dangerousGoods(3) this container shall be present.
 *
 * @param dangerousGoodsBasic Type of the dangerous goods being carried by a heavy vehicle. explosives1 (0),
 *                            explosives2 (1), explosives3 (2), explosives4…(15), infectiousSubstances (16),
 *                            radioactiveMaterial (17), corrosiveSubstances (18), miscellaneousDangerousSubstances (19)
 */
public record DangerousGoodsContainer(
        int dangerousGoodsBasic) implements SpecialVehiclePayload {}
