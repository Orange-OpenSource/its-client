/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model.sensorinformationcontainer;

import com.orange.iot3mobility.messages.cpm.v211.model.defs.Shape;

/**
 * Information for an individual sensor.
 *
 * @param sensorId Sensor identifier (0..255).
 * @param sensorType Type of attached sensor (0..31). Examples: radar (1), lidar (2), monovideo (3), stereovision (4),
 *                   nightvision (5), ultrasonic (6), pmd (7), inductionloop (8), sphericalCamera (9), uwb (10),
 *                   acoustic (11), localAggregation (12), itsAggregation (13).
 * @param perceptionRegionShape Optional {@link Shape} describing the perception region.
 * @param perceptionRegionConfidence Optional homogeneous confidence for the perception region (1..101). 101 indicates
 *                                   unavailable.
 * @param shadowingApplies Indicates if the standard shadowing approach applies.
 */
public record SensorInformation(
        int sensorId,
        int sensorType,
        Shape perceptionRegionShape,
        Integer perceptionRegionConfidence,
        boolean shadowingApplies) {}
