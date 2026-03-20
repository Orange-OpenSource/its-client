/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.messages.cpm.v211.model.managementcontainer;

import com.orange.iot3mobility.messages.cpm.v211.model.defs.MessageRateHz;

/**
 * Message rate range
 * <p>
 * Planned or expected range of the CPM generation rate.
 *
 * @param messageRateMin Minimum {@link MessageRateHz} (mantissa * 10^exponent) in Hz.
 * @param messageRateMax Maximum {@link MessageRateHz} (mantissa * 10^exponent) in Hz.
 */
public record MessageRateRange(MessageRateHz messageRateMin, MessageRateHz messageRateMax) {}
