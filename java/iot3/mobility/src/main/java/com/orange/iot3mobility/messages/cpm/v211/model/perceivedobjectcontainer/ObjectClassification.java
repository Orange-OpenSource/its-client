package com.orange.iot3mobility.messages.cpm.v211.model.perceivedobjectcontainer;

/**
 * Classification of the described object.
 */
public record ObjectClassification(ObjectClass objectClass, int confidence) {}

