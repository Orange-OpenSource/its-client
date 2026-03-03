package com.orange.iot3mobility.message.cam.v230.model.specialvehiclecontainer;

/**
 * SpecialTransportType v2.3.0
 * <p>
 * Vehicle is carrying goods in the special transport conditions.
 *
 * @param heavyLoad Vehicle is carrying goods with heavy load
 * @param excessWidth Vehicle is carrying goods in excess of width
 * @param excessLength Vehicle is carrying goods in excess of length
 * @param excessHeight Vehicle is carrying goods in excess of height
 */
public record SpecialTransportType(
        boolean heavyLoad,
        boolean excessWidth,
        boolean excessLength,
        boolean excessHeight) {}
