/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.message.cam.v230.model.highfrequencycontainer;

import java.util.List;
import java.util.Objects;

/**
 * RsuContainerHighFrequency v2.3.0
 *
 * @param protectedCommunicationZonesRsu List of RSU {@link ProtectedCommunicationZone}
 */
public record RsuContainerHighFrequency(
        List<ProtectedCommunicationZone> protectedCommunicationZonesRsu) implements HighFrequencyContainer {
    public RsuContainerHighFrequency {
        protectedCommunicationZonesRsu = List.copyOf(Objects.requireNonNull(protectedCommunicationZonesRsu));
    }
}
