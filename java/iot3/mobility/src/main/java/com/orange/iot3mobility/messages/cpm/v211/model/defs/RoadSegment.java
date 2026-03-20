package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * Road segment reference in a MAPEM.
 *
 * @param id Road segment identifier.
 * @param region Optional region identifier of the responsible entity.
 */
public record RoadSegment(int id, Integer region) {}

