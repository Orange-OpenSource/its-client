/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json;

/**
 * Created by Mathieu Lefebvre on 22/02/18.
 * Feel free to improve it
 */

import com.orange.iot3mobility.TrueTime;
import com.orange.iot3mobility.Utils;

/**
 * Common base of all Orange ITS JSON messages.
 */
public abstract class MessageBase {

    private final String type;
    private final String context;
    private final String origin;
    private final String version;
    private String sourceUuid;
    private final String destinationUuid;
    private long timestamp;
    private String messageId;
    private final String extra = "";
    private final String signature;

    public MessageBase(String type, String context, String origin, String version,
                       String sourceUuid, String destinationUuid, long timestamp,
                       String messageUuid, String signature) {
        this.type = type;
        this.context = context;
        this.origin = origin;
        this.version = version;
        this.sourceUuid = sourceUuid;
        this.destinationUuid = destinationUuid;
        this.timestamp = timestamp;
        this.messageId = sourceUuid+"/"+messageUuid+"/"+timestamp;
        this.signature = signature;
    }

    public MessageBase(String type,
                       String origin,
                       String version,
                       String sourceUuid,
                       String destinationUuid,
                       long timestamp) {
        this(type, JsonValue.Context.UNDEFINED.value(), origin, version, sourceUuid, destinationUuid, timestamp, "");
    }

    public MessageBase(String type,
                       String context,
                       String origin,
                       String version,
                       String sourceUuid,
                       long timestamp,
                       String signature) {
        this(type, context, origin, version, sourceUuid, "", timestamp, signature);
    }

    public MessageBase(String type,
                       String context,
                       String origin,
                       String version,
                       String sourceUuid,
                       String destinationUuid,
                       long timestamp,
                       String signature) {
        this(type, context, origin, version, sourceUuid, destinationUuid, timestamp, Utils.getRandomUuid(), signature);
    }

    public MessageBase(String type,
                       String context,
                       String origin,
                       String version,
                       String sourceUuid,
                       long timestamp,
                       String messageUuid,
                       String signature) {
        this(type, context, origin, version, sourceUuid, "", timestamp, messageUuid, signature);
    }

    public String getType() {
        return type;
    }

    public String getContext() {
        return context;
    }

    public String getOrigin() {
        return origin;
    }

    public String getVersion() {
        return version;
    }

    public int getVersionInt() {
        String tmpVersion = version.replace(".", "");
        return Integer.parseInt(tmpVersion);
    }

    public static int getVersionInt(String version) {
        String tmpVersion = version.replace(".", "");
        return Integer.parseInt(tmpVersion);
    }

    public String getSourceUuid() {
        return sourceUuid;
    }

    public void updateSourceUuid() {
        //sourceUuid = SELF.getSourceUUID();
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

    public String getMessageId() {
        return messageId;
    }

    public String getExtra() {
        return extra;
    }

    public String getSignature() {
        return signature;
    }

}
