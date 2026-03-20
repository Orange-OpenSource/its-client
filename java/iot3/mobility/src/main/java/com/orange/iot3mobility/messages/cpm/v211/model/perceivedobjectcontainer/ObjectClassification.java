/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

/**
 * Classification of the described object.
 *
 * @param objectClass {@link ObjectClass} describing the detected object.
 * @param confidence Confidence value of the classification (1..101). 101 indicates unavailable.
 */
public record ObjectClassification(ObjectClass objectClass, int confidence) {}

