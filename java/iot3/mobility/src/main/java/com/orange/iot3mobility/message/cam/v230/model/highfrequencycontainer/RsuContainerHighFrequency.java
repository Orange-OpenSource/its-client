package com.orange.iot3mobility.message.cam.v230.model.highfrequencycontainer;

import java.util.List;
import java.util.Objects;

public record RsuContainerHighFrequency(
        List<ProtectedCommunicationZone> protectedCommunicationZonesRsu) implements HighFrequencyContainer {
    public RsuContainerHighFrequency {
        protectedCommunicationZonesRsu = List.copyOf(Objects.requireNonNull(protectedCommunicationZonesRsu));
    }
}
