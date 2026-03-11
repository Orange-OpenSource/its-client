package com.orange.iot3mobility.messages.cpm.v211.model.managementcontainer;

import com.orange.iot3mobility.messages.cpm.v211.model.defs.MessageRateHz;

/**
 * Planned or expected range of the CPM generation rate.
 */
public record MessageRateRange(MessageRateHz messageRateMin, MessageRateHz messageRateMax) {}
