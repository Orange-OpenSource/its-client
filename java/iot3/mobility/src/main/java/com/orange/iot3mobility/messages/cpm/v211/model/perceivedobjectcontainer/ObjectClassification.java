package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

/**
 * Classification of the described object.
 *
 * @param objectClass {@link ObjectClass} describing the detected object.
 * @param confidence Confidence value of the classification (1..101). 101 indicates unavailable.
 */
public record ObjectClassification(ObjectClass objectClass, int confidence) {}

