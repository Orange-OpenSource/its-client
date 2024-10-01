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

    private final String type;
    private final String origin;
    private final String version;
    private final String sourceUuid;
    private final String destinationUuid;
    private long timestamp;

    protected MessageBase(String type,
                       String origin,
                       String version,
                       String sourceUuid,
                       String destinationUuid,
                       long timestamp) {
        this.type = type;
        this.origin = origin;
        this.version = version;
        this.sourceUuid = sourceUuid;
        this.destinationUuid = destinationUuid;
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

    public String getDestinationUuid() {
        return destinationUuid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void updateTimestamp() {
        timestamp = TrueTime.getAccurateTime();
    }

}
