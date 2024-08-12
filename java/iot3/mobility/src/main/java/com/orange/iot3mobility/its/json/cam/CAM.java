/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.cam;

import com.orange.iot3mobility.its.json.JsonKey;
import com.orange.iot3mobility.its.json.JsonValue;
import com.orange.iot3mobility.its.json.MessageBase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CAM extends MessageBase {

    private static final Logger LOGGER = Logger.getLogger(CAM.class.getName());

    private final JSONObject jsonCAM = new JSONObject();
    private final int protocolVersion;
    private final long stationId;
    private final int generationDeltaTime;
    private final BasicContainer basicContainer;
    private final HighFrequencyContainer highFrequencyContainer;
    private final LowFrequencyContainer lowFrequencyContainer;

    public CAM(
            final String type,
            final String origin,
            final String version,
            final String sourceUuid,
            final String destinationUuid,
            final long timestamp,
            final int protocolVersion,
            final long stationId,
            final int generationDeltaTime,
            final BasicContainer basicContainer,
            final HighFrequencyContainer highFrequencyContainer,
            final LowFrequencyContainer lowFrequencyContainer)
    {
        super(type, origin, version, sourceUuid, destinationUuid, timestamp);
        if(protocolVersion > 255 || protocolVersion < 0) {
            throw new IllegalArgumentException("CAM ProtocolVersion should be in the range of [0 - 255]."
                    + " Value: " + protocolVersion);
        }
        this.protocolVersion = protocolVersion;
        if(stationId > 4294967295L || stationId < 0) {
            throw new IllegalArgumentException("CAM StationID should be in the range of [0 - 4294967295]."
                    + " Value: " + stationId);
        }
        this.stationId = stationId;
        if(generationDeltaTime > 65535 || generationDeltaTime < 0) {
            throw new IllegalArgumentException("CAM GenerationDeltaTime should be in the range of [0 - 65535]."
                    + " Value: " + generationDeltaTime);
        }
        this.generationDeltaTime = generationDeltaTime;
        if(basicContainer == null) {
            throw new IllegalArgumentException("CAM BasicContainer missing.");
        }
        this.basicContainer = basicContainer;
        if(highFrequencyContainer == null) {
            throw new IllegalArgumentException("CAM HighFrequencyContainer missing.");
        }
        this.highFrequencyContainer = highFrequencyContainer;
        this.lowFrequencyContainer = lowFrequencyContainer;

        createJson();
    }

    private void createJson() {
        try {
            JSONObject message = new JSONObject();
            message.put(JsonKey.Cam.PROTOCOL_VERSION.key(), protocolVersion);
            message.put(JsonKey.Cam.STATION_ID.key(), stationId);
            message.put(JsonKey.Cam.GENERATION_DELTA_TIME.key(), generationDeltaTime);
            message.put(JsonKey.Cam.BASIC_CONTAINER.key(), basicContainer.getJsonBasicContainer());
            message.put(JsonKey.Cam.HIGH_FREQ_CONTAINER.key(), highFrequencyContainer.getJsonHighFrequencyContainer());
            if(lowFrequencyContainer != null)
                message.put(JsonKey.Cam.LOW_FREQ_CONTAINER.key(), lowFrequencyContainer.getJsonLowFrequencyContainer());

            jsonCAM.put(JsonKey.Header.TYPE.key(), getType());
            jsonCAM.put(JsonKey.Header.ORIGIN.key(), getOrigin());
            jsonCAM.put(JsonKey.Header.VERSION.key(), getVersion());
            jsonCAM.put(JsonKey.Header.SOURCE_UUID.key(), getSourceUuid());
            if(!getDestinationUuid().isEmpty())
                jsonCAM.put(JsonKey.Header.DESTINATION_UUID.key(), getDestinationUuid());
            jsonCAM.put(JsonKey.Header.TIMESTAMP.key(), getTimestamp());
            jsonCAM.put(JsonKey.Header.MESSAGE.key(), message);
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "CAM", "Error building CAM: " + e);
        }
    }

    public JSONObject getJsonCAM() {
        return jsonCAM;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public long getStationId() {
        return stationId;
    }

    public int getGenerationDeltaTime() {
        return generationDeltaTime;
    }

    public BasicContainer getBasicContainer() {
        return basicContainer;
    }

    public HighFrequencyContainer getHighFrequencyContainer() {
        return highFrequencyContainer;
    }

    public LowFrequencyContainer getLowFrequencyContainer() {
        return lowFrequencyContainer;
    }

    public static class CAMBuilder {
        private final String type;
        private String origin;
        private String version;
        private String sourceUuid;
        private String destinationUuid;
        private long timestamp;
        private int protocolVersion;
        private long stationId;
        private int generationDeltaTime;
        private BasicContainer basicContainer;
        private HighFrequencyContainer highFrequencyContainer;
        private LowFrequencyContainer lowFrequencyContainer;

        public CAMBuilder() {
            this.type = JsonValue.Type.CAM.value();
        }

        public CAMBuilder header(String origin,
                                 String version,
                                 String sourceUuid,
                                 String destinationUuid,
                                 long timestamp) {
            this.origin = origin;
            this.version = version;
            this.sourceUuid = sourceUuid;
            this.destinationUuid = destinationUuid;
            this.timestamp = timestamp;
            return this;
        }

        public CAMBuilder header(String origin,
                                 String version,
                                 String sourceUuid,
                                 long timestamp) {
            this.origin = origin;
            this.version = version;
            this.sourceUuid = sourceUuid;
            this.destinationUuid = "";
            this.timestamp = timestamp;
            return this;
        }

        public CAMBuilder pduHeader(int protocolVersion,
                                    long stationId,
                                    int generationDeltaTime) {
            this.protocolVersion = protocolVersion;
            this.stationId = stationId;
            this.generationDeltaTime = generationDeltaTime;
            return this;
        }

        public CAMBuilder basicContainer(BasicContainer basicContainer) {
            this.basicContainer = basicContainer;
            return this;
        }

        public CAMBuilder highFreqContainer(HighFrequencyContainer highFrequencyContainer) {
            this.highFrequencyContainer = highFrequencyContainer;
            return this;
        }

        public CAMBuilder lowFreqContainer(LowFrequencyContainer lowFrequencyContainer) {
            this.lowFrequencyContainer = lowFrequencyContainer;
            return this;
        }

        public CAM build() {
            return new CAM(type,
                    origin,
                    version,
                    sourceUuid,
                    destinationUuid,
                    timestamp,
                    protocolVersion,
                    stationId,
                    generationDeltaTime,
                    basicContainer,
                    highFrequencyContainer,
                    lowFrequencyContainer);
        }
    }

    public static CAM jsonParser(JSONObject jsonCAM) {
        if(jsonCAM == null || jsonCAM.isEmpty()) return null;
        try {
            String type = jsonCAM.getString(JsonKey.Header.TYPE.key());

            if(type.equals(JsonValue.Type.CAM.value())){
                JSONObject message = jsonCAM.getJSONObject(JsonKey.Header.MESSAGE.key());

                String origin = jsonCAM.getString(JsonKey.Header.ORIGIN.key());
                String version = jsonCAM.getString(JsonKey.Header.VERSION.key());
                String sourceUuid = jsonCAM.getString(JsonKey.Header.SOURCE_UUID.key());
                String destinationUuid = jsonCAM.optString(JsonKey.Header.DESTINATION_UUID.key());
                long timestamp = jsonCAM.getLong(JsonKey.Header.TIMESTAMP.key());

                int protocolVersion = message.getInt(JsonKey.Cam.PROTOCOL_VERSION.key());
                long stationId = message.getLong(JsonKey.Cam.STATION_ID.key());
                int generationDeltaTime = message.getInt(JsonKey.Cam.GENERATION_DELTA_TIME.key());

                JSONObject jsonBasicContainer = message.getJSONObject(JsonKey.Cam.BASIC_CONTAINER.key());
                BasicContainer basicContainer = BasicContainer.jsonParser(jsonBasicContainer);

                JSONObject jsonHighFreqContainer = message.getJSONObject(JsonKey.Cam.HIGH_FREQ_CONTAINER.key());
                HighFrequencyContainer highFrequencyContainer = HighFrequencyContainer.jsonParser(jsonHighFreqContainer);

                JSONObject jsonLowFreqContainer = message.optJSONObject(JsonKey.Cam.LOW_FREQ_CONTAINER.key());
                LowFrequencyContainer lowFrequencyContainer = LowFrequencyContainer.jsonParser(jsonLowFreqContainer);

                return new CAMBuilder()
                        .header(origin,
                                version,
                                sourceUuid,
                                destinationUuid,
                                timestamp)
                        .pduHeader(protocolVersion,
                                stationId,
                                generationDeltaTime)
                        .basicContainer(basicContainer)
                        .highFreqContainer(highFrequencyContainer)
                        .lowFreqContainer(lowFrequencyContainer)
                        .build();
            }
        } catch (JSONException | IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "CAM", "Error parsing CAM: " + e);
        }
        return null;
    }

}
