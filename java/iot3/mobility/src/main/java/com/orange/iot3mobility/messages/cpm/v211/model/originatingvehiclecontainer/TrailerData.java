package com.orange.iot3mobility.messages.cpm.v211.model.originatingvehiclecontainer;

import com.orange.iot3mobility.messages.cpm.v211.model.defs.Angle;

/**
 * Trailer data entry
 * <p>
 * Provides detailed information about an attached trailer.
 *
 * @param refPointId Identifier of the reference point of the trailer (0..255).
 * @param hitchPointOffset Position of the hitch point in negative x-direction (according to ISO 8855) from the vehicle
 *                         reference point (0..255).
 * @param hitchAngle {@link Angle} between the trailer orientation (corresponding to the x direction of the ISO 8855
 *                                coordinate system centered on the trailer) and the direction of the segment having as
 *                                end points the reference point of the trailer and the reference point of the pulling
 *                                vehicle, which can be another trailer or a vehicle looking on the horizontal plane xy,
 *                                described in the local Cartesian coordinate system of the trailer. Unit: 0,1 degrees.
 * @param frontOverhang Optional. Length of the trailer overhang in the positive x direction (according to ISO 8855)
 *                      from the trailer Reference Point indicated by the refPointID (0..255).
 * @param rearOverhang Optional. Length of the trailer overhang in the negative x direction (according to ISO 8855)
 *                     from the trailer Reference Point indicated by the refPointID (0..255).
 * @param trailerWidth Optional. Width of the trailer. Unit: 0,1 m. outOfRange (61), unavailable (62).
 */
public record TrailerData(
        int refPointId,
        int hitchPointOffset,
        Angle hitchAngle,
        Integer frontOverhang,
        Integer rearOverhang,
        Integer trailerWidth) {}
