/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.message.cam.v230.model.lowfrequencycontainer;

import java.util.List;
import java.util.Objects;

/**
 * BasicVehicleContainerLowFrequency v2.3.0
 *
 * @param vehicleRole Role of the vehicle ITS-S that originates the CAM. default (0), publicTransport (1),
 *                    specialTransport (2), dangerousGoods (3), roadWork (4), rescue (5), emergency (6), safetyCar (7),
 *                    agriculture (8), commercial (9), military (10), roadOperator (11), taxi (12), uvar (13),
 *                    rfu1 (14), rfu2 (15)
 * @param exteriorLights {@link ExteriorLights}
 * @param pathHistory List of {@link PathPoint}
 */
public record BasicVehicleContainerLowFrequency(
        int vehicleRole,
        ExteriorLights exteriorLights,
        List<PathPoint> pathHistory) {
    public BasicVehicleContainerLowFrequency {
        pathHistory = List.copyOf(Objects.requireNonNull(pathHistory));
    }
}
