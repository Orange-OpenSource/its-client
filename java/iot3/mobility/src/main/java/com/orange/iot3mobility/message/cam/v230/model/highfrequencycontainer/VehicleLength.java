package com.orange.iot3mobility.message.cam.v230.model.highfrequencycontainer;

/**
 * VehicleLength v2.3.0
 *
 * @param value Length of vehicle. tenCentimeters(1), outOfRange(1022), unavailable(1023)
 * @param confidence Indication of the length value confidence. noTrailerPresent (0), trailerPresentWithKnownLength (1),
 *                   trailerPresentWithUnknownLength (2), trailerPresenceIsUnknown (3), unavailable (4)
 */
public record VehicleLength(int value, int confidence) {}
