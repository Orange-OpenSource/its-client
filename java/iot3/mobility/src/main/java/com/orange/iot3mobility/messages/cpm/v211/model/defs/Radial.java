package com.orange.iot3mobility.messages.cpm.v211.model.defs;

/**
 * Radial shape definition.
 *
 * @param range Radial range from the reference point (0..4095).
 * @param stationaryHorizontalOpeningAngleStart Horizontal opening angle start. Unit: 0,1 degrees (0..3601).
 * @param stationaryHorizontalOpeningAngleEnd Horizontal opening angle end. Unit: 0,1 degrees (0..3601).
 * @param shapeReferencePoint Optional {@link CartesianPosition3d} reference point.
 * @param verticalOpeningAngleStart Optional vertical opening angle start. Unit: 0,1 degrees (0..3601).
 * @param verticalOpeningAngleEnd Optional vertical opening angle end. Unit: 0,1 degrees (0..3601).
 */
public record Radial(
        int range,
        int stationaryHorizontalOpeningAngleStart,
        int stationaryHorizontalOpeningAngleEnd,
        CartesianPosition3d shapeReferencePoint,
        Integer verticalOpeningAngleStart,
        Integer verticalOpeningAngleEnd) {}
