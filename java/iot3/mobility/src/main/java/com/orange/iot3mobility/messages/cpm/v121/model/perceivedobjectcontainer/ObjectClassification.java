/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v121.model.perceivedobjectcontainer;

/**
 * Object classification entry.
 *
 * @param objectClass {@link ObjectClass}
 * @param confidence Confidence value for the type. Value: unknown(0), onePercent(1), oneHundredPercent(100), unavailable(101).
 */
public record ObjectClassification(
        ObjectClass objectClass,
        int confidence) {}
