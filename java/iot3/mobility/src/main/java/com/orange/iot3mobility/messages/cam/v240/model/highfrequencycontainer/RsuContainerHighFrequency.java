/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cam.v240.model.highfrequencycontainer;

import java.util.List;

/**
 * RsuContainerHighFrequency v2.4.0
 *
 * @param protectedCommunicationZonesRsu Optional list of RSU {@link ProtectedCommunicationZone}.
 *                                       May be {@code null} — the field is optional per the schema.
 *                                       When present, must contain between 1 and 16 entries.
 */
public record RsuContainerHighFrequency(
        List<ProtectedCommunicationZone> protectedCommunicationZonesRsu) implements HighFrequencyContainer {
    public RsuContainerHighFrequency {
        protectedCommunicationZonesRsu = protectedCommunicationZonesRsu != null
                ? List.copyOf(protectedCommunicationZonesRsu)
                : null;
    }
}
