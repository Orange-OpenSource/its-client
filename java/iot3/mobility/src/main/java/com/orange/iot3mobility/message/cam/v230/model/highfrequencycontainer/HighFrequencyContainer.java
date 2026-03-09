/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.message.cam.v230.model.highfrequencycontainer;

/**
 * HighFrequencyContainer v2.3.0
 * <p>
 * One of {@link BasicVehicleContainerHighFrequency} or {@link RsuContainerHighFrequency}
 */
public sealed interface HighFrequencyContainer
        permits BasicVehicleContainerHighFrequency, RsuContainerHighFrequency {}
