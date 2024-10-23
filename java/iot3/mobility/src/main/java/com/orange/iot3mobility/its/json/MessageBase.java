/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json;

import com.orange.iot3mobility.TrueTime;

/**
 * Common base of all Orange ITS JSON messages.
 */
public abstract class MessageBase {

    /**
     * Type of the message.
     * <p>
     * cam, denm, cpm, etc.
     */
    private final String type;

    /**
     * The entity responsible for emitting the message.
     * <p>
     * self, global_application, mec_application, on_board_application
     */
    private final String origin;

    /**
     * JSON message format version.
     */
    private final String version;

    /**
     * The identifier of the entity responsible for emitting the message.
     * <p>
     * Format com_type_number, e.g. ora_car_42
     */
    private final String sourceUuid;

    /**
     * The timestamp when the message was generated since Unix Epoch (1970/01/01).
     * <p>
     * Unit: millisecond.
     */
    private long timestamp;

    protected MessageBase(String type,
                       String origin,
                       String version,
                       String sourceUuid,
                       long timestamp) {
        this.type = type;
        this.origin = origin;
        this.version = version;
        this.sourceUuid = sourceUuid;
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public String getOrigin() {
        return origin;
    }

    public String getVersion() {
        return version;
    }

    public String getSourceUuid() {
        return sourceUuid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void updateTimestamp() {
        timestamp = TrueTime.getAccurateTime();
    }

}
