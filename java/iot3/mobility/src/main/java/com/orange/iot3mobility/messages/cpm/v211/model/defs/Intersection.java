package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * Intersection reference in a MAPEM.
 *
 * @param id Intersection identifier.
 * @param region Optional region identifier of the responsible entity.
 */
public record Intersection(int id, Integer region) {}

