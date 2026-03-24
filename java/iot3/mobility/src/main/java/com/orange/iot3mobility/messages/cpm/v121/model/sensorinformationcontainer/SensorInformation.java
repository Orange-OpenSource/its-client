/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v121.model.sensorinformationcontainer;

/**
 * Sensor information item.
 *
 * @param sensorId Sensor identifier. Value: [0..255].
 * @param type Type of attached sensor. Value: undefined(0), radar(1), lidar(2), monovideo(3), stereovision(4),
 *             nightvision(5), ultrasonic(6), pmd(7), fusion(8), inductionloop(9), sphericalCamera(10), itssaggregation(11).
 * @param detectionArea {@link DetectionArea}
 */
public record SensorInformation(
        int sensorId,
        int type,
        DetectionArea detectionArea) {}
