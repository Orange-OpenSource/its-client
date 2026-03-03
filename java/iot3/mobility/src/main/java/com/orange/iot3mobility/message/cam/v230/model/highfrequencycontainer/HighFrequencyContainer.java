package com.orange.iot3mobility.message.cam.v230.model.highfrequencycontainer;

/**
 * HighFrequencyContainer v2.3.0
 * <p>
 * One of {@link BasicVehicleContainerHighFrequency} or {@link RsuContainerHighFrequency}
 */
public sealed interface HighFrequencyContainer
        permits BasicVehicleContainerHighFrequency, RsuContainerHighFrequency {}
